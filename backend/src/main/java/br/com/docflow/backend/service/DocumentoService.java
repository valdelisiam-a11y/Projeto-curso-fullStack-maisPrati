package br.com.docflow.backend.service;

import br.com.docflow.backend.entity.Documento;
import br.com.docflow.backend.entity.Etapa;
import br.com.docflow.backend.repository.DocumentoRepository;
import br.com.docflow.backend.repository.EtapaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DocumentoService {

    @Autowired
    private DocumentoRepository documentoRepository;

    @Autowired
    private EtapaRepository etapaRepository;

    private Etapa validarEtapaDoTrabalho(Long trabalhoId, Long etapaId) {
        Etapa etapa = etapaRepository.findById(etapaId)
                .orElseThrow(() -> new IllegalArgumentException("Etapa não encontrada no DocFlow."));

        if (!etapa.getTrabalho().getId().equals(trabalhoId)) {
            throw new IllegalArgumentException("A etapa informada não pertence ao trabalho especificado.");
        }
        return etapa;
    }

    public Documento adicionarDocumento(Long trabalhoId, Long etapaId, Documento documento) {
        Etapa etapa = validarEtapaDoTrabalho(trabalhoId, etapaId);
        documento.setEtapa(etapa);
        return documentoRepository.save(documento);
    }

    public List<Documento> listarDocumentosDaEtapa(Long trabalhoId, Long etapaId) {
        validarEtapaDoTrabalho(trabalhoId, etapaId);
        return documentoRepository.findByEtapaId(etapaId);
    }

    public Documento consultarDocumento(Long trabalhoId, Long etapaId, Long documentoId) {
        validarEtapaDoTrabalho(trabalhoId, etapaId);

        Documento documento = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new IllegalArgumentException("Documento não encontrado no DocFlow."));

        if (!documento.getEtapa().getId().equals(etapaId)) {
            throw new IllegalArgumentException("O documento não pertence à etapa informada.");
        }
        return documento;
    }

    public void removerDocumento(Long trabalhoId, Long etapaId, Long documentoId) {
        Documento documento = consultarDocumento(trabalhoId, etapaId, documentoId);
        documentoRepository.delete(documento);
    }
}
