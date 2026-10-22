import React from "react";

export default function HistoricoTrabalho({ historico }) {
  return (
    <div className="historico-card">
      <h3>O que aconteceu?</h3>
      <div className="timeline-lista">
        {historico.map((hist) => (
          <div key={hist.id} className="timeline-item">
            <span className={`timeline-ponto ${hist.tipo}`}></span>
            <div className="timeline-conteudo">
              <p className="timeline-texto">{hist.texto}</p>
              <span className="timeline-data">{hist.data}</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
