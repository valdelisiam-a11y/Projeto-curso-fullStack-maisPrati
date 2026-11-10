
import React, { useCallback, useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  ArrowLeft,
  AlertCircle,
  FileText,
  Download,
  Eye,
  Upload,
  LoaderCircle,
} from "lucide-react";

import { useContextoTrabalho } from "../contexts/ContextoTrabalho";
import {
  listarDocumentos,
  baixarDocumento,
} from "../services/documentoService";
import ModalUploadDocumento from "../components/documentosUpload/ModalUploadDocumento.jsx";

import './DetalhesEtapa.css';

function normalizarStatus(status) {
  const statusMapeados = {
    EM_ANDAMENTO: "Em andamento",
    CONCLUIDO: "Concluído",
    CONCLUIDA: "Concluído",
    PENDENTE: "Pendente",
    CANCELADO: "Cancelado",
  };

  return statusMapeados[status] ?? status ?? "Pendente";
}

function formatarTamanho(bytes) {
  if (bytes == null) return "Tamanho não informado";

  if (bytes < 1024) return `${bytes} bytes`;

  if (bytes < 1024 * 1024) {
    return `${(bytes / 1024).toFixed(1)} KB`;
  }

  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

function formatarData(data) {
  if (!data) return "Data não informada";

  const dataFormatada = new Date(data);

  if (Number.isNaN(dataFormatada.getTime())) {
    return "Data não informada";
  }

  return dataFormatada.toLocaleDateString("pt-BR");
}

export default function DetalhesEtapa() {
  const { id } = useParams();
  const navigate = useNavigate();

  const {
    contexto,
    empresaSelecionada,
    minhasEtapas,
    carregandoEtapas,
  } = useContextoTrabalho();

  const [etapa, setEtapa] = useState(null);
  const [documentos, setDocumentos] = useState([]);
  const [carregandoDocumentos, setCarregandoDocumentos] = useState(false);
  const [erroDocumentos, setErroDocumentos] = useState("");
  const [erro, setErro] = useState("");
  const [modalAberto, setModalAberto] = useState(false);

  const etapaPermitida = minhasEtapas.find(
    (item) =>
      Number(item.etapaId) === Number(id) &&
      Number(item.empresaId) === Number(empresaSelecionada?.id)
  );

  useEffect(() => {
    if (carregandoEtapas) return;

    if (contexto !== "empresa" || !empresaSelecionada) {
      setEtapa(null);
      setErro("Esta etapa não está disponível no contexto atual.");
      return;
    }

    if (!etapaPermitida) {
      setEtapa(null);
      setErro("Etapa não encontrada ou você não tem acesso a ela.");
      return;
    }

    setErro("");

    setEtapa({
      id: etapaPermitida.etapaId,
      titulo: etapaPermitida.etapaTitulo,
      status: normalizarStatus(etapaPermitida.status),
      percentualConclusao: etapaPermitida.percentualConclusao ?? 0,
      ordem: etapaPermitida.ordem,
      trabalhoId: etapaPermitida.trabalhoId,
      trabalhoTitulo: etapaPermitida.trabalhoTitulo,
      empresaId: etapaPermitida.empresaId,
      empresaNome: etapaPermitida.empresaNome,
    });
  }, [
    id,
    contexto,
    empresaSelecionada?.id,
    carregandoEtapas,
    etapaPermitida,
  ]);

  const carregarDocumentos = useCallback(async () => {
    if (!etapa?.id) return;

    setCarregandoDocumentos(true);
    setErroDocumentos("");

    try {
      const lista = await listarDocumentos(etapa.id);
      setDocumentos(lista);
    } catch (erroApi) {
      setErroDocumentos(
        erroApi.message || "Não foi possível carregar os documentos."
      );
    } finally {
      setCarregandoDocumentos(false);
    }
  }, [etapa?.id]);

  useEffect(() => {
    carregarDocumentos();
  }, [carregarDocumentos]);

  async function abrirDocumento(documento) {
    try {
      const arquivo = await baixarDocumento(etapa.id, documento.id);
      const url = URL.createObjectURL(arquivo);

      window.open(url, "_blank", "noopener,noreferrer");

      setTimeout(() => URL.revokeObjectURL(url), 60000);
    } catch (erroApi) {
      setErroDocumentos(
        erroApi.message || "Não foi possível visualizar o documento."
      );
    }
  }

  async function fazerDownload(documento) {
    try {
      const arquivo = await baixarDocumento(etapa.id, documento.id);
      const url = URL.createObjectURL(arquivo);

      const link = document.createElement("a");
      link.href = url;
      link.download = documento.nomeOriginal;
      document.body.appendChild(link);
      link.click();
      link.remove();

      URL.revokeObjectURL(url);
    } catch (erroApi) {
      setErroDocumentos(
        erroApi.message || "Não foi possível baixar o documento."
      );
    }
  }

  if (carregandoEtapas) {
    return (
      <div className="detalhes-etapa-container">
        <p>Carregando etapa...</p>
      </div>
    );
  }

  if (erro || !etapa) {
    return (
      <div className="detalhes-etapa-container">
        <div className="detalhes-card">
          <AlertCircle size={22} />
          <p>{erro || "Etapa não encontrada."}</p>

          <button
            type="button"
            className="btn-voltar"
            onClick={() => navigate(-1)}
          >
            <ArrowLeft size={16} />
            Voltar
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="detalhes-etapa-container">
      <button
        type="button"
        className="btn-voltar"
        onClick={() => navigate(-1)}
      >
        <ArrowLeft size={16} />
        Voltar
      </button>

      <div className="detalhes-card">
        <div className="detalhes-header-content">
          <div>
            <h1>{etapa.titulo}</h1>

            <p className="detalhes-subtitulo">
              Do trabalho “{etapa.trabalhoTitulo}” · {etapa.empresaNome}
            </p>

            <p className="detalhes-meta">
              Etapa {etapa.ordem ?? ""}
            </p>
          </div>

          <span className="badge-status">
            {etapa.status}
          </span>
        </div>

        <div className="input-group">
          <label>Progresso da etapa</label>

          <div className="progresso-etapa">
            <div className="progresso-etapa-barra">
              <div
                className="progresso-etapa-preenchimento"
                style={{
                  width: `${Math.min(
                    100,
                    Math.max(0, etapa.percentualConclusao)
                  )}%`,
                }}
              />
            </div>

            <span>{etapa.percentualConclusao}%</span>
          </div>
        </div>

        <section className="documentos-etapa">
          <div className="documentos-etapa-header">
            <div>
              <h2>Documentos da etapa</h2>
              <p>Arquivos vinculados a esta etapa.</p>
            </div>

            <button
              type="button"
              className="btn-adicionar-documento"
              onClick={() => setModalAberto(true)}
            >
              <Upload size={16} />
              Adicionar documento
            </button>
          </div>

          {carregandoDocumentos ? (
            <p>Carregando documentos...</p>
          ) : erroDocumentos ? (
            <div className="erro-documentos">
              <AlertCircle size={16} />
              <span>{erroDocumentos}</span>

              <button type="button" onClick={carregarDocumentos}>
                Tentar novamente
              </button>
            </div>
          ) : documentos.length === 0 ? (
            <div className="documentos-vazio">
              <FileText size={28} />
              <p>Nenhum documento foi adicionado a esta etapa.</p>
            </div>
          ) : (
            <div className="lista-documentos-etapa">
              {documentos.map((documento) => (
                <div
                  className="documento-etapa-item"
                  key={documento.id}
                >
                  <div className="documento-etapa-info">
                    <FileText size={20} />

                    <div>
                      <strong>{documento.nomeOriginal}</strong>

                      <span>
                        {formatarTamanho(documento.tamanho)} ·{" "}
                        {formatarData(documento.dataUpload)}
                      </span>
                    </div>
                  </div>

                  <div className="documento-etapa-acoes">
                    <button
                      type="button"
                      title="Visualizar documento"
                      onClick={() => abrirDocumento(documento)}
                    >
                      <Eye size={17} />
                    </button>

                    <button
                      type="button"
                      title="Baixar documento"
                      onClick={() => fazerDownload(documento)}
                    >
                      <Download size={17} />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>
      </div>

      <ModalUploadDocumento
        aberto={modalAberto}
        onFechar={() => setModalAberto(false)}
        onAdicionar={carregarDocumentos}
        trabalhoIdInicial={etapa.trabalhoId}
        etapaIdInicial={etapa.id}
        contexto={contexto}
        empresaSelecionada={empresaSelecionada}
        minhasEtapas={minhasEtapas}
      />
    </div>
  );
}
