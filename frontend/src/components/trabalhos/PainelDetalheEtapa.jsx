import React from "react";
import {
  CheckCircle2,
  FileText,
  Eye,
  Download,
  Plus,
  Edit3,
  Check,
} from "lucide-react";

export default function PainelDetalheEtapa({
  etapaAtualObj,
  getTopBadgeClass,
  observacao,
  setObservacao,
  onSalvarObservacao,
  onMarcarConcluida,
}) {
  if (!etapaAtualObj) return null;

  return (
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
          className={`badge-status-geral ${getTopBadgeClass(etapaAtualObj.status)}`}
        >
          <span>{etapaAtualObj.status}</span>
        </span>
      </div>

      <div className="bloco-secao">
        <h4>Documentos</h4>
        <div className="documento-item">
          <div className="doc-info-nome">
            <FileText className="w-4 h-4 text-purple-600" />
            <span>proposta.pdf</span>
          </div>
          <div className="doc-acoes">
            <button
              type="button"
              className="btn-doc-acao"
              onClick={() => alert("Simulação: Visualizando proposta.pdf")}
            >
              <Eye className="w-3.5 h-3.5" />
              <span>Visualizar</span>
            </button>
            <button
              type="button"
              className="btn-doc-acao"
              onClick={() => alert("Simulação: Baixando proposta.pdf")}
            >
              <Download className="w-3.5 h-3.5" />
              <span>Baixar</span>
            </button>
          </div>
        </div>
        <button
          type="button"
          className="btn-adicionar-doc"
          onClick={() => alert("Simulação: Painel de upload")}
        >
          <Plus className="w-4 h-4" />
          <span>Adicionar documento</span>
        </button>
      </div>

      <div className="bloco-secao">
        <h4>Observações</h4>
        <textarea
          className="textarea-observacao"
          rows={3}
          value={observacao}
          onChange={(e) => setObservacao(e.target.value)}
        />
        <button
          type="button"
          className="btn-salvar-obs"
          onClick={onSalvarObservacao}
        >
          <Check className="w-3.5 h-3.5" />
          <span>Salvar observação</span>
        </button>
      </div>

      <div className="acoes-etapa-footer">
        <button
          type="button"
          className="btn-marcar-concluida"
          onClick={onMarcarConcluida}
        >
          <CheckCircle2 className="w-4 h-4" />
          <span>Marcar como concluída</span>
        </button>
        <button
          type="button"
          className="btn-editar-etapa"
          onClick={() => alert("Simulação: Editar etapa")}
        >
          <Edit3 className="w-3.5 h-3.5" />
          <span>Editar etapa</span>
        </button>
      </div>
    </div>
  );
}
