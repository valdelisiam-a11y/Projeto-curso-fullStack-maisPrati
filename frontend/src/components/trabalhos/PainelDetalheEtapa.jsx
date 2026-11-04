
import React, { useEffect, useState, useCallback } from "react";
import {
  CheckCircle2,
  FileText,
  Eye,
  Download,
  Plus,
  Edit3,
  Check,
} from "lucide-react";

import {
  listarDocumentos,
  baixarDocumento as baixarArquivo,
} from "../../services/documentoService";

import ModalUploadDocumento from "../documentosUpload/ModalUploadDocumento.jsx";
import "./EditarEtapa.css";

export default function PainelDetalheEtapa({
  etapaAtualObj,
  getTopBadgeClass,
  observacao,
  setObservacao,
  onSalvarObservacao,
  onMarcarConcluida,
  onSalvarEdicao,
}) {
  const [documentos, setDocumentos] = useState([]);
  const [carregandoDocumentos, setCarregandoDocumentos] = useState(false);
  const [erroDocumentos, setErroDocumentos] = useState("");
  const [modalAberto, setModalAberto] = useState(false);
  const [editando, setEditando] = useState(false);
  const [tituloEdicao, setTituloEdicao] = useState("");
  const [descricaoEdicao, setDescricaoEdicao] = useState("");
  const [salvandoEdicao, setSalvandoEdicao] = useState(false);

  const carregarDocumentos = useCallback(async () => {
    if (!etapaAtualObj?.id) {
      setDocumentos([]);
      return;
    }

    try {
      setCarregandoDocumentos(true);
      setErroDocumentos("");

      const dados = await listarDocumentos(etapaAtualObj.id);
      setDocumentos(Array.isArray(dados) ? dados : []);
    } catch (error) {
      setErroDocumentos(
        error.message || "Não foi possível carregar os documentos."
      );
    } finally {
      setCarregandoDocumentos(false);
    }
  }, [etapaAtualObj?.id]);

  useEffect(() => {
    carregarDocumentos();
  }, [carregarDocumentos]);

  useEffect(() => {
    setTituloEdicao(etapaAtualObj?.titulo ?? "");
    setDescricaoEdicao(etapaAtualObj?.descricao ?? "");
    setEditando(false);
  }, [etapaAtualObj?.id]);

  if (!etapaAtualObj) return null;

  async function obterArquivo(documento) {
    return baixarArquivo(etapaAtualObj.id, documento.id);
  }

  async function baixarDocumento(documento) {
    try {
      const arquivo = await obterArquivo(documento);
      const url = URL.createObjectURL(arquivo);

      const link = document.createElement("a");
      link.href = url;
      link.download = documento.nomeOriginal;

      document.body.appendChild(link);
      link.click();
      link.remove();

      setTimeout(() => URL.revokeObjectURL(url), 1000);
    } catch (error) {
      window.alert(error.message || "Não foi possível baixar o documento.");
    }
  }

  async function visualizarDocumento(documento) {
    const novaAba = window.open("", "_blank");

    if (!novaAba) {
      window.alert("Permita a abertura de novas abas para visualizar o documento.");
      return;
    }

    try {
      const arquivo = await obterArquivo(documento);
      const url = URL.createObjectURL(arquivo);

      novaAba.location.href = url;

      setTimeout(() => URL.revokeObjectURL(url), 60000);
    } catch (error) {
      novaAba.close();
      window.alert(error.message || "Não foi possível visualizar o documento.");
    }
  }

  return (
    <div className="detalhe-etapa-card">
      <div className="etapa-detalhe-header">
       <div>
          <h3>{etapaAtualObj.titulo}</h3>
          {etapaAtualObj.descricao && (
            <p>{etapaAtualObj.descricao}</p>
          )}

          <div className="etapa-detalhe-info">
            <p>
              Responsável: {etapaAtualObj.responsavel || "Não atribuído"}
            </p>

            <p>Data: {etapaAtualObj.data}</p>
          </div>
        </div>

        <span
          className={`badge-status-geral ${getTopBadgeClass(etapaAtualObj.status)}`}
        >
          <span>{etapaAtualObj.status}</span>
        </span>
      </div>

      <div className="bloco-secao">
        <h4>Documentos</h4>

        {carregandoDocumentos ? (
          <p>Carregando documentos...</p>
        ) : erroDocumentos ? (
          <div>
            <p>{erroDocumentos}</p>
            <button type="button" onClick={carregarDocumentos}>
              Tentar novamente
            </button>
          </div>
        ) : documentos.length > 0 ? (
          documentos.map((documento) => (
            <div className="documento-item" key={documento.id}>
              <div className="doc-info-nome">
                <FileText size={16} />
                <span>{documento.nomeOriginal}</span>
              </div>

              <div className="doc-acoes">
                <button
                  type="button"
                  className="btn-doc-acao"
                  onClick={() => visualizarDocumento(documento)}
                >
                  <Eye size={14} />
                  <span>Visualizar</span>
                </button>

                <button
                  type="button"
                  className="btn-doc-acao"
                  onClick={() => baixarDocumento(documento)}
                >
                  <Download size={14} />
                  <span>Baixar</span>
                </button>
              </div>
            </div>
          ))
        ) : (
          <p>Nenhum documento anexado a esta etapa.</p>
        )}

        <button
          type="button"
          className="btn-adicionar-doc"
          onClick={() => setModalAberto(true)}
        >
          <Plus size={16} />
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
          <Check size={14} />
          <span>Salvar observação</span>
        </button>
      </div>

      {editando && (
        <div className="editar-etapa-form">
          <h4>Editar etapa</h4>

          <div className="editar-etapa-campo">
            <label htmlFor="titulo-etapa">Título</label>
            <input
              id="titulo-etapa"
              type="text"
              value={tituloEdicao}
              onChange={(e) => setTituloEdicao(e.target.value)}
              maxLength={200}
              required
            />
          </div>

          <div className="editar-etapa-campo">
            <label htmlFor="descricao-etapa">Descrição</label>
            <textarea
              id="descricao-etapa"
              rows={3}
              value={descricaoEdicao}
              onChange={(e) => setDescricaoEdicao(e.target.value)}
            />
          </div>

          <div className="editar-etapa-acoes">
            <button
              type="button"
              className="editar-etapa-salvar"
              disabled={salvandoEdicao || !tituloEdicao.trim()}
              onClick={async () => {
                try {
                  setSalvandoEdicao(true);

                  await onSalvarEdicao({
                    titulo: tituloEdicao.trim(),
                    descricao: descricaoEdicao,
                  });

                  setEditando(false);
                } catch (error) {
                  window.alert(
                    error.message || "Não foi possível editar a etapa."
                  );
                } finally {
                  setSalvandoEdicao(false);
                }
              }}
            >
              {salvandoEdicao ? "Salvando..." : "Salvar alterações"}
            </button>

            <button
              type="button"
              className="editar-etapa-cancelar"
              onClick={() => setEditando(false)}
              disabled={salvandoEdicao}
            >
              Cancelar
            </button>
          </div>
        </div>
      )}

      <div className="acoes-etapa-footer">
        <button
          type="button"
          className="btn-marcar-concluida"
          onClick={onMarcarConcluida}
        >
          <CheckCircle2 size={16} />
          <span>Marcar como concluída</span>
        </button>

        {!editando && (
          <button
            type="button"
            className="btn-editar-etapa"
            onClick={() => setEditando(true)}
          >
            <Edit3 size={14} />
            <span>Editar etapa</span>
          </button>
        )}
      </div>

      <ModalUploadDocumento
        aberto={modalAberto}
        onFechar={() => setModalAberto(false)}
        onAdicionar={carregarDocumentos}
        trabalhoIdInicial={etapaAtualObj.trabalhoId}
        etapaIdInicial={etapaAtualObj.id}
      />
    </div>
  );
}
