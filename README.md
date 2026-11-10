# DocFlow

**Gerenciador Inteligente de Trabalhos para Pequenas Empresas e Uso Pessoal**

O DocFlow é uma aplicação web desenvolvida para organizar trabalhos, acompanhar etapas, distribuir responsabilidades e centralizar documentos. Pode ser utilizado individualmente ou em equipes de pequenas empresas.

## Objetivo

Simplificar o gerenciamento de trabalhos e atividades, oferecendo visibilidade sobre o andamento das etapas, os responsáveis e os documentos relacionados.

## Funcionalidades

- **Gestão de trabalhos:** cadastro, consulta, edição e acompanhamento do status.
- **Gestão de etapas:** organização das etapas, definição de ordem, atualização de status e percentual de conclusão.
- **Colaboração:** compartilhamento de trabalhos e atribuição de responsáveis às etapas.
- **Gestão de empresas:** criação de empresas e gerenciamento de membros, respeitando o limite de usuários.
- **Minhas Etapas:** visualização das etapas atribuídas ao usuário.
- **Documentos:** envio e acesso aos documentos vinculados às etapas.
- **Histórico e acompanhamento:** registro de alterações e visualização da evolução dos trabalhos.
- **Conta do usuário:** cadastro, autenticação e atualização de perfil.

## Tecnologias

**Frontend**
- React
- JavaScript
- Vite
- HTML5 e CSS3

**Backend**
- Java 21
- Spring Boot
- Spring Security
- API REST
- JWT
- Maven

**Banco de dados e armazenamento**
- PostgreSQL (Neon)
- Backblaze B2 para armazenamento de documentos


## Arquitetura

O projeto utiliza uma arquitetura separada entre frontend e backend:

- **Frontend:** interface e interação com o usuário.
- **Backend:** API REST responsável pelas regras de negócio, autenticação e processamento das operações.
- **Banco de dados:** armazenamento das informações da aplicação.
- **Armazenamento externo:** guarda os documentos enviados pelos usuários.

## Estrutura do projeto

```text
Projeto-curso-fullStack-maisPrati/
├── backend/       # API REST em Java e Spring Boot
├── frontend/      # Aplicação web em React
├── docs/          # Documentação técnica
└── README.md
```

## Documentação

- [Documentação da API](docs/API.md)
- [Configuração e execução com Docker](docs/DOCKER.md)

## Equipe — Grupo 38

- Camila dos Santos Regner
- Erik Fernando Rodrigues da Silva
- Francisco de Assis de Vasconcelos Souza
- Gean Oldra
- Jonatas Bernardo Santos da Silva
- Joshua Abdi de Morais
- Thaís dos Santos Jorge
- Valdelisia Queiros Monteiro
- Verenice Almeida Pires

Projeto desenvolvido como parte da formação Full Stack da +PraTi.