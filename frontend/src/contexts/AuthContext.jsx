
import { useState } from 'react';
import { AuthContext } from './authContext';

export function AuthProvider({ children }) { 
    const [usuario, setUsuario] = useState(() => {
        const usuarioSalvo = localStorage.getItem('usuario');
    
        return usuarioSalvo ? JSON.parse(usuarioSalvo) : null;
    });

    const [token, setToken] = useState(() => {
        return localStorage.getItem('token');
    });

    const autenticado = Boolean(token);

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
        localStorage.removeItem('token');
        localStorage.removeItem('usuario');

        setToken(null);
        setUsuario(null);
    }

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


