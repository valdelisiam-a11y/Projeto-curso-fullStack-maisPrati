
import { useState } from "react";
import {
  ArrowLeft,
  Building2,
  Plus,
  Users,
  UserPlus,
  Trash2,
  Crown,
  ChevronDown,
  Check,
} from "lucide-react";
import { useNavigate } from "react-router-dom";
import "./GerenciarEmpresas.css";

const empresasIniciais = [
  {
    id: 1,
    nome: "DocFlow Tecnologia",
    papel: "Criador",
    membros: [
      {
        id: 1,
        nome: "Maria",
        email: "maria@exemplo.com.br",
        papel: "Criador",
      },
      {
        id: 2,
        nome: "João",
        email: "joao@exemplo.com.br",
        papel: "Colaborador",
      },
      {
        id: 3,
        nome: "Ana",
        email: "ana@exemplo.com.br",
        papel: "Colaborador",
      },
    ],
  },
  {
    id: 2,
    nome: "Empresa de Consultoria",
    papel: "Colaborador",
    membros: [
      {
        id: 4,
        nome: "Carlos",
        email: "carlos@exemplo.com.br",
        papel: "Criador",
      },
      {
        id: 5,
        nome: "Maria",
        email: "maria@exemplo.com.br",
        papel: "Colaborador",
      },
    ],
  },
];

