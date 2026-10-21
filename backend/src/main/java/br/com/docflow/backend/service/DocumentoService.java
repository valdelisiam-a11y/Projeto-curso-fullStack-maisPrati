package br.com.docflow.backend.service;

import br.com.docflow.backend.dto.DocumentoCadastroDTO;
import br.com.docflow.backend.dto.DocumentoRespostaDTO;
import br.com.docflow.backend.entity.Documento;
import br.com.docflow.backend.repository.DocumentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class DocumentoService {

    @Autowired
    private DocumentoRepository documentoRepository;

    // TODO: Injetar o EtapaRepository assim que o Erick mesclar a branch dele
    // @Autowired
    // private EtapaRepository etapaRepository;

    @Transactional
    public DocumentoRespostaDTO adicionar(DocumentoCadastroDTO dto) {
        // [VALIDAÇÃO FUTURA COM A BRANCH DO ERICK]:
        // Etapa etapa = etapaRepository.findById(dto.etapaId())
        //         .orElseThrow(() -> new RuntimeException("Etapa não encontrada."));
        // if (!etapa.getTrabalho().getId().equals(dto.trabalhoId())) {
        //     throw new IllegalArgumentException("A etapa informada não pertence ao trabalho informado.");
        // }

        Documento documento = new Documento();
        documento.setNomeArquivo(dto.nomeArquivo());
        documento.setUrlArquivo(dto.urlArquivo());
        documento.setEtapaId(dto.etapaId());

        documentoRepository.save(documento);

        // Retorna o DTO garantindo a consistência do trabalhoId enviado
        return new DocumentoRespostaDTO(documento, dto.trabalhoId());
    }

    public List<DocumentoRespostaDTO> listarPorEtapa(Long etapaId, Long trabalhoId) {
        return documentoRepository.findByEtapaId(etapaId)
                .stream()
                .map(doc -> new DocumentoRespostaDTO(doc, trabalhoId))
                .toList();
    }

    public DocumentoRespostaDTO consultar(Long id, Long trabalhoId) {
        Documento doc = documentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado com o ID: " + id));
        return new DocumentoRespostaDTO(doc, trabalhoId);
    }

    @Transactional
    public void remover(Long id) {
        if (!documentoRepository.existsById(id)) {
            throw new RuntimeException("Não foi possível remover: Documento inexistente.");
        }
        documentoRepository.deleteById(id);
    }
}
