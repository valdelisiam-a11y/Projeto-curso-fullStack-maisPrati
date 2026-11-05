
import React, { useEffect, useRef, useState } from "react";
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
  registrarObservacaoEtapa,
  listarHistoricoTrabalho,
  alterarStatusEtapa,
  atualizarEtapa,
  definirResponsavelEtapa,
  cadastrarEtapa,
} from "../services/trabalhoService";
import { listarMembrosEmpresa } from "../services/empresaService";
import { useContextoTrabalho } from "../contexts/ContextoTrabalho";
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
  const tipos = {
    OBSERVACAO_REGISTRADA: "observacao",
    STATUS: "andamento",
    ETAPA_CRIADA: "andamento",
    PERCENTUAL_ALTERADO: "andamento",
    RESPONSAVEL_ALTERADO: "andamento",
    TRABALHO_CRIADO: "andamento",
  };

  const tipo = tipos[item.tipo] ?? "andamento";

  let texto = item.texto;

  if (item.tipo === "STATUS") {
    const statusAnterior = normalizarStatus(item.statusAnterior);
    const statusNovo = normalizarStatus(item.statusNovo);

    texto = `Status alterado de ${statusAnterior} para ${statusNovo}.`;
  }

  return {
    id: `${tipo}-${item.id}`,
    tipo,
    texto: `${texto ?? "Evento registrado"}${
      item.etapaTitulo ? ` (Etapa: ${item.etapaTitulo})` : ""
    }${item.usuarioNome ? ` — ${item.usuarioNome}` : ""}`,
    data: formatarData(item.dataAlteracao),
  };
}

