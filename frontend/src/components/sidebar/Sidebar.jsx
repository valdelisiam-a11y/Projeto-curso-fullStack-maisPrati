import "./Sidebar.css";
import { NavLink } from "react-router-dom";
import {
  Home,
  FolderKanban,
  FileText,
  Settings,
  User,
  LogOut,
} from "lucide-react";

function Sidebar() {
  return (
    <aside className="sidebar">
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
              <NavLink to="/">
                <Home size={20} />
                <span>Início</span>
              </NavLink>
            </li>

            <li>
              <NavLink to="/trabalhos">
                <FolderKanban size={20} />
                <span>Meus trabalhos</span>
              </NavLink>
            </li>

            <li>
              <NavLink to="/documentos">
                <FileText size={20} />
                <span>Documentos</span>
              </NavLink>
            </li>

            <li>
              <NavLink to="/configuracoes">
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
                <span>Maria</span>
            </div>
        </div>    

       <NavLink to="/login" className="sidebar__logout">
            <LogOut size={20} />
            <span>Sair</span>
       </NavLink>

    </div>
    </aside>
  );
}

export default Sidebar;