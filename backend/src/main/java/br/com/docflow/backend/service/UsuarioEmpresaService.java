
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

}
