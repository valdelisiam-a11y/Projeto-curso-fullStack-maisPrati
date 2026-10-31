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
## 10. Persistência e integração com PostgreSQL

A API está integrada a um banco de dados PostgreSQL utilizado no ambiente de desenvolvimento e integração do projeto.

A conexão com o banco é configurada no `application.properties`, utilizando as informações de conexão e a variável de ambiente `DB_PASSWORD` para a senha.

A persistência é realizada utilizando JPA/Hibernate.

As entidades relacionadas a esta etapa e persistidas no banco são:

* `Empresa`
* `Usuario`
* `Trabalho`
* `HistoricoTrabalho`

A integração com o PostgreSQL foi validada por meio das operações realizadas pela API e de consultas diretamente no banco de dados.

## 11. Validações realizadas

Foram realizados testes da integração da API com o PostgreSQL, incluindo:

* criação de usuário e persistência no banco;
* criação automática da empresa associada ao usuário;
* armazenamento da senha utilizando hash;
* validação de e-mail duplicado;
* validações dos campos obrigatórios e formato do e-mail;
* criação de `Trabalho`;
* listagem de trabalhos;
* busca de trabalho por ID;
* busca de trabalho por título;
* atualização de dados do trabalho;
* alteração de status;
* registro do histórico de status em `HistoricoTrabalho`;
* persistência das datas de criação, atualização e conclusão;
* exclusão de `Trabalho`.

Durante a validação da exclusão, foi identificado que `HistoricoTrabalho` possui uma chave estrangeira relacionada a `Trabalho`. O serviço foi ajustado para excluir primeiro os registros do histórico relacionados ao trabalho e, em seguida, realizar a exclusão do trabalho.

A exclusão foi validada com retorno `HTTP 204 No Content` e confirmada diretamente no PostgreSQL, verificando que não permaneceram registros do trabalho nem de seu histórico.

## 12. Observações sobre a documentação

A documentação dos endpoints da API é mantida manualmente neste arquivo e deve ser atualizada conforme novos endpoints forem implementados ou houver alterações no comportamento da API.

O projeto ainda não utiliza Swagger/OpenAPI para geração automática da documentação.

As informações sensíveis de acesso ao banco de dados não são armazenadas diretamente na documentação ou no código versionado. A senha do banco é fornecida por meio da variável de ambiente `DB_PASSWORD`.

## 13. Etapas
 
Um trabalho pode ser dividido em etapas. Cada etapa pertence a um único trabalho, pode ter um usuário responsável (criador ou participante do trabalho), possui percentual de conclusão (0 a 100), ordem dentro do trabalho e status, com histórico de alterações.
 
**Autenticação:** todos os endpoints exigem token JWT.
 
### 13.1 Controle de acesso
 
O acesso a uma etapa (consultar, listar, alterar, excluir ou ver o histórico) é permitido somente para:
 
* o **criador** do trabalho; ou
* um **participante** do trabalho.
Pertencer à mesma empresa do trabalho **não** concede acesso. Quem não atende a nenhuma das duas condições recebe `403 Forbidden`.
 
O usuário que realiza cada operação é sempre o **usuário autenticado** (obtido do token JWT). O campo `usuarioId`, quando enviado no corpo das requisições de status e percentual, é opcional e **ignorado** pelo backend; ele existe apenas por compatibilidade temporária. O histórico registra o usuário autenticado.
 
### 13.2 Resumo dos endpoints
 
| Método | Endpoint                        | Operação                      |
| ------ | ------------------------------- | ----------------------------- |
| POST   | `/trabalhos/{trabalhoId}/etapas`| Cadastrar etapa               |
| GET    | `/trabalhos/{trabalhoId}/etapas`| Listar etapas (ordenadas)     |
| GET    | `/etapas/{id}`                  | Buscar etapa por ID           |
| PUT    | `/etapas/{id}`                  | Atualizar título e descrição  |
| PATCH  | `/etapas/{id}/status`           | Alterar status                |
| PATCH  | `/etapas/{id}/percentual`       | Atualizar percentual          |
| PATCH  | `/etapas/{id}/ordem`            | Alterar ordem                 |
| PATCH  | `/etapas/{id}/responsavel`      | Definir ou remover responsável|
| GET    | `/etapas/{id}/historico`        | Consultar histórico de status |
| DELETE | `/etapas/{id}`                  | Excluir etapa                 |
 
### 13.3 Status da etapa
 
| Status         | Descrição                                   |
| -------------- | ------------------------------------------- |
| `PENDENTE`     | Etapa criada, ainda sem progresso.          |
| `EM_ANDAMENTO` | Etapa em execução.                          |
| `IMPEDIDO`     | Etapa bloqueada por algum impedimento.      |
| `CONCLUIDO`    | Etapa finalizada (sempre com 100%).         |
 
`CANCELADO` é um status de Trabalho e **não existe** para Etapa; enviá-lo retorna `400 Bad Request`.
 
Uma etapa `IMPEDIDO` pode fazer o trabalho ser exibido como "Precisa de atenção" no dashboard, mas **não altera** o status do trabalho. O status `IMPEDIDO` não impõe restrições adicionais ao percentual.
 
### 13.4 Cadastrar etapa
 
### POST `/trabalhos/{trabalhoId}/etapas`
 
```json
{
  "titulo": "Revisão jurídica",
  "descricao": "Revisar o contrato",
  "ordem": 1,
  "responsavelId": 2
}
```
 
Somente `titulo` é obrigatório (máximo de 200 caracteres). Se `ordem` não for informada, a etapa vai para o final; se for informada, as demais são reposicionadas. Se `responsavelId` for informado, o usuário deve ser o criador ou um participante do trabalho. A etapa nasce com status `PENDENTE` e percentual `0`.
 
