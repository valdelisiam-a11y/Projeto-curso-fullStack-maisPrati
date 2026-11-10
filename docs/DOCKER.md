
# Configuração e execução com Docker

Este documento explica como configurar e executar o DocFlow utilizando Docker.

## 1. Pré-requisitos

Antes de iniciar, instale:

- Git
- Docker Desktop
- Docker Compose (incluído no Docker Desktop)
- OpenSSL, disponível no Git Bash

Para verificar o OpenSSL, execute no Git Bash:

```bash
openssl version
```

## 2. Clonar o projeto

No terminal:

```bash
git clone https://github.com/valdelisiam-a11y/Projeto-curso-fullStack-maisPrati.git
cd Projeto-curso-fullStack-maisPrati
```

## 3. Configurar as variáveis de ambiente

Na raiz do projeto, crie um arquivo chamado `.env`.

Gere uma chave JWT individual:

```bash
openssl rand -hex 32
```

Copie o valor gerado e preencha o arquivo `.env`:

```env
DB_PASSWORD='sua_senha_do_neon'
JWT_SECRET='sua_chave_gerada'
B2_KEY_ID='sua_application_key_id'
B2_APPLICATION_KEY='sua_application_key'
```

Substitua os valores de exemplo pelas credenciais correspondentes.

**Importante:**
- Cada integrante deve gerar sua própria chave JWT.
- Não compartilhe nem versione o arquivo `.env`.
- Não envie credenciais para o repositório.
- A chave JWT deve ser mantida em segurança. Ao alterá-la, tokens emitidos anteriormente deixam de ser válidos.

## 4. Construir as imagens

Com o Docker Desktop aberto, execute na raiz do projeto:

```bash
docker compose build
```

Esse comando constrói as imagens do backend e do frontend.

## 5. Iniciar a aplicação

Execute:

```bash
docker compose up -d
```

A aplicação ficará disponível em:

http://localhost:5173

## 6. Comandos úteis

Verificar os containers:

```bash
docker compose ps
```

Consultar os logs:

```bash
docker compose logs -f
```

Consultar somente o backend:

```bash
docker compose logs -f backend
```

Parar os containers:

```bash
docker compose stop
```

Iniciar novamente:

```bash
docker compose start
```

Reiniciar:

```bash
docker compose restart
```

Reconstruir as imagens após alterações no código:

```bash
docker compose up -d --build
```

Parar e remover os containers e a rede:

```bash
docker compose down
```

## 7. Banco de dados e armazenamento

O projeto utiliza serviços externos:

- **PostgreSQL:** banco de dados hospedado no Neon.
- **Backblaze B2:** armazenamento de documentos.

Esses serviços não são executados em containers Docker. O acesso é configurado pelas variáveis de ambiente do arquivo `.env`.

## 8. Segurança

O arquivo `.env` contém credenciais e segredos da aplicação. Ele não deve ser enviado ao Git.

O repositório disponibiliza o arquivo `.env.example` como referência das variáveis necessárias, sem valores reais.
