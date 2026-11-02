
import { useEffect, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import {
  FolderKanban,
  Search,
  ArrowRight,
  BriefcaseBusiness,
  CirclePlay,
  TriangleAlert,
  CircleCheck,
  Clock3,
  Plus,
} from 'lucide-react';

import './MeusTrabalhos.css';
import { useContextoTrabalho } from '../contexts/ContextoTrabalho';
import { listarTrabalhos } from '../services/trabalhoService';

const statusConfig = {
  EM_ANDAMENTO: {
    texto: 'Em andamento',
    classe: 'andamento',
    Icone: CirclePlay,
  },
  PENDENTE: {
    texto: 'Pendente',
    classe: 'atencao',
    Icone: Clock3,
  },
  CONCLUIDO: {
    texto: 'Concluído',
    classe: 'concluido',
    Icone: CircleCheck,
  },
  CANCELADO: {
    texto: 'Cancelado',
    classe: 'atrasado',
    Icone: TriangleAlert,
  },
};

function formatarData(data) {
  if (!data) return '—';

  const dataFormatada = new Date(data);

  if (Number.isNaN(dataFormatada.getTime())) return '—';

  return dataFormatada.toLocaleDateString('pt-BR');
}

export default function MeusTrabalhos() {
  const navigate = useNavigate();
  const location = useLocation();

  const [searchTerm, setSearchTerm] = useState('');
  const [filtroAtivo, setFiltroAtivo] = useState('Todos');
  const [trabalhos, setTrabalhos] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');

  const { contexto, empresaSelecionada } = useContextoTrabalho();

  useEffect(() => {
    async function carregarTrabalhos() {
      try {
        setCarregando(true);
        setErro('');

        const dados = await listarTrabalhos();
        const lista = Array.isArray(dados) ? dados : dados.content ?? [];

        const trabalhosDoContexto = lista.filter((trabalho) => {
          if (contexto === 'pessoal') {
            return trabalho.tipo === 'PESSOAL';
          }

          if (contexto === 'empresa') {
            return (
              trabalho.tipo === 'EMPRESA' &&
              empresaSelecionada?.id != null &&
              Number(trabalho.empresaId) === Number(empresaSelecionada.id)
            );
          }

          return false;
        });

        setTrabalhos(trabalhosDoContexto);
      } catch (error) {
        setErro(error.message || 'Não foi possível carregar os trabalhos.');
      } finally {
        setCarregando(false);
      }
    }

    carregarTrabalhos();
  }, [contexto, empresaSelecionada?.id, location.key]);

  const filtros = [
    'Todos',
    'Pendentes',
    'Em andamento',
    'Concluídos',
    'Cancelados',
  ];

  const trabalhosFiltrados = trabalhos.filter((trabalho) => {
    const nome = (trabalho.titulo ?? '').toLowerCase();
    const correspondeAoNome = nome.includes(searchTerm.trim().toLowerCase());

    if (!correspondeAoNome) return false;

    switch (filtroAtivo) {
      case 'Pendentes':
        return trabalho.status === 'PENDENTE';

      case 'Em andamento':
        return trabalho.status === 'EM_ANDAMENTO';

      case 'Concluídos':
        return trabalho.status === 'CONCLUIDO';

      case 'Cancelados':
        return trabalho.status === 'CANCELADO';

      case 'Todos':
      default:
        return true;
    }
  });

  const handleVerTrabalho = (id) => {
    navigate(`/trabalhos/${id}`);
  };

  return (
    <main className="trabalhos-container">
      <header className="trabalhos-header-flex">
        <div className="trabalhos-main-header">
          <h1>Meus trabalhos</h1>

          <p>
            {contexto === 'pessoal'
              ? 'Seus trabalhos pessoais — só você vê.'
              : `Trabalhos da empresa ${empresaSelecionada?.nome ?? ''}.`}
          </p>
        </div>

        <button
          type="button"
          className="btn-novo-trabalho"
          onClick={() => navigate('/trabalhos/novo')}
        >
          <Plus size={18} />
          <span>Novo trabalho</span>
        </button>
      </header>

      <section className="search-filter-card">
        <label className="search-label" htmlFor="pesquisa-trabalhos">
          O que você está procurando?
        </label>

        <div className="search-input-wrapper">
          <Search className="search-icon" />

          <input
            id="pesquisa-trabalhos"
            type="search"
            placeholder="Procure pelo nome do trabalho"
            value={searchTerm}
            onChange={(event) => setSearchTerm(event.target.value)}
          />
        </div>

        <div
          className="filter-pills"
          role="group"
          aria-label="Filtrar trabalhos por situação"
        >
          {filtros.map((filtro) => (
            <button
              key={filtro}
              type="button"
              className={`filter-pill ${
                filtroAtivo === filtro ? 'active' : ''
              }`}
              aria-pressed={filtroAtivo === filtro}
              onClick={() => setFiltroAtivo(filtro)}
            >
              {filtro}
            </button>
          ))}
        </div>
      </section>

      {carregando ? (
        <section className="empty-state" role="status">
          <p>Carregando trabalhos...</p>
        </section>
      ) : erro ? (
        <section className="empty-state" role="alert">
          <h3>Não foi possível carregar os trabalhos</h3>
          <p>{erro}</p>
        </section>
      ) : trabalhosFiltrados.length === 0 ? (
        <section className="empty-state">
          <FolderKanban className="empty-icon" />
          <h3>Nenhum trabalho encontrado</h3>
          <p>Tente buscar por outro termo ou categoria.</p>
        </section>
      ) : (
        <section className="trabalhos-list">
          {trabalhosFiltrados.map((trabalho) => {
            const configuracao =
              statusConfig[trabalho.status] ?? {
                texto: trabalho.status ?? 'Status desconhecido',
                classe: 'atencao',
                Icone: TriangleAlert,
              };

            const IconeStatus = configuracao.Icone;
            const progresso = Math.min(
              100,
              Math.max(0, Number(trabalho.progresso) || 0)
            );

            return (
              <article key={trabalho.id} className="trabalho-card">
                <div className="trabalho-card__conteudo">
                  <div className="trabalho-card-header">
                    <div className="trabalho-card__titulo">
                      <BriefcaseBusiness size={20} />

                      <h3 className="trabalho-nome">
                        {trabalho.titulo}
                      </h3>
                    </div>

                    <span
                      className={`status-badge status-${configuracao.classe}`}
                    >
                      <IconeStatus className="status-icon" />
                      <span>{configuracao.texto}</span>
                    </span>
                  </div>

                  <p className="trabalho-descricao">
                    {trabalho.descricao || 'Nenhuma descrição informada.'}
                  </p>

                  <div className="etapa-atual-box">
                    <span>
                      Etapa atual:{' '}
                      <strong>Consulte as etapas do trabalho</strong>
                    </span>
                  </div>

                  <div className="progress-container">
                    <div
                      className="progress-bar-bg"
                      role="progressbar"
                      aria-label={`Progresso de ${trabalho.titulo}`}
                      aria-valuenow={progresso}
                      aria-valuemin={0}
                      aria-valuemax={100}
                    >
                      <div
                        className={`progress-bar-fill ${
                          trabalho.status === 'CONCLUIDO'
                            ? 'fill-concluido'
                            : ''
                        }`}
                        style={{ width: `${progresso}%` }}
                      />
                    </div>

                    <span>{progresso}% concluído</span>
                  </div>
                </div>

                <footer className="trabalho-card-footer">
                  <span className="atualizado-texto">
                    Atualizado em{' '}
                    {formatarData(
                      trabalho.dataAtualizacao || trabalho.dataCriacao
                    )}
                  </span>

                  <button
                    type="button"
                    className="btn-ver-trabalho"
                    onClick={() => handleVerTrabalho(trabalho.id)}
                  >
                    <span>Ver trabalho</span>
                    <ArrowRight size={15} />
                  </button>
                </footer>
              </article>
            );
          })}
        </section>
      )}
    </main>
  );
}
