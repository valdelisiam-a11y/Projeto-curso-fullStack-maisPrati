import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  ArrowRight,
  BriefcaseBusiness,
  CirclePlay,
  TriangleAlert,
  CircleCheck,
  Clock3,
  Plus,
} from 'lucide-react';
import './Inicio.css';
import ilustracaoOrganizacao from '../assets/ilustracao-organizacao-Inicial.png';
import { useAuth } from '../contexts/useAuth.js';
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

function Inicio() {
  const { usuario } = useAuth();
  const { contexto, empresaSelecionada } = useContextoTrabalho();

  const [trabalhos, setTrabalhos] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');

  useEffect(() => {
    async function carregarTrabalhos() {
      try {
        setCarregando(true);
        setErro('');

        const dados = await listarTrabalhos();
        const lista = Array.isArray(dados) ? dados : dados.content ?? [];

        setTrabalhos(lista);
      } catch (error) {
        setErro(error.message || 'Não foi possível carregar os trabalhos.');
      } finally {
        setCarregando(false);
      }
    }

    carregarTrabalhos();
  }, []);

  const trabalhosDoContexto = trabalhos
    .filter((trabalho) => {
      if (contexto === 'pessoal') {
        return trabalho.tipo === 'PESSOAL';
      }

      return (
        trabalho.tipo === 'EMPRESA' &&
        Number(trabalho.empresaId) === Number(empresaSelecionada?.id)
      );
    })
    .sort((a, b) => {
      const dataA = new Date(a.dataAtualizacao || a.dataCriacao || 0).getTime();
      const dataB = new Date(b.dataAtualizacao || b.dataCriacao || 0).getTime();

      return dataB - dataA;
    });

  const totalEmAndamento = trabalhosDoContexto.filter(
    (trabalho) => trabalho.status === 'EM_ANDAMENTO'
  ).length;

  const totalAtencao = trabalhosDoContexto.filter(
    (trabalho) => trabalho.status === 'PENDENTE'
  ).length;

  const totalConcluidos = trabalhosDoContexto.filter(
    (trabalho) => trabalho.status === 'CONCLUIDO'
  ).length;

  const recentes = trabalhosDoContexto.slice(0, 4);

  return (
    <main className="inicio">
      <header className="inicio__boas-vindas">
        <div className="inicio__apresentacao">
          {contexto === 'pessoal' ? (
            <span className="inicio__contexto-label">
              Seus trabalhos pessoais
            </span>
          ) : (
            <span className="inicio__contexto-label">
              Empresa: {empresaSelecionada?.nome ?? 'Sua empresa'}
            </span>
          )}

          <h1>Olá, {usuario?.nome || 'usuário'}!</h1>

          <p>
            {contexto === 'pessoal'
              ? 'O que você precisa organizar hoje?'
              : 'Veja como andam os trabalhos da empresa.'}
          </p>

          <Link to="/trabalhos/novo" className="inicio__novo">
            <Plus size={18} />
            Novo trabalho
          </Link>
        </div>

        <div className="inicio__ilustracao">
          <img
            src={ilustracaoOrganizacao}
            alt="Pessoa organizando pastas e documentos"
          />
        </div>
      </header>

      <section className="inicio__resumo">
        <h2>Como estão seus trabalhos?</h2>

        <div className="inicio__indicadores">
          <article className="indicador indicador--andamento">
            <span className="indicador__icone">
              <CirclePlay size={20} />
            </span>
            <strong>{carregando ? '—' : totalEmAndamento}</strong>
            <span>Em andamento</span>
          </article>

          <article className="indicador indicador--atencao">
            <span className="indicador__icone">
              <TriangleAlert size={20} />
            </span>
            <strong>{carregando ? '—' : totalAtencao}</strong>
            <span>Precisam de atenção</span>
          </article>

          <article className="indicador indicador--concluido">
            <span className="indicador__icone">
              <CircleCheck size={20} />
            </span>
            <strong>{carregando ? '—' : totalConcluidos}</strong>
            <span>Concluídos</span>
          </article>
        </div>
      </section>

      <section className="inicio__recentes">
        <div className="inicio__secao-header">
          <div>
            <h2>Trabalhos recentes</h2>
            <p>Acompanhe as últimas atualizações dos seus trabalhos.</p>
          </div>

          <Link to="/trabalhos" className="inicio__ver-todos">
            Ver todos <ArrowRight size={16} />
          </Link>
        </div>

        {carregando && (
          <p>Carregando seus trabalhos...</p>
        )}

        {!carregando && erro && (
          <p role="alert">{erro}</p>
        )}

        {!carregando && !erro && recentes.length === 0 && (
          <p>Nenhum trabalho encontrado neste contexto.</p>
        )}

        {!carregando && !erro && recentes.length > 0 && (
          <div className="trabalhos-lista">
            {recentes.map((trabalho) => {
              const configuracao =
                statusConfig[trabalho.status] ?? {
                  texto: trabalho.status ?? 'Status desconhecido',
                  classe: 'atencao',
                  Icone: TriangleAlert,
                };

              const IconeStatus = configuracao.Icone;

              return (

                <article className="trabalho-card" key={trabalho.id}>
                  <div className="trabalho-card__conteudo">
                    <div className="trabalho-card__cabecalho">
                      <div className="trabalho-card__titulo">
                        <BriefcaseBusiness size={20} />
                        <h3>{trabalho.titulo}</h3>
                      </div>

                      <span className={`status status--${configuracao.classe}`}>
                        <IconeStatus size={14} />
                        {configuracao.texto}
                      </span>
                    </div>

                    <p>
                      {trabalho.descricao || 'Nenhuma descrição informada.'}
                    </p>

                    <div className="trabalho-card__etapa">
                      <span>Etapa atual: </span>
                      <strong>Consulte as etapas do trabalho</strong>
                    </div>

                    <div className="trabalho-card__progresso">
                      <div
                        className="trabalho-card__progresso-barra"
                        role="progressbar"
                        aria-label={`Progresso de ${trabalho.titulo}`}
                        aria-valuenow={trabalho.progresso ?? 0}
                        aria-valuemin={0}
                        aria-valuemax={100}
                      >
                        <div
                          className="trabalho-card__progresso-valor"
                          style={{ width: `${trabalho.progresso ?? 0}%` }}
                        />
                      </div>

                      <span>{trabalho.progresso ?? 0}% concluído</span>
                    </div>
                  </div>

                  <div className="trabalho-card__detalhes">
                    <span className="trabalho-card__data">
                      Atualizado em{' '}
                      {formatarData(
                        trabalho.dataAtualizacao || trabalho.dataCriacao
                      )}
                    </span>

                    <Link
                      to={`/trabalhos/${trabalho.id}`}
                      className="trabalho-card__link"
                    >
                      Ver trabalho <ArrowRight size={15} />
                    </Link>
                  </div>
                </article>

              );
            })}
          </div>
        )}
      </section>
    </main>
  );
}

export default Inicio;