import React, { useState } from "react";
import {
  FolderKanban,
  Search,
  ArrowRight,
  AlertCircle,
  CheckCircle2,
  Clock3,
  Plus,
} from "lucide-react";
import "./MeusTrabalhos.css";

export default function MeusTrabalhos() {
  const [searchTerm, setSearchTerm] = useState("");
  const [filtroAtivo, setFiltroAtivo] = useState("Todos");

  // Dados mockados exatos conforme as imagens de referência do Lovable
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

  // Filtros combinados por termo de pesquisa e pílula selecionada
  const trabalhosFiltrados = trabalhos.filter((trabalho) => {
    const matchNome = trabalho.nome
      .toLowerCase()
      .includes(searchTerm.toLowerCase());

    if (filtroAtivo === "Todos") return matchNome;
    if (filtroAtivo === "Em andamento")
      return matchNome && trabalho.situacao === "Em andamento";
    if (filtroAtivo === "Precisam de atenção")
      return matchNome && trabalho.situacao === "Precisa de atenção";
    if (filtroAtivo === "Atrasados")
      return matchNome && trabalho.situacao === "Atrasado";
    if (filtroAtivo === "Concluídos")
      return matchNome && trabalho.situacao === "Concluído";

    return matchNome;
  });

  const handleVerTrabalho = (id) => {
    alert(`Acessando detalhes do trabalho ID: ${id}`);
  };

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
          icon: <AlertCircle className="status-icon" />,
        };
      default:
        return {
          className: "status-badge status-andamento",
          icon: <Clock3 className="status-icon" />,
        };
    }
  };

  return (
    <div className="trabalhos-container">
      {/* Cabeçalho Principal */}
      <div className="trabalhos-header-flex">
        <div className="trabalhos-main-header">
          <h1>Meus trabalhos</h1>
          <p>Acompanhe tudo o que você está organizando.</p>
        </div>
        <button
          className="btn-novo-trabalho"
          onClick={() => alert("Criar novo trabalho (Fora de escopo)")}
        >
          <Plus className="w-4 h-4" />
          <span>Novo trabalho</span>
        </button>
      </div>

      {/* Pesquisa e Filtros */}
      <div className="search-filter-card">
        <label className="search-label">O que você está procurando?</label>
        <div className="search-input-wrapper">
          <Search className="search-icon" />
          <input
            type="text"
            placeholder="Procure pelo nome do trabalho"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>

        {/* Pílulas de Filtro */}
        <div className="filter-pills">
          {[
            "Todos",
            "Em andamento",
            "Precisam de atenção",
            "Atrasados",
            "Concluídos",
          ].map((filtro) => (
            <button
              key={filtro}
              className={`filter-pill ${filtroAtivo === filtro ? "active" : ""}`}
              onClick={() => setFiltroAtivo(filtro)}
            >
              {filtro}
            </button>
          ))}
        </div>
      </div>

      {/* Lista de Cards ou Estado Vazio */}
      {trabalhosFiltrados.length === 0 ? (
        <div className="empty-state">
          <FolderKanban className="empty-icon" />
          <h3>Nenhum trabalho encontrado</h3>
          <p>Tente buscar por outro termo ou categoria.</p>
        </div>
      ) : (
        <div className="trabalhos-list">
          {trabalhosFiltrados.map((trabalho) => {
            const badge = getBadgeStatus(trabalho.situacao);
            const isConcluido = trabalho.situacao === "Concluído";

            return (
              <div key={trabalho.id} className="trabalho-card">
                <div className="trabalho-card-header">
                  <h3 className="trabalho-nome">{trabalho.nome}</h3>
                  <span className={badge.className}>
                    {badge.icon}
                    <span>{trabalho.situacao}</span>
                  </span>
                </div>

                <p className="trabalho-descricao">{trabalho.descricao}</p>

                <div className="etapa-atual-box">
                  <span>
                    Etapa atual: <strong>{trabalho.etapaAtual}</strong>
                  </span>
                </div>

                {/* Barra de Progresso (Verde se concluído, Roxa se em andamento/atrasado) */}
                <div className="progress-container">
                  <div className="progress-bar-bg">
                    <div
                      className={`progress-bar-fill ${isConcluido ? "fill-concluido" : ""}`}
                      style={{ width: `${trabalho.progresso}%` }}
                    ></div>
                  </div>
                  <span className="progress-text">
                    {trabalho.progresso}% concluído
                  </span>
                </div>

                <div className="trabalho-card-footer">
                  <span className="atualizado-texto">
                    Atualizado em {trabalho.atualizadoEm}
                  </span>
                  <button
                    className="btn-ver-trabalho"
                    onClick={() => handleVerTrabalho(trabalho.id)}
                  >
                    <span>Ver trabalho</span>
                    <ArrowRight className="w-4 h-4" />
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
