
package br.com.docflow.backend.service;

import br.com.docflow.backend.dto.DocumentoRespostaDTO;
import br.com.docflow.backend.entity.Documento;
import br.com.docflow.backend.entity.Etapa;
import br.com.docflow.backend.repository.DocumentoRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class DocumentoService {

    private static final long TAMANHO_MAXIMO = 10 * 1024 * 1024;

    private final DocumentoRepository documentoRepository;
    private final EtapaService etapaService;
    private final S3Client s3Client;
    private final String bucketName;

    public DocumentoService(
            DocumentoRepository documentoRepository,
            EtapaService etapaService,
            S3Client s3Client,
            @Value("${b2.bucket-name}") String bucketName) {

        this.documentoRepository = documentoRepository;
        this.etapaService = etapaService;
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    @Transactional
    public DocumentoRespostaDTO enviar(Long etapaId, MultipartFile arquivo) {

        Etapa etapa = etapaService.buscarEntidadeComAcesso(etapaId);

        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Selecione um arquivo.");
        }

        if (arquivo.getSize() > TAMANHO_MAXIMO) {
            throw new IllegalArgumentException(
                    "O arquivo não pode ultrapassar 10 MB.");
        }

        String nomeOriginal = arquivo.getOriginalFilename();

        if (nomeOriginal == null || nomeOriginal.isBlank()
                || nomeOriginal.length() > 255) {
            throw new IllegalArgumentException("Nome de arquivo inválido.");
        }

        nomeOriginal = nomeOriginal.replace('\\', '/');
        nomeOriginal = nomeOriginal.substring(
                nomeOriginal.lastIndexOf('/') + 1);

        String extensao = obterExtensao(nomeOriginal);

        byte[] conteudo;

        try {
            conteudo = arquivo.getBytes();
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Não foi possível ler o arquivo.", e);
        }

        String contentType = validarArquivo(extensao, conteudo);

        String chaveArquivo = UUID.randomUUID() + "." + extensao;

        PutObjectRequest requisicao = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(chaveArquivo)
                .contentType(contentType)
                .build();

        s3Client.putObject(
                requisicao,
                RequestBody.fromBytes(conteudo));

        try {
            Documento documento = new Documento();
            documento.setNomeOriginal(nomeOriginal);
            documento.setChaveArquivo(chaveArquivo);
            documento.setContentType(contentType);
            documento.setTamanho((long) conteudo.length);
            documento.setEtapa(etapa);

            Documento salvo = documentoRepository.save(documento);

            return converterParaDTO(salvo);

        } catch (RuntimeException e) {
            try {
                excluirObjetoB2(chaveArquivo);
            } catch (RuntimeException erroExclusao) {
                e.addSuppressed(erroExclusao);
            }

            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<DocumentoRespostaDTO> listar(Long etapaId) {

        etapaService.buscarEntidadeComAcesso(etapaId);

        return documentoRepository
                .findByEtapaIdOrderByDataUploadDesc(etapaId)
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public DocumentoDownload baixar(Long etapaId, Long documentoId) {

        etapaService.buscarEntidadeComAcesso(etapaId);

        Documento documento = documentoRepository
                .findByIdAndEtapaId(documentoId, etapaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Documento não encontrado."));

        byte[] conteudo = s3Client.getObjectAsBytes(
                GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(documento.getChaveArquivo())
                        .build()
        ).asByteArray();

        return new DocumentoDownload(
                documento.getNomeOriginal(),
                documento.getContentType(),
                conteudo
        );
    }

    @Transactional
    public void excluir(Long etapaId, Long documentoId) {

        etapaService.buscarEntidadeComAcesso(etapaId);

        Documento documento = documentoRepository
                .findByIdAndEtapaId(documentoId, etapaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Documento não encontrado."));

        excluirObjetoB2(documento.getChaveArquivo());

        documentoRepository.delete(documento);
    }

    private String obterExtensao(String nomeArquivo) {

        int posicao = nomeArquivo.lastIndexOf('.');

        if (posicao <= 0 || posicao == nomeArquivo.length() - 1) {
            throw new IllegalArgumentException(
                    "Formato de arquivo não permitido.");
        }

        String extensao = nomeArquivo.substring(posicao + 1)
                .toLowerCase(Locale.ROOT);

        List<String> permitidas = List.of(
                "pdf", "doc", "docx",
                "xls", "xlsx", "jpg", "jpeg", "png"
        );

        if (!permitidas.contains(extensao)) {
            throw new IllegalArgumentException(
                    "Formato de arquivo não permitido.");
        }

        return extensao;
    }

    private String validarArquivo(String extensao, byte[] bytes) {

        switch (extensao) {
            case "pdf" -> {
                if (!comecaCom(bytes, "%PDF-".getBytes())) {
                    throw arquivoInvalido();
                }
                return "application/pdf";
            }

            case "jpg", "jpeg" -> {
                if (bytes.length < 3
                        || (bytes[0] & 0xFF) != 0xFF
                        || (bytes[1] & 0xFF) != 0xD8
                        || (bytes[2] & 0xFF) != 0xFF) {
                    throw arquivoInvalido();
                }
                return "image/jpeg";
            }

            case "png" -> {
                byte[] assinatura = {
                        (byte) 0x89, 0x50, 0x4E, 0x47,
                        0x0D, 0x0A, 0x1A, 0x0A
                };

                if (!comecaCom(bytes, assinatura)) {
                    throw arquivoInvalido();
                }
                return "image/png";
            }

            case "doc", "xls" -> {
                byte[] assinatura = {
                        (byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0,
                        (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1
                };

                if (!comecaCom(bytes, assinatura)) {
                    throw arquivoInvalido();
                }

                return extensao.equals("doc")
                        ? "application/msword"
                        : "application/vnd.ms-excel";
            }

            case "docx" -> {
                if (!validarEstruturaZip(bytes, "word/document.xml")) {
                    throw arquivoInvalido();
                }
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            }

            case "xlsx" -> {
                if (!validarEstruturaZip(bytes, "xl/workbook.xml")) {
                    throw arquivoInvalido();
                }
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            }

            default -> throw arquivoInvalido();
        }
    }

    private boolean validarEstruturaZip(byte[] bytes, String arquivoEsperado) {

        boolean possuiContentTypes = false;
        boolean possuiArquivoEsperado = false;

        try (ZipInputStream zip = new ZipInputStream(
                new ByteArrayInputStream(bytes))) {

            ZipEntry entrada;

            while ((entrada = zip.getNextEntry()) != null) {

                if (entrada.getName().equals("[Content_Types].xml")) {
                    possuiContentTypes = true;
                }

                if (entrada.getName().equals(arquivoEsperado)) {
                    possuiArquivoEsperado = true;
                }

                if (possuiContentTypes && possuiArquivoEsperado) {
                    return true;
                }
            }

        } catch (IOException e) {
            return false;
        }

        return false;
    }

    private boolean comecaCom(byte[] conteudo, byte[] assinatura) {

        if (conteudo.length < assinatura.length) {
            return false;
        }

        for (int i = 0; i < assinatura.length; i++) {
            if (conteudo[i] != assinatura[i]) {
                return false;
            }
        }

        return true;
    }

    private IllegalArgumentException arquivoInvalido() {
        return new IllegalArgumentException(
                "O conteúdo do arquivo não corresponde ao formato informado.");
    }

    private DocumentoRespostaDTO converterParaDTO(Documento documento) {

        DocumentoRespostaDTO dto = new DocumentoRespostaDTO();

        dto.setId(documento.getId());
        dto.setNomeOriginal(documento.getNomeOriginal());
        dto.setContentType(documento.getContentType());
        dto.setTamanho(documento.getTamanho());
        dto.setDataUpload(documento.getDataUpload());
        dto.setEtapaId(documento.getEtapa().getId());

        return dto;
    }

    private void excluirObjetoB2(String chaveArquivo) {

        s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(chaveArquivo)
                        .build()
        );
    }

    public static class DocumentoDownload {

        private final String nomeOriginal;
        private final String contentType;
        private final byte[] conteudo;

        public DocumentoDownload(
                String nomeOriginal,
                String contentType,
                byte[] conteudo) {
            this.nomeOriginal = nomeOriginal;
            this.contentType = contentType;
            this.conteudo = conteudo;
        }

        public String getNomeOriginal() {
            return nomeOriginal;
        }

        public String getContentType() {
            return contentType;
        }

        public byte[] getConteudo() {
            return conteudo;
        }
    }
}
