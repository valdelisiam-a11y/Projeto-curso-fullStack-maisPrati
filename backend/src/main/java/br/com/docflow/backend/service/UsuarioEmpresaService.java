
package br.com.docflow.backend.service;

import br.com.docflow.backend.entity.Empresa;
import br.com.docflow.backend.entity.PapelUsuarioEmpresa;
import br.com.docflow.backend.entity.Usuario;
import br.com.docflow.backend.entity.UsuarioEmpresa;
import br.com.docflow.backend.repository.EmpresaRepository;
import br.com.docflow.backend.repository.UsuarioEmpresaRepository;
import br.com.docflow.backend.repository.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.docflow.backend.dto.EmpresaResumoDTO;
import br.com.docflow.backend.dto.MembroEmpresaDTO;

@Service
public class UsuarioEmpresaService {

    private static final long LIMITE_MEMBROS = 5;

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioEmpresaRepository usuarioEmpresaRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public UsuarioEmpresaService(
            UsuarioRepository usuarioRepository,
            EmpresaRepository empresaRepository,
            UsuarioEmpresaRepository usuarioEmpresaRepository,
            UsuarioAutenticadoService usuarioAutenticadoService) {
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
        this.usuarioEmpresaRepository = usuarioEmpresaRepository;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @Transactional
    public UsuarioEmpresa adicionarMembro(Long empresaId, String email) {
        Usuario criador = usuarioAutenticadoService.obterUsuario();

        Empresa empresa = empresaRepository.findByIdComBloqueio(empresaId)
                .orElseThrow(() -> new ParticipanteNaoEncontradoException(
                        "Empresa não encontrada"
                ));

        UsuarioEmpresa vinculoCriador =
                usuarioEmpresaRepository
                        .findByUsuarioAndEmpresa(criador, empresa)
                        .orElseThrow(() -> new AccessDeniedException(
                                "Você não pertence a esta empresa"
                        ));

        if (vinculoCriador.getPapel() != PapelUsuarioEmpresa.CRIADOR) {
            throw new AccessDeniedException(
                    "Somente o criador da empresa pode adicionar membros"
            );
        }

        Usuario membro = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ParticipanteNaoEncontradoException(
                        "Não existe usuário cadastrado com esse e-mail"
                ));

        if (usuarioEmpresaRepository.existsByUsuarioAndEmpresa(
                membro, empresa)) {
            throw new ParticipanteJaAssociadoException(
                    "Este usuário já pertence à empresa"
            );
        }

        if (usuarioEmpresaRepository.countByEmpresa(empresa)
                >= LIMITE_MEMBROS) {
            throw new LimiteMembrosEmpresaException(
                    "A empresa já atingiu o limite de cinco usuários"
            );
        }

        UsuarioEmpresa vinculo = new UsuarioEmpresa();
        vinculo.setUsuario(membro);
        vinculo.setEmpresa(empresa);
        vinculo.setPapel(PapelUsuarioEmpresa.MEMBRO);

        return usuarioEmpresaRepository.save(vinculo);
    }

    @Transactional
    public void removerMembro(Long empresaId, Long usuarioId) {
        Usuario criador = usuarioAutenticadoService.obterUsuario();

        Empresa empresa = empresaRepository.findByIdComBloqueio(empresaId)
                .orElseThrow(() -> new ParticipanteNaoEncontradoException(
                        "Empresa não encontrada"
                ));

        UsuarioEmpresa vinculoCriador =
                usuarioEmpresaRepository
                        .findByUsuarioAndEmpresa(criador, empresa)
                        .orElseThrow(() -> new AccessDeniedException(
                                "Você não pertence a esta empresa"
                        ));

        if (vinculoCriador.getPapel() != PapelUsuarioEmpresa.CRIADOR) {
            throw new AccessDeniedException(
                    "Somente o criador da empresa pode remover membros"
            );
        }

        if (criador.getId().equals(usuarioId)) {
            throw new AccessDeniedException(
                    "O criador não pode remover o próprio vínculo"
            );
        }

        Usuario membro = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ParticipanteNaoEncontradoException(
                        "Usuário não encontrado"
                ));

        UsuarioEmpresa vinculoMembro =
                usuarioEmpresaRepository
                        .findByUsuarioAndEmpresa(membro, empresa)
                        .orElseThrow(() -> new ParticipanteNaoEncontradoException(
                                "Este usuário não pertence à empresa"
                        ));

        usuarioEmpresaRepository.delete(vinculoMembro);
    }

    @Transactional
    public Empresa cadastrarEmpresa(String nome, String cnpj) {
        Usuario criador = usuarioAutenticadoService.obterUsuario();

        Empresa empresa = new Empresa();
        empresa.setNome(nome.trim());
        empresa.setCnpj(
                cnpj == null || cnpj.isBlank() ? null : cnpj.trim()
        );
        empresa.setDataCadastro(java.time.LocalDateTime.now());
        empresa.setAtivo(true);

        Empresa empresaSalva = empresaRepository.save(empresa);

        UsuarioEmpresa vinculo = new UsuarioEmpresa();
        vinculo.setUsuario(criador);
        vinculo.setEmpresa(empresaSalva);
        vinculo.setPapel(PapelUsuarioEmpresa.CRIADOR);

        usuarioEmpresaRepository.save(vinculo);

        return empresaSalva;
    }

    @Transactional(readOnly = true)
    public java.util.List<EmpresaResumoDTO> listarMinhasEmpresas() {
        Usuario usuario = usuarioAutenticadoService.obterUsuario();

        return usuarioEmpresaRepository.findByUsuario(usuario)
                .stream()
                .map(vinculo -> {
                    EmpresaResumoDTO dto = new EmpresaResumoDTO();
                    dto.setId(vinculo.getEmpresa().getId());
                    dto.setNome(vinculo.getEmpresa().getNome());
                    dto.setPapel(vinculo.getPapel().name());
                    return dto;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public java.util.List<MembroEmpresaDTO> listarMembros(Long empresaId) {
        Usuario usuario = usuarioAutenticadoService.obterUsuario();

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ParticipanteNaoEncontradoException(
                        "Empresa não encontrada"
                ));

        usuarioEmpresaRepository.findByUsuarioAndEmpresa(usuario, empresa)
                .orElseThrow(() -> new AccessDeniedException(
                        "Você não pertence a esta empresa"
                ));

        return usuarioEmpresaRepository.findByEmpresa(empresa)
                .stream()
                .map(vinculo -> {
                    MembroEmpresaDTO dto = new MembroEmpresaDTO();
                    dto.setUsuarioId(vinculo.getUsuario().getId());
                    dto.setNome(vinculo.getUsuario().getNome());
                    dto.setEmail(vinculo.getUsuario().getEmail());
                    dto.setPapel(vinculo.getPapel().name());
                    return dto;
                })
                .toList();
    }

}
