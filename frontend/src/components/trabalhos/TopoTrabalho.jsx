import React from "react";
import { CheckCircle2, Clock3 } from "lucide-react";

export default function TopoTrabalho({ trabalho, getTopBadgeClass }) {
  return (
    <div className="trabalho-topo-card">
      <div className="topo-header-flex">
        <div>
          <h2>{trabalho.nome}</h2>
          <p>{trabalho.descricao}</p>
        </div>
        <span
          className={`badge-status-geral ${getTopBadgeClass(trabalho.situacao)}`}
        >
          {trabalho.situacao === "Concluído" ? (
            <CheckCircle2 className="w-3.5 h-3.5" />
          ) : (
            <Clock3 className="w-3.5 h-3.5" />
          )}
          <span>{trabalho.situacao}</span>
        </span>
      </div>

      <div className="progresso-wrapper">
        <div className="barra-progresso-fundo">
          <div
            className="barra-progresso-preenchimento"
            style={{ width: `${trabalho.progresso}%` }}
          ></div>
        </div>
        <span className="progresso-porcentagem">
          {trabalho.progresso}% concluído
        </span>
      </div>

      <div className="prazo-info">
        <Clock3 className="w-4 h-4 text-gray-500" />
        <span>Prazo: {trabalho.prazo}</span>
      </div>
    </div>
  );
}