**Resposta:** `HTTP 201 Created`
 
```json
{
  "id": 1,
  "titulo": "Revisão jurídica",
  "descricao": "Revisar o contrato",
  "status": "PENDENTE",
  "percentualConclusao": 0,
  "ordem": 1,
  "dataCriacao": "2026-10-21T16:00:00",
  "dataAtualizacao": "2026-10-21T16:00:00",
  "trabalhoId": 1,
  "responsavelId": 2
}
```
 
### 13.5 Listar etapas de um trabalho
 
### GET `/trabalhos/{trabalhoId}/etapas`
 
Retorna as etapas do trabalho ordenadas pelo campo `ordem`. **Resposta:** `HTTP 200 OK`.
 
### 13.6 Buscar etapa por ID
 
### GET `/etapas/{id}`
 
**Resposta:** `HTTP 200 OK`.
 
### 13.7 Atualizar etapa
 
### PUT `/etapas/{id}`
 
```json
{
  "titulo": "Revisão jurídica - ajustada",
  "descricao": "Nova descrição"
}
```
 
Altera apenas título (obrigatório) e descrição. Status, percentual, ordem e responsável têm endpoints próprios. **Resposta:** `HTTP 200 OK`.
 
### 13.8 Alterar status
 
### PATCH `/etapas/{id}/status`
 
```json
{
  "status": "EM_ANDAMENTO"
}
```
 
Para reabrir uma etapa `CONCLUIDO`, informe também o percentual ao qual ela retorna:
 
```json
{
  "status": "EM_ANDAMENTO",
  "percentualConclusao": 60
}
```
 
Regras:
 
* informar o status que a etapa já possui retorna `409 Conflict`;
* ao marcar como `CONCLUIDO`, o percentual passa para 100;
* ao sair de `CONCLUIDO` para outro status, `percentualConclusao` é **obrigatório** e deve estar entre 0 e 99 (uma etapa não pode continuar com 100% se deixou de estar concluída); para `PENDENTE`, o único valor aceito é 0. Caso contrário, retorna `400 Bad Request` e nada é alterado;
* nas demais mudanças de status, o percentual atual é mantido e `percentualConclusao` é ignorado;
* toda mudança de status é registrada no histórico com o usuário autenticado.
### 13.9 Atualizar percentual
 
### PATCH `/etapas/{id}/percentual`
 
```json
{
  "percentualConclusao": 60
}
```
 
O percentual deve estar entre 0 e 100.
 
Regras:
 
* ao chegar em 100, a etapa passa para `CONCLUIDO`;
* se a etapa estava `CONCLUIDO` e o percentual diminui, ela volta para `EM_ANDAMENTO` (com o percentual informado);
* se a etapa estava `PENDENTE` e o percentual informado é maior que 0, ela passa para `EM_ANDAMENTO`;
* em `EM_ANDAMENTO` e `IMPEDIDO`, percentuais abaixo de 100 mantêm o status;
* o histórico só é registrado quando o percentual altera o status.
### 13.10 Alterar ordem
 
### PATCH `/etapas/{id}/ordem`
 
```json
{
  "ordem": 2
}
```
 
A etapa é movida para a posição informada e as demais descem ou sobem uma posição (comportamento de lista), mantendo a sequência 1, 2, 3... sem repetições. Se a ordem for maior que o total de etapas, a etapa vai para o final. Ao excluir uma etapa, a sequência é renumerada.
 
### 13.11 Definir ou remover responsável
 
### PATCH `/etapas/{id}/responsavel`
 
```json
{
  "responsavelId": 2
}
```
 
Enviar `"responsavelId": null` remove o responsável. O responsável deve ser o **criador** ou um **participante** do trabalho; caso contrário, retorna `409 Conflict`. Ser da mesma empresa não é suficiente.
 
### 13.12 Histórico de status
 
### GET `/etapas/{id}/historico`
 
Retorna as alterações de status da etapa, da mais recente para a mais antiga.
 
```json
[
  {
    "id": 2,
    "statusAnterior": "EM_ANDAMENTO",
    "statusNovo": "CONCLUIDO",
    "dataAlteracao": "2026-10-21T16:30:00",
    "usuarioId": 1,
    "usuarioNome": "Maria Silva"
  }
]
```
 
### 13.13 Excluir etapa
 
### DELETE `/etapas/{id}`
 
**Resposta:** `HTTP 204 No Content`. O histórico da etapa é excluído junto e as etapas restantes são renumeradas.
 
Ao excluir um trabalho, suas etapas e os históricos delas também são excluídos (`ON DELETE CASCADE` no banco).
 
### 13.14 Erros
 
Os erros tratados pelas etapas seguem o formato `timestamp`, `status`, `erro`, `mensagem`.
 
| Status | Quando                                                                                                                                                         |
| ------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 400    | Dados inválidos (título vazio, percentual fora de 0 a 100, ordem menor que 1, status inexistente ou `CANCELADO`, campo obrigatório ausente); percentual de retorno ausente ou inválido ao reabrir uma etapa `CONCLUIDO` |
| 403    | Usuário sem token válido, ou que não é o criador nem participante do trabalho. O corpo da resposta é vazio (resposta padrão do Spring Security)                |
| 404    | Trabalho, etapa ou usuário responsável não encontrado                                                                                                          |
| 409    | Responsável que não é criador nem participante do trabalho; status igual ao atual                                                                              |
 
### 13.15 Testes automatizados
 
Os testes das etapas ficam em `EtapaServiceTest` (regras de negócio e autorização do service) e `EtapaDtoValidacaoTest` (validações dos DTOs). Para executá-los, sem acessar o banco de dados:
 
```bash
cd backend
mvn test -Dtest='EtapaServiceTest,EtapaDtoValidacaoTest'
```
 
