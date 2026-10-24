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

export default function Configuracoes() {
  const [profileData, setProfileData] = useState({
    nome: "Maria",
    email: "maria@exemplo.com.br",
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

  useEffect(() => {
    if (preferences.useDarkMode) {
      document.body.classList.add("dark-theme");
      localStorage.setItem("docflow_dark_mode", "true");
    } else {
      document.body.classList.remove("dark-theme");
      localStorage.setItem("docflow_dark_mode", "false");
    }
  }, [preferences.useDarkMode]);

  const handleSaveProfile = (e) => {
    e.preventDefault();
    setFeedback("Alterações salvas com sucesso!");
    setTimeout(() => setFeedback(""), 3000);
  };

  const handleChangePassword = (e) => {
    e.preventDefault();
    setFeedback("Senha alterada com sucesso!");
    setTimeout(() => setFeedback(""), 3000);
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
            <h3>DocFlow Tecnologia</h3>
            <p>Perfil: Criador</p>
            <p>3 de 5 usuários</p>
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
      <div className="config-card">
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
