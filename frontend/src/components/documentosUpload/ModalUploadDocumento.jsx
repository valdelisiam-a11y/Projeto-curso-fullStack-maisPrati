import { useState } from "react";
import { X, Upload, FileText, Image as ImageIcon, File, Trash2 } from "lucide-react";
import "./ModalUploadDocumento.css";

const trabalhosIniciais = [
  {
    id: 1,
    nome: "Cadastro de novo cliente",
    etapas: ["Conferência de documentos", "Assinatura do contrato"],
  },
  {
    id: 2,
    nome: "Compra de materiais",
    etapas: ["Pesquisa de fornecedores", "Análise de orçamento"],
  },
  {
    id: 3,
    nome: "Prestação de serviço — Manutenção elétrica",
    etapas: ["Avaliação técnica", "Execução do serviço"],
  },
  {
    id: 4,
    nome: "Pagamento de contas do mês",
    etapas: ["Conferência das contas", "Realização dos pagamentos"],
  },
];

function ModalUploadDocumento({ aberto, onFechar, onAdicionar }) {
  const [nome, setNome] = useState("");
  const [trabalhoId, setTrabalhoId] = useState("");
  const [etapa, setEtapa] = useState("");
  const [arquivo, setArquivo] = useState(null);
  const [erro, setErro] = useState("");

  if (!aberto) return null;

  const trabalhoSelecionado = trabalhosIniciais.find(
    (trabalho) => String(trabalho.id) === trabalhoId
  );

  function selecionarArquivo(evento) {
    const arquivoSelecionado = evento.target.files?.[0];

    if (!arquivoSelecionado) return;

    setErro("");
    setArquivo(arquivoSelecionado);

    if (!nome.trim()) {
      setNome(arquivoSelecionado.name);
    }
  }

  function enviarDocumento(evento) {
    evento.preventDefault();

    if (!nome.trim() || !trabalhoId || !etapa || !arquivo) {
      setErro("Preencha os campos e selecione um arquivo.");
      return;
    }

    onAdicionar?.({
      nome: nome.trim(),
      trabalhoId: Number(trabalhoId),
      trabalho: trabalhoSelecionado.nome,
      etapa,
      arquivo,
      tipo: arquivo.type.startsWith("image/") ? "Imagem" : "PDF",
      data: new Date().toLocaleDateString("pt-BR"),
    });

    setNome("");
    setTrabalhoId("");
    setEtapa("");
    setArquivo(null);
    setErro("");
    onFechar();
  }

  function fecharModal() {
    setErro("");
    onFechar();
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
          >
            <X size={20} />
          </button>
        </header>

        <form className="modal-upload__formulario" onSubmit={enviarDocumento}>
          <div className="modal-upload__campo">
            <label htmlFor="nome-documento">Nome do documento</label>
            <input
              id="nome-documento"
              type="text"
              placeholder="Ex.: Contrato assinado"
              value={nome}
              onChange={(evento) => setNome(evento.target.value)}
              maxLength={120}
              required
            />
          </div>

          <div className="modal-upload__campo">
            <label htmlFor="trabalho-documento">Trabalho</label>
            <select
              id="trabalho-documento"
              value={trabalhoId}
              onChange={(evento) => {
                setTrabalhoId(evento.target.value);
                setEtapa("");
              }}
              required
            >
              <option value="">Selecione um trabalho</option>
              {trabalhosIniciais.map((trabalho) => (
                <option key={trabalho.id} value={trabalho.id}>
                  {trabalho.nome}
                </option>
              ))}
            </select>
          </div>

          <div className="modal-upload__campo">
            <label htmlFor="etapa-documento">Etapa</label>
            <select
              id="etapa-documento"
              value={etapa}
              onChange={(evento) => setEtapa(evento.target.value)}
              disabled={!trabalhoSelecionado}
              required
            >
              <option value="">Selecione uma etapa</option>
              {trabalhoSelecionado?.etapas.map((nomeEtapa) => (
                <option key={nomeEtapa} value={nomeEtapa}>
                  {nomeEtapa}
                </option>
              ))}
            </select>
          </div>

          <div className="modal-upload__campo">
            <label>Arquivo</label>

            <label className="modal-upload__seletor-arquivo">
              <input
                type="file"
                accept=".pdf,.png,.jpg,.jpeg,.webp"
                onChange={selecionarArquivo}
              />
              <Upload size={22} />
              <span>Selecionar arquivo</span>
              <small>PDF ou imagem (PNG, JPG, WEBP)</small>
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
            >
              Cancelar
            </button>

            <button type="submit" className="modal-upload__botao-enviar">
              Enviar documento
            </button>
          </footer>
        </form>
      </section>
    </div>
  );
}

export default ModalUploadDocumento;