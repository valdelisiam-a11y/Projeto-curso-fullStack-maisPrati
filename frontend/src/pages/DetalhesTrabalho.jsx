
import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { ArrowLeft, AlertCircle } from "lucide-react";
import ListaEtapas from "../components/etapas/ListaEtapas";
import TopoTrabalho from "../components/trabalhos/TopoTrabalho";
import PainelDetalheEtapa from "../components/trabalhos/PainelDetalheEtapa";
import HistoricoTrabalho from "../components/trabalhos/HistoricoTrabalho";
import { useAuth } from "../contexts/useAuth.js";
import {
  buscarTrabalhoPorId,
  listarEtapasDoTrabalho,
  listarHistoricoTrabalho,
  alterarStatusEtapa,
} from "../services/trabalhoService";
import "./DetalhesTrabalho.css";

function normalizarStatus(status) {
  const statusMapeados = {
    EM_ANDAMENTO: "Em andamento",
    CONCLUIDO: "Concluído",
    CONCLUIDA: "Concluído",
    PENDENTE: "Pendente",
    PRECISA_DE_ATENCAO: "Precisa de atenção",
    ATRASADO: "Atrasado",
    CANCELADO: "Cancelado",
  };

  return statusMapeados[status] ?? status ?? "Pendente";
}

function formatarData(data) {
  if (!data) return "Não informada";

  const dataFormatada = new Date(data);

  if (Number.isNaN(dataFormatada.getTime())) return "Não informada";

  return dataFormatada.toLocaleDateString("pt-BR");
}

function mapearEtapa(etapa) {
  return {
    ...etapa,
    status: normalizarStatus(etapa.status),
    responsavel: etapa.responsavelNome ?? "Não atribuído",
    data: formatarData(etapa.dataAtualizacao ?? etapa.dataCriacao),
    percentualConclusao: etapa.percentualConclusao ?? 0,
  };
}

function mapearHistorico(item) {
  const statusAnterior = normalizarStatus(item.statusAnterior);
  const statusNovo = normalizarStatus(item.statusNovo);

  return {
    id: item.id,
    tipo: statusNovo === "Concluído" ? "concluido" : "andamento",
    texto: `Status alterado de ${statusAnterior} para ${statusNovo}${
      item.usuarioNome ? ` por ${item.usuarioNome}` : ""
    }.`,
    data: formatarData(item.dataAlteracao),
  };
}

export default function DetalhesTrabalho() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [trabalho, setTrabalho] = useState(null);
  const [etapas, setEtapas] = useState([]);
  const [historico, setHistorico] = useState([]);
  const [etapaSelecionadaId, setEtapaSelecionadaId] = useState(null);
  const [observacao, setObservacao] = useState("");
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState("");
  const { usuario } = useAuth();

  useEffect(() => {
    async function carregarDetalhes() {
      setCarregando(true);
      setErro("");

      try {
        const [trabalhoApi, etapasApi, historicoApi] = await Promise.all([
          buscarTrabalhoPorId(id),
          listarEtapasDoTrabalho(id),
          listarHistoricoTrabalho(id),
        ]);

        const etapasMapeadas = etapasApi
          .map(mapearEtapa)
          .sort((a, b) => (a.ordem ?? 0) - (b.ordem ?? 0));

        setTrabalho({
          ...trabalhoApi,
          nome: trabalhoApi.titulo,
          situacao: normalizarStatus(trabalhoApi.status),
          progresso: etapasMapeadas.length
            ? Math.round(
                etapasMapeadas.reduce(
                  (total, etapa) => total + etapa.percentualConclusao,
                  0,
                ) / etapasMapeadas.length,
              )
            : 0,
          prazo: "Não informado",
        });

        setEtapas(etapasMapeadas);
        setHistorico(historicoApi.map(mapearHistorico));
        setEtapaSelecionadaId(etapasMapeadas[0]?.id ?? null);
      } catch (erroApi) {
        setErro(erroApi.message || "Não foi possível carregar o trabalho.");
      } finally {
        setCarregando(false);
      }
    }

    carregarDetalhes();
  }, [id]);

  const etapaAtualObj =
    etapas.find((etapa) => etapa.id === etapaSelecionadaId) ?? null;

  function getTopBadgeClass(situacao) {
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
  }

  async function marcarEtapaConcluida() {
      if (!etapaAtualObj) {
        alert("Selecione uma etapa para concluir.");
        return;
      }

      if (!usuario?.id) {
        alert("Não foi possível identificar o usuário autenticado.");
        return;
      }

      if (etapaAtualObj.status === "Concluído") {
        alert("Esta etapa já está concluída.");
        return;
      }

      try {
        await alterarStatusEtapa(etapaAtualObj.id, {
          status: "CONCLUIDO",
          usuarioId: usuario.id,
        });

        const [etapasApi, historicoApi, trabalhoApi] = await Promise.all([
          listarEtapasDoTrabalho(id),
          listarHistoricoTrabalho(id),
          buscarTrabalhoPorId(id),
        ]);

        const etapasMapeadas = etapasApi
          .map(mapearEtapa)
          .sort((a, b) => (a.ordem ?? 0) - (b.ordem ?? 0));

        const progresso = etapasMapeadas.length
          ? Math.round(
              etapasMapeadas.reduce(
                (total, etapa) => total + etapa.percentualConclusao,
                0,
              ) / etapasMapeadas.length,
            )
          : 0;

        setEtapas(etapasMapeadas);
        setHistorico(historicoApi.map(mapearHistorico));
        setTrabalho((anterior) => ({
          ...anterior,
          ...trabalhoApi,
          nome: trabalhoApi.titulo,
          situacao: normalizarStatus(trabalhoApi.status),
          progresso,
        }));
      } catch (erroApi) {
        alert(erroApi.message || "Não foi possível concluir a etapa.");
      }
  }

  if (carregando) {
    return (
      <div className="detalhes-container">
        <p>Carregando detalhes do trabalho...</p>
      </div>
    );
  }

  if (erro || !trabalho) {
    return (
      <div className="detalhes-container">
        <AlertCircle className="w-5 h-5" />
        <p>{erro || "Trabalho não encontrado."}</p>
        <button
          type="button"
          className="btn-voltar"
          onClick={() => navigate("/trabalhos")}
        >
          <ArrowLeft className="w-4 h-4" />
          Voltar para trabalhos
        </button>
      </div>
    );
  }

  return (
    <div className="detalhes-container">
      <button
        type="button"
        className="btn-voltar"
        onClick={() => navigate("/trabalhos")}
      >
        <ArrowLeft className="w-4 h-4" />
        Voltar
      </button>

      <TopoTrabalho
        trabalho={trabalho}
        getTopBadgeClass={getTopBadgeClass}
      />

      <ListaEtapas
        etapas={etapas}
        etapaSelecionadaId={etapaSelecionadaId}
        onSelecionarEtapa={setEtapaSelecionadaId}
      />

      <PainelDetalheEtapa
        etapaAtualObj={etapaAtualObj}
        getTopBadgeClass={getTopBadgeClass}
        observacao={observacao}
        setObservacao={setObservacao}
        onSalvarObservacao={() =>
          alert("As observações ainda não estão integradas à API.")
        }
        onMarcarConcluida={marcarEtapaConcluida}
      />

      <HistoricoTrabalho historico={historico} />
    </div>
  );
}

