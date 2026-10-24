
package br.com.docflow.backend.service;

import br.com.docflow.backend.entity.Empresa;
import br.com.docflow.backend.entity.PapelUsuarioEmpresa;
import br.com.docflow.backend.entity.Usuario;
import br.com.docflow.backend.entity.UsuarioEmpresa;
import br.com.docflow.backend.repository.EmpresaRepository;
import br.com.docflow.backend.repository.UsuarioEmpresaRepository;
import br.com.docflow.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioEmpresaServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private UsuarioEmpresaRepository usuarioEmpresaRepository;

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @InjectMocks
    private UsuarioEmpresaService usuarioEmpresaService;

    private Usuario criador;
    private Usuario membro;
    private Empresa empresa;
    private UsuarioEmpresa vinculoCriador;

    @BeforeEach
    void configurar() {
        empresa = new Empresa();
        empresa.setId(1L);

        criador = new Usuario();
        criador.setId(10L);
        criador.setEmail("criador@exemplo.com");

        membro = new Usuario();
        membro.setId(20L);
        membro.setEmail("membro@exemplo.com");

        vinculoCriador = new UsuarioEmpresa();
        vinculoCriador.setUsuario(criador);
        vinculoCriador.setEmpresa(empresa);
        vinculoCriador.setPapel(PapelUsuarioEmpresa.CRIADOR);

        when(usuarioAutenticadoService.obterUsuario())
                .thenReturn(criador);

        when(empresaRepository.findByIdComBloqueio(1L))
                .thenReturn(Optional.of(empresa));

        when(usuarioEmpresaRepository.findByUsuarioAndEmpresa(
                criador, empresa))
                .thenReturn(Optional.of(vinculoCriador));
    }

    @Test
    void deveAdicionarMembroQuandoCriadorEHaVaga() {
        when(usuarioRepository.findByEmail("membro@exemplo.com"))
                .thenReturn(Optional.of(membro));

        when(usuarioEmpresaRepository.existsByUsuarioAndEmpresa(
                membro, empresa))
                .thenReturn(false);

        when(usuarioEmpresaRepository.countByEmpresa(empresa))
                .thenReturn(1L);

        usuarioEmpresaService.adicionarMembro(
                1L, "membro@exemplo.com");

        verify(usuarioEmpresaRepository).save(any(UsuarioEmpresa.class));
    }

    @Test
    void deveRejeitarInclusaoQuandoUsuarioNaoECriador() {
        vinculoCriador.setPapel(PapelUsuarioEmpresa.MEMBRO);

        assertThrows(
                AccessDeniedException.class,
                () -> usuarioEmpresaService.adicionarMembro(
                        1L, "membro@exemplo.com")
        );

        verify(usuarioEmpresaRepository, never())
                .save(any(UsuarioEmpresa.class));
    }

    @Test
    void deveRejeitarInclusaoQuandoLimiteFoiAtingido() {
        when(usuarioRepository.findByEmail("membro@exemplo.com"))
                .thenReturn(Optional.of(membro));

        when(usuarioEmpresaRepository.existsByUsuarioAndEmpresa(
                membro, empresa))
                .thenReturn(false);

        when(usuarioEmpresaRepository.countByEmpresa(empresa))
                .thenReturn(5L);

        assertThrows(
                LimiteMembrosEmpresaException.class,
                () -> usuarioEmpresaService.adicionarMembro(
                        1L, "membro@exemplo.com")
        );

        verify(usuarioEmpresaRepository, never())
                .save(any(UsuarioEmpresa.class));
    }

    @Test
    void deveImpedirCriadorDeRemoverProprioVinculo() {
        assertThrows(
                AccessDeniedException.class,
                () -> usuarioEmpresaService.removerMembro(1L, 10L)
        );

        verify(usuarioEmpresaRepository, never())
                .delete(any(UsuarioEmpresa.class));
    }


    @Test
    void deveRejeitarInclusaoQuandoEmailNaoEstaCadastrado() {
        when(usuarioRepository.findByEmail("inexistente@exemplo.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                ParticipanteNaoEncontradoException.class,
                () -> usuarioEmpresaService.adicionarMembro(
                        1L, "inexistente@exemplo.com")
        );

        verify(usuarioEmpresaRepository, never())
                .save(any(UsuarioEmpresa.class));
    }

    @Test
    void deveRejeitarInclusaoQuandoUsuarioJaEstaVinculado() {
        when(usuarioRepository.findByEmail("membro@exemplo.com"))
                .thenReturn(Optional.of(membro));

        when(usuarioEmpresaRepository.existsByUsuarioAndEmpresa(
                membro, empresa))
                .thenReturn(true);

        assertThrows(
                ParticipanteJaAssociadoException.class,
                () -> usuarioEmpresaService.adicionarMembro(
                        1L, "membro@exemplo.com")
        );

        verify(usuarioEmpresaRepository, never())
                .save(any(UsuarioEmpresa.class));
    }


    @Test
    void deveAdicionarMembroComPapelMEMBRO() {
        when(usuarioRepository.findByEmail("membro@exemplo.com"))
                .thenReturn(Optional.of(membro));

        when(usuarioEmpresaRepository.existsByUsuarioAndEmpresa(
                membro, empresa))
                .thenReturn(false);

        when(usuarioEmpresaRepository.countByEmpresa(empresa))
                .thenReturn(1L);

        when(usuarioEmpresaRepository.save(any(UsuarioEmpresa.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        UsuarioEmpresa resultado = usuarioEmpresaService.adicionarMembro(
                1L, "membro@exemplo.com");

        org.junit.jupiter.api.Assertions.assertEquals(
                PapelUsuarioEmpresa.MEMBRO,
                resultado.getPapel()
        );
    }

    @Test
    void deveRejeitarRemocaoQuandoUsuarioNaoECriador() {
        vinculoCriador.setPapel(PapelUsuarioEmpresa.MEMBRO);

        assertThrows(
                AccessDeniedException.class,
                () -> usuarioEmpresaService.removerMembro(1L, 20L)
        );

        verify(usuarioEmpresaRepository, never())
                .delete(any(UsuarioEmpresa.class));
    }

    @Test
    void deveRemoverVinculoQuandoCriadorSolicita() {
        UsuarioEmpresa vinculoMembro = new UsuarioEmpresa();
        vinculoMembro.setUsuario(membro);
        vinculoMembro.setEmpresa(empresa);
        vinculoMembro.setPapel(PapelUsuarioEmpresa.MEMBRO);

        when(usuarioRepository.findById(20L))
                .thenReturn(Optional.of(membro));

        when(usuarioEmpresaRepository.findByUsuarioAndEmpresa(
                membro, empresa))
                .thenReturn(Optional.of(vinculoMembro));

        usuarioEmpresaService.removerMembro(1L, 20L);

        verify(usuarioEmpresaRepository).delete(vinculoMembro);
    }


}
