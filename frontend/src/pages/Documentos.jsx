import { useState } from "react";
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
import "./Documentos.css";
import ModalUploadDocumento from "../components/documentosUpload/ModalUploadDocumento.jsx";

const documentosIniciais = [
  {
    id: 1,
    nome: "Contrato_cliente.pdf",
    tipo: "PDF",
    trabalho: "Cadastro de novo cliente",
    trabalhoId: 1,
    etapa: "Assinatura do contrato",
    data: "10/10/2026",
  },
  {
    id: 2,
    nome: "Documento_identidade.png",
    tipo: "Imagem",
    trabalho: "Cadastro de novo cliente",
    trabalhoId: 1,
    etapa: "Conferência de documentos",
    data: "09/10/2026",
  },
  {
    id: 3,
    nome: "Orcamento_materiais.pdf",
    tipo: "PDF",
    trabalho: "Compra de materiais",
    trabalhoId: 2,
    etapa: "Análise de orçamento",
    data: "08/10/2026",
  },
];

function Documentos() {
  const [documentos, setDocumentos] = useState(documentosIniciais);
  const [busca, setBusca] = useState("");
  const [filtro, setFiltro] = useState("Todos");
  const [modalAberto, setModalAberto] = useState(false);

  const navigate = useNavigate();

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

  function excluirDocumento(id) {
    const confirmar = window.confirm(
      "Deseja realmente excluir este documento?"
    );

    if (confirmar) {
      setDocumentos((lista) =>
        lista.filter((documento) => documento.id !== id)
      );
    }
  }

  function visualizarDocumento(nome) {
    window.alert(`A visualização de "${nome}" será integrada posteriormente.`);
  }

  function baixarDocumento(nome) {
    window.alert(`O download de "${nome}" será integrado posteriormente.`);
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
          {["Todos", "PDF", "Imagem"].map((tipo) => (
            <button
              key={tipo}
              type="button"
              className={`documentos__filtro ${
                filtro === tipo ? "documentos__filtro--ativo" : ""
              }`}
              aria-pressed={filtro === tipo}
              onClick={() => setFiltro(tipo)}
            >
              {tipo === "Imagem" ? "Imagens" : tipo}
            </button>
          ))}
        </div>
      </section>

      <section
        className="documentos__lista"
        aria-label="Meus documentos"
      >
        {documentosFiltrados.length > 0 ? (
          documentosFiltrados.map((documento) => (
            <article className="documento-card" key={documento.id}>
              <div className="documento-card__icone" aria-hidden="true">
                {documento.tipo === "PDF" ? (
                  <FileText size={24} />
                ) : (
                  <ImageIcon size={24} />
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
                  Etapa: {documento.etapa} <span aria-hidden="true">·</span>{" "}
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
                  onClick={() => visualizarDocumento(documento.nome)}
                >
                  <Eye size={18} />
                  <span>Visualizar</span>
                </button>

                <button
                  type="button"
                  aria-label={`Baixar ${documento.nome}`}
                  title="Baixar"
                  onClick={() => baixarDocumento(documento.nome)}
                >
                  <Download size={18} />
                  <span>Baixar</span>
                </button>

                <button
                  type="button"
                  className="documento-card__acao-excluir"
                  aria-label={`Excluir ${documento.nome}`}
                  title="Excluir"
                  onClick={() => excluirDocumento(documento.id)}
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
              Experimente alterar a busca ou selecionar outro filtro.
            </p>
          </div>
        )}
      </section>

      <ModalUploadDocumento
        aberto={modalAberto}
        onFechar={() => setModalAberto(false)}
        onAdicionar={(novoDocumento) => {
          setDocumentos((lista) => [
            {
              ...novoDocumento,
              id: Date.now(),
            },
            ...lista,
          ]);
        }}
      />
    </main>
  );
}

export default Documentos;