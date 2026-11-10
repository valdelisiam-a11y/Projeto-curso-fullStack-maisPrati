
import { useState, useEffect, useCallback } from "react";

import {
  Search,
  Plus,
  FileText,
  Image as ImageIcon,
  Eye,
  Download,
  Trash2,
} from "lucide-react";

import { useNavigate } from "react-router-dom";
import { useContextoTrabalho } from "../contexts/ContextoTrabalho";
import { useAuth } from "../contexts/useAuth";

import "./Documentos.css";
import ModalUploadDocumento from "../components/documentosUpload/ModalUploadDocumento.jsx";

import {
  listarTrabalhos,
  listarEtapasDoTrabalho,
} from "../services/trabalhoService";

import {
  listarDocumentos,
  baixarDocumento as baixarArquivo,
  excluirDocumento,
} from "../services/documentoService";

import Confirmacao from "../components/confirmacao/confirmacao.jsx";
import Feedback from "../components/feedback/feedback.jsx";

function formatarData(data) {
  if (!data) return "Data não informada";

  return new Date(data).toLocaleDateString("pt-BR");
}

function identificarTipo(documento) {
  const contentType = documento.contentType?.toLowerCase() || "";
  const nome = documento.nomeOriginal?.toLowerCase() || "";

  if (
    contentType.startsWith("image/") ||
    /\.(jpg|jpeg|png)$/.test(nome)
  ) {
    return "Imagem";
  }

  if (contentType === "application/pdf" || nome.endsWith(".pdf")) {
    return "PDF";
  }

  return "Documento";
}

