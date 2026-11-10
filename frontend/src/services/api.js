
const BASE_URL = '/api';

export async function apiFetch(endpoint, options = {}) {
    const token = localStorage.getItem('token');

    const headers = new Headers(options.headers);

    if (token) {
        headers.set('Authorization', `Bearer ${token}`);
    }

    const resposta = await fetch(`${BASE_URL}${endpoint}`, {
        ...options,
        headers,
    });

    return resposta;
}
