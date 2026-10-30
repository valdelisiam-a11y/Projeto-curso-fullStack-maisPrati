import {
  CheckCircle2,
  CircleAlert,
  AlertTriangle,
  LoaderCircle,
  X
} from "lucide-react";

import "./feedback.css";

const configuracoes = {
  sucesso: {
    titulo: "Sucesso",
    icone: CheckCircle2,
    classe: "feedback--sucesso"
  },
  erro: {
    titulo: "Erro",
    icone: CircleAlert,
    classe: "feedback--erro"
  },
  aviso: {
    titulo: "Atenção",
    icone: AlertTriangle,
    classe: "feedback--aviso"
  },
  carregamento: {
    titulo: "Aguarde",
    icone: LoaderCircle,
    classe: "feedback--carregamento"
  }
};

function Feedback({
  tipo = "sucesso",
  mensagem,
  titulo,
  fechavel = true,
  onClose,
  className = ""
}) {
  const configuracao = configuracoes[tipo];

  if (!configuracao || !mensagem) {
    return null;
  }

  const Icone = configuracao.icone;
  const estaCarregando = tipo === "carregamento";

  const classeFinal = [
    "feedback",
    configuracao.classe,
    className
  ].filter(Boolean).join(" ");

  return (
    <div
      className={classeFinal}
      role={tipo === "erro" ? "alert" : "status"}
      aria-live={tipo === "erro" ? "assertive" : "polite"}
      aria-atomic="true"
    >
      <Icone
        className={
          estaCarregando
            ? "feedback_icone feedback_icone--animada"
            : "feedback__icone"
        }
        size={22}
        aria-hidden="true"
      />

      <div className="feedback__conteudo">
        <strong className="feedback__titulo">
          {titulo || configuracao.titulo}
        </strong>

        <p className="feedback__mensagem">
          {mensagem}
        </p>
      </div>

      {fechavel && onClose && !estaCarregando && (
        <button
          type="button"
          className="feedback__fechar"
          onClick={onClose}
          aria-label="Fechar mensagem"
        >
          <X size={18} aria-hidden="true" />
        </button>
      )}
    </div>
  );
}

export default Feedback;