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

export default function NovoTrabalho() {
  const navigate = useNavigate();

  const [nomeTrabalho, setNomeTrabalho] = useState("");
  const [descricao, setDescricao] = useState("");
  const [prazo, setPrazo] = useState("");

  const [etapas, setEtapas] = useState([
    { id: 1, titulo: "Receber documentos" },
    { id: 2, titulo: "Conferir documentação" },
  ]);
  const [novaEtapaInput, setNovaEtapaInput] = useState("");
  const [erros, setErros] = useState({});

  // Estado específico para a frase de aviso ao tentar submetendo sem nome
  const [erroCriacao, setErroCriacao] = useState("");

  const handleAdicionarEtapa = () => {
    if (!novaEtapaInput.trim()) return;
    const novaEtapa = {
      id: Date.now(),
      titulo: novaEtapaInput.trim(),
    };
    setEtapas([...etapas, novaEtapa]);
    setNovaEtapaInput("");
    if (erros.etapas) setErros((prev) => ({ ...prev, etapas: null }));
  };

  const handleRemoverEtapa = (id) => {
    setEtapas(etapas.filter((etapa) => etapa.id !== id));
  };

  const handleEditarEtapa = (id, novoTitulo) => {
    setEtapas(
      etapas.map((etapa) =>
        etapa.id === id ? { ...etapa, titulo: novoTitulo } : etapa,
      ),
    );
  };

  const handleMoverEtapa = (index, direcao) => {
    const novoIndex = direcao === "up" ? index - 1 : index + 1;
    if (novoIndex < 0 || novoIndex >= etapas.length) return;
    const etapasAtualizadas = [...etapas];
    const [itemRemovido] = etapasAtualizadas.splice(index, 1);
    etapasAtualizadas.splice(novoIndex, 0, itemRemovido);
    setEtapas(etapasAtualizadas);
  };

  const handleSubmit = (e) => {
    e.preventDefault();

    // Validação do nome do trabalho para exibir a mensagem solicitada
    if (!nomeTrabalho.trim()) {
      setErroCriacao("Escreva o nome do trabalho para continuar.");
      return;
    }

    // Limpa o erro se estiver preenchido corretamente
    setErroCriacao("");

    const novosErros = {};
    if (!prazo) novosErros.prazo = "O prazo é obrigatório.";
    if (etapas.length === 0)
      novosErros.etapas = "Adicione pelo menos uma etapa.";

    if (Object.keys(novosErros).length > 0) {
      setErros(novosErros);
      return;
    }

    alert("Trabalho criado com sucesso!");
    navigate("/trabalhos");
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
        {/* COLUNA ESQUERDA: FORMULÁRIO */}
        <form className="novo-trabalho-form" onSubmit={handleSubmit}>
          {/* Dados Principais */}
          <div className="form-card">
            <div className="form-group">
              <label htmlFor="nomeTrabalho">Nome do trabalho</label>
              <input
                id="nomeTrabbelho"
                type="text"
                placeholder="Ex.: Cadastro de novo cliente"
                value={nomeTrabalho}
                onChange={(e) => {
                  setNomeTrabalho(e.target.value);
                  if (e.target.value.trim()) setErroCriacao(""); // Limpa o aviso ao digitar
                }}
                className={erros.nomeTrabalho ? "input-erro" : ""}
              />
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
                onChange={(e) => setPrazo(e.target.value)}
                className={erros.prazo ? "input-erro" : ""}
              />
            </div>
          </div>

          {/* Card Separado: Quais são os passos? */}
          <div className="form-card">
            <div className="etapas-header-title">
              <h3>Quais são os passos?</h3>
              <p>Você pode adicionar, editar, excluir e reordenar as etapas.</p>
            </div>

            {erros.etapas && (
              <div className="alerta-erro-etapas">
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
                      disabled={index === 0}
                    >
                      <ArrowUp className="w-3.5 h-3.5" />
                    </button>
                    <button
                      type="button"
                      className="btn-acao-etapa"
                      onClick={() => handleMoverEtapa(index, "down")}
                      disabled={index === etapas.length - 1}
                    >
                      <ArrowDown className="w-3.5 h-3.5" />
                    </button>
                    <button
                      type="button"
                      className="btn-acao-etapa btn-deletar"
                      onClick={() => handleRemoverEtapa(etapa.id)}
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
              >
                + Adicionar etapa
              </button>
            </div>
          </div>
        </form>

        {/* COLUNA DIREITA: LIVE PREVIEW E AÇÕES */}
        <div className="preview-column">
          <div className="preview-card">
            <span className="preview-badge">Prévia do seu fluxo</span>
            <p className="preview-subtext">Assim o trabalho vai aparecer.</p>

            <h4 className="preview-trabalho-titulo">
              {nomeTrabalho.trim() ? nomeTrabalho : "Nome do trabalho"}
            </h4>

            <div className="preview-etapas-lista">
              {etapas.length === 0 ? (
                <p className="preview-vazio">Nenhuma etapa cadastrada ainda.</p>
              ) : (
                etapas.map((etapa, idx) => (
                  <div key={etapa.id} className="preview-etapa-item">
                    {idx === 0 ? (
                      <CheckCircle2 className="w-4 h-4 text-concluido" />
                    ) : (
                      <span className="preview-numero">{idx + 1}</span>
                    )}
                    <span>{etapa.titulo || "Etapa sem título"}</span>
                  </div>
                ))
              )}
            </div>
          </div>

          <div className="preview-actions">
            {/* Mensagem exibida condicionalmente idêntica à referência visual */}
            {erroCriacao && (
              <span className="aviso-obrigatorio-criacao">{erroCriacao}</span>
            )}

            <button
              type="button"
              className="btn-criar-trabalho"
              onClick={handleSubmit}
            >
              <CheckCircle2 className="w-4 h-4" />
              <span>Criar trabalho</span>
            </button>
            <button
              type="button"
              className="btn-cancelar"
              onClick={() => navigate(-1)}
            >
              Cancelar
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
