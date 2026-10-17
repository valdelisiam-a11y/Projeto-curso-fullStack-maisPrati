package br.com.docflow.backend.dto;

import br.com.docflow.backend.entity.Documento;
import java.time.LocalDateTime;

public record DocumentoRespostaDTO(
        Long id,
        String nomeArquivo,
        String urlArquivo,
        LocalDateTime dataCriacao,
        Long etapaId,
        Long trabalhoId
) {
    public DocumentoRespostaDTO(Documento doc, Long trabalhoId) {
        this(doc.getId(), doc.getNomeArquivo(), doc.getUrlArquivo(), doc.getDataCriacao(), doc.getEtapa().getId(), trabalhoId);
    }
}
