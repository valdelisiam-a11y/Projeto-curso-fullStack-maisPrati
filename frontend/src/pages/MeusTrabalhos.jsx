import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  FolderKanban,
  Search,
  ArrowRight,
  AlertCircle,
  CheckCircle2,
  Clock3,
  Plus,
  CalendarClock,
} from "lucide-react";

import "./MeusTrabalhos.css";
import { useContextoTrabalho } from "../contexts/ContextoTrabalho";

export default function MeusTrabalhos() {
  const navigate = useNavigate();

  const [searchTerm, setSearchTerm] = useState("");
  const [filtroAtivo, setFiltroAtivo] = useState("Todos");

  const { contexto, empresaSelecionada } = useContextoTrabalho();

  // Dados de demonstração completos atualizados com os exemplos de referência
  const [trabalhos] = useState([
    {
      id: 1,
      nome: "Cadastro de novo cliente",
      descricao: "Documentação e contrato do cliente Marina Souza.",
      situacao: "Em andamento",
      etapaAtual: "Enviar proposta",
      progresso: 40,
      atualizadoEm: "17/08/2026",
    },
    {
      id: 2,
      nome: "Compra de materiais",
      descricao: "Cimento, areia e ferragem para a obra da Rua das Acácias.",
      situacao: "Precisa de atenção",
      etapaAtual: "Aprovação",
      progresso: 40,
      atualizadoEm: "16/08/2026",
    },
    {
      id: 3,
      nome: "Prestação de serviço — Manutenção elétrica",
      descricao: "Visita técnica e reparo no comércio do Sr. Antônio.",
      situacao: "Atrasado",
      etapaAtual: "Executar serviço",
      progresso: 50,
      atualizadoEm: "13/08/2026",
    },
    {
      id: 4,
      nome: "Pagamento de contas do mês",
      descricao: "Boletos de água, luz e internet de agosto.",
      situacao: "Concluído",
      etapaAtual: "Todas as etapas concluídas",
      progresso: 100,
      atualizadoEm: "10/08/2026",
    },
  ]);

  // Opções de filtro exibidas na referência visual
  const filtros = [
    "Todos",
    "Em andamento",
    "Precisam de atenção",
    "Atrasado",
    "Concluídos",
  ];

  // Pesquisa pelo nome e filtra pela situação de forma flexível
  const trabalhosFiltrados = trabalhos.filter((trabalho) => {
    const correspondeAoNome = trabalho.nome
      .toLowerCase()
      .includes(searchTerm.trim().toLowerCase());

    if (!correspondeAoNome) {
      return false;
    }

    switch (filtroAtivo) {
      case "Em andamento":
        return trabalho.situacao === "Em andamento";

      case "Precisam de atenção":
        return trabalho.situacao === "Precisa de atenção";

      case "Atrasado":
        return trabalho.situacao === "Atrasado";

      case "Concluídos":
        return trabalho.situacao === "Concluído";

      case "Todos":
      default:
        return true;
    }
  });

  // Navega para os detalhes do trabalho
  const handleVerTrabalho = (id) => {
    navigate(`/trabalhos/${id}`);
  };

  // Define o ícone e a classe visual de cada situação
  const getBadgeStatus = (situacao) => {
    switch (situacao) {
      case "Concluído":
        return {
          className: "status-badge status-concluido",
          icon: <CheckCircle2 className="status-icon" />,
        };

      case "Precisa de atenção":
        return {
          className: "status-badge status-atencao",
          icon: <AlertCircle className="status-icon" />,
        };

      case "Atrasado":
        return {
          className: "status-badge status-atrasado",
          icon: <Clock3 className="status-icon" />,
        };

      case "Em andamento":
      default:
        return {
          className: "status-badge status-andamento",
          icon: <Clock3 className="status-icon" />,
        };
    }
  };

  return (
    <main className="trabalhos-container">
      {/* Cabeçalho */}
      <header className="trabalhos-header-flex">
        <div className="trabalhos-main-header">
          <h1>Meus trabalhos</h1>

          <p>
            {contexto === "pessoal"
              ? "Seus trabalhos pessoais — só você vê."
              : `Trabalhos da empresa ${empresaSelecionada?.nome ?? ""}.`}
          </p>
        </div>

        <button
          type="button"
          className="btn-novo-trabalho"
          onClick={() => navigate("/trabalhos/novo")}
        >
          <Plus className="novo-trabalho-icon" />
          <span>Novo trabalho</span>
        </button>
      </header>

      {/* Pesquisa e filtros */}
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
                filtroAtivo === filtro ? "active" : ""
              }`}
              aria-pressed={filtroAtivo === filtro}
              onClick={() => setFiltroAtivo(filtro)}
            >
              {filtro}
            </button>
          ))}
        </div>
      </section>

      {/* Lista de trabalhos */}
      {trabalhosFiltrados.length === 0 ? (
        <section className="empty-state">
          <FolderKanban className="empty-icon" />

          <h3>Nenhum trabalho encontrado</h3>

          <p>Tente buscar por outro termo ou categoria.</p>
        </section>
      ) : (
        <section className="trabalhos-list">
          {trabalhosFiltrados.map((trabalho) => {
            const badge = getBadgeStatus(trabalho.situacao);
            const isConcluido = trabalho.situacao === "Concluído";

            return (
              <article key={trabalho.id} className="trabalho-card">
                {/* Nome e situação */}
                <div className="trabalho-card-header">
                  <h3 className="trabalho-nome">{trabalho.nome}</h3>

                  <span className={badge.className}>
                    {badge.icon}
                    <span>{trabalho.situacao}</span>
                  </span>
                </div>

                {/* Descrição */}
                <p className="trabalho-descricao">{trabalho.descricao}</p>

                {/* Etapa atual */}
                <div className="etapa-atual-box">
                  <span>
                    Etapa atual: <strong>{trabalho.etapaAtual}</strong>
                  </span>
                </div>

                {/* Progresso */}
                <div className="progress-container">
                  <div
                    className="progress-bar-bg"
                    role="progressbar"
                    aria-label={`Progresso de ${trabalho.nome}`}
                    aria-valuenow={trabalho.progresso}
                    aria-valuemin={0}
                    aria-valuemax={100}
                  >
                    <div
                      className={`progress-bar-fill ${
                        isConcluido ? "fill-concluido" : ""
                      }`}
                      style={{
                        width: `${trabalho.progresso}%`,
                      }}
                    />
                  </div>

                  <span className="progress-text">
                    {trabalho.progresso}% concluído
                  </span>
                </div>

                {/* Data e botão */}
                <footer className="trabalho-card-footer">
                  <span className="atualizado-texto">
                    <CalendarClock className="atualizado-icon" />
                    <span>Atualizado em {trabalho.atualizadoEm}</span>
                  </span>

                  <button
                    type="button"
                    className="btn-ver-trabalho"
                    onClick={() => handleVerTrabalho(trabalho.id)}
                  >
                    <span>Ver trabalho</span>
                    <ArrowRight className="ver-trabalho-icon" />
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
