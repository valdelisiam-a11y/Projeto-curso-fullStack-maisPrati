import { useState } from "react";
import { NavLink } from "react-router-dom";
import {
  Home,
  FolderKanban,
  ListChecks,
  FileText,
  Settings,
  User,
  Building2,
  LogOut,
  Menu,
  X,
  ChevronDown,
  Check,
} from "lucide-react";
import "./Sidebar.css";
import { useAuth } from "../../contexts/useAuth";
import { useContextoTrabalho } from "../../contexts/ContextoTrabalho";

function Sidebar() {
  const [menuAberto, setMenuAberto] = useState(false);
  const { usuario, logout } = useAuth();
  const {
    contexto,
    selecionarContexto,
    empresas,
    empresaSelecionada,
    selecionarEmpresa,
    carregandoEmpresas,
    minhasEtapas,
    carregandoEtapas,
  } = useContextoTrabalho();
  const [menuEmpresasAberto, setMenuEmpresasAberto] = useState(false);
  const [avisoEmpresa, setAvisoEmpresa] = useState(false);

function handleSelecionarEmpresa() {
  if (carregandoEmpresas) return;

  if (empresas.length === 0) {
    setAvisoEmpresa(true);
    return;
  }

  setAvisoEmpresa(false);
  selecionarContexto("empresa");
}

  function fecharMenu() {
    setMenuAberto(false);
    setAvisoEmpresa(false);
  }

  return (
    <>
      <button
        type="button"
        className={`sidebar__toggle ${menuAberto ? "sidebar__toggle--oculto" : ""}`}
        onClick={() => setMenuAberto(true)}
        aria-label="Abrir menu de navegação"
        aria-expanded={menuAberto}
        aria-controls="menu-lateral"
      >
        <Menu size={24} />
      </button>

      {menuAberto && (
        <button
          type="button"
          className="sidebar__overlay"
          onClick={fecharMenu}
          aria-label="Fechar menu de navegação"
        />
      )}

      <aside
        id="menu-lateral"
        className={`sidebar ${menuAberto ? "sidebar--aberta" : ""}`}
      >
        <button
          type="button"
          className="sidebar__fechar"
          onClick={fecharMenu}
          aria-label="Fechar menu"
        >
          <X size={22} />
        </button>

        <div>
          <div className="sidebar__logo">
            <div className="sidebar__logo-icon">
              <FolderKanban size={20} />
            </div>
            <h1>DocFlow</h1>
          </div>

          <div className="sidebar__contexto">
            <button
              type="button"
              className={contexto === "pessoal" ? "ativo" : ""}
              onClick={() => selecionarContexto("pessoal")}
              aria-pressed={contexto === "pessoal"}
            >
              <User size={16} />
              <span>Pessoal</span>
            </button>

            <button
              type="button"
              className={contexto === "empresa" ? "ativo" : ""}
              onClick={handleSelecionarEmpresa}
              aria-pressed={contexto === "empresa"}
            >
              <Building2 size={16} />
              <span>Empresa</span>
            </button>
          </div>

          {avisoEmpresa && (
            <div className="sidebar__empresa-aviso" role="status">
              <button
                type="button"
                className="sidebar__empresa-aviso-fechar"
                onClick={() => setAvisoEmpresa(false)}
                aria-label="Fechar aviso"
              >
                <X size={16} />
              </button>

              <strong>Você ainda não possui uma empresa.</strong>

              <p>
                Para utilizar o modo Empresa, cadastre uma empresa em Configurações.
              </p>

              <NavLink
                to="/configuracoes#empresas"
                onClick={() => {
                  setAvisoEmpresa(false);
                  fecharMenu();
                }}
              >
                Ir para Configurações
              </NavLink>
            </div>
          )}

          {contexto === "empresa" && (
            <div className="sidebar__empresa">
              <button
                type="button"
                className="sidebar__empresa-seletor"
                aria-label="Empresa selecionada"
                aria-expanded={menuEmpresasAberto}
                onClick={() => setMenuEmpresasAberto((aberto) => !aberto)}
              >
                <span>{empresaSelecionada?.nome ?? "Selecionar empresa"}</span>
                <ChevronDown size={16} />
              </button>

              {menuEmpresasAberto && (
                <div className="sidebar__empresa-menu">
                  {empresas.map((empresa) => (
                    <button
                      key={empresa.id}
                      type="button"
                      className={`sidebar__empresa-opcao ${
                        empresaSelecionada?.id === empresa.id ? "ativo" : ""
                      }`}
                      onClick={() => {
                        selecionarEmpresa(empresa.id);
                        setMenuEmpresasAberto(false);
                      }}
                    >
                      <span>{empresa.nome}</span>
                      {empresaSelecionada?.id === empresa.id && (
                        <Check size={16} />
                      )}
                    </button>
                  ))}
                </div>
              )}
            </div>
          )}

          <nav className="sidebar__nav">
            <ul>
              <li>
                <NavLink to="/" onClick={fecharMenu}>
                  <Home size={20} />
                  <span>Início</span>
                </NavLink>
              </li>

              <li>
                <NavLink to="/trabalhos" onClick={fecharMenu}>
                  <FolderKanban size={20} />
                  <span>Meus trabalhos</span>
                </NavLink>
              </li>

              {contexto === "empresa" &&
                !carregandoEtapas &&
                minhasEtapas.length > 0 && (
                  <li>
                    <NavLink to="/minhas-etapas" onClick={fecharMenu}>
                      <ListChecks size={20} />
                      <span>Minhas etapas</span>
                    </NavLink>
                  </li>
              )}

              <li>
                <NavLink to="/documentos" onClick={fecharMenu}>
                  <FileText size={20} />
                  <span>Documentos</span>
                </NavLink>
              </li>

              <li>
                <NavLink to="/configuracoes" onClick={fecharMenu}>
                  <Settings size={20} />
                  <span>Configurações</span>
                </NavLink>
              </li>
            </ul>
          </nav>
        </div>

        <div className="sidebar__user">
          <div className="sidebar__user-info">
            <div className="sidebar__user-icon">
              <User size={16} />
            </div>

            <div className="sidebar__user-text">
              <span>Perfil</span>
              <span>{usuario?.nome || "Usuário"}</span>
            </div>
          </div>

          <button
            type="button"
            className="sidebar__logout"
            onClick={() => {
              logout();
              fecharMenu();
            }}
          >
            <LogOut size={20} />
            <span>Sair</span>
          </button>
        </div>
      </aside>
    </>
  );
}

export default Sidebar;
