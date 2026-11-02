import React, { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  ArrowLeft,
  PlayCircle,
  AlertTriangle,
  Check,
  Eye,
  Pencil,
  ChevronDown,
} from "lucide-react";
import "./DetalhesEtapa.css";

export default function DetalhesEtapa() {
  const { id } = useParams();
  const navigate = useNavigate();

  const etapasMock = [
    {
      id: "1",
      nome: "Conferir notas fiscais",
      trabalho: "Reforma da loja centro",
      empresa: "Construtora Acácias",
      status: "Em andamento",
      permissao: "editar",
      compartilhadoPor: "Carlos",
      data: "17/08/2026",
      observacao: "Separar as notas de julho e agosto.",
    },
    {
      id: "2",
      nome: "Revisar relatório final",
      trabalho: "Auditoria trimestral",
      empresa: "Souza Serviços",
      status: "Precisa de atenção",
      permissao: "visualizar",
      compartilhadoPor: "João",
      data: "20/08/2026",
      observacao: "Nenhuma observação.",
    },
  ];

  const etapaAtual = etapasMock.find((e) => e.id === id) || etapasMock[0];

  const [status, setStatus] = useState(etapaAtual.status);
  const [observacao, setObservacao] = useState(etapaAtual.observacao);
  const [salvo, setSalvo] = useState(false);

  // Estado para controlar a abertura do select customizado
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);

  const opcoesStatus = [
    "Não iniciado",
    "Em andamento",
    "Precisa de atenção",
    "Concluído",
    "Atrasado",
  ];

  const handleSalvar = (e) => {
    e.preventDefault();
    setSalvo(true);
    setTimeout(() => setSalvo(false), 2500);
  };

  return (
    <div className="detalhes-etapa-container">
      {/* Botão Voltar */}
      <button className="btn-voltar" onClick={() => navigate(-1)}>
        <ArrowLeft size={16} /> Voltar
      </button>

      <div className="detalhes-card">
        {/* Badge de Permissão no Topo */}
        <div className="topo-permissao">
          {etapaAtual.permissao === "editar" ? (
            <span className="badge-permissao pode-editar">
              <Pencil size={12} /> Pode editar
            </span>
          ) : (
            <span className="badge-permissao pode-visualizar">
              <Eye size={12} /> Pode visualizar
            </span>
          )}
        </div>

        {/* Cabeçalho do Card */}
        <div className="detalhes-header-content">
          <div>
            <h1>{etapaAtual.nome}</h1>
            <p className="detalhes-subtitulo">
              Do trabalho “{etapaAtual.trabalho}” · {etapaAtual.empresa}
            </p>
            <p className="detalhes-meta">
              Responsável: {etapaAtual.compartilhadoPor} · Data:{" "}
              {etapaAtual.data}
            </p>
          </div>

          <div>
            {status === "Em andamento" ? (
              <span className="badge-status em-andamento">
                <PlayCircle size={14} /> Em andamento
              </span>
            ) : (
              <span className="badge-status precisa-atencao">
                <AlertTriangle size={14} /> Precisa de atenção
              </span>
            )}
          </div>
        </div>

        {/* SE CENÁRIO 1: PERMISSÃO DE EDITAR */}
        {etapaAtual.permissao === "editar" ? (
          <form onSubmit={handleSalvar} className="form-edicao-etapa">
            <div className="input-group">
              <label>Situação da etapa</label>

              {/* SELECT CUSTOMIZADO */}
              <div
                className={`custom-select-wrapper ${isDropdownOpen ? "open" : ""}`}
              >
                <div
                  className="custom-select-trigger"
                  onClick={() => setIsDropdownOpen(!isDropdownOpen)}
                >
                  <span>{status}</span>
                  <ChevronDown size={16} color="#6b7280" />
                </div>

                {isDropdownOpen && (
                  <ul className="custom-options-list">
                    {opcoesStatus.map((opcao) => {
                      const selecionado = status === opcao;
                      return (
                        <li
                          key={opcao}
                          className={`custom-option ${selecionado ? "selected" : ""}`}
                          onClick={() => {
                            setStatus(opcao);
                            setIsDropdownOpen(false);
                          }}
                        >
                          <span>{opcao}</span>
                          {selecionado && <Check size={16} color="#271f3d" />}
                        </li>
                      );
                    })}
                  </ul>
                )}
              </div>
            </div>

            <div className="input-group">
              <label htmlFor="observacoes-textarea">Observações</label>
              <textarea
                id="observacoes-textarea"
                rows={4}
                value={observacao}
                onChange={(e) => setObservacao(e.target.value)}
              />
            </div>

            <button type="submit" className="btn-salvar-obs">
              <Check size={14} />{" "}
              {salvo ? "Salvo com sucesso!" : "Salvar observação"}
            </button>
          </form>
        ) : (
          /* SE CENÁRIO 2: PERMISSÃO APENAS DE VISUALIZAÇÃO */
          <div className="visualizacao-etapa">
            <div className="input-group">
              <label>Observações</label>
              <p className="texto-observacao-estatico">{observacao}</p>
            </div>

            <div className="aviso-visualizacao-container">
              <Eye size={16} />
              <span>Você pode apenas visualizar esta etapa.</span>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
