
const API_ORIGIN = (import.meta.env.VITE_API_URL || '').replace(/\/$/, '');

const BASE_URL = import.meta.env.DEV
    ? '/api'
    : (API_ORIGIN || '/api');

export async function apiFetch(endpoint, options = {}) {
    const token = localStorage.getItem('token');

    const headers = new Headers(options.headers);

    if (token) {
        headers.set('Authorization', `Bearer ${token}`);
    }

    const url = endpoint.startsWith('/api/')
        ? `${API_ORIGIN}${endpoint}`
        : `${BASE_URL}${endpoint}`;

    const resposta = await fetch(url, {
        ...options,
        headers,
    });

    return resposta;
}
