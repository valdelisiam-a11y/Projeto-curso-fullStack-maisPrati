import React from "react";
import { CheckCircle2 } from "lucide-react";

export default function ListaEtapas({
  etapas,
  etapaSelecionadaId,
  onSelecionarEtapa,
}) {
  return (
    <div className="etapas-secao">
      <h3 className="secao-titulo">Etapas</h3>

      <p className="secao-subtitulo">
        Toque em uma etapa para ver os detalhes.
      </p>

      <div
        className="etapas-horizontal-grid"
        role="tablist"
        aria-label="Etapas do trabalho"
      >
        {etapas.map((etapa) => {
          const isSelecionada = etapa.id === etapaSelecionadaId;
          const isConcluida = etapa.status === "Concluído";
          const isEmAndamento = etapa.status === "Em andamento";
          const isAtencao = etapa.status === "Precisa de atenção";
          const isAtrasado = etapa.status === "Atrasado";

          return (
            <button
              key={etapa.id}
              type="button"
              role="tab"
              aria-selected={isSelecionada}
              className={`etapa-card-item ${
                isSelecionada ? "selecionada" : ""
              }`}
              onClick={() => onSelecionarEtapa(etapa.id)}
            >
              <div className="etapa-card-top">
                {isConcluida ? (
                  <span className="icone-etapa-status concluido">
                    <CheckCircle2 className="w-3.5 h-3.5" />
                  </span>
                ) : (
                  <span
                    className={`icone-etapa-status ${
                      isEmAndamento
                        ? "andamento"
                        : isAtencao
                          ? "atencao"
                          : isAtrasado
                            ? "atrasado"
                            : "inativo"
                    }`}
                  >
                    {etapa.id}
                  </span>
                )}

                <span className="etapa-card-conteudo">
                  <span className="etapa-card-titulo">{etapa.titulo}</span>

                  <span
                    className={`etapa-card-substatus ${etapa.status
                      .toLowerCase()
                      .replace(/\s+/g, "-")}`}
                  >
                    {etapa.status}
                  </span>
                </span>
              </div>
            </button>
          );
        })}
      </div>
    </div>
  );
}
