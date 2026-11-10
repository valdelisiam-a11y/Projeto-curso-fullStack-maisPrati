
import { apiFetch } from "./api";

export async function listarTrabalhos() {
  const resposta = await apiFetch("/trabalhos");

  if (!resposta.ok) {
    const mensagem = await resposta.text();
    throw new Error(
      mensagem || `Erro ao listar trabalhos (${resposta.status}).`
    );
  }

  return resposta.json();
}

export async function cadastrarTrabalho(dados) {
  const resposta = await apiFetch("/trabalhos", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(dados),
  });

  if (!resposta.ok) {
    const mensagem = await resposta.text();
    throw new Error(
      mensagem || `Erro ao cadastrar trabalho (${resposta.status}).`
    );
  }

  return resposta.json();
}

export async function cadastrarEtapa(trabalhoId, dados) {
  const resposta = await apiFetch(`/trabalhos/${trabalhoId}/etapas`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(dados),
  });

  if (!resposta.ok) {
    const mensagem = await resposta.text();
    throw new Error(
      mensagem || `Erro ao cadastrar etapa (${resposta.status}).`
    );
  }

  return resposta.json();
}


export async function buscarTrabalhoPorId(id) {
  const resposta = await apiFetch(`/trabalhos/${id}`);

  if (!resposta.ok) {
    const mensagem = await resposta.text();
    throw new Error(
      mensagem || `Erro ao buscar trabalho (${resposta.status}).`
    );
  }

  return resposta.json();
}

export async function listarEtapasDoTrabalho(trabalhoId) {
  const resposta = await apiFetch(`/trabalhos/${trabalhoId}/etapas`);

  if (!resposta.ok) {
    const mensagem = await resposta.text();
    throw new Error(
      mensagem || `Erro ao listar etapas (${resposta.status}).`
    );
  }

  return resposta.json();
}

export async function atualizarEtapa(etapaId, dados) {
  const resposta = await apiFetch(`/etapas/${etapaId}`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(dados),
  });

  if (!resposta.ok) {
    const mensagem = await resposta.text();
    throw new Error(
      mensagem || `Erro ao atualizar etapa (${resposta.status}).`
    );
  }

  return resposta.json();
}

export async function listarHistoricoTrabalho(trabalhoId) {
  const resposta = await apiFetch(`/trabalhos/${trabalhoId}/historico`);

  if (!resposta.ok) {
    const mensagem = await resposta.text();
    throw new Error(
      mensagem || `Erro ao listar histórico (${resposta.status}).`
    );
  }

  return resposta.json();
}

export async function alterarStatusEtapa(etapaId, dados) {
  const resposta = await apiFetch(`/etapas/${etapaId}/status`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(dados),
  });

  if (!resposta.ok) {
    const mensagem = await resposta.text();
    throw new Error(
      mensagem || `Erro ao alterar status da etapa (${resposta.status}).`
    );
  }

  return resposta.json();
}

export async function listarMinhasEtapas(empresaId) {
  const resposta = await apiFetch(
    `/empresas/${empresaId}/minhas-etapas`
  );

  if (!resposta.ok) {
    const mensagem = await resposta.text();
    throw new Error(
      mensagem || `Erro ao listar minhas etapas (${resposta.status}).`
    );
  }

  return resposta.json();
}

export async function definirResponsavelEtapa(etapaId, responsavelId) {
  const resposta = await apiFetch(`/etapas/${etapaId}/responsavel`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      responsavelId:
        responsavelId === "" || responsavelId == null
          ? null
          : Number(responsavelId),
    }),
  });

  if (!resposta.ok) {
    const mensagem = await resposta.text();
    throw new Error(
      mensagem || `Erro ao definir responsável (${resposta.status}).`
    );
  }

  return resposta.json();
}

export async function registrarObservacaoEtapa(etapaId, texto) {
  const resposta = await apiFetch(`/etapas/${etapaId}/observacoes`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ texto }),
  });

  if (!resposta.ok) {
    const mensagem = await resposta.text();

    throw new Error(
      mensagem || `Erro ao registrar observação (${resposta.status}).`
    );
  }

  return resposta.json();
}
