
import "./confirmacao.css";

function Confirmacao({
  aberto = false,
  titulo = "Confirmar ação",
  mensagem = "Tem certeza de que deseja continuar?",
  textoConfirmar = "Confirmar",
  textoCancelar = "Cancelar",
  onConfirmar,
  onCancelar,
  carregando = false,
}) {
  if (!aberto) {
    return null;
  }

  function handleConfirmar() {
    if (carregando) return;

    onConfirmar?.();
  }

  function handleCancelar() {
    if (carregando) return;

    onCancelar?.();
  }

  return (
    <div className="confirmacao-overlay">
      <section
        className="confirmacao-modal"
        role="alertdialog"
        aria-modal="true"
        aria-labelledby="confirmacao-titulo"
        aria-describedby="confirmacao-mensagem"
      >
        <div className="confirmacao-conteudo">
          <h2
            id="confirmacao-titulo"
            className="confirmacao-titulo"
          >
            {titulo}
          </h2>

          <p
            id="confirmacao-mensagem"
            className="confirmacao-mensagem"
          >
            {mensagem}
          </p>
        </div>

        <div className="confirmacao-acoes">
          <button
            type="button"
            className="confirmacao-botao confirmacao-botao-cancelar"
            onClick={handleCancelar}
            disabled={carregando}
          >
            {textoCancelar}
          </button>

          <button
            type="button"
            className="confirmacao-botao confirmacao-botao-confirmar"
            onClick={handleConfirmar}
            disabled={carregando}
          >
            {carregando ? "Aguarde..." : textoConfirmar}
          </button>
        </div>
      </section>
    </div>
  );
}

export default Confirmacao;

