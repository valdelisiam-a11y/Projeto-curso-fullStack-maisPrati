
        package br.com.docflow.backend.service;

import br.com.docflow.backend.entity.Empresa;
import br.com.docflow.backend.entity.Trabalho;
import br.com.docflow.backend.entity.Usuario;
import br.com.docflow.backend.entity.UsuarioTrabalho;
import br.com.docflow.backend.repository.EmpresaRepository;
import br.com.docflow.backend.repository.TrabalhoRepository;
import br.com.docflow.backend.repository.UsuarioEmpresaRepository;
import br.com.docflow.backend.repository.UsuarioRepository;
import br.com.docflow.backend.repository.UsuarioTrabalhoRepository;
import br.com.docflow.backend.service.UsuarioEmpresaDiferenteException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioTrabalhoServiceTest {

    @Mock
    private UsuarioTrabalhoRepository usuarioTrabalhoRepository;

    @Mock
    private TrabalhoRepository trabalhoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioEmpresaRepository usuarioEmpresaRepository;

    @InjectMocks
    private UsuarioTrabalhoService usuarioTrabalhoService;

    private Trabalho trabalho;
    private Usuario usuario;
    private Empresa empresa;

    @BeforeEach
    void configurar() {
        empresa = new Empresa();
        empresa.setId(1L);

        trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setEmpresa(empresa);

        usuario = new Usuario();
        usuario.setId(2L);
    }

    @Test
    void deveAdicionarParticipanteComVinculoNaEmpresaDoTrabalho() {
        when(trabalhoRepository.findById(10L))
                .thenReturn(Optional.of(trabalho));

        when(usuarioRepository.findById(2L))
                .thenReturn(Optional.of(usuario));

        when(usuarioEmpresaRepository.existsByUsuarioAndEmpresa(usuario, empresa))
                .thenReturn(true);

        when(usuarioTrabalhoRepository.findByTrabalhoAndUsuario(trabalho, usuario))
                .thenReturn(Optional.empty());

        when(usuarioTrabalhoRepository.save(any(UsuarioTrabalho.class)))
                .thenAnswer(invocacao -> {
                    UsuarioTrabalho participante = invocacao.getArgument(0);
                    participante.setId(100L);
                    return participante;
                });

        usuarioTrabalhoService.adicionarParticipante(10L, 2L);

        verify(usuarioTrabalhoRepository).save(any(UsuarioTrabalho.class));
    }

    @Test
    void deveRejeitarParticipanteSemVinculoNaEmpresaDoTrabalho() {
        when(trabalhoRepository.findById(10L))
                .thenReturn(Optional.of(trabalho));

        when(usuarioRepository.findById(2L))
                .thenReturn(Optional.of(usuario));

        when(usuarioEmpresaRepository.existsByUsuarioAndEmpresa(usuario, empresa))
                .thenReturn(false);

        assertThrows(
                UsuarioEmpresaDiferenteException.class,
                () -> usuarioTrabalhoService.adicionarParticipante(10L, 2L)
        );
    }
}
