import React, { useState, useEffect } from "react";
import "./Configuracoes.css";
import {
  User,
  Palette,
  Bell,
  Lock,
  Check,
  Building2,
  ArrowRight,
} from "lucide-react";
import { alterarSenha, atualizarPerfil } from "../services/authService";
import { useAuth } from "../contexts/useAuth";
import {
  listarMinhasEmpresas,
  listarMembrosEmpresa,
} from "../services/empresaService";

export default function Configuracoes() {
  const { usuario, atualizarSessao } = useAuth();
  const [profileData, setProfileData] = useState({
    nome: usuario?.nome || "",
    email: usuario?.email || "",
  });

  const [preferences, setPreferences] = useState(() => {
    const savedTheme = localStorage.getItem("docflow_dark_mode");
    return {
      alertAttentionStep: true,
      useDarkMode: savedTheme === "true",
    };
  });

  const [securityData, setSecurityData] = useState({
    senhaAtual: "",
    novaSenha: "",
  });

  const [feedback, setFeedback] = useState("");

  const [empresaRecente, setEmpresaRecente] = useState(null);
  const [quantidadeMembros, setQuantidadeMembros] = useState(0);
  const [carregandoEmpresa, setCarregandoEmpresa] = useState(true);
  const [erroEmpresa, setErroEmpresa] = useState("");

  useEffect(() => {
    if (preferences.useDarkMode) {
      document.body.classList.add("dark-theme");
      localStorage.setItem("docflow_dark_mode", "true");
    } else {
      document.body.classList.remove("dark-theme");
      localStorage.setItem("docflow_dark_mode", "false");
    }
  }, [preferences.useDarkMode]);

  useEffect(() => {
    if (window.location.hash === "#empresas") {
      requestAnimationFrame(() => {
        document.getElementById("empresas")?.scrollIntoView({
          behavior: "smooth",
          block: "start",
        });
      });
    }
  }, []);

  useEffect(() => {
    let ativo = true;

    async function carregarEmpresaRecente() {
      setCarregandoEmpresa(true);
      setErroEmpresa("");

      try {
        const empresas = await listarMinhasEmpresas();

        if (!ativo) return;

        if (empresas.length === 0) {
          setEmpresaRecente(null);
          setQuantidadeMembros(0);
          return;
        }

        const empresa = empresas[0];
        const membros = await listarMembrosEmpresa(empresa.id);

        if (!ativo) return;

        setEmpresaRecente(empresa);
        setQuantidadeMembros(membros.length);
      } catch (erro) {
        if (!ativo) return;

        setErroEmpresa(
          "Não foi possível carregar os dados da empresa."
        );
      } finally {
        if (ativo) {
          setCarregandoEmpresa(false);
        }
      }
    }

    carregarEmpresaRecente();

    return () => {
      ativo = false;
    };
  }, []);

  const handleSaveProfile = async (e) => {
    e.preventDefault();
    setFeedback("");

    try {
      const resposta = await atualizarPerfil(
        profileData.nome,
        profileData.email
      );

      if (!resposta.ok) {
        const erro = await resposta.json().catch(() => null);

        setFeedback(
          erro?.message ||
          erro?.mensagem ||
          "Não foi possível atualizar o perfil."
        );
        return;
      }

      const dados = await resposta.json();

      atualizarSessao(dados);

      setProfileData({
        nome: dados.nome,
        email: dados.email,
      });

      setFeedback("Perfil atualizado com sucesso!");
      setTimeout(() => setFeedback(""), 3000);
    } catch {
      setFeedback("Não foi possível conectar ao servidor.");
    }
  };

  
const handleChangePassword = async (e) => {
  e.preventDefault();
  setFeedback("");

  try {
    const resposta = await alterarSenha(
      securityData.senhaAtual,
      securityData.novaSenha
    );

    if (!resposta.ok) {
      const erro = await resposta.json().catch(() => null);

      setFeedback(
        erro?.message ||
        erro?.mensagem ||
        "Não foi possível alterar a senha. Verifique a senha atual."
      );
      return;
    }

    setFeedback("Senha alterada com sucesso!");
    setSecurityData({ senhaAtual: "", novaSenha: "" });
  } catch {
    setFeedback("Não foi possível conectar ao servidor.");
  }
};


  return (
    <div className="configuracoes-container">
      <div className="configuracoes-main-header">
        <h1>Configurações</h1>
        <p>Seus dados e preferências, do jeito simples.</p>
      </div>

      {feedback && <div className="feedback-alert">{feedback}</div>}

      {/* CARD 1: MEU PERFIL */}
      <div className="config-card">
        <div className="config-card-title">
          <User className="icon-title" />
          <span>Meu perfil</span>
        </div>

        <form onSubmit={handleSaveProfile}>
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="nome">Nome</label>
              <input
                id="nome"
                type="text"
                value={profileData.nome}
                onChange={(e) =>
                  setProfileData({ ...profileData, nome: e.target.value })
                }
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="email">E-mail</label>
              <input
                id="email"
                type="email"
                value={profileData.email}
                onChange={(e) =>
                  setProfileData({ ...profileData, email: e.target.value })
                }
                required
              />
            </div>
          </div>

          <div className="form-actions">
            <button type="submit" className="btn-purple">
              <Check className="w-4 h-4" />
              <span>Salvar alterações</span>
            </button>
          </div>
        </form>
      </div>

      {/* CARD 2: PREFERÊNCIAS */}
      <div className="config-card">
        <div className="config-card-title">
          <Palette className="icon-title" />
          <span>Preferências</span>
        </div>

        <div className="preferences-list">
          <div className="preference-item">
            <div className="preference-label-wrapper">
              <Bell className="icon-title preference-inline-icon" />
              <span className="preference-label">
                Avisar quando uma etapa precisar de atenção
              </span>
            </div>
            <label className="switch">
              <input
                type="checkbox"
                checked={preferences.alertAttentionStep}
                onChange={(e) =>
                  setPreferences({
                    ...preferences,
                    alertAttentionStep: e.target.checked,
                  })
                }
              />
              <span className="slider"></span>
            </label>
          </div>

          <div className="preference-item">
            <span className="preference-label">Usar tema escuro</span>
            <label className="switch">
              <input
                type="checkbox"
                checked={preferences.useDarkMode}
                onChange={(e) =>
                  setPreferences({
                    ...preferences,
                    useDarkMode: e.target.checked,
                  })
                }
              />
              <span className="slider"></span>
            </label>
          </div>
        </div>
      </div>

      {/* CARD 3: EMPRESA */}
      <div className="config-card">
        <div className="config-card-title">
          <Building2 className="icon-title" />
          <span>Empresa</span>
        </div>

        <div className="empresa-resumo">
          <div>
            {carregandoEmpresa ? (
              <p>Carregando dados da empresa...</p>
            ) : erroEmpresa ? (
              <p>{erroEmpresa}</p>
            ) : empresaRecente ? (
              <>
                <h3>{empresaRecente.nome}</h3>
                <p>
                  Perfil:{" "}
                  {empresaRecente.papel === "CRIADOR"
                    ? "Criador"
                    : "Colaborador"}
                </p>
                <p>{quantidadeMembros} de 5 usuários</p>
              </>
            ) : (
              <>
                <h3>Nenhuma empresa cadastrada</h3>
                <p>Você ainda não está vinculado a uma empresa.</p>
              </>
            )}
          </div>

          <button
            type="button"
            className="btn-purple"
            onClick={() => {
              window.location.href = "/empresas";
            }}
          >
            <span>Gerenciar empresas</span>
            <ArrowRight className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* CARD 3: SEGURANÇA */}
      <div className="config-card" id="empresas">
        <div className="config-card-title">
          <Lock className="icon-title" />
          <span>Segurança</span>
        </div>

        <form onSubmit={handleChangePassword}>
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="senhaAtual">Senha atual</label>
              <input
                id="senhaAtual"
                type="password"
                required
                value={securityData.senhaAtual}
                onChange={(e) =>
                  setSecurityData({
                    ...securityData,
                    senhaAtual: e.target.value,
                  })
                }
              />
            </div>

            <div className="form-group">
              <label htmlFor="novaSenha">Nova senha</label>
              <input
                id="novaSenha"
                type="password"
                required
                minLength={8}
                value={securityData.novaSenha}
                onChange={(e) =>
                  setSecurityData({
                    ...securityData,
                    novaSenha: e.target.value,
                  })
                }
              />
            </div>
          </div>

          <div className="form-actions">
            <button type="submit" className="btn-purple-light">
              <span>Alterar senha</span>
            </button>
          </div>
        </form>
      </div>

    </div>
  );
}
