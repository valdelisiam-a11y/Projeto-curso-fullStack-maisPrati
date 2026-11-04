
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

import "./Documentos.css";
import ModalUploadDocumento from "../components/documentosUpload/ModalUploadDocumento.jsx";

import {
  listarTrabalhos,
  listarEtapasDoTrabalho,
} from "../services/trabalhoService";

import {
  listarDocumentos,
  baixarDocumento as baixarArquivo,
} from "../services/documentoService";

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
  const { contexto, empresaSelecionada } = useContextoTrabalho();

const carregarDocumentos = useCallback(async () => {
  try {
    setCarregando(true);
    setErro("");

    const dados = await listarTrabalhos();
    const lista = Array.isArray(dados) ? dados : dados.content ?? [];

    const trabalhos = lista.filter((trabalho) => {
      if (contexto === "pessoal") {
        return trabalho.tipo === "PESSOAL";
      }

      if (contexto === "empresa") {
        return (
          trabalho.tipo === "EMPRESA" &&
          empresaSelecionada?.id != null &&
          Number(trabalho.empresaId) === Number(empresaSelecionada.id)
        );
      }

      return false;
    });

    const documentosPorTrabalho = await Promise.all(
      trabalhos.map(async (trabalho) => {
        const etapas = await listarEtapasDoTrabalho(trabalho.id);

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
              data: formatarData(documento.dataUpload),
            }));
          })
        );

        return documentosPorEtapa.flat();
      })
    );

    setDocumentos(documentosPorTrabalho.flat());

  } catch (error) {
    setErro(error.message || "Não foi possível carregar os documentos.");
  } finally {
    setCarregando(false);
  }
}, [contexto, empresaSelecionada?.id]);

  useEffect(() => {
    carregarDocumentos();
  }, [carregarDocumentos]);

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
    <main className="documentos">
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
            <article className="documento-card" key={`${documento.etapaId}-${documento.id}`}>
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
                  disabled
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
      />
    </main>
  );
}

export default Documentos;
