import "./App.css";
import { BrowserRouter, Routes, Route, Outlet } from "react-router-dom";
import { AuthProvider } from './contexts/AuthContext.jsx';

import Login from "./pages/Login";
import Cadastro from "./pages/Cadastro";
import Inicio from "./pages/Inicio";
import MeusTrabalhos from "./pages/MeusTrabalhos";
import Documentos from "./pages/Documentos";
import Configuracoes from "./pages/Configuracoes";
import Sidebar from "./components/sidebar/Sidebar";
import DetalhesTrabalho from "./pages/DetalhesTrabalho";
import PaginaNaoEncontrada from "./pages/PaginaNaoEncontrada";

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

function App() {
  return (
      <AuthProvider>
        <BrowserRouter>
          <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/cadastro" element={<Cadastro />} />

          <Route element={<Layout />}>
            <Route path="/" element={<Inicio />} />
            <Route path="/trabalhos" element={<MeusTrabalhos />} />
            <Route path="/trabalhos/:id" element={<DetalhesTrabalho />} />
            <Route path="/documentos" element={<Documentos />} />
            <Route path="/configuracoes" element={<Configuracoes />} />
            
          </Route>
          <Route path="*" element={<PaginaNaoEncontrada />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;