import React, { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { ArrowLeft, AlertCircle } from "lucide-react";

// Importação dos componentes modulares (corrigido com ../)
import ListaEtapas from "../components/etapas/ListaEtapas";
import TopoTrabalho from "../components/trabalhos/TopoTrabalho";
import PainelDetalheEtapa from "../components/trabalhos/PainelDetalheEtapa";
import HistoricoTrabalho from "../components/trabalhos/HistoricoTrabalho";

import "./DetalhesTrabalho.css";

// Importação do mock (corrigido com ../)
import { trabalhosData } from "../services/trabalhosMock";

export default function DetalhesTrabalho() {
  const { id } = useParams();
  const navigate = useNavigate();

  const trabalho = trabalhosData[id];

  const [etapaSelecionadaId, setEtapaSelecionadaId] = useState(
    trabalho?.etapas[2]?.id ?? trabalho?.etapas[0]?.id ?? null,
  );
  const [observacao, setObservacao] = useState(
    "Cliente pediu para revisar o valor do plano anual.",
  );

  if (!trabalho) {
    return (
      <main
        className="detalhes-container"
        aria-labelledby="titulo-trabalho-nao-encontrado"
      >
        <section className="trabalho-nao-encontrado" role="alert">
          <AlertCircle aria-hidden="true" />
          <h1 id="titulo-trabalho-nao-encontrado">Trabalho não encontrado</h1>
          <p>Não foi possível localizar o trabalho solicitado.</p>
          <button
            type="button"
            className="btn-voltar"
            onClick={() => navigate("/trabalhos")}
          >
            <ArrowLeft aria-hidden="true" />
            <span>Voltar para trabalhos</span>
          </button>
        </section>
      </main>
    );
  }

  const etapaAtualObj =
    trabalho.etapas.find((e) => e.id === etapaSelecionadaId) ?? null;

  const getTopBadgeClass = (situacao) => {
    switch (situacao) {
      case "Concluído":
        return "badge-concluido";
      case "Precisa de atenção":
        return "badge-atencao";
      case "Atrasado":
        return "badge-atrasado";
      default:
        return "badge-andamento";
    }
  };

  return (
    <div className="detalhes-container">
      <button className="btn-voltar" onClick={() => navigate("/trabalhos")}>
        <ArrowLeft className="w-4 h-4" />
        <span>Voltar</span>
      </button>

      <TopoTrabalho trabalho={trabalho} getTopBadgeClass={getTopBadgeClass} />

      <ListaEtapas
        etapas={trabalho.etapas}
        etapaSelecionadaId={etapaSelecionadaId}
        onSelecionarEtapa={setEtapaSelecionadaId}
      />

      <PainelDetalheEtapa
        etapaAtualObj={etapaAtualObj}
        getTopBadgeClass={getTopBadgeClass}
        observacao={observacao}
        setObservacao={setObservacao}
        onSalvarObservacao={() =>
          alert("Simulação: Observação salva localmente.")
        }
        onMarcarConcluida={() =>
          alert(`Simulação: Etapa "${etapaAtualObj?.titulo}" concluída.`)
        }
      />

      <HistoricoTrabalho historico={trabalho.historico} />
    </div>
  );
}
