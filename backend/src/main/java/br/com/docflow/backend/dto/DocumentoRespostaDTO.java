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
    // Construtor mapeando diretamente da entidade e injetando o trabalhoId garantido pelo Service
    public DocumentoRespostaDTO(Documento doc, Long trabalhoId) {
        this(
                doc.getId(),
                doc.getNomeArquivo(),
                doc.getUrlArquivo(),
                doc.getDataCriacao(),
                doc.getEtapaId(), // Pegando o ID temporário enquanto a classe Etapa não chega
                trabalhoId
        );
    }
}
