
import { useState } from "react";
import { NavLink } from "react-router-dom";
import {
  Home,
  FolderKanban,
  FileText,
  Settings,
  User,
  LogOut,
  Menu,
  X,
} from "lucide-react";
import "./Sidebar.css";
import { useAuth } from "../../contexts/useAuth";

function Sidebar() {
  const [menuAberto, setMenuAberto] = useState(false);
    const { usuario, logout } = useAuth();

  function fecharMenu() {
    setMenuAberto(false);
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
