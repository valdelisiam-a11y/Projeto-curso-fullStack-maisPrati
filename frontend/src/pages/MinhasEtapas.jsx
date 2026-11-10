import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  PlayCircle,
  AlertTriangle,
  ArrowRight,
} from "lucide-react";

import { listarMinhasEtapas } from "../services/trabalhoService";
import { useContextoTrabalho } from "../contexts/ContextoTrabalho";

import "./MinhasEtapas.css";

export default function MinhasEtapas() {
  const navigate = useNavigate();

  const { contexto, empresaSelecionada } = useContextoTrabalho();

  const [etapas, setEtapas] = useState([]);
  const [loading, setLoading] = useState(false);
  const [erro, setErro] = useState(null);

  useEffect(() => {
    let ativo = true;

    async function carregarEtapas() {
      if (contexto !== "empresa" || !empresaSelecionada?.id) {
        setEtapas([]);
        setErro(null);
        setLoading(false);
        return;
      }

      try {
        setLoading(true);
        setErro(null);

        const resultado = await listarMinhasEtapas(
          empresaSelecionada.id
        );

        if (ativo) {
          setEtapas(Array.isArray(resultado) ? resultado : []);
        }
      } catch (error) {
        if (ativo) {
          setEtapas([]);
          setErro(
            error.message || "Não foi possível carregar suas etapas."
          );
        }
      } finally {
        if (ativo) {
          setLoading(false);
        }
      }
    }

    carregarEtapas();

    return () => {
      ativo = false;
    };
  }, [contexto, empresaSelecionada?.id]);

  function obterStatus(status) {
    switch (status) {
      case "EM_ANDAMENTO":
        return (
          <span className="badge-status em-andamento">
            <PlayCircle size={14} /> Em andamento
          </span>
        );

      case "PENDENTE":
        return (
          <span className="badge-status precisa-atencao">
            <AlertTriangle size={14} /> Pendente
          </span>
        );

      case "CONCLUIDO":
        return (
          <span className="badge-status">
            Concluída
          </span>
        );

      case "CANCELADO":
        return (
          <span className="badge-status">
            Cancelada
          </span>
        );

      default:
        return (
          <span className="badge-status">
            {status || "Sem status"}
          </span>
        );
    }
  }

  return (
    <div className="minhas-etapas-container">
      <div className="header-secao">
        <h1>Minhas etapas</h1>
        <p>
          Etapas atribuídas a você na empresa selecionada.
        </p>
      </div>

      {contexto !== "empresa" && (
        <div className="estado-vazio">
          <p>Minhas etapas está disponível no contexto empresarial.</p>
        </div>
      )}

      {contexto === "empresa" && loading && (
        <div className="estado-carregamento">
          Carregando etapas...
        </div>
      )}

      {contexto === "empresa" && erro && (
        <div className="estado-erro">{erro}</div>
      )}

      {contexto === "empresa" &&
        !loading &&
        !erro &&
        etapas.length === 0 && (
          <div className="estado-vazio">
            <p>
              Você não está participando de nenhuma etapa nesta empresa.
            </p>
          </div>
        )}

      {!loading && !erro && (
        <div className="lista-etapas-compartilhadas">
          {etapas.map((etapa) => (
            <div key={etapa.etapaId} className="card-etapa">
              <div className="card-topo">
                <div className="card-info-principal">
                  <h3>{etapa.etapaTitulo}</h3>

                  <p className="subtitulo-trabalho">
                    {etapa.trabalhoTitulo} · {etapa.empresaNome}
                  </p>
                </div>

                <div>
                  {obterStatus(etapa.status)}
                </div>
              </div>

              <div className="card-rodape">
                <div className="permissao-info">
                  Progresso: {etapa.percentualConclusao}%
                </div>

                <button
                  className="btn-abrir-etapa"
                  onClick={() =>
                    navigate(`/etapas/${etapa.etapaId}`)
                  }
                >
                  Abrir etapa <ArrowRight size={14} />
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
