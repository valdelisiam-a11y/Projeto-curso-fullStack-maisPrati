
import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  ArrowUp,
  ArrowDown,
  Trash2,
  CheckCircle2,
  AlertCircle,
} from "lucide-react";

import "./NovoTrabalho.css";
import { useContextoTrabalho } from "../contexts/ContextoTrabalho";
import {
  cadastrarTrabalho,
  cadastrarEtapa,
} from "../services/trabalhoService";

export default function NovoTrabalho() {
  const navigate = useNavigate();
  const { contexto, empresaSelecionada } = useContextoTrabalho();

  const [nomeTrabalho, setNomeTrabalho] = useState("");
  const [descricao, setDescricao] = useState("");
  const [prazo, setPrazo] = useState("");

  const [etapas, setEtapas] = useState([
    { id: 1, titulo: "Receber documentos" },
    { id: 2, titulo: "Conferir documentação" },
  ]);

  const [novaEtapaInput, setNovaEtapaInput] = useState("");
  const [erros, setErros] = useState({});
  const [erroCriacao, setErroCriacao] = useState("");
  const [salvando, setSalvando] = useState(false);

  const handleAdicionarEtapa = () => {
    if (!novaEtapaInput.trim()) return;

    setEtapas((anteriores) => [
      ...anteriores,
      {
        id: Date.now(),
        titulo: novaEtapaInput.trim(),
      },
    ]);

    setNovaEtapaInput("");
    setErros((anteriores) => ({ ...anteriores, etapas: "" }));
  };

  const handleRemoverEtapa = (id) => {
    setEtapas((anteriores) =>
      anteriores.filter((etapa) => etapa.id !== id)
    );
  };

  const handleEditarEtapa = (id, novoTitulo) => {
    setEtapas((anteriores) =>
      anteriores.map((etapa) =>
        etapa.id === id ? { ...etapa, titulo: novoTitulo } : etapa
      )
    );
  };

  const handleMoverEtapa = (index, direcao) => {
    const novoIndex = direcao === "up" ? index - 1 : index + 1;

    if (novoIndex < 0 || novoIndex >= etapas.length) return;

    const atualizadas = [...etapas];
    const [etapaMovida] = atualizadas.splice(index, 1);
    atualizadas.splice(novoIndex, 0, etapaMovida);

    setEtapas(atualizadas);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (salvando) return;

    const novosErros = {};

    if (!nomeTrabalho.trim()) {
      novosErros.nomeTrabalho = "Informe o nome do trabalho.";
    }

    if (!prazo) {
      novosErros.prazo = "O prazo é obrigatório.";
    }

    if (etapas.length === 0) {
      novosErros.etapas = "Adicione pelo menos uma etapa.";
    } else if (etapas.some((etapa) => !etapa.titulo.trim())) {
      novosErros.etapas = "Todas as etapas precisam ter um título.";
    }

    if (Object.keys(novosErros).length > 0) {
      setErros(novosErros);
      setErroCriacao("");
      return;
    }

    // Por segurança, não enviamos IDs de empresas demonstrativas.
    if (contexto === "empresa" && !empresaSelecionada?.id) {
      setErroCriacao(
        "Selecione uma empresa antes de cadastrar um trabalho empresarial."
      );
      return;
    }

    setSalvando(true);
    setErroCriacao("");
    setErros({});

    let trabalhoCriado;

    try {
     trabalhoCriado = await cadastrarTrabalho({
        titulo: nomeTrabalho.trim(),
        descricao: descricao.trim(),
        tipo: contexto === "empresa" ? "EMPRESA" : "PESSOAL",
        ...(contexto === "empresa"
          ? { empresaId: empresaSelecionada.id }
          : {}),
    });

      if (!trabalhoCriado?.id) {
        throw new Error(
          "O trabalho foi enviado, mas a resposta não trouxe o ID esperado."
        );
      }
    } catch (error) {
      setErroCriacao(
        error.message || "Não foi possível cadastrar o trabalho."
      );
      setSalvando(false);
      return;
    }

    try {
      for (let index = 0; index < etapas.length; index++) {
        const etapa = etapas[index];

        await cadastrarEtapa(trabalhoCriado.id, {
          titulo: etapa.titulo.trim(),
          ordem: index + 1,
        });
      }

      navigate("/trabalhos");
    } catch (error) {
      setErroCriacao(
        `O trabalho foi criado, mas houve um erro ao cadastrar as etapas. ${error.message || "Verifique os dados e tente novamente."} O trabalho já existe; não o cadastre novamente.`
      );
    } finally {
      setSalvando(false);
    }
  };

  return (
    <div className="novo-trabalho-container">
      <div className="novo-trabalho-header">
        <h1>O que você quer organizar?</h1>
        <p>
          Dê um nome ao trabalho e escreva os passos que precisam ser feitos.
        </p>
      </div>

      <div className="novo-trabalho-grid">
        <form className="novo-trabalho-form" onSubmit={handleSubmit}>
          <div className="form-card">
            <div className="form-group">
              <label htmlFor="nomeTrabalho">Nome do trabalho</label>
              <input
                id="nomeTrabalho"
                type="text"
                placeholder="Ex.: Cadastro de novo cliente"
                value={nomeTrabalho}
                onChange={(e) => {
                  setNomeTrabalho(e.target.value);
                  setErros((anteriores) => ({
                    ...anteriores,
                    nomeTrabalho: "",
                  }));
                }}
                className={erros.nomeTrabalho ? "input-erro" : ""}
              />
              {erros.nomeTrabalho && (
                <p className="aviso-obrigatorio-criacao">
                  {erros.nomeTrabalho}
                </p>
              )}
            </div>

            <div className="form-group">
              <label htmlFor="descricao">Descrição</label>
              <textarea
                id="descricao"
                rows={3}
                placeholder="Explique com suas palavras o que precisa ser feito."
                value={descricao}
                onChange={(e) => setDescricao(e.target.value)}
              />
            </div>

            <div className="form-group">
              <label htmlFor="prazo">Prazo</label>
              <input
                id="prazo"
                type="date"
                value={prazo}
                onChange={(e) => {
                  setPrazo(e.target.value);
                  setErros((anteriores) => ({
                    ...anteriores,
                    prazo: "",
                  }));
                }}
                className={erros.prazo ? "input-erro" : ""}
              />
              {erros.prazo && (
                <p className="aviso-obrigatorio-criacao">
                  {erros.prazo}
                </p>
              )}
              <small>
                O prazo ainda não é persistido pelo backend.
              </small>
            </div>
          </div>

          <div className="form-card">
            <div className="etapas-header-title">
              <h3>Quais são os passos?</h3>
              <p>Você pode adicionar, editar, excluir e reordenar as etapas.</p>
            </div>

            {erros.etapas && (
              <div className="alerta-erro-etapas" role="alert">
                <AlertCircle className="w-4 h-4" />
                <span>{erros.etapas}</span>
              </div>
            )}

            <div className="etapas-lista">
              {etapas.map((etapa, index) => (
                <div key={etapa.id} className="etapa-item">
                  <span className="etapa-numero">{index + 1}</span>
                  <input
                    type="text"
                    value={etapa.titulo}
                    aria-label={`Título da etapa ${index + 1}`}
                    onChange={(e) =>
                      handleEditarEtapa(etapa.id, e.target.value)
                    }
                    className="etapa-input-edit"
                  />

                  <div className="etapa-acoes">
                    <button
                      type="button"
                      className="btn-acao-etapa"
                      onClick={() => handleMoverEtapa(index, "up")}
                      disabled={index === 0 || salvando}
                      aria-label="Mover etapa para cima"
                    >
                      <ArrowUp className="w-3.5 h-3.5" />
                    </button>

                    <button
                      type="button"
                      className="btn-acao-etapa"
                      onClick={() => handleMoverEtapa(index, "down")}
                      disabled={index === etapas.length - 1 || salvando}
                      aria-label="Mover etapa para baixo"
                    >
                      <ArrowDown className="w-3.5 h-3.5" />
                    </button>

                    <button
                      type="button"
                      className="btn-acao-etapa btn-deletar"
                      onClick={() => handleRemoverEtapa(etapa.id)}
                      disabled={salvando}
                      aria-label="Excluir etapa"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>
              ))}
            </div>

            <div className="add-etapa-wrapper">
              <input
                type="text"
                placeholder="Ex.: Enviar proposta"
                value={novaEtapaInput}
                onChange={(e) => setNovaEtapaInput(e.target.value)}
                disabled={salvando}
                onKeyDown={(e) => {
                  if (e.key === "Enter") {
                    e.preventDefault();
                    handleAdicionarEtapa();
                  }
                }}
              />
              <button
                type="button"
                className="btn-add-etapa"
                onClick={handleAdicionarEtapa}
                disabled={salvando}
              >
                + Adicionar etapa
              </button>
            </div>
          </div>
        </form>

        <div className="preview-column">
          <div className="preview-card">
            <span className="preview-badge">Prévia do seu fluxo</span>
            <p className="preview-subtext">Assim o trabalho vai aparecer.</p>

            <h4 className="preview-trabalho-titulo">
              {nomeTrabalho.trim() || "Nome do trabalho"}
            </h4>

            <div className="preview-etapas-lista">
              {etapas.length === 0 ? (
                <p className="preview-vazio">
                  Nenhuma etapa cadastrada ainda.
                </p>
              ) : (
                etapas.map((etapa, index) => (
                  <div key={etapa.id} className="preview-etapa-item">
                    {index === 0 ? (
                      <CheckCircle2 className="w-4 h-4 text-concluido" />
                    ) : (
                      <span className="preview-numero">{index + 1}</span>
                    )}
                    <span>{etapa.titulo || "Etapa sem título"}</span>
                  </div>
                ))
              )}
            </div>
          </div>

          <div className="preview-actions">
            {erroCriacao && (
              <p className="aviso-obrigatorio-criacao" role="alert">
                {erroCriacao}
              </p>
            )}

            <button
              type="button"
              className="btn-criar-trabalho"
              onClick={handleSubmit}
              disabled={salvando}
            >
              <CheckCircle2 className="w-4 h-4" />
              <span>
                {salvando ? "Salvando..." : "Criar trabalho"}
              </span>
            </button>

            <button
              type="button"
              className="btn-cancelar"
              onClick={() => navigate(-1)}
              disabled={salvando}
            >
              Cancelar
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
