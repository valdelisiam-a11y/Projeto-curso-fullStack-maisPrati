import { apiFetch } from "./api";

export async function listarDocumentos(etapaId) {
  const resposta = await apiFetch(`/etapas/${etapaId}/documentos`);

  if (!resposta.ok) {
    const mensagem = await resposta.text();
    throw new Error(
      mensagem || `Erro ao listar documentos (${resposta.status}).`
    );
  }

  return resposta.json();
}

export async function enviarDocumento(etapaId, arquivo) {
  const formulario = new FormData();
  formulario.append("arquivo", arquivo);

  const resposta = await apiFetch(`/etapas/${etapaId}/documentos`, {
    method: "POST",
    body: formulario,
  });

  if (!resposta.ok) {
    const mensagem = await resposta.text();
    throw new Error(
      mensagem || `Erro ao enviar documento (${resposta.status}).`
    );
  }

  return resposta.json();
}

export async function baixarDocumento(etapaId, documentoId) {
  const resposta = await apiFetch(
    `/etapas/${etapaId}/documentos/${documentoId}/download`
  );

  if (!resposta.ok) {
    const mensagem = await resposta.text();
    throw new Error(
      mensagem || `Erro ao baixar documento (${resposta.status}).`
    );
  }

  return resposta.blob();
}


export async function excluirDocumento(etapaId, documentoId) {
  const resposta = await apiFetch(
    `/etapas/${etapaId}/documentos/${documentoId}`,
    {
      method: "DELETE",
    }
  );

  if (!resposta.ok) {
    const mensagem = await resposta.text();
    throw new Error(
      mensagem || `Erro ao excluir documento (${resposta.status}).`
    );
  }
}
