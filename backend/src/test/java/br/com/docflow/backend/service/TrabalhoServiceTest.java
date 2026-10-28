
package br.com.docflow.backend.service;

import br.com.docflow.backend.entity.*;
import br.com.docflow.backend.repository.EmpresaRepository;
import br.com.docflow.backend.repository.HistoricoTrabalhoRepository;
import br.com.docflow.backend.repository.TrabalhoRepository;
import br.com.docflow.backend.repository.UsuarioRepository;
import br.com.docflow.backend.repository.UsuarioTrabalhoRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import java.time.LocalDateTime;
import br.com.docflow.backend.dto.TrabalhoCadastroDTO;
import br.com.docflow.backend.dto.TrabalhoStatusDTO;
import br.com.docflow.backend.repository.UsuarioEmpresaRepository;


@ExtendWith(MockitoExtension.class)
class TrabalhoServiceTest {

    @Mock
    private TrabalhoRepository trabalhoRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private HistoricoTrabalhoRepository historicoTrabalhoRepository;

    @Mock
    private UsuarioTrabalhoRepository usuarioTrabalhoRepository;

    @Mock
    private UsuarioEmpresaRepository usuarioEmpresaRepository;

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @InjectMocks
    private TrabalhoService trabalhoService;

    @Test
    void deveNegarAcessoQuandoUsuarioNaoForCriadorNemParticipante() {
        // Arrange: prepara dois usuários diferentes.
        Usuario usuarioAutenticado = new Usuario();
        usuarioAutenticado.setId(2L);

        Usuario criador = new Usuario();
        criador.setId(1L);

        Empresa empresa = new Empresa();
        empresa.setId(1L);

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setTitulo("Trabalho privado");
        trabalho.setCriadoPor(criador);
        trabalho.setEmpresa(empresa);

        when(usuarioAutenticadoService.obterUsuario())
                .thenReturn(usuarioAutenticado);

        when(trabalhoRepository.findById(10L))
                .thenReturn(Optional.of(trabalho));

        when(usuarioTrabalhoRepository
                .findByTrabalhoAndUsuario(trabalho, usuarioAutenticado))
                .thenReturn(Optional.empty());

        // Act + Assert: o acesso deve ser negado.
        assertThrows(
                AccessDeniedException.class,
                () -> trabalhoService.buscarPorId(10L)
        );
    }

    @Test
    void devePermitirAcessoAoCriadorDoTrabalho() {
        Usuario criador = new Usuario();
        criador.setId(1L);

        Empresa empresa = new Empresa();
        empresa.setId(1L);

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setTitulo("Trabalho do criador");
        trabalho.setDescricao("Descrição do trabalho");
        trabalho.setStatus(StatusTrabalho.PENDENTE);
        trabalho.setDataCriacao(LocalDateTime.now());
        trabalho.setDataAtualizacao(LocalDateTime.now());
        trabalho.setEmpresa(empresa);
        trabalho.setCriadoPor(criador);

        when(usuarioAutenticadoService.obterUsuario())
                .thenReturn(criador);

        when(trabalhoRepository.findById(10L))
                .thenReturn(Optional.of(trabalho));

        var resposta = trabalhoService.buscarPorId(10L);

        Assertions.assertEquals(10L, resposta.getId());
        Assertions.assertEquals("Trabalho do criador", resposta.getTitulo());
        Assertions.assertEquals(1L, resposta.getCriadoPorId());

    }

    @Test
    void deveNegarAtualizacaoQuandoUsuarioNaoForCriadorNemParticipante() {
        Usuario usuarioAutenticado = new Usuario();
        usuarioAutenticado.setId(2L);

        Usuario criador = new Usuario();
        criador.setId(1L);

        Empresa empresa = new Empresa();
        empresa.setId(1L);

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setTitulo("Trabalho privado");
        trabalho.setDescricao("Descrição original");
        trabalho.setStatus(StatusTrabalho.PENDENTE);
        trabalho.setDataCriacao(LocalDateTime.now());
        trabalho.setDataAtualizacao(LocalDateTime.now());
        trabalho.setEmpresa(empresa);
        trabalho.setCriadoPor(criador);

        when(usuarioAutenticadoService.obterUsuario())
                .thenReturn(usuarioAutenticado);

        when(trabalhoRepository.findById(10L))
                .thenReturn(Optional.of(trabalho));

        when(usuarioTrabalhoRepository
                .findByTrabalhoAndUsuario(trabalho, usuarioAutenticado))
                .thenReturn(Optional.empty());

        assertThrows(
                AccessDeniedException.class,
                () -> trabalhoService.atualizar(10L, null)
        );
    }


