import React, { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  ArrowLeft,
  CheckCircle2,
  Clock3,
  FileText,
  Eye,
  Download,
  Plus,
  Edit3,
  Check,
  AlertCircle,
} from "lucide-react";
import "./DetalhesTrabalho.css";

export default function DetalhesTrabalho() {
  const { id } = useParams();
  const navigate = useNavigate();

  // Banco de dados mockado completo para os 4 trabalhos conforme as referências
  const trabalhosData = {
    1: {
      id: 1,
      nome: "Cadastro de novo cliente",
      descricao: "Documentação e contrato do cliente Marina Souza.",
      situacao: "Em andamento",
      progresso: 40,
      prazo: "24/08/2026",
      etapas: [
        {
          id: 1,
          titulo: "Receber documentos",
          status: "Concluído",
          responsavel: "Ana",
          data: "15/08/2026",
        },
        {
          id: 2,
          titulo: "Conferir documentação",
          status: "Concluído",
          responsavel: "Carlos",
          data: "16/08/2026",
        },
        {
          id: 3,
          titulo: "Enviar proposta",
          status: "Em andamento",
          responsavel: "João",
          data: "16/08/2026",
        },
        {
          id: 4,
          titulo: "Assinar contrato",
          status: "Não iniciado",
          responsavel: "Maria",
          data: "-",
        },
        {
          id: 5,
          titulo: "Finalizar cadastro",
          status: "Não iniciado",
          responsavel: "Equipe",
          data: "-",
        },
      ],
      historico: [
        {
          id: 1,
          texto: "Nova etapa iniciada: Enviar proposta",
          data: "17/08 - 11:25",
          tipo: "info",
        },
        {
          id: 2,
          texto: "Etapa concluída: Conferir documentação",
          data: "16/08 - 11:20",
          tipo: "sucesso",
        },
        {
          id: 3,
          texto: "Trabalho criado",
          data: "15/08 - 09:30",
          tipo: "sucesso",
        },
      ],
    },
    2: {
      id: 2,
      nome: "Compra de materiais",
      descricao: "Cimento, areia e ferragem para a obra da Rua das Acácias.",
      situacao: "Precisa de atenção",
      progresso: 40,
      prazo: "19/08/2026",
      etapas: [
        {
          id: 1,
          titulo: "Solicitação",
          status: "Concluído",
          responsavel: "Eng. Marcos",
          data: "14/08/2026",
        },
        {
          id: 2,
          titulo: "Orçamento",
          status: "Concluído",
          responsavel: "Financeiro",
          data: "15/08/2026",
        },
        {
          id: 3,
          titulo: "Aprovação",
          status: "Precisa de atenção",
          responsavel: "Dona Rita",
          data: "-",
        },
        {
          id: 4,
          titulo: "Compra",
          status: "Não iniciado",
          responsavel: "Compras",
          data: "-",
        },
        {
          id: 5,
          titulo: "Entrega",
          status: "Não iniciado",
          responsavel: "Logística",
          data: "-",
        },
      ],
      historico: [
        {
          id: 1,
          texto: "Aprovação pendente de verificação de orçamento",
          data: "16/08 - 14:10",
          tipo: "atencao",
        },
        {
          id: 2,
          texto: "Orçamento concluído",
          data: "15/08 - 10:00",
          tipo: "sucesso",
        },
      ],
    },
    3: {
      id: 3,
      nome: "Prestação de serviço — Manutenção elétrica",
      descricao: "Visita técnica e reparo no comércio do Sr. Antônio.",
      situacao: "Atrasado",
      progresso: 50,
      prazo: "13/08/2026",
      etapas: [
        {
          id: 1,
          titulo: "Agendar visita",
          status: "Concluído",
          responsavel: "Paulo",
          data: "10/08/2026",
        },
        {
          id: 2,
          titulo: "Fazer orçamento",
          status: "Concluído",
          responsavel: "Paulo",
          data: "11/08/2026",
        },
        {
          id: 3,
          titulo: "Executar serviço",
          status: "Atrasado",
          responsavel: "Paulo",
          data: "-",
        },
        {
          id: 4,
          titulo: "Emitir recibo",
          status: "Não iniciado",
          responsavel: "Financeiro",
          data: "-",
        },
      ],
      historico: [
        {
          id: 1,
          texto: "Prazo da execução ultrapassado",
          data: "13/08 - 18:00",
          tipo: "atraso",
        },
        {
          id: 2,
          texto: "Fazer orçamento concluído",
          data: "11/08 - 16:30",
          tipo: "sucesso",
        },
      ],
    },
    4: {
      id: 4,
      nome: "Pagamento de contas do mês",
      descricao: "Boletos de água, luz e internet de agosto.",
      situacao: "Concluído",
      progresso: 100,
      prazo: "10/08/2026",
      etapas: [
        {
          id: 1,
          titulo: "Receber boleto",
          status: "Concluído",
          responsavel: "Sistema",
          data: "08/08/2026",
        },
        {
          id: 2,
          titulo: "Efetuar pagamento",
          status: "Concluído",
          responsavel: "Financeiro",
          data: "09/08/2026",
        },
        {
          id: 3,
          titulo: "Anexar comprovante",
          status: "Concluído",
          responsavel: "Financeiro",
          data: "09/08/2026",
        },
        {
          id: 4,
          titulo: "Arquivar",
          status: "Concluído",
          responsavel: "Administrativo",
          data: "10/08/2026",
        },
      ],
      historico: [
        {
          id: 1,
          texto: "Todas as etapas concluídas com sucesso",
          data: "10/08 - 09:00",
          tipo: "sucesso",
        },
        {
          id: 2,
          texto: "Trabalho criado",
          data: "08/08 - 08:00",
          tipo: "sucesso",
        },
      ],
    },
  };

  // Seleciona o trabalho com base no ID da URL (fallback para o ID 1 caso não encontre)
  const trabalho = trabalhosData[id] || trabalhosData[1];

  const [etapaSelecionadaId, setEtapaSelecionadaId] = useState(
    trabalho.etapas[2]?.id || trabalho.etapas[0].id,
  );
  const [observacao, setObservacao] = useState(
    "Cliente pediu para revisar o valor do plano anual.",
  );

  const etapaAtualObj =
    trabalho.etapas.find((e) => e.id === etapaSelecionadaId) ||
    trabalho.etapas[0];

  const handleSalvarObservacao = () => {
    alert("Observação salva com sucesso!");
  };

  const handleMarcarConcluida = () => {
    alert(`Etapa "${etapaAtualObj.titulo}" marcada como concluída!`);
  };

  // Função para definir o estilo do badge de status do topo
  const getTopBadgeClass = (situacao) => {
    switch (situacao) {
      case "Concluído":
        return "badge-concluido";
      case "Precisa de atenção":
        return "badge-atencao";
      case "Atrasado":
        return "badge-atrasado";
      default:
        return "badge-andamento";
    }
  };

  return (
    <div className="detalhes-container">
      {/* Botão Voltar */}
      <button className="btn-voltar" onClick={() => navigate("/trabalhos")}>
        <ArrowLeft className="w-4 h-4" />
        <span>Voltar</span>
      </button>

      {/* Card Principal do Trabalho */}
      <div className="trabalho-topo-card">
        <div className="topo-header-flex">
          <div>
            <h2>{trabalho.nome}</h2>
            <p>{trabalho.descricao}</p>
          </div>
          <span
            className={`badge-status-geral ${getTopBadgeClass(trabalho.situacao)}`}
          >
            {trabalho.situacao === "Concluído" ? (
              <CheckCircle2 className="w-3.5 h-3.5" />
            ) : (
              <Clock3 className="w-3.5 h-3.5" />
            )}
            <span>{trabalho.situacao}</span>
          </span>
        </div>

        <div className="progresso-wrapper">
          <div className="barra-progresso-fundo">
            <div
              className="barra-progresso-preenchimento"
              style={{ width: `${trabalho.progresso}%` }}
            ></div>
          </div>
          <span className="progresso-porcentagem">
            {trabalho.progresso}% concluído
          </span>
        </div>

        <div className="prazo-info">
          <Clock3 className="w-4 h-4 text-gray-500" />
          <span>Prazo: {trabalho.prazo}</span>
        </div>
      </div>

      {/* Seção de Etapas (Carrossel Horizontal) */}
      <div className="etapas-secao">
        <h3 className="secao-titulo">Etapas</h3>
        <p className="secao-subtitulo">
          Toque em uma etapa para ver os detalhes.
        </p>

        <div className="etapas-horizontal-grid">
          {trabalho.etapas.map((etapa) => {
            const isSelecionada = etapa.id === etapaSelecionadaId;
            const isConcluida = etapa.status === "Concluído";
            const isEmAndamento = etapa.status === "Em andamento";
            const isAtencao = etapa.status === "Precisa de atenção";
            const isAtrasado = etapa.status === "Atrasado";

            return (
              <div
                key={etapa.id}
                className={`etapa-card-item ${isSelecionada ? "selecionada" : ""}`}
                onClick={() => setEtapaSelecionadaId(etapa.id)}
              >
                <div className="etapa-card-top">
                  {isConcluida ? (
                    <span className="icone-etapa-status concluido">
                      <CheckCircle2 className="w-3.5 h-3.5" />
                    </span>
                  ) : (
                    <span
                      className={`icone-etapa-status ${isEmAndamento ? "andamento" : isAtencao ? "atencao" : isAtrasado ? "atrasado" : "inativo"}`}
                    >
                      {etapa.id}
                    </span>
                  )}
                  <span className="etapa-card-titulo">{etapa.titulo}</span>
                </div>
                <span
                  className={`etapa-card-substatus ${etapa.status.toLowerCase().replace(/\s+/g, "-")}`}
                >
                  {etapa.status}
                </span>
              </div>
            );
          })}
        </div>
      </div>

      {/* Painel de Detalhes da Etapa Ativa */}
      <div className="detalhe-etapa-card">
        <div className="etapa-detalhe-header">
          <div>
            <h3>{etapaAtualObj.titulo}</h3>
            <p>
              Responsável: {etapaAtualObj.responsavel} · Data:{" "}
              {etapaAtualObj.data}
            </p>
          </div>
          <span
            className={`badge-status-geral ${getTopBadgeClass(trabalho.situacao)}`}
          >
            <span>{etapaAtualObj.status}</span>
          </span>
        </div>

        {/* Documentos */}
        <div className="bloco-secao">
          <h4>Documentos</h4>
          <div className="documento-item">
            <div className="doc-info-nome">
              <FileText className="w-4 h-4 text-purple-600" />
              <span>proposta.pdf</span>
            </div>
            <div className="doc-acoes">
              <button
                className="btn-doc-acao"
                onClick={() => alert("Visualizando proposta.pdf")}
              >
                <Eye className="w-3.5 h-3.5" />
                <span>Visualizar</span>
              </button>
              <button
                className="btn-doc-acao"
                onClick={() => alert("Baixando proposta.pdf")}
              >
                <Download className="w-3.5 h-3.5" />
                <span>Baixar</span>
              </button>
            </div>
          </div>
          <button
            className="btn-adicionar-doc"
            onClick={() => alert("Abrir upload")}
          >
            <Plus className="w-4 h-4" />
            <span>Adicionar documento</span>
          </button>
        </div>

        {/* Observações */}
        <div className="bloco-secao">
          <h4>Observações</h4>
          <textarea
            className="textarea-observacao"
            rows={3}
            value={observacao}
            onChange={(e) => setObservacao(e.target.value)}
          />
          <button className="btn-salvar-obs" onClick={handleSalvarObservacao}>
            <Check className="w-3.5 h-3.5" />
            <span>Salvar observação</span>
          </button>
        </div>

        {/* Ações da Etapa */}
        <div className="acoes-etapa-footer">
          <button
            className="btn-marcar-concluida"
            onClick={handleMarcarConcluida}
          >
            <CheckCircle2 className="w-4 h-4" />
            <span>Marcar como concluída</span>
          </button>
          <button
            className="btn-editar-etapa"
            onClick={() => alert("Editar etapa")}
          >
            <Edit3 className="w-3.5 h-3.5" />
            <span>Editar etapa</span>
          </button>
        </div>
      </div>

      {/* Histórico / Linha do tempo */}
      <div className="historico-card">
        <h3>O que aconteceu?</h3>
        <div className="timeline-lista">
          {trabalho.historico.map((hist) => (
            <div key={hist.id} className="timeline-item">
              <span className={`timeline-ponto ${hist.tipo}`}></span>
              <div className="timeline-conteudo">
                <p className="timeline-texto">{hist.texto}</p>
                <span className="timeline-data">{hist.data}</span>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
