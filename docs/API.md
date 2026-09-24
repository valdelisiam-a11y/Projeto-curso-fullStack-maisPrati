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

As rotas que já possuem autenticação implementada exigem um token JWT válido. Novos endpoints serão protegidos conforme a implementação da autenticação e autorização do projeto


## 7. Trabalhos

A API permite criar, consultar, atualizar e excluir trabalhos, além de controlar seu status e registrar o histórico das alterações.

### 7.1 Cadastrar trabalho

### POST `/trabalhos`

Cria um novo trabalho.

**Autenticação:** atualmente não está configurada no endpoint. A identificação do usuário é informada na requisição. Com a implementação do JWT, essa identificação deverá ser obtida pelo usuário autenticado.

### Requisição

```json
{
  "titulo": "Organizar documentos do cliente",
  "descricao": "Separar e organizar os documentos necessários.",
  "empresaId": 1,
  "criadoPorId": 1
}
```

O trabalho é criado inicialmente com o status:

```text
PENDENTE
```

### Resposta

**HTTP 201 Created**

```json
{
  "id": 1,
  "titulo": "Organizar documentos do cliente",
  "descricao": "Separar e organizar os documentos necessários.",
  "status": "PENDENTE",
  "dataCriacao": "2026-09-23T10:30:00",
  "dataAtualizacao": "2026-09-23T10:30:00",
  "dataConclusao": null,
  "empresaId": 1,
  "criadoPorId": 1
}
```

---

### 7.2 Listar trabalhos

### GET `/trabalhos`

Retorna todos os trabalhos cadastrados.

**Resposta:** `HTTP 200 OK`

Exemplo:

```json
[
  {
    "id": 1,
    "titulo": "Organizar documentos do cliente",
    "descricao": "Separar e organizar os documentos necessários.",
    "status": "PENDENTE",
    "dataCriacao": "2026-09-23T10:30:00",
    "dataAtualizacao": "2026-09-23T10:30:00",
    "dataConclusao": null,
    "empresaId": 1,
    "criadoPorId": 1
  }
]
```

---

### 7.3 Buscar trabalho por ID

### GET `/trabalhos/{id}`

Busca um trabalho específico pelo seu identificador.

Exemplo:

```text
GET /trabalhos/1
```

**Resposta:** `HTTP 200 OK`

Retorna o trabalho correspondente ao ID informado.

Caso o trabalho não seja encontrado, o Service retorna:

```text
Trabalho não encontrado
```

---

### 7.4 Buscar trabalhos por título

### GET `/trabalhos/busca?titulo={titulo}`

Permite localizar trabalhos pelo título.

A busca não diferencia letras maiúsculas de minúsculas e permite encontrar o texto mesmo quando ele aparece apenas como parte do título.

Exemplo:

```text
GET /trabalhos/busca?titulo=documentos
```

**Resposta:** `HTTP 200 OK`

Retorna uma lista dos trabalhos encontrados.

---

### 7.5 Atualizar trabalho

### PUT `/trabalhos/{id}`

Atualiza o título e a descrição de um trabalho existente.

Exemplo:

```text
PUT /trabalhos/1
```

### Requisição

```json
{
  "titulo": "Organizar documentos do cliente - atualizado",
  "descricao": "Nova descrição do trabalho."
}
```

**Resposta:** `HTTP 200 OK`

O status, empresa, criador e histórico do trabalho não são alterados por essa operação.

---

### 7.6 Alterar status do trabalho

### PATCH `/trabalhos/{id}/status`

Altera o status atual do trabalho e registra a alteração no histórico.

### Status disponíveis

```text
PENDENTE
EM_ANDAMENTO
CONCLUIDO
CANCELADO
```

### Requisição

```json
{
  "status": "EM_ANDAMENTO",
  "usuarioId": 1
}
```

**Resposta:** `HTTP 200 OK`

Quando o trabalho passa para `CONCLUIDO`, a data de conclusão é registrada.

Exemplo:

```json
{
  "id": 1,
  "titulo": "Organizar documentos do cliente",
  "descricao": "Separar e organizar os documentos necessários.",
  "status": "CONCLUIDO",
  "dataCriacao": "2026-09-23T10:30:00",
  "dataAtualizacao": "2026-09-23T12:30:00",
  "dataConclusao": "2026-09-23T12:30:00",
  "empresaId": 1,
  "criadoPorId": 1
}
```

---

### 7.7 Histórico de status

Sempre que o status de um trabalho é alterado, a API registra uma entrada na entidade `HistoricoTrabalho`.

São armazenadas:

* o status atribuído;
* a data da alteração;
* o trabalho alterado;
* o usuário responsável pela alteração.

Essa estrutura permitirá posteriormente consultar a evolução do trabalho e utilizar essas informações no dashboard.

> A consulta do histórico por endpoint próprio ainda não foi implementada.

---

### 7.8 Excluir trabalho

### DELETE `/trabalhos/{id}`

Exclui um trabalho existente.

Exemplo:

```text
DELETE /trabalhos/1
```

**Resposta:**

```text
HTTP 204 No Content
```

Não há conteúdo no corpo da resposta.

---

## 8. Resumo dos endpoints de Trabalho

| Método | Endpoint                           | Operação           |
| ------ | ---------------------------------- | ------------------ |
| POST   | `/trabalhos`                       | Cadastrar trabalho |
| GET    | `/trabalhos`                       | Listar trabalhos   |
| GET    | `/trabalhos/{id}`                  | Buscar por ID      |
| GET    | `/trabalhos/busca?titulo={titulo}` | Buscar por título  |
| PUT    | `/trabalhos/{id}`                  | Atualizar trabalho |
| PATCH  | `/trabalhos/{id}/status`           | Alterar status     |
| DELETE | `/trabalhos/{id}`                  | Excluir trabalho   |

---

## 9. Status do Trabalho

Os status atualmente definidos para `Trabalho` são:

| Status         | Descrição                                |
| -------------- | ---------------------------------------- |
| `PENDENTE`     | Trabalho criado, mas ainda não iniciado. |
| `EM_ANDAMENTO` | Trabalho em execução.                    |
| `CONCLUIDO`    | Trabalho finalizado.                     |
| `CANCELADO`    | Trabalho cancelado.                      |

**Observação:** `PRECISA_DE_ATENCAO` não é um status do trabalho. Essa informação será tratada como uma condição derivada para o dashboard, conforme as regras de negócio do sistema.

---

## 10. Observações sobre a documentação atual

A documentação dos endpoints de `Trabalho` corresponde à implementação atual do Back-end.

A integração e validação desses endpoints com o PostgreSQL será realizada em uma etapa específica do projeto.

O projeto ainda não utiliza Swagger/OpenAPI para geração automática da documentação. Este arquivo é mantido manualmente e deverá ser atualizado conforme novos endpoints forem implementados.
