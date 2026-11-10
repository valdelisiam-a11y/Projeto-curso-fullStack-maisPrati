
import React, { useState } from "react";
import { CheckCircle2, Plus } from "lucide-react";
import "./ListaEtapas.css";

export default function ListaEtapas({
  etapas,
  etapaSelecionadaId,
  onSelecionarEtapa,
  onAdicionarEtapa,
  contexto,
}) {
  const [adicionando, setAdicionando] = useState(false);
  const [titulo, setTitulo] = useState("");
  const [salvando, setSalvando] = useState(false);
  const [erro, setErro] = useState("");

  async function handleAdicionarEtapa(event) {
    event.preventDefault();

    if (!titulo.trim()) {
      setErro("Informe o título da etapa.");
      return;
    }

    setSalvando(true);
    setErro("");

    try {
      await onAdicionarEtapa(titulo);
      setTitulo("");
      setAdicionando(false);
    } catch (erroApi) {
      setErro(erroApi.message || "Não foi possível adicionar a etapa.");
    } finally {
      setSalvando(false);
    }
  }

  return (
    <div className="etapas-secao">
      <div className="etapas-secao-cabecalho">
        <h3 className="secao-titulo">Etapas</h3>

        <button
          type="button"
          className="btn-adicionar-etapa"
          onClick={() => {
            setAdicionando((anterior) => !anterior);
            setErro("");
          }}
        >
          <Plus className="w-4 h-4" />
          Adicionar etapa
        </button>
      </div>

      <p className="secao-subtitulo">
        Toque em uma etapa para ver os detalhes.
      </p>

      {adicionando && (
        <form
          className="form-adicionar-etapa"
          onSubmit={handleAdicionarEtapa}
        >
          <input
            type="text"
            value={titulo}
            onChange={(event) => setTitulo(event.target.value)}
            placeholder="Digite o título da etapa"
            maxLength={150}
            autoFocus
            disabled={salvando}
          />

          <button type="submit" disabled={salvando}>
            {salvando ? "Salvando..." : "Salvar etapa"}
          </button>

          <button
            type="button"
            onClick={() => {
              setAdicionando(false);
              setTitulo("");
              setErro("");
            }}
            disabled={salvando}
          >
            Cancelar
          </button>

          {erro && <p className="erro-adicionar-etapa">{erro}</p>}
        </form>
      )}

      {etapas.length === 0 ? (
        <p className="preview-vazio">
          Nenhuma etapa cadastrada ainda. Adicione a primeira etapa.
        </p>
      ) : (
        <div
          className="etapas-horizontal-grid"
          role="tablist"
          aria-label="Etapas do trabalho"
        >
          {etapas.map((etapa, index) => {
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
                      {index + 1}
                    </span>
                  )}

                  <span className="etapa-card-conteudo">
                    <span className="etapa-card-titulo">
                      {etapa.titulo}
                    </span>

                    <span
                      className={`etapa-card-substatus ${etapa.status
                        .toLowerCase()
                        .replace(/\s+/g, "-")}`}
                    >
                      {etapa.status}
                    </span>

                    {contexto === "empresa" && (
                      <span className="etapa-card-responsavel">
                        Responsável: {etapa.responsavel || "Não atribuído"}
                      </span>
                    )}
                  </span>
                </div>
              </button>
            );
          })}
        </div>
      )}
    </div>
  );
}
