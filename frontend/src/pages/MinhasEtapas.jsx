import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  PlayCircle,
  AlertTriangle,
  Eye,
  Pencil,
  ArrowRight,
} from "lucide-react";
import "./MinhasEtapas.css";

export default function MinhasEtapas() {
  const navigate = useNavigate();
  const [etapas, setEtapas] = useState([
    {
      id: 1,
      nome: "Conferir notas fiscais",
      trabalho: "Reforma da loja centro",
      empresa: "Construtora Acácias",
      status: "Em andamento",
      permissao: "editar",
      compartilhadoPor: "Carlos Mendes",
    },
    {
      id: 2,
      nome: "Revisar relatório final",
      trabalho: "Auditoria trimestral",
      empresa: "Souza Serviços",
      status: "Precisa de atenção",
      permissao: "visualizar",
      compartilhadoPor: "João Pereira",
    },
  ]);
  const [loading, setLoading] = useState(false);
  const [erro, setErro] = useState(null);

  return (
    <div className="minhas-etapas-container">
      <div className="header-secao">
        <h1>Minhas etapas</h1>
        <p>
          Etapas que outras pessoas compartilharam com você. Você vê só a etapa,
          não o trabalho inteiro.
        </p>
        <div className="banner-demo">
          Demonstração: estas etapas são exemplos, ainda não vêm de outras
          contas reais.
        </div>
      </div>

      {loading && (
        <div className="estado-carregamento">A carregar etapas...</div>
      )}
      {erro && <div className="estado-erro">{erro}</div>}

      {!loading && !erro && etapas.length === 0 && (
        <div className="estado-vazio">
          <p>Nenhuma etapa compartilhada encontrada.</p>
        </div>
      )}

      <div className="lista-etapas-compartilhadas">
        {etapas.map((etapa) => (
          <div key={etapa.id} className="card-etapa">
            <div className="card-topo">
              <div className="card-info-principal">
                <h3>{etapa.nome}</h3>
                <p className="subtitulo-trabalho">
                  {etapa.trabalho} · {etapa.empresa}
                </p>
              </div>

              <div>
                {etapa.status === "Em andamento" ? (
                  <span className="badge-status em-andamento">
                    <PlayCircle size={14} /> Em andamento
                  </span>
                ) : (
                  <span className="badge-status precisa-atencao">
                    <AlertTriangle size={14} /> Precisa de atenção
                  </span>
                )}
              </div>
            </div>

            <div className="card-rodape">
              <div className="permissao-info">
                {etapa.permissao === "editar" ? (
                  <>
                    <Pencil size={14} /> Pode editar · compartilhada por{" "}
                    {etapa.compartilhadoPor}
                  </>
                ) : (
                  <>
                    <Eye size={14} /> Pode visualizar · compartilhada por{" "}
                    {etapa.compartilhadoPor}
                  </>
                )}
              </div>

              {/* Botão com o redirecionamento corrigido */}
              <button
                className="btn-abrir-etapa"
                onClick={() => navigate(`/etapas/${etapa.id}`)}
              >
                Abrir etapa <ArrowRight size={14} />
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
