import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  ArrowUp,
  ArrowDown,
  Trash2,
  CheckCircle2,
  AlertCircle,
  Paperclip,
  X,
} from "lucide-react";

import "./NovoTrabalho.css";
import { useContextoTrabalho } from "../contexts/ContextoTrabalho";
import {
  cadastrarTrabalho,
  cadastrarEtapa,
} from "../services/trabalhoService";
import { listarMembrosEmpresa } from "../services/empresaService";
import { enviarDocumento } from "../services/documentoService";

export default function NovoTrabalho() {
  const navigate = useNavigate();
  const { contexto, empresaSelecionada } = useContextoTrabalho();

  const [nomeTrabalho, setNomeTrabalho] = useState("");
  const [descricao, setDescricao] = useState("");
  const [prazo, setPrazo] = useState("");

  const [etapas, setEtapas] = useState([
    {
      id: 1,
      titulo: "Receber documentos",
      responsavelId: "",
      arquivo: null,
    },
    {
      id: 2,
      titulo: "Conferir documentação",
      responsavelId: "",
      arquivo: null,
    },
  ]);

  const [membros, setMembros] = useState([]);
  const [carregandoMembros, setCarregandoMembros] = useState(false);
  const [erroMembros, setErroMembros] = useState("");

  const [novaEtapaInput, setNovaEtapaInput] = useState("");
  const [erros, setErros] = useState({});
  const [erroCriacao, setErroCriacao] = useState("");
  const [salvando, setSalvando] = useState(false);

  useEffect(() => {
    setMembros([]);
    setErroMembros("");

    setEtapas((anteriores) =>
      anteriores.map((etapa) => ({
        ...etapa,
        responsavelId: "",
      }))
    );

    if (contexto !== "empresa" || !empresaSelecionada?.id) {
      setCarregandoMembros(false);
      return;
    }

    let cancelado = false;

    async function carregarMembros() {
      setCarregandoMembros(true);

      try {
        const resposta = await listarMembrosEmpresa(empresaSelecionada.id);

        const lista = Array.isArray(resposta)
          ? resposta
          : resposta?.membros ?? [];

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

        if (!cancelado) {
          setMembros(membrosNormalizados);
        }
      } catch (error) {
        if (!cancelado) {
          setErroMembros(
            error.message || "Não foi possível carregar os membros da empresa."
          );
        }
      } finally {
        if (!cancelado) {
          setCarregandoMembros(false);
        }
      }
    }

    carregarMembros();

    return () => {
      cancelado = true;
    };
  }, [contexto, empresaSelecionada?.id]);

  const handleAdicionarEtapa = () => {
    if (!novaEtapaInput.trim()) return;

    setEtapas((anteriores) => [
      ...anteriores,
      {
        id: Date.now(),
        titulo: novaEtapaInput.trim(),
        responsavelId: "",
        arquivo: null,
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

  const handleDefinirResponsavel = (id, responsavelId) => {
    setEtapas((anteriores) =>
      anteriores.map((etapa) =>
        etapa.id === id ? { ...etapa, responsavelId } : etapa
      )
    );
  };

  const handleSelecionarArquivo = (id, arquivo) => {
    setEtapas((anteriores) =>
      anteriores.map((etapa) =>
        etapa.id === id ? { ...etapa, arquivo } : etapa
      )
    );
  };

  const handleRemoverArquivo = (id) => {
    setEtapas((anteriores) =>
      anteriores.map((etapa) =>
        etapa.id === id ? { ...etapa, arquivo: null } : etapa
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

    if (contexto === "empresa" && !empresaSelecionada?.id) {
      setErroCriacao(
        "Selecione uma empresa antes de cadastrar um trabalho empresarial."
      );
      return;
    }

    if (contexto === "empresa" && carregandoMembros) {
      setErroCriacao("Aguarde o carregamento dos membros da empresa.");
      return;
    }

    if (contexto === "empresa" && erroMembros) {
      setErroCriacao(
        "Não foi possível carregar os membros da empresa. Tente novamente antes de criar o trabalho."
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
          "A resposta do cadastro do trabalho não trouxe o ID esperado."
        );
      }
    } catch (error) {
      setErroCriacao(
        error.message || "Não foi possível cadastrar o trabalho."
      );
      setSalvando(false);
      return;
    }

    let etapasCadastradas = 0;
    let documentosEnviados = 0;

    try {
      for (let index = 0; index < etapas.length; index++) {
        const etapa = etapas[index];

        const etapaCriada = await cadastrarEtapa(trabalhoCriado.id, {
          titulo: etapa.titulo.trim(),
          ordem: index + 1,
          ...(contexto === "empresa" && etapa.responsavelId
            ? { responsavelId: Number(etapa.responsavelId) }
            : {}),
        });

        etapasCadastradas++;

        if (etapa.arquivo) {
          if (!etapaCriada?.id) {
            throw new Error(
              `A etapa "${etapa.titulo}" foi cadastrada, mas a resposta não trouxe o ID necessário para anexar o documento.`
            );
          }

          await enviarDocumento(etapaCriada.id, etapa.arquivo);
          documentosEnviados++;
        }
      }

      navigate("/trabalhos");
    } catch (error) {
      const documentosPendentes = etapas.filter(
        (etapa) => etapa.arquivo
      ).length - documentosEnviados;

      setErroCriacao(
        `O trabalho foi criado e ${etapasCadastradas} de ${etapas.length} etapa(s) foram cadastradas. ${
          documentosPendentes > 0
            ? `${documentosPendentes} documento(s) não foram enviados. `
            : ""
        }${error.message || "Ocorreu um erro durante o cadastro."} Não tente cadastrar o trabalho novamente.`
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
              <small>O prazo ainda não é persistido pelo backend.</small>
            </div>
          </div>

          <div className="form-card">
            <div className="etapas-header-title">
              <h3>Quais são os passos?</h3>
              <p>
                Você pode adicionar, editar, excluir e reordenar as etapas.
              </p>
            </div>

            {erros.etapas && (
              <div className="alerta-erro-etapas" role="alert">
                <AlertCircle className="w-4 h-4" />
                <span>{erros.etapas}</span>
              </div>
            )}

            {contexto === "empresa" && carregandoMembros && (
              <p>Carregando colaboradores da empresa...</p>
            )}

            {contexto === "empresa" && erroMembros && (
              <div className="alerta-erro-etapas" role="alert">
                <AlertCircle className="w-4 h-4" />
                <span>{erroMembros}</span>
              </div>
            )}

            <div className="etapas-lista">
              {etapas.map((etapa, index) => (
                <div key={etapa.id} className="etapa-item">
                  <div className="etapa-linha-principal">
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

                  {contexto === "empresa" && (
                    <div className="etapa-campo">
                      <label htmlFor={`responsavel-${etapa.id}`}>
                        Colaborador responsável
                      </label>

                      <select
                        id={`responsavel-${etapa.id}`}
                        className="etapa-select"
                        value={etapa.responsavelId}
                        onChange={(e) =>
                          handleDefinirResponsavel(
                            etapa.id,
                            e.target.value
                          )
                        }
                        disabled={
                          salvando || carregandoMembros || !!erroMembros
                        }
                      >
                        <option value="">Sem responsável</option>

                        {membros.map((membro) => (
                          <option key={membro.id} value={membro.id}>
                            {membro.nome}
                          </option>
                        ))}
                      </select>
                    </div>
                  )}

                  <div className="etapa-campo">
                    <label htmlFor={`arquivo-${etapa.id}`}>
                      Documento da etapa
                    </label>

                    <div className="etapa-arquivo">
                      <label
                        htmlFor={`arquivo-${etapa.id}`}
                        className="btn-selecionar-arquivo"
                      >
                        <Paperclip size={15} />
                        <span>
                          {etapa.arquivo ? "Trocar arquivo" : "Anexar documento"}
                        </span>
                      </label>

                     <input
                        id={`arquivo-${etapa.id}`}
                        type="file"
                        className="input-arquivo-oculto"
                        onChange={(e) => {
                          const arquivo = e.target.files?.[0] ?? null;
                          handleSelecionarArquivo(etapa.id, arquivo);
                          e.target.value = "";
                        }}
                        disabled={salvando}
                      />

                      {etapa.arquivo && (
                        <div className="arquivo-selecionado">
                          <span title={etapa.arquivo.name}>
                            {etapa.arquivo.name}
                          </span>

                          <button
                            type="button"
                            className="btn-remover-arquivo"
                            onClick={() => handleRemoverArquivo(etapa.id)}
                            disabled={salvando}
                            aria-label="Remover documento selecionado"
                            title="Remover documento"
                          >
                            <X size={15} />
                          </button>
                        </div>
                      )}
                    </div>
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
              <span>{salvando ? "Salvando..." : "Criar trabalho"}</span>
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