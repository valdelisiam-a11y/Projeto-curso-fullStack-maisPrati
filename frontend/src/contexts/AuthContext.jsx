
import { useEffect, useState } from 'react';
import { AuthContext } from './authContext';


function tokenExpirado(token) {
    if (!token) return true;

    try {
        const payload = token.split('.')[1];
        const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
        const dados = JSON.parse(atob(base64));

        if (typeof dados.exp !== 'number') return true;

        return Date.now() >= dados.exp * 1000;
    } catch {
        return true;
    }
}


function limparDadosSessao() {
    localStorage.removeItem('token');
    localStorage.removeItem('usuario');
}


export function AuthProvider({ children }) { 
    const [usuario, setUsuario] = useState(() => {
        const usuarioSalvo = localStorage.getItem('usuario');
    
        return usuarioSalvo ? JSON.parse(usuarioSalvo) : null;
    });

    const [token, setToken] = useState(() => {
        const tokenSalvo = localStorage.getItem('token');

        if (tokenExpirado(tokenSalvo)) {
            limparDadosSessao();
            return null;
        }

        return tokenSalvo;
    });

    const autenticado = Boolean(token) && !tokenExpirado(token);

    function login(dados) {
        localStorage.setItem('token', dados.token);

        const dadosUsuario = {
            id: dados.id,
            nome: dados.nome,
            email: dados.email,
        };

        localStorage.setItem('usuario', JSON.stringify(dadosUsuario));

        setToken(dados.token);
        setUsuario(dadosUsuario);
    }

    function logout() {
    limparDadosSessao();

    setToken(null);
    setUsuario(null);
    }

    useEffect(() => {
        function encerrarSessao() {
            limparDadosSessao();
            setToken(null);
            setUsuario(null);
        }

        window.addEventListener('sessao-expirada', encerrarSessao);

        return () => {
            window.removeEventListener('sessao-expirada', encerrarSessao);
        };
    }, []);

    return (
        <AuthContext.Provider
            value={{    
                usuario,
                token,
                autenticado,
                login,
                logout,
            }}
        >
            {children}
        </AuthContext.Provider>
    );
}


