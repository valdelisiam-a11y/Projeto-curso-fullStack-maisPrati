import './Login.css'

function Login() {
    return (
        <main className="login">

        <section className="login-image"> {/* Lado esquerdo da tela */}
            <div className="image-placeholder">Imagem</div> {/* Espaço reservado para a imagem */}
            <p className="login-message">Organize seus trabalhos. Saiba o que falta.
                                        <br />Encontre seus documentos. Conclua.
            </p>
        </section>

        <section className="login-form"> {/* Lado direito da tela */}
            <div className="logo">DocFlow</div>

            <h1>Organize seus trabalhos de forma simples.</h1>

            <form> {/* Formulário de login */}
                <div className="form-group">
                    <label htmlFor="email">E-mail</label>
                    <input type="email" id="email" placeholder="voce@exemplo.com.br"                       
                    />
                </div>

                <div className="form-group">
                    <label htmlFor="password">Senha</label>
                    <input type="password" id="password" placeholder="Sua senha" 
                    />
                </div>

                <button className="login-button" type="submit"> Entrar</button> {/* Botão de envio */}

                {/* Links de navegação */}
                {/*
                Para posterior implementação de outras páginas, rotas ou outra solução
               */}
                <a href="#">Esqueci minha senha</a>

                <a href="#">Criar minha conta</a>
            </form>
        </section>
    </main>
    );
}

export default Login;
