package br.com.docflow.backend.service;

import br.com.docflow.backend.dto.DocumentoRequestDTO;
import br.com.docflow.backend.dto.DocumentoResponseDTO;
import br.com.docflow.backend.entity.Documento;
import br.com.docflow.backend.entity.Etapa;
import br.com.docflow.backend.repository.DocumentoRepository;
import br.com.docflow.backend.repository.EtapaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentoService {

    @Autowired
    private DocumentoRepository documentoRepository;

    @Autowired
    private EtapaRepository etapaRepository;

    private Etapa validarEtapaDoTrabalho(Long trabalhoId, Long etapaId) {
        Etapa etapa = etapaRepository.findById(etapaId)
                .orElseThrow(() -> new EntityNotFoundException("Etapa não encontrada no DocFlow."));

        if (!etapa.getTrabalho().getId().equals(trabalhoId)) {
            throw new IllegalArgumentException("A etapa informada não pertence ao trabalho especificado.");
        }
        return etapa;
    }

    public DocumentoResponseDTO adicionarDocumento(Long trabalhoId, Long etapaId, DocumentoRequestDTO request) {
        Etapa etapa = validarEtapaDoTrabalho(trabalhoId, etapaId);

        Documento documento = new Documento();
        documento.setNome(request.getNome());
        documento.setUrlArquivo(request.getUrlArquivo());
        documento.setEtapa(etapa);

        Documento salvo = documentoRepository.save(documento);
        return convertToResponseDTO(salvo);
    }

    public List<DocumentoResponseDTO> listarDocumentosDaEtapa(Long trabajoId, Long etapaId) {
        validarEtapaDoTrabalho(trabajoId, etapaId);
        return documentoRepository.findByEtapaId(etapaId)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public DocumentoResponseDTO consultarDocumento(Long trabalhoId, Long etapaId, Long documentoId) {
        validarEtapaDoTrabalho(trabalhoId, etapaId);

        Documento documento = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new EntityNotFoundException("Documento não encontrado no DocFlow."));

        if (!documento.getEtapa().getId().equals(etapaId)) {
            throw new IllegalArgumentException("O documento não pertence à etapa informada.");
        }
        return convertToResponseDTO(documento);
    }

    public void removerDocumento(Long trabalhoId, Long etapaId, Long documentoId) {
        validarEtapaDoTrabalho(trabalhoId, etapaId);
        Documento documento = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new EntityNotFoundException("Documento não encontrado no DocFlow."));

        if (!documento.getEtapa().getId().equals(etapaId)) {
            throw new IllegalArgumentException("O documento não pertence à etapa informada.");
        }
        documentoRepository.delete(documento);
    }

    private DocumentoResponseDTO convertToResponseDTO(Documento documento) {
        DocumentoResponseDTO response = new DocumentoResponseDTO();
        response.setId(documento.getId());
        response.setNome(documento.getNome());
        response.setUrlArquivo(documento.getUrlArquivo());
        response.setDataCriacao(documento.getDataCriacao());
        return response;
    }
}
