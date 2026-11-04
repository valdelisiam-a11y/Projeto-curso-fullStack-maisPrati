import "./App.css";
import { BrowserRouter, Routes, Route, Outlet, Navigate  } from "react-router-dom";
import { AuthProvider } from "./contexts/AuthContext.jsx";
import RotaProtegida from "./components/RotaProtegida.jsx";

import Login from "./pages/Login";
import Cadastro from "./pages/Cadastro";
import Inicio from "./pages/Inicio";
import MeusTrabalhos from "./pages/MeusTrabalhos";
import MinhasEtapas from "./pages/MinhasEtapas";
import DetalhesEtapa from "./pages/DetalhesEtapa";
import NovoTrabalho from "./pages/NovoTrabalho";
import Documentos from "./pages/Documentos";
import Configuracoes from "./pages/Configuracoes";
import Sidebar from "./components/sidebar/Sidebar";
import PaginaNaoEncontrada from "./pages/PaginaNaoEncontrada";
import { ContextoTrabalhoProvider, useContextoTrabalho,} from "./contexts/ContextoTrabalho";
import GerenciarEmpresas from "./pages/GerenciarEmpresas";
import DetalhesTrabalho from "./pages/DetalhesTrabalho";

function Layout() {
  return (
    <div className="layout">
      <Sidebar />

      <main className="layout__content">
        <Outlet />
      </main>
    </div>
  );
}

function RotaSomenteEmpresa() {
  const { contexto } = useContextoTrabalho();

  if (contexto !== "empresa") {
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
}

function App() {
  return (
    <AuthProvider>
      <ContextoTrabalhoProvider>
        <BrowserRouter>
          <Routes>
            {/* Rotas públicas */}
            <Route path="/login" element={<Login />} />
            <Route path="/cadastro" element={<Cadastro />} />

            {/* Rotas protegidas */}
            <Route element={<RotaProtegida />}>
              <Route element={<Layout />}>
                <Route path="/" element={<Inicio />} />
                <Route path="/trabalhos" element={<MeusTrabalhos />} />
                <Route element={<RotaSomenteEmpresa />}>
                  <Route path="/minhas-etapas" element={<MinhasEtapas />} />
                </Route>
                <Route path="/etapas/:id" element={<DetalhesEtapa />} />
                <Route path="/trabalhos/novo" element={<NovoTrabalho />} />
                <Route path="/trabalhos/:id" element={<DetalhesTrabalho />} />
                <Route path="/documentos" element={<Documentos />} />
                <Route path="/configuracoes" element={<Configuracoes />} />
                <Route path="/empresas" element={<GerenciarEmpresas />} />
              </Route>
            </Route>

            {/* Página não encontrada */}
            <Route path="*" element={<PaginaNaoEncontrada />} />
          </Routes>
        </BrowserRouter>
      </ContextoTrabalhoProvider>
    </AuthProvider>
  );
}

export default App;
