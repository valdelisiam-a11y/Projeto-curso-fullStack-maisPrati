
const BASE_URL = 'http://localhost:8080';

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
