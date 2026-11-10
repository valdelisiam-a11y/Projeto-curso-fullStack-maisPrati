
package br.com.docflow.backend.service;

import br.com.docflow.backend.dto.DocumentoRespostaDTO;
import br.com.docflow.backend.entity.Documento;
import br.com.docflow.backend.entity.Etapa;
import br.com.docflow.backend.repository.DocumentoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockMultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentoServiceTest {

    @Mock
    private DocumentoRepository documentoRepository;

    @Mock
    private EtapaService etapaService;

    @Mock
    private S3Client s3Client;

    private DocumentoService documentoService;

    private Etapa etapa;

    @BeforeEach
    void configurar() {
        documentoService = new DocumentoService(
                documentoRepository,
                etapaService,
                s3Client,
                "docflow-documentos"
        );

        etapa = new Etapa();
        etapa.setId(10L);

        when(etapaService.buscarEntidadeComAcesso(10L))
                .thenReturn(etapa);
    }

    private Documento criarDocumento() {
        Documento documento = new Documento();
        documento.setId(1L);
        documento.setNomeOriginal("relatorio.pdf");
        documento.setChaveArquivo("arquivo-teste.pdf");
        documento.setContentType("application/pdf");
        documento.setTamanho(15L);
        documento.setDataUpload(LocalDateTime.now());
        documento.setEtapa(etapa);

        return documento;
    }

    @Test
    void deveEnviarArquivoValidoERegistrarNoBanco() {
        byte[] conteudo = "%PDF-1.4 documento de teste"
                .getBytes();

        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "relatorio.pdf",
                "application/pdf",
                conteudo
        );

        when(documentoRepository.save(any(Documento.class)))
                .thenAnswer(invocacao -> {
                    Documento documento = invocacao.getArgument(0);
                    documento.setId(1L);
                    documento.setDataUpload(LocalDateTime.now());
                    return documento;
                });

        DocumentoRespostaDTO resposta =
                documentoService.enviar(10L, arquivo);

        assertEquals(1L, resposta.getId());
        assertEquals("relatorio.pdf", resposta.getNomeOriginal());
        assertEquals("application/pdf", resposta.getContentType());
        assertEquals((long) conteudo.length, resposta.getTamanho());
        assertEquals(10L, resposta.getEtapaId());

        verify(s3Client).putObject(
                any(PutObjectRequest.class),
                any(RequestBody.class)
        );

        verify(documentoRepository).save(any(Documento.class));
    }

    @Test
    void deveRejeitarArquivoNulo() {
        assertThrows(
                IllegalArgumentException.class,
                () -> documentoService.enviar(10L, null)
        );

        verifyNoInteractions(s3Client, documentoRepository);
    }

    @Test
    void deveRejeitarArquivoVazio() {
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "relatorio.pdf",
                "application/pdf",
                new byte[0]
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> documentoService.enviar(10L, arquivo)
        );

        verifyNoInteractions(s3Client, documentoRepository);
    }

    @Test
    void deveRejeitarArquivoMaiorQueDezMb() {
        byte[] conteudo = new byte[10 * 1024 * 1024 + 1];

        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "relatorio.pdf",
                "application/pdf",
                conteudo
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> documentoService.enviar(10L, arquivo)
        );

        assertEquals(
                "O arquivo não pode ultrapassar 10 MB.",
                exception.getMessage()
        );

        verifyNoInteractions(s3Client, documentoRepository);
    }

    @Test
    void deveRejeitarExtensaoNaoPermitida() {
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "relatorio.exe",
                "application/octet-stream",
                "conteudo".getBytes()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> documentoService.enviar(10L, arquivo)
        );

        verifyNoInteractions(s3Client, documentoRepository);
    }

    @Test
    void deveRejeitarConteudoIncompativelComExtensao() {
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "relatorio.pdf",
                "application/pdf",
                "conteudo que nao e pdf".getBytes()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> documentoService.enviar(10L, arquivo)
        );

        verifyNoInteractions(s3Client, documentoRepository);
    }

    @Test
    void deveExcluirArquivoDoB2QuandoFalharSalvamentoNoBanco() {
        byte[] conteudo = "%PDF-1.4 documento de teste"
                .getBytes();

        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo",
                "relatorio.pdf",
                "application/pdf",
                conteudo
        );

        when(documentoRepository.save(any(Documento.class)))
                .thenThrow(new RuntimeException("Falha no banco"));

        assertThrows(
                RuntimeException.class,
                () -> documentoService.enviar(10L, arquivo)
        );

        verify(s3Client).putObject(
                any(PutObjectRequest.class),
                any(RequestBody.class)
        );

        verify(s3Client).deleteObject(
                org.mockito.ArgumentMatchers.any(
                        software.amazon.awssdk.services.s3.model.DeleteObjectRequest.class
                )
        );
    }

    @Test
    void deveListarDocumentosDaEtapa() {
        Documento documento = criarDocumento();

        when(documentoRepository
                .findByEtapaIdOrderByDataUploadDesc(10L))
                .thenReturn(List.of(documento));

        List<DocumentoRespostaDTO> resposta =
                documentoService.listar(10L);

        assertEquals(1, resposta.size());
        assertEquals(1L, resposta.get(0).getId());
        assertEquals("relatorio.pdf", resposta.get(0).getNomeOriginal());
        assertEquals(10L, resposta.get(0).getEtapaId());
    }

    @Test
    void deveBaixarDocumentoDaEtapa() {
        Documento documento = criarDocumento();
        byte[] conteudo = "%PDF-1.4 documento".getBytes();

        when(documentoRepository.findByIdAndEtapaId(1L, 10L))
                .thenReturn(Optional.of(documento));

        when(s3Client.getObjectAsBytes(any(GetObjectRequest.class)))
                .thenReturn(ResponseBytes.fromByteArray(
                        GetObjectResponse.builder().build(),
                        conteudo
                ));

        DocumentoService.DocumentoDownload resposta =
                documentoService.baixar(10L, 1L);

        assertEquals("relatorio.pdf", resposta.getNomeOriginal());
        assertEquals("application/pdf", resposta.getContentType());
        assertArrayEquals(conteudo, resposta.getConteudo());
    }

    @Test
    void deveExcluirDocumentoDoB2EDoBanco() {
        Documento documento = criarDocumento();

        when(documentoRepository.findByIdAndEtapaId(1L, 10L))
                .thenReturn(Optional.of(documento));

        documentoService.excluir(10L, 1L);

        verify(s3Client).deleteObject(any(
                software.amazon.awssdk.services.s3.model.DeleteObjectRequest.class
        ));

        verify(documentoRepository).delete(documento);
    }

    @Test
    void deveRejeitarDownloadDeDocumentoInexistente() {
        when(documentoRepository.findByIdAndEtapaId(1L, 10L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> documentoService.baixar(10L, 1L)
        );

        verifyNoInteractions(s3Client);
    }
}