function Documentos() {
  const [documentos, setDocumentos] = useState([]);
  const [busca, setBusca] = useState("");
  const [filtro, setFiltro] = useState("Todos");
  const [modalAberto, setModalAberto] = useState(false);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState("");

  const navigate = useNavigate();

  const {
    contexto,
    empresaSelecionada,
    minhasEtapas,
    carregandoEtapas,
  } = useContextoTrabalho();

  const { usuario } = useAuth();

  const [excluindo, setExcluindo] = useState(false);
  const [documentoParaExcluir, setDocumentoParaExcluir] = useState(null);
  const [feedback, setFeedback] = useState(null);

  const carregarDocumentos = useCallback(async () => {
    try {
      setCarregando(true);
      setErro("");
      setDocumentos([]);

      // Aguarda o carregamento das etapas da empresa selecionada.
      // Isso evita consultar dados antes de receber as permissões
      // de acesso do colaborador.
      if (contexto === "empresa" && carregandoEtapas) {
        return;
      }

      const dados = await listarTrabalhos();

      const lista = Array.isArray(dados)
        ? dados
        : dados?.content ?? [];

      let trabalhosDisponiveis = [];

      // CONTEXTO PESSOAL
      // O usuário visualiza somente seus próprios trabalhos pessoais.
      if (contexto === "pessoal") {
        trabalhosDisponiveis = lista
          .filter(
            (trabalho) =>
              trabalho.tipo === "PESSOAL" &&
              Number(trabalho.criadoPorId) === Number(usuario?.id)
          )
          .map((trabalho) => ({
            ...trabalho,
            etapasPermitidas: null,
          }));
      }

      // CONTEXTO EMPRESA
      if (contexto === "empresa" && empresaSelecionada?.id) {
        // Trabalhos criados pelo usuário:
        // ele pode consultar todas as etapas.
        const trabalhosCriados = lista
          .filter(
            (trabalho) =>
              trabalho.tipo === "EMPRESA" &&
              Number(trabalho.empresaId) ===
                Number(empresaSelecionada.id) &&
              Number(trabalho.criadoPorId) === Number(usuario?.id)
          )
          .map((trabalho) => ({
            ...trabalho,
            etapasPermitidas: null,
          }));

        const idsCriados = new Set(
          trabalhosCriados.map((trabalho) => Number(trabalho.id))
        );

        // Trabalhos em que o usuário participa como colaborador:
        // utiliza somente as etapas retornadas pelo backend
        // em minhasEtapas.
        const grupos = {};

        (minhasEtapas ?? []).forEach((etapa) => {
          if (
            Number(etapa.empresaId) !==
              Number(empresaSelecionada.id) ||
            idsCriados.has(Number(etapa.trabalhoId))
          ) {
            return;
          }

          if (!grupos[etapa.trabalhoId]) {
            grupos[etapa.trabalhoId] = {
              id: etapa.trabalhoId,
              titulo: etapa.trabalhoTitulo,
              tipo: "EMPRESA",
              etapasPermitidas: [],
            };
          }

          grupos[etapa.trabalhoId].etapasPermitidas.push({
            id: etapa.etapaId,
            titulo: etapa.etapaTitulo,
          });
        });

        trabalhosDisponiveis = [
          ...trabalhosCriados,
          ...Object.values(grupos),
        ];
      }

      // Busca documentos somente das etapas permitidas.
      const documentosPorTrabalho = await Promise.all(
        trabalhosDisponiveis.map(async (trabalho) => {
          const etapas =
            trabalho.etapasPermitidas ??
            await listarEtapasDoTrabalho(trabalho.id);

          const documentosPorEtapa = await Promise.all(
            etapas.map(async (etapa) => {
              const documentosEtapa = await listarDocumentos(etapa.id);

              return documentosEtapa.map((documento) => ({
                ...documento,
                nome: documento.nomeOriginal,
                tipo: identificarTipo(documento),
                trabalho: trabalho.titulo,
                trabalhoId: trabalho.id,
                etapa: etapa.titulo,
                etapaId: etapa.id,
                data: formatarData(documento.dataUpload),
              }));
            })
          );

          return documentosPorEtapa.flat();
        })
      );

      setDocumentos(documentosPorTrabalho.flat());
    } catch (error) {
      console.error("Erro ao carregar documentos:", error);

      setErro(
        error.message || "Não foi possível carregar os documentos."
      );
    } finally {
      setCarregando(false);
    }
  }, [
    contexto,
    empresaSelecionada?.id,
    minhasEtapas,
    carregandoEtapas,
    usuario?.id,
  ]);

  useEffect(() => {
    carregarDocumentos();
  }, [carregarDocumentos]);

  useEffect(() => {
    if (feedback?.tipo !== "sucesso") return;

    const temporizador = setTimeout(() => {
      setFeedback(null);
    }, 3500);

    return () => clearTimeout(temporizador);
  }, [feedback]);

  const documentosFiltrados = documentos.filter((documento) => {
    const termo = busca.trim().toLowerCase();

    const correspondeBusca =
      documento.nome.toLowerCase().includes(termo) ||
      documento.trabalho.toLowerCase().includes(termo) ||
      documento.etapa.toLowerCase().includes(termo);

    const correspondeFiltro =
      filtro === "Todos" || documento.tipo === filtro;

    return correspondeBusca && correspondeFiltro;
  });

  async function obterArquivo(documento) {
    return baixarArquivo(documento.etapaId, documento.id);
  }

  async function baixarDocumento(documento) {
    try {
      const arquivo = await obterArquivo(documento);
      const url = URL.createObjectURL(arquivo);

      const link = document.createElement("a");
      link.href = url;
      link.download = documento.nome;
      document.body.appendChild(link);
      link.click();
      link.remove();

      URL.revokeObjectURL(url);
    } catch (error) {
      setFeedback({
        tipo: "erro",
        titulo: "Erro ao baixar documento",
        mensagem: error.message || "Não foi possível baixar o documento.",
      });
    }
  }

  async function confirmarExclusaoDocumento() {
    if (!documentoParaExcluir) return;

    const documento = documentoParaExcluir;

    try {
      setExcluindo(true);

      await excluirDocumento(documento.etapaId, documento.id);

      setDocumentos((atuais) =>
        atuais.filter(
          (item) =>
            item.id !== documento.id ||
            item.etapaId !== documento.etapaId
        )
      );

      setDocumentoParaExcluir(null);

      setFeedback({
        tipo: "sucesso",
        titulo: "Documento excluído",
        mensagem: "O documento foi excluído com sucesso.",
      });
    } catch (error) {
      setDocumentoParaExcluir(null);

      setFeedback({
        tipo: "erro",
        titulo: "Erro ao excluir documento",
        mensagem: error.message || "Não foi possível excluir o documento.",
      });
    } finally {
      setExcluindo(false);
    }
  }

  async function visualizarDocumento(documento) {
    const novaAba = window.open("", "_blank");

    if (!novaAba) {
      setFeedback({
        tipo: "aviso",
        titulo: "Visualização bloqueada",
        mensagem:
          "Permita a abertura de novas abas no navegador para visualizar o documento.",
      });
      return;
    }

    try {
      const arquivo = await obterArquivo(documento);
      const url = URL.createObjectURL(arquivo);

      novaAba.location.href = url;

      setTimeout(() => URL.revokeObjectURL(url), 60000);
    } catch (error) {
      novaAba.close();

      setFeedback({
        tipo: "erro",
        titulo: "Erro ao visualizar documento",
        mensagem:
          error.message || "Não foi possível visualizar o documento.",
      });
    }
  }

  return (
    <main className="documentos">
      {feedback && (
        <Feedback
          tipo={feedback.tipo}
          titulo={feedback.titulo}
          mensagem={feedback.mensagem}
          fechavel={true}
          className="feedback--flutuante"
          onClose={() => setFeedback(null)}
        />
      )}

      <Confirmacao
        aberto={Boolean(documentoParaExcluir)}
        titulo="Excluir documento?"
        mensagem={
          documentoParaExcluir
            ? `Documento: ${documentoParaExcluir.nome}\n\nEste documento pertence ao trabalho:\n${documentoParaExcluir.trabalho}\n\nA exclusão é definitiva e não poderá ser desfeita.`
            : ""
        }
        textoConfirmar="Excluir documento"
        textoCancelar="Cancelar"
        onConfirmar={confirmarExclusaoDocumento}
        onCancelar={() => {
          if (!excluindo) setDocumentoParaExcluir(null);
        }}
        carregando={excluindo}
      />

      <header className="documentos__cabecalho">
        <div className="documentos__introducao">
          <h1>Meus documentos</h1>
          <p>Encontre rapidamente os documentos dos seus trabalhos.</p>
        </div>

        <button
          type="button"
          className="documentos__botao-adicionar"
          onClick={() => setModalAberto(true)}
        >
          <Plus size={18} />
          Adicionar documento
        </button>
      </header>

      <section
        className="documentos__ferramentas"
        aria-label="Busca e filtros de documentos"
      >
        <div className="documentos__busca-container">
          <label
            className="documentos__busca-label"
            htmlFor="busca-doc"
          >
            Procurar documento
          </label>

          <div className="documentos__busca">
            <Search size={18} aria-hidden="true" />

            <input
              id="busca-doc"
              type="search"
              placeholder="Digite o nome do arquivo"
              aria-label="Digite o nome do arquivo"
              value={busca}
              onChange={(evento) => setBusca(evento.target.value)}
            />
          </div>
        </div>

        <div
          className="documentos__filtros"
          aria-label="Filtrar por tipo"
        >
          {["Todos", "PDF", "Imagem", "Documento"].map((tipo) => (
            <button
              key={tipo}
              type="button"
              className={`documentos__filtro ${
                filtro === tipo ? "documentos__filtro--ativo" : ""
              }`}
              aria-pressed={filtro === tipo}
              onClick={() => setFiltro(tipo)}
            >
              {tipo === "Imagem"
                ? "Imagens"
                : tipo === "Documento"
                  ? "Outros documentos"
                  : tipo}
            </button>
          ))}
        </div>
      </section>

      <section
        className="documentos__lista"
        aria-label="Meus documentos"
      >
        {carregando ? (
          <div className="documentos__vazio">
            <p>Carregando documentos...</p>
          </div>
        ) : erro ? (
          <div className="documentos__vazio">
            <h2>Não foi possível carregar os documentos</h2>
            <p>{erro}</p>

            <button type="button" onClick={carregarDocumentos}>
              Tentar novamente
            </button>
          </div>
        ) : documentosFiltrados.length > 0 ? (
          documentosFiltrados.map((documento) => (
            <article
              className="documento-card"
              key={`${documento.etapaId}-${documento.id}`}
            >
              <div className="documento-card__icone" aria-hidden="true">
                {documento.tipo === "Imagem" ? (
                  <ImageIcon size={24} />
                ) : (
                  <FileText size={24} />
                )}
              </div>

              <div className="documento-card__conteudo">
                <h2>{documento.nome}</h2>

                <p>
                  Trabalho:{" "}
                  <button
                    type="button"
                    className="documento-card__link"
                    onClick={() =>
                      navigate(`/trabalhos/${documento.trabalhoId}`)
                    }
                  >
                    {documento.trabalho}
                  </button>
                </p>

                <p>
                  Etapa: {documento.etapa}{" "}
                  <span aria-hidden="true">·</span>{" "}
                  Data: {documento.data}
                </p>
              </div>

              <div
                className="documento-card__acoes"
                aria-label={`Ações para ${documento.nome}`}
              >
                <button
                  type="button"
                  aria-label={`Visualizar ${documento.nome}`}
                  title="Visualizar"
                  onClick={() => visualizarDocumento(documento)}
                >
                  <Eye size={18} />
                  <span>Visualizar</span>
                </button>

                <button
                  type="button"
                  aria-label={`Baixar ${documento.nome}`}
                  title="Baixar"
                  onClick={() => baixarDocumento(documento)}
                >
                  <Download size={18} />
                  <span>Baixar</span>
                </button>

                <button
                  type="button"
                  className="documento-card__acao-excluir"
                  aria-label={`Excluir ${documento.nome}`}
                  title="Excluir"
                  disabled={excluindo}
                  onClick={() => setDocumentoParaExcluir(documento)}
                >
                  <Trash2 size={18} />
                  <span>Excluir</span>
                </button>
              </div>
            </article>
          ))
        ) : (
          <div className="documentos__vazio">
            <FileText size={36} aria-hidden="true" />
            <h2>Nenhum documento encontrado</h2>
            <p>
              {documentos.length === 0
                ? "Ainda não existem documentos nos seus trabalhos."
                : "Experimente alterar a busca ou selecionar outro filtro."}
            </p>
          </div>
        )}
      </section>

      <ModalUploadDocumento
        aberto={modalAberto}
        onFechar={() => setModalAberto(false)}
        onAdicionar={carregarDocumentos}
        contexto={contexto}
        empresaSelecionada={empresaSelecionada}
        minhasEtapas={minhasEtapas}
      />
    </main>
  );
}

export default Documentos;