export default function GerenciarEmpresas() {
  const navigate = useNavigate();

  const [empresas, setEmpresas] = useState(empresasIniciais);
  const [empresaSelecionadaId, setEmpresaSelecionadaId] = useState(1);

  const [novoNomeEmpresa, setNovoNomeEmpresa] = useState("");
  const [novoNomeMembro, setNovoNomeMembro] = useState("");
  const [novoEmailMembro, setNovoEmailMembro] = useState("");
  const [colaboradorSelecionadoEmail, setColaboradorSelecionadoEmail] =
    useState("");

  const [mostrarFormularioEmpresa, setMostrarFormularioEmpresa] =
    useState(false);
  const [mostrarFormularioMembro, setMostrarFormularioMembro] =
    useState(false);

  const [feedback, setFeedback] = useState("");

  const empresaSelecionada = empresas.find(
    (empresa) => empresa.id === empresaSelecionadaId
  );

  const ehCriador = empresaSelecionada?.papel === "Criador";

  const limiteAtingido =
    (empresaSelecionada?.membros.length ?? 0) >= 5;

  const [menuColaboradoresAberto, setMenuColaboradoresAberto] = useState(false);

  // Reúne os usuários conhecidos, sem duplicar e-mails.
  const usuariosCadastrados = [
    ...new Map(
      empresas
        .flatMap((empresa) => empresa.membros)
        .map((usuario) => [
          usuario.email.toLowerCase(),
          {
            nome: usuario.nome,
            email: usuario.email,
          },
        ])
    ).values(),
  ];

  // Sugere somente usuários que ainda não participam da empresa selecionada.
  const colaboradoresDisponiveis = usuariosCadastrados.filter(
    (usuario) =>
      !empresaSelecionada?.membros.some(
        (membro) =>
          membro.email.toLowerCase() === usuario.email.toLowerCase()
      )
  );

  function atualizarEmpresaSelecionada(atualizacao) {
    setEmpresas((empresasAtuais) =>
      empresasAtuais.map((empresa) =>
        empresa.id === empresaSelecionadaId
          ? { ...empresa, ...atualizacao }
          : empresa
      )
    );
  }

  function limparFormularioMembro() {
    setNovoNomeMembro("");
    setNovoEmailMembro("");
    setColaboradorSelecionadoEmail("");
  }

  function criarEmpresa(event) {
    event.preventDefault();

    const nome = novoNomeEmpresa.trim();

    if (!nome) return;

    const id = Date.now();

    const novaEmpresa = {
      id,
      nome,
      papel: "Criador",
      membros: [
        {
          id: id + 1,
          nome: "Maria",
          email: "maria@exemplo.com.br",
          papel: "Criador",
        },
      ],
    };

    setEmpresas((atuais) => [...atuais, novaEmpresa]);
    setEmpresaSelecionadaId(id);
    setNovoNomeEmpresa("");
    setMostrarFormularioEmpresa(false);
    setMostrarFormularioMembro(false);
    setFeedback("Empresa criada na demonstração.");
  }

  function selecionarColaborador(email) {
    setColaboradorSelecionadoEmail(email);

    const colaborador = colaboradoresDisponiveis.find(
      (usuario) => usuario.email === email
    );

    if (colaborador) {
      setNovoNomeMembro(colaborador.nome);
      setNovoEmailMembro(colaborador.email);
    } else {
      setNovoNomeMembro("");
      setNovoEmailMembro("");
    }
  }

  function adicionarMembro(event) {
    event.preventDefault();

    if (!empresaSelecionada || !ehCriador || limiteAtingido) return;

    const nome = novoNomeMembro.trim();
    const email = novoEmailMembro.trim();

    if (!nome || !email) return;

    const emailJaExiste = empresaSelecionada.membros.some(
      (membro) => membro.email.toLowerCase() === email.toLowerCase()
    );

    if (emailJaExiste) {
      setFeedback("Este e-mail já está cadastrado nesta empresa.");
      return;
    }

    atualizarEmpresaSelecionada({
      membros: [
        ...empresaSelecionada.membros,
        {
          id: Date.now(),
          nome,
          email,
          papel: "Colaborador",
        },
      ],
    });

    limparFormularioMembro();
    setMostrarFormularioMembro(false);
    setFeedback("Colaborador adicionado na demonstração.");
  }

  function removerMembro(membroId) {
    if (!empresaSelecionada || !ehCriador) return;

    const membro = empresaSelecionada.membros.find(
      (item) => item.id === membroId
    );

    if (!membro || membro.papel === "Criador") return;

    atualizarEmpresaSelecionada({
      membros: empresaSelecionada.membros.filter(
        (item) => item.id !== membroId
      ),
    });

    setFeedback("Colaborador removido na demonstração.");
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
              onChange={(event) => {
                setEmpresaSelecionadaId(Number(event.target.value));
                setFeedback("");
                setMostrarFormularioMembro(false);
                limparFormularioMembro();
              }}
            >
              {empresas.map((empresa) => (
                <option key={empresa.id} value={empresa.id}>
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
                maxLength={100}
                required
              />
            </div>

            <div className="empresa-formulario-acoes">
              <button
                type="button"
                className="btn-cancelar"
                onClick={() => setMostrarFormularioEmpresa(false)}
              >
                Cancelar
              </button>

              <button type="submit" className="btn-purple">
                Criar empresa
              </button>
            </div>
          </form>
        )}
      </section>

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

                  Você é {empresaSelecionada.papel.toLowerCase()}
                </span>
              </div>

              <div className="empresa-contagem">
                <strong>
                  {empresaSelecionada.membros.length} de 5
                </strong>
                <span>usuários</span>
              </div>
            </div>

            <div
              className="empresa-progresso"
              role="progressbar"
              aria-label="Ocupação de usuários"
              aria-valuenow={empresaSelecionada.membros.length}
              aria-valuemin={0}
              aria-valuemax={5}
            >
              <span
                style={{
                  width: `${
                    (empresaSelecionada.membros.length / 5) * 100
                  }%`,
                }}
              />
            </div>

            <p className="empresa-limite-info">
              {limiteAtingido
                ? "O limite de usuários foi atingido."
                : `Você pode adicionar mais ${
                    5 - empresaSelecionada.membros.length
                  } usuário(s).`}
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
                  disabled={limiteAtingido}
                  onClick={() => {
                    setMostrarFormularioMembro((visivel) => !visivel);
                    limparFormularioMembro();
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
                    <label>Selecionar colaborador cadastrado</label>
                    <div className="colaborador-seletor">
                        <button
                        type="button"
                        className="colaborador-seletor-botao"
                        aria-expanded={menuColaboradoresAberto}
                        onClick={() =>
                            setMenuColaboradoresAberto((aberto) => !aberto)
                        }
                        >
                        <span>
                            {colaboradorSelecionadoEmail
                            ? colaboradoresDisponiveis.find(
                                (colaborador) =>
                                    colaborador.email === colaboradorSelecionadoEmail
                                )?.nome ?? "Selecionar colaborador"
                            : "Selecionar colaborador"}
                        </span>

                        <ChevronDown size={16} />
                        </button>

                        {menuColaboradoresAberto && (
                        <div className="colaborador-seletor-menu">
                            {colaboradoresDisponiveis.length > 0 ? (
                            colaboradoresDisponiveis.map((colaborador) => {
                                const selecionado =
                                colaboradorSelecionadoEmail === colaborador.email;

                                return (
                                <button
                                    key={colaborador.email}
                                    type="button"
                                    className={`colaborador-seletor-opcao ${
                                    selecionado ? "ativo" : ""
                                    }`}
                                    onClick={() => {
                                    selecionarColaborador(colaborador.email);
                                    setMenuColaboradoresAberto(false);
                                    }}
                                >
                                    <span>
                                    <strong>{colaborador.nome}</strong>
                                    <small>{colaborador.email}</small>
                                    </span>

                                    {selecionado && <Check size={16} />}
                                </button>
                                );
                            })
                            ) : (
                            <p className="colaborador-seletor-vazio">
                                Não há colaboradores disponíveis.
                            </p>
                            )}
                        </div>
                        )}
                    </div>
                </div>

                <div className="form-row">
                  <div className="form-group">
                    <label htmlFor="novoNomeMembro">Nome</label>

                    <input
                      id="novoNomeMembro"
                      type="text"
                      value={novoNomeMembro}
                      onChange={(event) => {
                        setNovoNomeMembro(event.target.value);
                        setColaboradorSelecionadoEmail("");
                      }}
                      placeholder="Nome do colaborador"
                      maxLength={100}
                      required
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="novoEmailMembro">E-mail</label>

                    <input
                      id="novoEmailMembro"
                      type="email"
                      value={novoEmailMembro}
                      onChange={(event) => {
                        setNovoEmailMembro(event.target.value);
                        setColaboradorSelecionadoEmail("");
                      }}
                      placeholder="email@exemplo.com"
                      maxLength={150}
                      required
                    />
                  </div>
                </div>

                <div className="empresa-formulario-acoes">
                 <button
                    type="button"
                    className="btn-cancelar"
                    onClick={() => {
                        setMostrarFormularioMembro(false);
                        setMenuColaboradoresAberto(false);
                        limparFormularioMembro();
                    }}
                    >
                    Cancelar
                  </button>

                  <button type="submit" className="btn-purple">
                    Adicionar colaborador
                  </button>
                </div>
              </form>
            )}

            <div className="membros-lista">
              {empresaSelecionada.membros.map((membro) => (
                <div className="membro-item" key={membro.id}>
                  <div className="membro-avatar" aria-hidden="true">
                    {membro.nome.charAt(0).toUpperCase()}
                  </div>

                  <div className="membro-dados">
                    <strong>{membro.nome}</strong>
                    <span>{membro.email}</span>
                  </div>

                  <span
                    className={`membro-papel ${
                      membro.papel === "Criador"
                        ? "membro-papel-criador"
                        : ""
                    }`}
                  >
                    {membro.papel}
                  </span>

                  {ehCriador && membro.papel !== "Criador" && (
                    <button
                      type="button"
                      className="btn-remover-membro"
                      title={`Remover ${membro.nome}`}
                      aria-label={`Remover ${membro.nome}`}
                      onClick={() => removerMembro(membro.id)}
                    >
                      <Trash2 size={17} />
                    </button>
                  )}
                </div>
              ))}
            </div>
          </section>
        </>
      )}
    </div>
  );
}
