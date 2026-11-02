
import { useEffect, useState } from "react";
import {
  ArrowLeft,
  Building2,
  Plus,
  Users,
  UserPlus,
  Trash2,
  Crown,
} from "lucide-react";
import { useNavigate } from "react-router-dom";
import {
  listarMinhasEmpresas,
  cadastrarEmpresa,
  listarMembrosEmpresa,
  adicionarMembroEmpresa,
  removerMembroEmpresa,
} from "../services/empresaService";
import "./GerenciarEmpresas.css";

export default function GerenciarEmpresas() {
  const navigate = useNavigate();

  const [empresas, setEmpresas] = useState([]);
  const [empresaSelecionadaId, setEmpresaSelecionadaId] = useState("");
  const [carregando, setCarregando] = useState(true);
  const [carregandoMembros, setCarregandoMembros] = useState(false);
  const [salvando, setSalvando] = useState(false);

  const [novoNomeEmpresa, setNovoNomeEmpresa] = useState("");
  const [novoEmailMembro, setNovoEmailMembro] = useState("");

  const [mostrarFormularioEmpresa, setMostrarFormularioEmpresa] =
    useState(false);
  const [mostrarFormularioMembro, setMostrarFormularioMembro] =
    useState(false);

  const [feedback, setFeedback] = useState("");

  const empresaSelecionada = empresas.find(
    (empresa) => String(empresa.id) === String(empresaSelecionadaId)
  );

  const membros = empresaSelecionada?.membros ?? [];
  const ehCriador = empresaSelecionada?.papel === "CRIADOR";
  const limiteAtingido = membros.length >= 5;

  async function carregarEmpresas() {
    const dados = await listarMinhasEmpresas();

    setEmpresas((atuais) =>
      dados.map((empresa) => {
        const existente = atuais.find(
          (item) => String(item.id) === String(empresa.id)
        );

        return {
          ...empresa,
          membros: existente?.membros ?? [],
        };
      })
    );

    return dados;
  }

  async function carregarMembros(empresaId) {
    setCarregandoMembros(true);

    try {
      const dados = await listarMembrosEmpresa(empresaId);

      setEmpresas((atuais) =>
        atuais.map((empresa) =>
          String(empresa.id) === String(empresaId)
            ? { ...empresa, membros: dados }
            : empresa
        )
      );
    } finally {
      setCarregandoMembros(false);
    }
  }

  useEffect(() => {
    async function inicializar() {
      try {
        const dados = await listarMinhasEmpresas();

        setEmpresas(
          dados.map((empresa) => ({
            ...empresa,
            membros: [],
          }))
        );

        if (dados.length > 0) {
          setEmpresaSelecionadaId(String(dados[0].id));
        }
      } catch (erro) {
        setFeedback(
          erro.message || "Não foi possível carregar as empresas."
        );
      } finally {
        setCarregando(false);
      }
    }

    inicializar();
  }, []);

  useEffect(() => {
    if (!empresaSelecionadaId) return;

    let ativo = true;

    async function atualizarMembros() {
      setCarregandoMembros(true);

      try {
        const dados = await listarMembrosEmpresa(empresaSelecionadaId);

        if (ativo) {
          setEmpresas((atuais) =>
            atuais.map((empresa) =>
              String(empresa.id) === String(empresaSelecionadaId)
                ? { ...empresa, membros: dados }
                : empresa
            )
          );
        }
      } catch (erro) {
        if (ativo) {
          setFeedback(
            erro.message || "Não foi possível carregar os membros."
          );
        }
      } finally {
        if (ativo) {
          setCarregandoMembros(false);
        }
      }
    }

    atualizarMembros();

    return () => {
      ativo = false;
    };
  }, [empresaSelecionadaId]);

  async function criarEmpresa(event) {
    event.preventDefault();

    const nome = novoNomeEmpresa.trim();

    if (!nome || salvando) return;

    setSalvando(true);
    setFeedback("");

    try {
      const novaEmpresa = await cadastrarEmpresa({ nome });

      const empresaComMembros = {
        ...novaEmpresa,
        membros: [],
      };

      setEmpresas((atuais) => [
        ...atuais,
        empresaComMembros,
      ]);

      setEmpresaSelecionadaId(String(novaEmpresa.id));
      setNovoNomeEmpresa("");
      setMostrarFormularioEmpresa(false);
      setMostrarFormularioMembro(false);
      setFeedback("Empresa criada com sucesso.");
    } catch (erro) {
      setFeedback(
        erro.message || "Não foi possível criar a empresa."
      );
    } finally {
      setSalvando(false);
    }
  }

  async function adicionarMembro(event) {
    event.preventDefault();

    if (
      !empresaSelecionada ||
      !ehCriador ||
      limiteAtingido ||
      salvando
    ) {
      return;
    }

    const email = novoEmailMembro.trim();

    if (!email) return;

    setSalvando(true);
    setFeedback("");

    try {
      await adicionarMembroEmpresa(empresaSelecionada.id, email);

      await carregarMembros(empresaSelecionada.id);

      setNovoEmailMembro("");
      setMostrarFormularioMembro(false);
      setFeedback("Colaborador adicionado com sucesso.");
    } catch (erro) {
      setFeedback(
        erro.message || "Não foi possível adicionar o colaborador."
      );
    } finally {
      setSalvando(false);
    }
  }

  async function removerMembro(usuarioId) {
    if (!empresaSelecionada || !ehCriador || salvando) return;

    setSalvando(true);
    setFeedback("");

    try {
      await removerMembroEmpresa(empresaSelecionada.id, usuarioId);

      await carregarMembros(empresaSelecionada.id);

      setFeedback("Colaborador removido com sucesso.");
    } catch (erro) {
      setFeedback(
        erro.message || "Não foi possível remover o colaborador."
      );
    } finally {
      setSalvando(false);
    }
  }

  return (
    <div className="gerenciar-empresas-container">
      <header className="empresas-header">
        <button
          type="button"
          className="btn-voltar"
          onClick={() => navigate("/configuracoes")}
        >
          <ArrowLeft size={18} />
          Voltar para configurações
        </button>

        <h1>Gerenciar empresas</h1>
        <p>
          Organize suas empresas e gerencie quem participa de cada uma.
        </p>
      </header>

      {feedback && (
        <div className="empresas-feedback" role="status">
          <span>{feedback}</span>

          <button
            type="button"
            onClick={() => setFeedback("")}
            aria-label="Fechar mensagem"
          >
            Fechar
          </button>
        </div>
      )}

      <section className="config-card empresas-selecao">
        <div className="config-card-title">
          <Building2 className="icon-title" />
          <span>Minhas empresas</span>
        </div>

        <div className="empresas-selecao-conteudo">
          <div className="form-group">
            <label htmlFor="empresaSelecionada">
              Empresa selecionada
            </label>

            <select
              id="empresaSelecionada"
              value={empresaSelecionadaId}
              disabled={carregando || empresas.length === 0}
              onChange={(event) => {
                setEmpresaSelecionadaId(event.target.value);
                setFeedback("");
                setMostrarFormularioMembro(false);
                setNovoEmailMembro("");
              }}
            >
              {empresas.length === 0 && (
                <option value="">
                  {carregando
                    ? "Carregando empresas..."
                    : "Nenhuma empresa cadastrada"}
                </option>
              )}

              {empresas.map((empresa) => (
                <option key={empresa.id} value={String(empresa.id)}>
                  {empresa.nome}
                </option>
              ))}
            </select>
          </div>

          <button
            type="button"
            className="btn-purple"
            onClick={() => {
              setMostrarFormularioEmpresa((visivel) => !visivel);
              setFeedback("");
            }}
          >
            <Plus size={18} />
            Nova empresa
          </button>
        </div>

        {mostrarFormularioEmpresa && (
          <form className="empresa-formulario" onSubmit={criarEmpresa}>
            <div className="form-group">
              <label htmlFor="novoNomeEmpresa">Nome da empresa</label>

              <input
                id="novoNomeEmpresa"
                type="text"
                value={novoNomeEmpresa}
                onChange={(event) =>
                  setNovoNomeEmpresa(event.target.value)
                }
                placeholder="Digite o nome da empresa"
                maxLength={150}
                required
              />
            </div>

            <div className="empresa-formulario-acoes">
              <button
                type="button"
                className="btn-cancelar"
                disabled={salvando}
                onClick={() => {
                  setMostrarFormularioEmpresa(false);
                  setNovoNomeEmpresa("");
                }}
              >
                Cancelar
              </button>

              <button
                type="submit"
                className="btn-purple"
                disabled={salvando}
              >
                {salvando ? "Criando..." : "Criar empresa"}
              </button>
            </div>
          </form>
        )}
      </section>

      {!carregando && empresas.length === 0 && (
        <section className="config-card empresas-detalhes">
          <h2>Nenhuma empresa cadastrada</h2>
          <p>
            Crie uma empresa para começar a gerenciar seus colaboradores.
          </p>
        </section>
      )}

      {carregando && (
        <section className="config-card">
          <p>Carregando empresas...</p>
        </section>
      )}

      {empresaSelecionada && (
        <>
          <section className="config-card empresas-detalhes">
            <div className="empresas-detalhes-topo">
              <div>
                <h2>{empresaSelecionada.nome}</h2>

                <span className="empresa-papel">
                  {ehCriador ? (
                    <Crown size={15} />
                  ) : (
                    <Users size={15} />
                  )}

                  Você é{" "}
                  {ehCriador
                    ? "criador"
                    : empresaSelecionada.papel === "MEMBRO"
                      ? "membro"
                      : String(empresaSelecionada.papel).toLowerCase()}
                </span>
              </div>

              <div className="empresa-contagem">
                <strong>
                  {membros.length} de 5
                </strong>
                <span>usuários</span>
              </div>
            </div>

            <div
              className="empresa-progresso"
              role="progressbar"
              aria-label="Ocupação de usuários"
              aria-valuenow={membros.length}
              aria-valuemin={0}
              aria-valuemax={5}
            >
              <span
                style={{
                  width: `${(membros.length / 5) * 100}%`,
                }}
              />
            </div>

            <p className="empresa-limite-info">
              {limiteAtingido
                ? "O limite de usuários foi atingido."
                : `Você pode adicionar mais ${5 - membros.length} usuário(s).`}
            </p>
          </section>

          <section className="config-card empresas-membros">
            <div className="empresas-membros-topo">
              <div className="config-card-title">
                <Users className="icon-title" />
                <span>Colaboradores da empresa</span>
              </div>

              {ehCriador && (
                <button
                  type="button"
                  className="btn-purple"
                  disabled={limiteAtingido || carregandoMembros}
                  onClick={() => {
                    setMostrarFormularioMembro((visivel) => !visivel);
                    setNovoEmailMembro("");
                    setFeedback("");
                  }}
                >
                  <UserPlus size={17} />
                  Adicionar colaborador
                </button>
              )}
            </div>

            {!ehCriador && (
              <p className="empresas-aviso">
                Apenas o criador da empresa pode gerenciar os colaboradores.
              </p>
            )}

            {mostrarFormularioMembro && ehCriador && !limiteAtingido && (
              <form
                className="empresa-formulario"
                onSubmit={adicionarMembro}
              >
                <div className="form-group">
                  <label htmlFor="novoEmailMembro">
                    E-mail do usuário cadastrado
                  </label>

                  <input
                    id="novoEmailMembro"
                    type="email"
                    value={novoEmailMembro}
                    onChange={(event) =>
                      setNovoEmailMembro(event.target.value)
                    }
                    placeholder="email@exemplo.com"
                    maxLength={150}
                    required
                  />

                  <small>
                    O usuário precisa ter uma conta no DocFlow.
                  </small>
                </div>

                <div className="empresa-formulario-acoes">
                  <button
                    type="button"
                    className="btn-cancelar"
                    disabled={salvando}
                    onClick={() => {
                      setMostrarFormularioMembro(false);
                      setNovoEmailMembro("");
                    }}
                  >
                    Cancelar
                  </button>

                  <button
                    type="submit"
                    className="btn-purple"
                    disabled={salvando}
                  >
                    {salvando ? "Adicionando..." : "Adicionar colaborador"}
                  </button>
                </div>
              </form>
            )}

            {carregandoMembros ? (
              <p>Carregando colaboradores...</p>
            ) : membros.length === 0 ? (
              <p>Nenhum colaborador encontrado nesta empresa.</p>
            ) : (
              <div className="membros-lista">
                {membros.map((membro) => {
                  const membroEhCriador = membro.papel === "CRIADOR";

                  return (
                    <div className="membro-item" key={membro.usuarioId}>
                      <div className="membro-avatar" aria-hidden="true">
                        {(membro.nome || "?").charAt(0).toUpperCase()}
                      </div>

                      <div className="membro-dados">
                        <strong>{membro.nome}</strong>
                        <span>{membro.email}</span>
                      </div>

                      <span
                        className={`membro-papel ${
                          membroEhCriador ? "membro-papel-criador" : ""
                        }`}
                      >
                        {membroEhCriador ? "Criador" : "Colaborador"}
                      </span>

                      {ehCriador && !membroEhCriador && (
                        <button
                          type="button"
                          className="btn-remover-membro"
                          title={`Remover ${membro.nome}`}
                          aria-label={`Remover ${membro.nome}`}
                          disabled={salvando}
                          onClick={() => removerMembro(membro.usuarioId)}
                        >
                          <Trash2 size={17} />
                        </button>
                      )}
                    </div>
                  );
                })}
              </div>
            )}
          </section>
        </>
      )}
    </div>
  );
}