    @Test
    void deveUsarUsuarioAutenticadoComoCriadorAoCadastrarTrabalho() {
        Usuario usuarioAutenticado = new Usuario();
        usuarioAutenticado.setId(2L);

        Empresa empresa = new Empresa();
        empresa.setId(1L);

        TrabalhoCadastroDTO dto = new TrabalhoCadastroDTO();
        dto.setTitulo("Trabalho de teste");
        dto.setDescricao("Teste de segurança");
        dto.setEmpresaId(1L);
        dto.setCriadoPorId(999L);
        dto.setTipo(TipoTrabalho.EMPRESA);// ID diferente do usuário autenticado

        when(usuarioAutenticadoService.obterUsuario())
                .thenReturn(usuarioAutenticado);

        when(empresaRepository.findById(1L))
                .thenReturn(Optional.of(empresa));

        when(usuarioEmpresaRepository.existsByUsuarioAndEmpresa(
                usuarioAutenticado, empresa))
                .thenReturn(true);

        usuarioAutenticado.setEmpresa(empresa);

        when(trabalhoRepository.save(
                org.mockito.ArgumentMatchers.any(Trabalho.class)))
                .thenAnswer(invocacao -> {
                    Trabalho trabalhoSalvo = invocacao.getArgument(0);
                    trabalhoSalvo.setId(10L);
                    return trabalhoSalvo;
                });

        var resposta = trabalhoService.cadastrar(dto);

        org.mockito.ArgumentCaptor<Trabalho> captor =
                org.mockito.ArgumentCaptor.forClass(Trabalho.class);

        org.mockito.Mockito.verify(trabalhoRepository)
                .save(captor.capture());

        Trabalho trabalhoSalvo = captor.getValue();

        Assertions.assertEquals(2L, trabalhoSalvo.getCriadoPor().getId());
        Assertions.assertNotEquals(999L, trabalhoSalvo.getCriadoPor().getId());
        Assertions.assertEquals(2L, resposta.getCriadoPorId());
    }


    @Test
    void deveRegistrarUsuarioAutenticadoNoHistoricoAoAlterarStatus() {
        Usuario usuarioAutenticado = new Usuario();
        usuarioAutenticado.setId(2L);
        usuarioAutenticado.setNome("Usuário autenticado");

        Usuario criador = new Usuario();
        criador.setId(1L);

        Empresa empresa = new Empresa();
        empresa.setId(1L);

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setTitulo("Trabalho de teste");
        trabalho.setDescricao("Teste de autoria do histórico");
        trabalho.setStatus(StatusTrabalho.PENDENTE);
        trabalho.setDataCriacao(LocalDateTime.now());
        trabalho.setDataAtualizacao(LocalDateTime.now());
        trabalho.setEmpresa(empresa);
        trabalho.setCriadoPor(criador);

        TrabalhoStatusDTO dto = new TrabalhoStatusDTO();
        dto.setStatus(StatusTrabalho.EM_ANDAMENTO);
        dto.setUsuarioId(999L); // ID diferente do usuário autenticado

        when(usuarioAutenticadoService.obterUsuario())
                .thenReturn(usuarioAutenticado);

        when(trabalhoRepository.findById(10L))
                .thenReturn(Optional.of(trabalho));

        when(usuarioTrabalhoRepository
                .findByTrabalhoAndUsuario(trabalho, usuarioAutenticado))
                .thenReturn(Optional.empty());

        // O usuário autenticado é o criador? Não: simulamos que é participante.
        // Para este teste, permitimos o acesso por meio do vínculo de participação.
        when(usuarioTrabalhoRepository
                .findByTrabalhoAndUsuario(trabalho, usuarioAutenticado))
                .thenReturn(Optional.of(new br.com.docflow.backend.entity.UsuarioTrabalho()));

        when(trabalhoRepository.save(
                org.mockito.ArgumentMatchers.any(Trabalho.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        var resposta = trabalhoService.alterarStatus(10L, dto);

        org.mockito.ArgumentCaptor<br.com.docflow.backend.entity.HistoricoTrabalho>
                captor = org.mockito.ArgumentCaptor.forClass(
                br.com.docflow.backend.entity.HistoricoTrabalho.class);

        org.mockito.Mockito.verify(historicoTrabalhoRepository)
                .save(captor.capture());

        Assertions.assertEquals(
                2L,
                captor.getValue().getUsuario().getId()
        );
        Assertions.assertNotEquals(
                999L,
                captor.getValue().getUsuario().getId()
        );
    }


}
