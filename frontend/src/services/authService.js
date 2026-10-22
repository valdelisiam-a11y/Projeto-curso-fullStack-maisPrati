
import { apiFetch } from './api';

export async function fazerLogin(email, senha) {
    const resposta = await apiFetch('/auth/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email, senha }),
    });

    return resposta;
}


export async function fazerCadastro(nome, email, senha) {
    const resposta = await apiFetch('/usuarios', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ nome, email, senha }),
    });

    return resposta;
}
