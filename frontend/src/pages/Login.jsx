
import './Login.css';
import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/useAuth';
import { fazerLogin } from '../services/authService';
import ilustracaoLogin from '../assets/ilustracao-organizacao-Inicial.png';
import { FolderKanban } from 'lucide-react';
import { LogIn } from 'lucide-react';

function Login() {
    const [email, setEmail] = useState('')
    const [senha, setSenha] = useState('')
    const [erro, setErro] = useState('');

    const { login } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();
    const mensagemSucesso = location.state?.mensagem || '';



const handleSubmit = async (e) => {
    e.preventDefault();
    setErro('');

    try {
        const resposta = await fazerLogin(email, senha);

        if (!resposta.ok) {
            throw new Error('E-mail ou senha inválidos.');
        }

        const dados = await resposta.json();

        if (!dados.token) {
            throw new Error('O servidor não retornou um token.');
        }

        login(dados);

        navigate('/');


    } catch (error) {
        console.error('Erro ao realizar login:', error);

        if (error instanceof TypeError) {
            setErro('Não foi possível conectar ao servidor. Tente novamente.');
        } else {
            setErro(error.message || 'Ocorreu um erro inesperado.');
        }
    }


};


    return (
        <main className="login">

        <section className="login-image"> {/* Lado esquerdo da tela */}
            <img
                src={ilustracaoLogin}
                alt="Ilustração de uma pessoa organizando documentos"
                className="login-illustration"
            />

            <p className="login-message">Organize seus trabalhos. Saiba o que falta.
                                        <br />Encontre seus documentos. Conclua.
            </p>
        </section>

        <section className="login-form"> {/* Lado direito da tela */}
            <div className="login-logo">
                <span className="login-logo-icon">
                    <FolderKanban size={20} />
                </span>
                <span>DocFlow</span>
            </div>

            <h1>Organize seus trabalhos de forma simples.</h1>

            <form onSubmit={handleSubmit}> {/* Formulário de login */}
                <div className="form-group">
                    <label htmlFor="email">E-mail</label>
                    <input
                        type="email"
                        id="email" placeholder="voce@exemplo.com.br"
                        onChange={(e) => setEmail(e.target.value)}
                    />
                </div>

                <div className="form-group">
                    <label htmlFor="password">Senha</label>
                    <input
                        type="password"
                        id="password"
                        placeholder="Sua senha"
                        onChange={(e) => setSenha(e.target.value)}
                    />
                </div>

                <div className="login-error-container">
                    {mensagemSucesso && (
                        <p className="login-success" role="status">
                            {mensagemSucesso}
                        </p>
                    )}

                    {erro && (
                        <p className="login-error" role="alert">
                            {erro}
                        </p>
                    )}
                </div>


                <button className="login-button" type="submit">
                    <LogIn size={18} aria-hidden="true" />
                    <span>Entrar</span>
                </button>

                <a href="#">Esqueci minha senha</a>

                <Link to="/cadastro">Criar minha conta</Link>
            </form>
        </section>
    </main>
    );
}

export default Login;
