# API DocFlow

## 1. Informações gerais

**Base URL local:**

```text
http://localhost:8080
```

A API utiliza HTTP e retorna os dados em formato JSON.

---

## 2. Cadastro de usuário

### POST `/usuarios`

Cria um novo usuário.

**Autenticação:** não é necessária.

### Requisição

```json
{
  "nome": "Maria Silva",
  "email": "maria@email.com",
  "senha": "123456"
}
```

### Resposta

Retorna os dados básicos do usuário cadastrado.

```json
{
  "id": 1,
  "nome": "Maria Silva",
  "email": "maria@email.com"
}
```

A senha não é retornada na resposta.

---

### Erros

**409 Conflict**

O e-mail informado já está cadastrado.

Exemplo:

```text
HTTP 409 Conflict
```


## 3. Login

### POST `/auth/login`

Realiza a autenticação do usuário.

**Autenticação:** não é necessária.

### Requisição

```json
{
  "email": "maria@email.com",
  "senha": "123456"
}
```

### Resposta

```json
{
  "id": 1,
  "nome": "Maria Silva",
  "email": "maria@email.com",
  "token": "eyJhbGciOi..."
}
```

O `token` retornado deve ser utilizado nas requisições que exigem autenticação.

### Header de autenticação

Nas requisições protegidas, enviar:

```text
Authorization: Bearer {token}
```

---

## 4. Autenticação das requisições

As rotas protegidas exigem um token JWT válido.

Exemplo:

```text
Authorization: Bearer eyJhbGciOi...
```

O token é gerado no login e possui validade de 24 horas.

---

## 5. Endpoints públicos

| Método | Endpoint      | Autenticação |
| ------ | ------------- | ------------ |
| POST   | `/usuarios`   | Não          |
| POST   | `/auth/login` | Não          |

---

## 6. Endpoints protegidos

As demais rotas da API exigem autenticação.

Novos endpoints serão adicionados a esta documentação conforme forem implementados no Back.
