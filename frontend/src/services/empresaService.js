
import { apiFetch } from "./api";

export async function listarMinhasEmpresas() {
  const resposta = await apiFetch("/empresas/minhas");

  if (!resposta.ok) {
    const mensagem = await resposta.text();
    throw new Error(
      mensagem || `Erro ao listar empresas (${resposta.status}).`
    );
  }

  return resposta.json();
}

export async function cadastrarEmpresa(dados) {
  const empresasAntes = await listarMinhasEmpresas();
  const idsAntes = new Set(
    empresasAntes.map((empresa) => empresa.id)
  );

  const resposta = await apiFetch("/empresas", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      nome: dados.nome.trim(),
      cnpj: dados.cnpj?.trim() || null,
    }),
  });

  if (!resposta.ok) {
    const mensagem = await resposta.text();
    throw new Error(
      mensagem || `Erro ao cadastrar empresa (${resposta.status}).`
    );
  }

  const empresasDepois = await listarMinhasEmpresas();

  const novaEmpresa = empresasDepois.find(
    (empresa) => !idsAntes.has(empresa.id)
  );

  if (!novaEmpresa) {
    throw new Error(
      "Empresa cadastrada, mas não foi possível recuperar seu ID."
    );
  }

  return novaEmpresa;
}

export async function listarMembrosEmpresa(empresaId) {
  const resposta = await apiFetch(
    `/empresas/${empresaId}/membros`
  );

  if (!resposta.ok) {
    const mensagem = await resposta.text();
    throw new Error(
      mensagem || `Erro ao listar membros (${resposta.status}).`
    );
  }

  return resposta.json();
}

export async function adicionarMembroEmpresa(empresaId, email) {
  const resposta = await apiFetch(
    `/empresas/${empresaId}/membros`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ email }),
    }
  );

  if (!resposta.ok) {
    const mensagem = await resposta.text();
    throw new Error(
      mensagem || `Erro ao adicionar membro (${resposta.status}).`
    );
  }
}

export async function removerMembroEmpresa(empresaId, usuarioId) {
  const resposta = await apiFetch(
    `/empresas/${empresaId}/membros/${usuarioId}`,
    {
      method: "DELETE",
    }
  );

  if (!resposta.ok) {
    const mensagem = await resposta.text();
    throw new Error(
      mensagem || `Erro ao remover membro (${resposta.status}).`
    );
  }
}
