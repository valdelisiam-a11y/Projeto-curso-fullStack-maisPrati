
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


const trabalhosRecentes = [
  {
    id: 't1',
    nome: 'Cadastro de novo cliente',
    descricao: 'Documentação e contrato do cliente Marina Souza.',
    status: 'Em andamento',
    etapaAtual: 'Enviar proposta',
    progresso: 40,
    atualizadoEm: '17/08/2026',
    contexto: 'empresa',
    empresaId: 1,
  },
  {
    id: 't2',
    nome: 'Compra de materiais',
    descricao: 'Cimento, areia e ferragem para a obra da Rua das Acácias.',
    status: 'Precisa de atenção',
    etapaAtual: 'Aprovação',
    progresso: 40,
    atualizadoEm: '16/08/2026',
    contexto: 'empresa',
    empresaId: 1,
  },
  {
    id: 't3',
    nome: 'Prestação de serviço — Manutenção elétrica',
    descricao: 'Visita técnica e reparo no comércio do Sr. Antônio.',
    status: 'Atrasado',
    etapaAtual: 'Executar serviço',
    progresso: 50,
    atualizadoEm: '13/08/2026',
    contexto: 'empresa',
    empresaId: 2,
  },
  {
    id: 't4',
    nome: 'Pagamento de contas do mês',
    descricao: 'Boletos de água, luz e internet de agosto.',
    status: 'Concluído',
    etapaAtual: 'Todas as etapas concluídas',
    progresso: 100,
    atualizadoEm: '10/08/2026',
    contexto: 'pessoal',
    empresaId: null,
  },
];



function Inicio() {
  const { usuario } = useAuth();
  const { contexto, empresaSelecionada } = useContextoTrabalho();

  const trabalhosDoContexto = trabalhosRecentes.filter((trabalho) => {
        if (trabalho.contexto !== contexto) {
          return false;
        }

        if (contexto === 'empresa') {
          return trabalho.empresaId === empresaSelecionada?.id;
        }

        return true;
      });

      const totalEmAndamento = trabalhosDoContexto.filter(
        (trabalho) => trabalho.status === 'Em andamento'
      ).length;

      const totalAtencao = trabalhosDoContexto.filter(
        (trabalho) =>
          trabalho.status === 'Precisa de atenção' ||
          trabalho.status === 'Atrasado'
      ).length;

      const totalConcluidos = trabalhosDoContexto.filter(
        (trabalho) => trabalho.status === 'Concluído'
      ).length;

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
            <strong>{totalEmAndamento}</strong>
            <span>Em andamento</span>
          </article>

          <article className="indicador indicador--atencao">
            <span className="indicador__icone">
             <TriangleAlert size={20} />
            </span>
            <strong>{totalAtencao}</strong>
            <span>Precisam de atenção</span>
          </article>

          <article className="indicador indicador--concluido">
            <span className="indicador__icone">
              <CircleCheck size={20} />
            </span>
            <strong>{totalConcluidos}</strong>
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

        <div className="trabalhos-lista">
          {trabalhosDoContexto.map((trabalho) => {  
            const statusConfig = {
              'Em andamento': {
                classe: 'andamento',
                Icone: CirclePlay,
              },
              'Precisa de atenção': {
                classe: 'atencao',
                Icone: TriangleAlert,
              },
              Atrasado: {
                classe: 'atrasado',
                Icone: TriangleAlert,
              },
              'Concluído': {
                classe: 'concluido',
                Icone: CircleCheck,
              },
            };

            const status = statusConfig[trabalho.status];
            const IconeStatus = status.Icone;
            const trabalhosDoContexto = trabalhosRecentes.filter((trabalho) => {
              if (trabalho.contexto !== contexto) {
                return false;
              }

              if (contexto === 'empresa') {
                return trabalho.empresaId === empresaSelecionada?.id;
              }

              return true;
            });

            const totalEmAndamento = trabalhosDoContexto.filter(
              (trabalho) => trabalho.status === 'Em andamento'
            ).length;

            const totalAtencao = trabalhosDoContexto.filter(
              (trabalho) =>
                trabalho.status === 'Precisa de atenção' ||
                trabalho.status === 'Atrasado'
            ).length;

            const totalConcluidos = trabalhosDoContexto.filter(
              (trabalho) => trabalho.status === 'Concluído'
            ).length;

            return (
              <article className="trabalho-card" key={trabalho.id}>
                <div className="trabalho-card__conteudo">
                  <div className="trabalho-card__cabecalho">
                    <div className="trabalho-card__titulo">
                      <BriefcaseBusiness size={20} />
                      <h3>{trabalho.nome}</h3>
                    </div>

                    <span className={`status status--${status.classe}`}>
                      <IconeStatus size={14} />
                      {trabalho.status}
                    </span>
                  </div>

                  <p>{trabalho.descricao}</p>

                  <div className="trabalho-card__etapa">
                    Etapa atual: <strong>{trabalho.etapaAtual}</strong>
                  </div>

                  <div className="trabalho-card__progresso">
                    <div
                      className="trabalho-card__barra"
                      role="progressbar"
                      aria-label={`Conclusão de ${trabalho.nome}`}
                      aria-valuenow={trabalho.progresso}
                      aria-valuemin={0}
                      aria-valuemax={100}
                    >
                      <span style={{ width: `${trabalho.progresso}%` }} />
                    </div>

                    <span>{trabalho.progresso}% concluído</span>
                  </div>

                  <span className="trabalho-card__data">
                    Atualizado em {trabalho.atualizadoEm}
                  </span>
                </div>

                <div className="trabalho-card__detalhes">
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

      </section>
    </main>
  );
}

export default Inicio;
