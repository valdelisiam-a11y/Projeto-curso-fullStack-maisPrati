# Documentação da API — DocFlow

## 1. Visão geral

A API REST do DocFlow é responsável pelo gerenciamento de usuários, empresas, trabalhos, etapas e documentos.

**URL base local:** `http://localhost:8080`

**Formato:** JSON, exceto operações de upload e download de arquivos.

## 2. Autenticação

A autenticação utiliza JWT (JSON Web Token).

Após realizar o login, o cliente deve enviar o token nas requisições protegidas:

```http
Authorization: Bearer SEU_TOKEN
```

O token possui validade de 24 horas.

### Rotas públicas

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/usuarios` | Cadastro de usuário |
| POST | `/auth/login` | Autenticação |

As demais rotas exigem autenticação.

## 3. Usuários

| Método | Endpoint | Descrição |
|---|---|---|
| PUT | `/usuarios/me` | Atualização do perfil do usuário |
| POST | `/auth/alterar-senha` | Alteração de senha |

## 4. Empresas

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/empresas` | Cadastro de empresa |
| GET | `/empresas/minhas` | Lista empresas do usuário |
| GET | `/empresas/{empresaId}/membros` | Lista membros da empresa |
| GET | `/empresas/{empresaId}/membros-disponiveis` | Lista usuários disponíveis para inclusão |
| POST | `/empresas/{empresaId}/membros` | Adiciona membro |
| DELETE | `/empresas/{empresaId}/membros/{usuarioId}` | Remove membro |

## 5. Trabalhos

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/trabalhos` | Cadastro de trabalho |
| GET | `/trabalhos` | Lista trabalhos |
| GET | `/trabalhos/{id}` | Consulta trabalho |
| GET | `/trabalhos/busca?titulo={titulo}` | Busca por título |
| PUT | `/trabalhos/{id}` | Atualização de trabalho |
| PATCH | `/trabalhos/{id}/status` | Atualização de status |
| GET | `/trabalhos/{id}/historico` | Consulta histórico |
| GET | `/trabalhos/{id}/linha-do-tempo` | Consulta linha do tempo |
| DELETE | `/trabalhos/{id}` | Exclusão de trabalho |

### Status disponíveis

- `PENDENTE`
- `EM_ANDAMENTO`
- `CONCLUIDO`
- `CANCELADO`

A condição "Precisa de atenção" é apresentada pela interface conforme as condições do trabalho, não sendo um status próprio.

## 6. Etapas

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/trabalhos/{trabalhoId}/etapas` | Cadastro de etapa |
| GET | `/trabalhos/{trabalhoId}/etapas` | Lista etapas do trabalho |
| GET | `/empresas/{empresaId}/minhas-etapas` | Lista etapas atribuídas ao usuário |
| GET | `/etapas/{id}` | Consulta etapa |
| PUT | `/etapas/{id}` | Atualização de etapa |
| PATCH | `/etapas/{id}/status` | Atualização de status |
| PATCH | `/etapas/{id}/percentual` | Atualização do percentual |
| PATCH | `/etapas/{id}/ordem` | Alteração da ordem |
| PATCH | `/etapas/{id}/responsavel` | Definição de responsável |
| GET | `/etapas/{id}/historico` | Consulta histórico |
| POST | `/etapas/{id}/observacoes` | Registro de observação |
| DELETE | `/etapas/{id}` | Exclusão de etapa |

## 7. Compartilhamento de trabalhos

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/api/usuario-trabalho` | Adiciona participante ao trabalho |
| GET | `/api/usuario-trabalho/{trabalhoId}` | Lista participantes |
| DELETE | `/api/usuario-trabalho/{trabalhoId}/{usuarioId}` | Remove participante |

## 8. Documentos

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/etapas/{etapaId}/documentos` | Envia documento |
| GET | `/etapas/{etapaId}/documentos` | Lista documentos da etapa |
| GET | `/etapas/{etapaId}/documentos/{documentoId}/download` | Realiza download |
| DELETE | `/etapas/{etapaId}/documentos/{documentoId}` | Exclui documento |

O envio utiliza `multipart/form-data`, com o campo `arquivo`.

O armazenamento dos arquivos é realizado no Backblaze B2.

**Limites de upload:**
- Arquivo: 10 MB.
- Requisição: 11 MB.

## 9. Banco de dados

A aplicação utiliza PostgreSQL, hospedado no Neon.

## 10. Segurança

- Autenticação baseada em JWT.
- Senhas protegidas com BCrypt.
- Rotas protegidas exigem token válido.
- A autorização das operações considera as permissões e os vínculos do usuário com trabalhos e empresas.