export default function DetalhesTrabalho() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { contexto, empresaSelecionada } = useContextoTrabalho();
  const contextoAtual = contexto === "empresa"
    ? `empresa-${empresaSelecionada?.id ?? ""}`
    : "pessoal";

  const contextoAnteriorRef = useRef(contextoAtual);

  const [trabalho, setTrabalho] = useState(null);
  const [etapas, setEtapas] = useState([]);
  const [historico, setHistorico] = useState([]);
  const [etapaSelecionadaId, setEtapaSelecionadaId] = useState(null);
  const [observacao, setObservacao] = useState("");
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState("");
  const { usuario } = useAuth();
  const [membros, setMembros] = useState([]);
  const [carregandoMembros, setCarregandoMembros] = useState(false);
  const [erroMembros, setErroMembros] = useState("");

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

  useEffect(() => {
  if (!trabalho) return;

  if (contextoAnteriorRef.current === contextoAtual) return;

  contextoAnteriorRef.current = contextoAtual;

  const trabalhoPertenceAoContexto =
    contexto === "empresa"
      ? Number(trabalho.empresaId) === Number(empresaSelecionada?.id)
      : trabalho.empresaId == null;

  if (!trabalhoPertenceAoContexto) {
    navigate("/trabalhos", { replace: true });
  }
}, [
  contextoAtual,
  contexto,
  empresaSelecionada?.id,
  trabalho,
  navigate,
]);

  useEffect(() => {
  const empresaId = trabalho?.empresaId;

  if (!empresaId) {
    setMembros([]);
    setErroMembros("");
    return;
  }

  async function carregarMembros() {
    setCarregandoMembros(true);
    setErroMembros("");

    try {
      const lista = await listarMembrosEmpresa(empresaId);

      const membrosNormalizados = lista
        .map((membro) => ({
          id: membro.usuarioId ?? membro.id,
          nome:
            membro.usuarioNome ??
            membro.nome ??
            membro.nomeUsuario ??
            membro.email,
        }))
        .filter((membro) => membro.id != null && membro.nome);

      setMembros(membrosNormalizados);
    } catch (erroApi) {
      setErroMembros(
        erroApi.message || "Não foi possível carregar os colaboradores."
      );
    } finally {
      setCarregandoMembros(false);
    }
  }

  carregarMembros();
}, [trabalho?.empresaId]);

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

  async function adicionarEtapa(titulo) {
    const tituloFormatado = titulo.trim();

    if (!tituloFormatado) {
      throw new Error("Informe o título da etapa.");
    }

    const novaEtapa = await cadastrarEtapa(trabalho.id, {
      titulo: tituloFormatado,
      ordem: etapas.length + 1,
    });

    const etapasAtualizadas = await listarEtapasDoTrabalho(trabalho.id);

    const etapasMapeadas = etapasAtualizadas
      .map(mapearEtapa)
      .sort((a, b) => (a.ordem ?? 0) - (b.ordem ?? 0));

    setEtapas(etapasMapeadas);
    setEtapaSelecionadaId(novaEtapa.id);
  }

  async function salvarEdicaoEtapa(dados) {
    if (!etapaAtualObj?.id) {
      throw new Error("Nenhuma etapa foi selecionada.");
    }

    await atualizarEtapa(etapaAtualObj.id, dados);

    const etapasAtualizadas = await listarEtapasDoTrabalho(trabalho.id);

    setEtapas(
      etapasAtualizadas
        .map(mapearEtapa)
        .sort((a, b) => (a.ordem ?? 0) - (b.ordem ?? 0))
    );
  }

  async function salvarResponsavelEtapa(responsavelId) {
    if (!etapaAtualObj?.id) {
      throw new Error("Nenhuma etapa foi selecionada.");
    }

    const etapaAtualizada = await definirResponsavelEtapa(
      etapaAtualObj.id,
      responsavelId
    );

    setEtapas((anteriores) =>
      anteriores.map((etapa) =>
        etapa.id === etapaAtualizada.id
          ? mapearEtapa(etapaAtualizada)
          : etapa
      )
    );
  }

    async function salvarObservacaoEtapa() {
    const texto = observacao.trim();

    if (!etapaAtualObj?.id) {
      alert("Selecione uma etapa para registrar a observação.");
      return;
    }

    if (!texto) {
      alert("Digite uma observação antes de salvar.");
      return;
    }

    try {
      const etapaAtualizada = await registrarObservacaoEtapa(
        etapaAtualObj.id,
        texto
      );

      setEtapas((anteriores) =>
        anteriores.map((etapa) =>
          etapa.id === etapaAtualizada.id
            ? mapearEtapa(etapaAtualizada)
            : etapa
        )
      );

      const historicoAtualizado = await listarHistoricoTrabalho(id);
      setHistorico(historicoAtualizado.map(mapearHistorico));

      setObservacao("");
    } catch (erroApi) {
      alert(erroApi.message || "Não foi possível registrar a observação.");
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

      {contexto === "empresa" && empresaSelecionada && (
        <div className="detalhes-empresa">
          <span>Empresa selecionada</span>
          <h2>{empresaSelecionada.nome}</h2>
        </div>
      )}

      <TopoTrabalho
        trabalho={trabalho}
        getTopBadgeClass={getTopBadgeClass}
      />

      <ListaEtapas
        etapas={etapas}
        etapaSelecionadaId={etapaSelecionadaId}
        onSelecionarEtapa={setEtapaSelecionadaId}
        onAdicionarEtapa={adicionarEtapa}
        contexto={contexto}
      />

      <PainelDetalheEtapa
        etapaAtualObj={etapaAtualObj}
        getTopBadgeClass={getTopBadgeClass}
        observacao={observacao}
        setObservacao={setObservacao}
        onSalvarObservacao={salvarObservacaoEtapa}
        onMarcarConcluida={marcarEtapaConcluida}
        onSalvarEdicao={salvarEdicaoEtapa}
        membros={membros}
        carregandoMembros={carregandoMembros}
        erroMembros={erroMembros}
        onSalvarResponsavel={salvarResponsavelEtapa}
        contexto={contexto}
      />

      <HistoricoTrabalho historico={historico} />
    </div>
  );
}

