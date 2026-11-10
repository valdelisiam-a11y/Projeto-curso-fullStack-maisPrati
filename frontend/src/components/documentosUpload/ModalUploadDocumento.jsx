
import { useEffect, useState } from "react";
import {
  X,
  Upload,
  FileText,
  Image as ImageIcon,
  File,
  Trash2,
} from "lucide-react";
import "./ModalUploadDocumento.css";

import {
  listarTrabalhos,
  listarEtapasDoTrabalho,
} from "../../services/trabalhoService";

import { enviarDocumento } from "../../services/documentoService";
import { useAuth } from "../../contexts/useAuth";

const TAMANHO_MAXIMO = 10 * 1024 * 1024;

const FORMATOS_PERMITIDOS = [
  ".pdf",
  ".doc",
  ".docx",
  ".xls",
  ".xlsx",
  ".jpg",
  ".jpeg",
  ".png",
];

function ModalUploadDocumento({
  aberto,
  onFechar,
  onAdicionar,
  trabalhoIdInicial = "",
  etapaIdInicial = "",
  contexto,
  empresaSelecionada,
  minhasEtapas = [],
}) {
  const [trabalhos, setTrabalhos] = useState([]);
  const [etapas, setEtapas] = useState([]);

  const { usuario } = useAuth();

  const [trabalhoId, setTrabalhoId] = useState(
    trabalhoIdInicial ? String(trabalhoIdInicial) : ""
  );

  const [etapaId, setEtapaId] = useState(
    etapaIdInicial ? String(etapaIdInicial) : ""
  );

  const [arquivo, setArquivo] = useState(null);

  const [carregandoTrabalhos, setCarregandoTrabalhos] = useState(false);
  const [carregandoEtapas, setCarregandoEtapas] = useState(false);
  const [enviando, setEnviando] = useState(false);

  const [erro, setErro] = useState("");

  // Carrega somente os trabalhos e as etapas permitidos ao usuário.
  useEffect(() => {
    if (!aberto) return;

    async function carregarTrabalhos() {
      try {
        setCarregandoTrabalhos(true);
        setErro("");

        // Limpa opções anteriores para evitar exibir dados
        // da empresa ou do contexto selecionado anteriormente.
        setTrabalhos([]);
        setEtapas([]);
        setTrabalhoId("");
        setEtapaId("");

        const dados = await listarTrabalhos();

        const lista = Array.isArray(dados)
          ? dados
          : dados?.content ?? [];

        let trabalhosDisponiveis = [];

        // CONTEXTO PESSOAL
        if (contexto === "pessoal") {
          const trabalhosPessoais = lista.filter(
            (trabalho) =>
              trabalho.tipo === "PESSOAL" &&
              Number(trabalho.criadoPorId) === Number(usuario?.id)
          );

          trabalhosDisponiveis = await Promise.all(
            trabalhosPessoais.map(async (trabalho) => {
              const etapasTrabalho = await listarEtapasDoTrabalho(
                trabalho.id
              );

              return {
                ...trabalho,
                ehCriador: true,
                etapasDisponiveis: etapasTrabalho,
              };
            })
          );
        }

        // CONTEXTO EMPRESA
        if (contexto === "empresa" && empresaSelecionada?.id) {
          // Trabalhos criados pelo usuário na empresa selecionada.
          const trabalhosCriados = lista.filter(
            (trabalho) =>
              trabalho.tipo === "EMPRESA" &&
              Number(trabalho.empresaId) ===
                Number(empresaSelecionada.id) &&
              Number(trabalho.criadoPorId) === Number(usuario?.id)
          );

          const trabalhosDoCriador = await Promise.all(
            trabalhosCriados.map(async (trabalho) => {
              const etapasTrabalho = await listarEtapasDoTrabalho(
                trabalho.id
              );

              return {
                ...trabalho,
                ehCriador: true,
                etapasDisponiveis: etapasTrabalho,
              };
            })
          );

          // Evita duplicar trabalhos que também apareçam
          // em minhasEtapas.
          const idsCriados = new Set(
            trabalhosDoCriador.map((trabalho) =>
              Number(trabalho.id)
            )
          );

          // Agrupa as etapas atribuídas ao colaborador por trabalho.
          const grupos = {};

          minhasEtapas.forEach((etapa) => {
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
                ehCriador: false,
                etapasDisponiveis: [],
              };
            }

            grupos[etapa.trabalhoId].etapasDisponiveis.push({
              id: etapa.etapaId,
              titulo: etapa.etapaTitulo,
            });
          });

          trabalhosDisponiveis = [
            ...trabalhosDoCriador,
            ...Object.values(grupos),
          ];
        }

        setTrabalhos(trabalhosDisponiveis);

        // Restaura o trabalho inicial somente se ele
        // estiver entre as opções permitidas.
        if (
          trabalhoIdInicial &&
          trabalhosDisponiveis.some(
            (trabalho) =>
              String(trabalho.id) === String(trabalhoIdInicial)
          )
        ) {
          setTrabalhoId(String(trabalhoIdInicial));
        }
      } catch (error) {
        console.error("Erro ao carregar trabalhos:", error);

        setErro(
          error.message || "Não foi possível carregar os trabalhos."
        );
      } finally {
        setCarregandoTrabalhos(false);
      }
    }

    carregarTrabalhos();
  }, [
    aberto,
    contexto,
    empresaSelecionada?.id,
    minhasEtapas,
    usuario?.id,
    trabalhoIdInicial,
  ]);

  // Carrega as etapas que já foram filtradas para cada trabalho.
  // Não realiza novas consultas ao backend para trabalhos de colaboradores.
  useEffect(() => {
    if (!aberto || !trabalhoId) {
      setEtapas([]);
      return;
    }

    const trabalhoSelecionado = trabalhos.find(
      (trabalho) =>
        String(trabalho.id) === String(trabalhoId)
    );

    if (!trabalhoSelecionado) {
      setEtapas([]);
      setEtapaId("");
      return;
    }

    setCarregandoEtapas(true);

    const etapasDisponiveis =
      trabalhoSelecionado.etapasDisponiveis ?? [];

    setEtapas(etapasDisponiveis);

    // Restaura a etapa inicial somente se ela estiver autorizada
    // entre as etapas disponíveis para o usuário.
    if (
      etapaIdInicial &&
      etapasDisponiveis.some(
        (etapa) => String(etapa.id) === String(etapaIdInicial)
      )
    ) {
      setEtapaId(String(etapaIdInicial));
    } else {
      setEtapaId("");
    }

    setCarregandoEtapas(false);
  }, [
    aberto,
    trabalhoId,
    trabalhos,
    etapaIdInicial,
  ]);

  if (!aberto) return null;

  const trabalhoSelecionado = trabalhos.find(
    (trabalho) => String(trabalho.id) === trabalhoId
  );

  function selecionarArquivo(evento) {
    const arquivoSelecionado = evento.target.files?.[0];

    if (!arquivoSelecionado) return;

    setErro("");

    if (arquivoSelecionado.size > TAMANHO_MAXIMO) {
      setErro("O arquivo não pode ultrapassar 10 MB.");
      evento.target.value = "";
      return;
    }

    const extensao = arquivoSelecionado.name
      .substring(arquivoSelecionado.name.lastIndexOf("."))
      .toLowerCase();

    if (!FORMATOS_PERMITIDOS.includes(extensao)) {
      setErro(
        "Formato não permitido. Selecione PDF, DOC, DOCX, XLS, XLSX, JPG, JPEG ou PNG."
      );
      evento.target.value = "";
      return;
    }

    setArquivo(arquivoSelecionado);
  }

  function limparFormulario() {
    setArquivo(null);
    setErro("");

    setTrabalhoId(
      trabalhoIdInicial ? String(trabalhoIdInicial) : ""
    );

    setEtapaId(
      etapaIdInicial ? String(etapaIdInicial) : ""
    );

    setEtapas([]);
  }

  function fecharModal() {
    if (enviando) return;

    limparFormulario();
    onFechar();
  }

  async function enviarDocumentoSelecionado(evento) {
    evento.preventDefault();

    if (!trabalhoId || !etapaId || !arquivo) {
      setErro("Selecione o trabalho, a etapa e o arquivo.");
      return;
    }

    try {
      setEnviando(true);
      setErro("");

      const documentoCriado = await enviarDocumento(
        etapaId,
        arquivo
      );

      const etapaSelecionada = etapas.find(
        (etapa) => String(etapa.id) === etapaId
      );

      onAdicionar?.({
        ...documentoCriado,
        nome: documentoCriado.nomeOriginal,
        trabalhoId: Number(trabalhoId),
        trabalho: trabalhoSelecionado?.titulo,
        etapa: etapaSelecionada?.titulo,
        tipo: arquivo.type.startsWith("image/")
          ? "Imagem"
          : "PDF",
        data: new Date().toLocaleDateString("pt-BR"),
      });

      limparFormulario();
      onFechar();
    } catch (error) {
      setErro(
        error.message || "Não foi possível enviar o documento."
      );
    } finally {
      setEnviando(false);
    }
  }

  function formatarTamanho(tamanho) {
    return tamanho < 1024 * 1024
      ? `${(tamanho / 1024).toFixed(1)} KB`
      : `${(tamanho / (1024 * 1024)).toFixed(2)} MB`;
  }

  function obterIconeArquivo() {
    if (arquivo?.type === "application/pdf") {
      return <FileText size={24} />;
    }

    if (arquivo?.type.startsWith("image/")) {
      return <ImageIcon size={24} />;
    }

    return <File size={24} />;
  }

  return (
    <div
      className="modal-upload__fundo"
      onMouseDown={(evento) => {
        if (evento.target === evento.currentTarget) fecharModal();
      }}
    >
      <section
        className="modal-upload"
        role="dialog"
        aria-modal="true"
        aria-labelledby="modal-upload-titulo"
      >
        <header className="modal-upload__cabecalho">
          <div>
            <h2 id="modal-upload-titulo">Adicionar documento</h2>
            <p>Guarde um arquivo dentro de um trabalho.</p>
          </div>

          <button
            type="button"
            className="modal-upload__fechar"
            onClick={fecharModal}
            aria-label="Fechar modal"
            disabled={enviando}
          >
            <X size={20} />
          </button>
        </header>

        <form
          className="modal-upload__formulario"
          onSubmit={enviarDocumentoSelecionado}
        >
          <div className="modal-upload__campo">
            <label htmlFor="trabalho-documento">Trabalho</label>

            <select
              id="trabalho-documento"
              value={trabalhoId}
              onChange={(evento) => {
                setTrabalhoId(evento.target.value);
                setEtapaId("");
                setArquivo(null);
                setErro("");
              }}
              disabled={
                carregandoTrabalhos ||
                enviando ||
                !!trabalhoIdInicial
              }
              required
            >
              <option value="">
                {carregandoTrabalhos
                  ? "Carregando trabalhos..."
                  : "Selecione um trabalho"}
              </option>

              {trabalhos.map((trabalho) => (
                <option key={trabalho.id} value={trabalho.id}>
                  {trabalho.titulo}
                </option>
              ))}
            </select>
          </div>

          <div className="modal-upload__campo">
            <label htmlFor="etapa-documento">Etapa</label>

            <select
              id="etapa-documento"
              value={etapaId}
              onChange={(evento) => setEtapaId(evento.target.value)}
              disabled={
                !trabalhoSelecionado ||
                carregandoEtapas ||
                enviando ||
                !!etapaIdInicial
              }
              required
            >
              <option value="">
                {carregandoEtapas
                  ? "Carregando etapas..."
                  : "Selecione uma etapa"}
              </option>

              {etapas.map((etapa) => (
                <option key={etapa.id} value={etapa.id}>
                  {etapa.titulo}
                </option>
              ))}
            </select>

            {!carregandoEtapas &&
              trabalhoId &&
              etapas.length === 0 && (
                <small>
                  Este trabalho ainda não possui etapas disponíveis.
                </small>
              )}
          </div>

          <div className="modal-upload__campo">
            <label>Arquivo</label>

            <label className="modal-upload__seletor-arquivo">
              <input
                type="file"
                accept=".pdf,.doc,.docx,.xls,.xlsx,.jpg,.jpeg,.png"
                onChange={selecionarArquivo}
                disabled={enviando}
              />

              <Upload size={22} />
              <span>Selecionar arquivo</span>
              <small>
                PDF, DOC, DOCX, XLS, XLSX, JPG ou PNG — máximo 10 MB
              </small>
            </label>
          </div>

          {arquivo && (
            <div className="modal-upload__arquivo">
              <div className="modal-upload__arquivo-icone">
                {obterIconeArquivo()}
              </div>

              <div className="modal-upload__arquivo-info">
                <strong>{arquivo.name}</strong>
                <span>{formatarTamanho(arquivo.size)}</span>
              </div>

              <button
                type="button"
                className="modal-upload__remover-arquivo"
                aria-label="Remover arquivo selecionado"
                onClick={() => setArquivo(null)}
                disabled={enviando}
              >
                <Trash2 size={18} />
              </button>
            </div>
          )}

          {erro && (
            <p className="modal-upload__erro" role="alert">
              {erro}
            </p>
          )}

          <footer className="modal-upload__acoes">
            <button
              type="button"
              className="modal-upload__botao-cancelar"
              onClick={fecharModal}
              disabled={enviando}
            >
              Cancelar
            </button>

            <button
              type="submit"
              className="modal-upload__botao-enviar"
              disabled={
                enviando ||
                carregandoTrabalhos ||
                carregandoEtapas
              }
            >
              {enviando ? "Enviando..." : "Enviar documento"}
            </button>
          </footer>
        </form>
      </section>
    </div>
  );
}

export default ModalUploadDocumento;
