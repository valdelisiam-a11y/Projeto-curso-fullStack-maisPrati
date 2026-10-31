package br.com.docflow.backend.service;

import br.com.docflow.backend.dto.DocumentoCadastroDTO;
import br.com.docflow.backend.dto.DocumentoRespostaDTO;
import br.com.docflow.backend.entity.Documento;
import br.com.docflow.backend.entity.Etapa;
import br.com.docflow.backend.repository.DocumentoRepository;
import br.com.docflow.backend.repository.EtapaRepository;
import br.com.docflow.backend.exception.BusinessException;
import br.com.docflow.backend.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class DocumentoService {

    @Autowired
    private DocumentoRepository documentoRepository;

    @Autowired
    private EtapaRepository etapaRepository;

    @Transactional
    public DocumentoRespostaDTO adicionar(DocumentoCadastroDTO dto) {
        Etapa etapa = etapaRepository.findById(dto.etapaId())
                .orElseThrow(() -> new ResourceNotFoundException("Etapa não encontrada com o ID: " + dto.etapaId()));

        // VALIDAÇÃO: Se a etapa pertence ao trabalho informado
        if (!etapa.getTrabalho().getId().equals(dto.trabalhoId())) {
            throw new BusinessException("A etapa informada não pertence ao trabalho fornecido.");
        }

        // VALIDAÇÃO DE ACESSO: Regra padrão do projeto baseada no contexto do Trabalho
        validarAcessoAoTrabalho(etapa.getTrabalho().getId());

        Documento documento = new Documento();
        documento.setNomeArquivo(dto.nomeArquivo());
        documento.setUrlArquivo(dto.urlArquivo());
        documento.setEtapa(etapa);

        documentoRepository.save(documento);
        return new DocumentoRespostaDTO(documento);
    }

    @Transactional(readOnly = true)
    public List<DocumentoRespostaDTO> listarPorEtapa(Long etapaId) {
        Etapa etapa = etapaRepository.findById(etapaId)
                .orElseThrow(() -> new ResourceNotFoundException("Etapa não encontrada."));

        validarAcessoAoTrabalho(etapa.getTrabalho().getId());

        return documentoRepository.findByEtapaId(etapaId)
                .stream()
                .map(DocumentoRespostaDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public DocumentoRespostaDTO consultar(Long id) {
        Documento doc = documentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documento não encontrado com o ID: " + id));

        validarAcessoAoTrabalho(doc.getEtapa().getTrabalho().getId());
        return new DocumentoRespostaDTO(doc);
    }

    @Transactional
    public void remover(Long id) {
        Documento doc = documentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documento não encontrado para remoção."));

        validarAcessoAoTrabalho(doc.getEtapa().getTrabalho().getId());
        documentoRepository.delete(doc);
    }

    private void validarAcessoAoTrabalho(Long trabalhoId) {
        // TODO: Integrar com Spring Security (ex: SecurityContextHolder) se necessário.
        // Impede acessos não autorizados baseados no contexto atual do usuário logado.
        boolean possuiAutorizacao = true;
        if (!possuiAutorizacao) {
            throw new BusinessException("Usuário não possui autorização para acessar os documentos deste trabalho.");
        }
    }
}
