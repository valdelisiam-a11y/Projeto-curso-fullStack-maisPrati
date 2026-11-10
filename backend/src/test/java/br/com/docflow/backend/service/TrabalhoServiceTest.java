
package br.com.docflow.backend.service;

import br.com.docflow.backend.entity.*;
import br.com.docflow.backend.repository.*;

import br.com.docflow.backend.entity.Etapa;
import br.com.docflow.backend.entity.UsuarioTrabalho;
import br.com.docflow.backend.entity.Empresa;

import br.com.docflow.backend.dto.TrabalhoCadastroDTO;
import br.com.docflow.backend.dto.TrabalhoStatusDTO;
import br.com.docflow.backend.dto.TrabalhoAtualizacaoDTO;
import br.com.docflow.backend.dto.LinhaDoTempoRespostaDTO;

import br.com.docflow.backend.repository.HistoricoEtapaRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import br.com.docflow.backend.service.RecursoNaoEncontradoException;
import static org.mockito.Mockito.any;
import org.springframework.security.access.AccessDeniedException;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Assertions;
import java.time.LocalDateTime;

import java.util.Optional;
import java.util.List;


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
    private EventoTrabalhoRepository eventoTrabalhoRepository;

    @Mock
    private UsuarioTrabalhoRepository usuarioTrabalhoRepository;

    @Mock
    private UsuarioEmpresaRepository usuarioEmpresaRepository;

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @Mock
    private EtapaRepository etapaRepository;

    @Mock
    private HistoricoEtapaRepository historicoEtapaRepository;

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

        when(etapaRepository.findByTrabalhoIdOrderByOrdemAsc(10L))
                .thenReturn(List.of());

        var resposta = trabalhoService.buscarPorId(10L);

        assertEquals(10L, resposta.getId());
        assertEquals("Trabalho do criador", resposta.getTitulo());
        assertEquals(1L, resposta.getCriadoPorId());

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


        when(trabalhoRepository.save(
                org.mockito.ArgumentMatchers.any(Trabalho.class)))
                .thenAnswer(invocacao -> {
                    Trabalho trabalhoSalvo = invocacao.getArgument(0);
                    trabalhoSalvo.setId(10L);
                    return trabalhoSalvo;
                });

        when(etapaRepository.findByTrabalhoIdOrderByOrdemAsc(10L))
                .thenReturn(List.of());

        var resposta = trabalhoService.cadastrar(dto);

        org.mockito.ArgumentCaptor<Trabalho> captor =
                org.mockito.ArgumentCaptor.forClass(Trabalho.class);

        org.mockito.Mockito.verify(trabalhoRepository)
                .save(captor.capture());

        Trabalho trabalhoSalvo = captor.getValue();

        assertEquals(2L, trabalhoSalvo.getCriadoPor().getId());
        Assertions.assertNotEquals(999L, trabalhoSalvo.getCriadoPor().getId());
        assertEquals(2L, resposta.getCriadoPorId());
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

        when(etapaRepository.findByTrabalhoIdOrderByOrdemAsc(10L))
                .thenReturn(List.of());

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

        assertEquals(
                2L,
                captor.getValue().getUsuario().getId()
        );
        Assertions.assertNotEquals(
                999L,
                captor.getValue().getUsuario().getId()
        );
    }


    @Test
    void deveAtualizarTituloDoTrabalho() {
        Usuario usuario = criarUsuarioTeste(1L);
        Trabalho trabalho = criarTrabalhoTeste(usuario);

        TrabalhoAtualizacaoDTO dto = new TrabalhoAtualizacaoDTO();
        dto.setTitulo("Novo título");
        dto.setDescricao("Descrição original");

        prepararAtualizacao(trabalho, usuario);

        when(trabalhoRepository.save(trabalho))
                .thenReturn(trabalho);

        trabalhoService.atualizar(10L, dto);

        assertEquals("Novo título", trabalho.getTitulo());

        org.mockito.Mockito.verify(eventoTrabalhoRepository)
                .save(org.mockito.ArgumentMatchers.any(EventoTrabalho.class));

        org.mockito.Mockito.verify(trabalhoRepository).save(trabalho);
    }

    @Test
    void deveAtualizarDescricaoDoTrabalho() {
        Usuario usuario = criarUsuarioTeste(1L);
        Trabalho trabalho = criarTrabalhoTeste(usuario);

        TrabalhoAtualizacaoDTO dto = new TrabalhoAtualizacaoDTO();
        dto.setTitulo("Trabalho original");
        dto.setDescricao("Nova descrição");

        prepararAtualizacao(trabalho, usuario);

        when(trabalhoRepository.save(trabalho))
                .thenReturn(trabalho);

        trabalhoService.atualizar(10L, dto);

        assertEquals("Nova descrição", trabalho.getDescricao());

        org.mockito.Mockito.verify(eventoTrabalhoRepository)
                .save(org.mockito.ArgumentMatchers.any(EventoTrabalho.class));

        org.mockito.Mockito.verify(trabalhoRepository).save(trabalho);
    }

    @Test
    void deveAtualizarTituloEDescricaoDoTrabalho() {
        Usuario usuario = criarUsuarioTeste(1L);
        Trabalho trabalho = criarTrabalhoTeste(usuario);

        TrabalhoAtualizacaoDTO dto = new TrabalhoAtualizacaoDTO();
        dto.setTitulo("Novo título");
        dto.setDescricao("Nova descrição");

        prepararAtualizacao(trabalho, usuario);

        when(trabalhoRepository.save(trabalho))
                .thenReturn(trabalho);

        trabalhoService.atualizar(10L, dto);

        assertEquals("Novo título", trabalho.getTitulo());
        assertEquals("Nova descrição", trabalho.getDescricao());

        org.mockito.Mockito.verify(eventoTrabalhoRepository,
                        org.mockito.Mockito.times(2))
                .save(org.mockito.ArgumentMatchers.any(EventoTrabalho.class));

        org.mockito.Mockito.verify(trabalhoRepository).save(trabalho);
    }

    @Test
    void naoDeveSalvarTrabalhoQuandoNenhumCampoForAlterado() {
        Usuario usuario = criarUsuarioTeste(1L);
        Trabalho trabalho = criarTrabalhoTeste(usuario);

        TrabalhoAtualizacaoDTO dto = new TrabalhoAtualizacaoDTO();
        dto.setTitulo("Trabalho original");
        dto.setDescricao("Descrição original");

        prepararAtualizacao(trabalho, usuario);

        trabalhoService.atualizar(10L, dto);

        org.mockito.Mockito.verify(trabalhoRepository,
                        org.mockito.Mockito.never())
                .save(org.mockito.ArgumentMatchers.any(Trabalho.class));

        org.mockito.Mockito.verify(eventoTrabalhoRepository,
                        org.mockito.Mockito.never())
                .save(org.mockito.ArgumentMatchers.any(EventoTrabalho.class));
    }

    private Usuario criarUsuarioTeste(Long id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome("Usuário teste");
        return usuario;
    }

    private Trabalho criarTrabalhoTeste(Usuario usuario) {
        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setTitulo("Trabalho original");
        trabalho.setDescricao("Descrição original");
        trabalho.setTipo(TipoTrabalho.EMPRESA);
        trabalho.setStatus(StatusTrabalho.PENDENTE);
        trabalho.setDataCriacao(LocalDateTime.now());
        trabalho.setDataAtualizacao(LocalDateTime.now());
        trabalho.setCriadoPor(usuario);
        return trabalho;
    }

    private void prepararAtualizacao(Trabalho trabalho, Usuario usuario) {
        when(usuarioAutenticadoService.obterUsuario())
                .thenReturn(usuario);

        when(trabalhoRepository.findById(10L))
                .thenReturn(Optional.of(trabalho));

        when(etapaRepository.findByTrabalhoIdOrderByOrdemAsc(10L))
                .thenReturn(List.of());

    }

    @Test
    void deveConcluirTrabalhoQuandoTodasAsEtapasEstiveremConcluidas() {

        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setCriadoPor(usuario);
        trabalho.setTipo(TipoTrabalho.PESSOAL);
        trabalho.setStatus(StatusTrabalho.EM_ANDAMENTO);

        Etapa etapa1 = new Etapa();
        etapa1.setStatus(StatusTrabalho.CONCLUIDO);

        Etapa etapa2 = new Etapa();
        etapa2.setStatus(StatusTrabalho.CONCLUIDO);

        when(usuarioAutenticadoService.obterUsuario()).thenReturn(usuario);
        when(trabalhoRepository.findById(10L))
                .thenReturn(Optional.of(trabalho));
        when(etapaRepository.findByTrabalhoIdOrderByOrdemAsc(10L))
                .thenReturn(List.of(etapa1, etapa2));
        when(trabalhoRepository.save(any(Trabalho.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        trabalhoService.sincronizarStatusComEtapas(10L);

        assertEquals(StatusTrabalho.CONCLUIDO, trabalho.getStatus());
        assertNotNull(trabalho.getDataConclusao());
        assertNotNull(trabalho.getDataAtualizacao());

        verify(trabalhoRepository).save(trabalho);
        verify(historicoTrabalhoRepository).save(
                argThat(historico ->
                        historico.getStatusAnterior() == StatusTrabalho.EM_ANDAMENTO
                                && historico.getStatusNovo() == StatusTrabalho.CONCLUIDO
                                && historico.getUsuario().equals(usuario)
                )
        );
    }

    @Test
    void deveManterTrabalhoEmAndamentoQuandoUmaEtapaEstiverEmAndamento() {

        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setCriadoPor(usuario);
        trabalho.setTipo(TipoTrabalho.PESSOAL);
        trabalho.setStatus(StatusTrabalho.PENDENTE);

        Etapa etapa1 = new Etapa();
        etapa1.setStatus(StatusTrabalho.EM_ANDAMENTO);
        etapa1.setPercentualConclusao(30);

        Etapa etapa2 = new Etapa();
        etapa2.setStatus(StatusTrabalho.PENDENTE);
        etapa2.setPercentualConclusao(0);

        when(usuarioAutenticadoService.obterUsuario()).thenReturn(usuario);
        when(trabalhoRepository.findById(10L))
                .thenReturn(Optional.of(trabalho));
        when(etapaRepository.findByTrabalhoIdOrderByOrdemAsc(10L))
                .thenReturn(List.of(etapa1, etapa2));
        when(trabalhoRepository.save(any(Trabalho.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        trabalhoService.sincronizarStatusComEtapas(10L);

        assertEquals(StatusTrabalho.EM_ANDAMENTO, trabalho.getStatus());
        assertNotNull(trabalho.getDataAtualizacao());
        assertNull(trabalho.getDataConclusao());

        verify(trabalhoRepository).save(trabalho);
        verify(historicoTrabalhoRepository).save(
                argThat(historico ->
                        historico.getStatusAnterior() == StatusTrabalho.PENDENTE
                                && historico.getStatusNovo() == StatusTrabalho.EM_ANDAMENTO
                                && historico.getUsuario().equals(usuario)
                )
        );
    }

    @Test
    void deveRetornarTrabalhoParaPendenteQuandoTodasAsEtapasEstiveremPendentes() {

        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setCriadoPor(usuario);
        trabalho.setTipo(TipoTrabalho.PESSOAL);
        trabalho.setStatus(StatusTrabalho.CONCLUIDO);
        trabalho.setDataConclusao(LocalDateTime.now());

        Etapa etapa1 = new Etapa();
        etapa1.setStatus(StatusTrabalho.PENDENTE);
        etapa1.setPercentualConclusao(0);

        Etapa etapa2 = new Etapa();
        etapa2.setStatus(StatusTrabalho.PENDENTE);
        etapa2.setPercentualConclusao(0);

        when(usuarioAutenticadoService.obterUsuario()).thenReturn(usuario);
        when(trabalhoRepository.findById(10L))
                .thenReturn(Optional.of(trabalho));
        when(etapaRepository.findByTrabalhoIdOrderByOrdemAsc(10L))
                .thenReturn(List.of(etapa1, etapa2));
        when(trabalhoRepository.save(any(Trabalho.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        trabalhoService.sincronizarStatusComEtapas(10L);

        assertEquals(StatusTrabalho.PENDENTE, trabalho.getStatus());
        assertNull(trabalho.getDataConclusao());
        assertNotNull(trabalho.getDataAtualizacao());

        verify(trabalhoRepository).save(trabalho);
        verify(historicoTrabalhoRepository).save(
                argThat(historico ->
                        historico.getStatusAnterior() == StatusTrabalho.CONCLUIDO
                                && historico.getStatusNovo() == StatusTrabalho.PENDENTE
                                && historico.getUsuario().equals(usuario)
                )
        );
    }

    @Test
    void deveManterTrabalhoCanceladoSemSincronizarEtapas() {

        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setCriadoPor(usuario);
        trabalho.setTipo(TipoTrabalho.PESSOAL);
        trabalho.setStatus(StatusTrabalho.CANCELADO);

        when(usuarioAutenticadoService.obterUsuario()).thenReturn(usuario);
        when(trabalhoRepository.findById(10L))
                .thenReturn(Optional.of(trabalho));

        trabalhoService.sincronizarStatusComEtapas(10L);

        assertEquals(StatusTrabalho.CANCELADO, trabalho.getStatus());

        verify(etapaRepository, never())
                .findByTrabalhoIdOrderByOrdemAsc(10L);
        verify(trabalhoRepository, never()).save(any(Trabalho.class));
        verify(historicoTrabalhoRepository, never())
                .save(any(HistoricoTrabalho.class));
    }

    @Test
    void deveDefinirTrabalhoComoPendenteQuandoNaoPossuirEtapas() {

        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setCriadoPor(usuario);
        trabalho.setTipo(TipoTrabalho.PESSOAL);
        trabalho.setStatus(StatusTrabalho.EM_ANDAMENTO);

        when(usuarioAutenticadoService.obterUsuario()).thenReturn(usuario);
        when(trabalhoRepository.findById(10L))
                .thenReturn(Optional.of(trabalho));
        when(etapaRepository.findByTrabalhoIdOrderByOrdemAsc(10L))
                .thenReturn(List.of());
        when(trabalhoRepository.save(any(Trabalho.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        trabalhoService.sincronizarStatusComEtapas(10L);

        assertEquals(StatusTrabalho.PENDENTE, trabalho.getStatus());
        assertNull(trabalho.getDataConclusao());

        verify(trabalhoRepository).save(trabalho);
        verify(historicoTrabalhoRepository).save(
                argThat(historico ->
                        historico.getStatusAnterior() == StatusTrabalho.EM_ANDAMENTO
                                && historico.getStatusNovo() == StatusTrabalho.PENDENTE
                )
        );
    }

    @Test
    void deveImpedirSincronizacaoPorUsuarioSemAcessoAoTrabalho() {

        Usuario criador = new Usuario();
        criador.setId(1L);

        Usuario usuario = new Usuario();
        usuario.setId(2L);

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setCriadoPor(criador);
        trabalho.setTipo(TipoTrabalho.PESSOAL);
        trabalho.setStatus(StatusTrabalho.PENDENTE);

        when(usuarioAutenticadoService.obterUsuario()).thenReturn(usuario);
        when(trabalhoRepository.findById(10L))
                .thenReturn(Optional.of(trabalho));

        assertThrows(
                AccessDeniedException.class,
                () -> trabalhoService.sincronizarStatusComEtapas(10L)
        );

        verify(etapaRepository, never())
                .findByTrabalhoIdOrderByOrdemAsc(10L);
        verify(trabalhoRepository, never()).save(any(Trabalho.class));
        verify(historicoTrabalhoRepository, never())
                .save(any(HistoricoTrabalho.class));
    }

    @Test
    void devePermitirSincronizacaoPorParticipanteDoTrabalhoEmpresarial() {

        Usuario criador = new Usuario();
        criador.setId(1L);

        Usuario participante = new Usuario();
        participante.setId(2L);

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setCriadoPor(criador);
        trabalho.setTipo(TipoTrabalho.EMPRESA);
        trabalho.setStatus(StatusTrabalho.PENDENTE);

        Etapa etapa = new Etapa();
        etapa.setStatus(StatusTrabalho.EM_ANDAMENTO);
        etapa.setPercentualConclusao(30);

        when(usuarioAutenticadoService.obterUsuario()).thenReturn(participante);
        when(trabalhoRepository.findById(10L))
                .thenReturn(Optional.of(trabalho));
        when(usuarioTrabalhoRepository.findByTrabalhoAndUsuario(trabalho, participante))
                .thenReturn(Optional.of(new UsuarioTrabalho()));
        when(etapaRepository.findByTrabalhoIdOrderByOrdemAsc(10L))
                .thenReturn(List.of(etapa));
        when(trabalhoRepository.save(any(Trabalho.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        trabalhoService.sincronizarStatusComEtapas(10L);

        assertEquals(StatusTrabalho.EM_ANDAMENTO, trabalho.getStatus());

        verify(trabalhoRepository).save(trabalho);
        verify(historicoTrabalhoRepository).save(
                argThat(historico ->
                        historico.getStatusNovo() == StatusTrabalho.EM_ANDAMENTO
                                && historico.getUsuario().equals(participante)
                )
        );
    }

    @Test
    void devePermitirSincronizacaoPorResponsavelDaEtapa() {
        Usuario criador = new Usuario();
        criador.setId(1L);

        Usuario responsavel = new Usuario();
        responsavel.setId(2L);

        Empresa empresa = new Empresa();
        empresa.setId(5L);

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setCriadoPor(criador);
        trabalho.setTipo(TipoTrabalho.EMPRESA);
        trabalho.setEmpresa(empresa);
        trabalho.setStatus(StatusTrabalho.PENDENTE);

        Etapa etapa = new Etapa();
        etapa.setTrabalho(trabalho);
        etapa.setResponsavel(responsavel);
        etapa.setStatus(StatusTrabalho.EM_ANDAMENTO);
        etapa.setPercentualConclusao(30);

        when(usuarioAutenticadoService.obterUsuario()).thenReturn(responsavel);
        when(trabalhoRepository.findById(10L)).thenReturn(Optional.of(trabalho));

        when(usuarioTrabalhoRepository.findByTrabalhoAndUsuario(trabalho, responsavel))
                .thenReturn(Optional.empty());

        when(usuarioEmpresaRepository.existsByUsuarioAndEmpresa(responsavel, empresa))
                .thenReturn(true);

        when(etapaRepository
                .findByResponsavelIdAndTrabalhoEmpresaIdOrderByOrdemAsc(2L, 5L))
                .thenReturn(List.of(etapa));

        when(etapaRepository.findByTrabalhoIdOrderByOrdemAsc(10L))
                .thenReturn(List.of(etapa));

        when(trabalhoRepository.save(any(Trabalho.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        trabalhoService.sincronizarStatusComEtapas(10L);

        assertEquals(StatusTrabalho.EM_ANDAMENTO, trabalho.getStatus());

        verify(trabalhoRepository).save(trabalho);
        verify(historicoTrabalhoRepository).save(argThat(historico ->
                historico.getStatusNovo() == StatusTrabalho.EM_ANDAMENTO
                        && historico.getUsuario().equals(responsavel)
        ));
    }

    @Test
    void deveLancarExcecaoQuandoTrabalhoNaoExistirNaSincronizacao() {
        when(trabalhoRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> trabalhoService.sincronizarStatusComEtapas(999L));

        verify(etapaRepository, never())
                .findByTrabalhoIdOrderByOrdemAsc(999L);

        verify(trabalhoRepository, never()).save(any(Trabalho.class));
        verify(historicoTrabalhoRepository, never()).save(any(HistoricoTrabalho.class));
    }

    @Test
    void deveEvitarSalvarQuandoStatusCalculadoForIgualAoAtual() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setCriadoPor(usuario);
        trabalho.setTipo(TipoTrabalho.PESSOAL);
        trabalho.setStatus(StatusTrabalho.EM_ANDAMENTO);

        Etapa etapa = new Etapa();
        etapa.setStatus(StatusTrabalho.EM_ANDAMENTO);
        etapa.setPercentualConclusao(30);

        when(usuarioAutenticadoService.obterUsuario()).thenReturn(usuario);
        when(trabalhoRepository.findById(10L)).thenReturn(Optional.of(trabalho));
        when(etapaRepository.findByTrabalhoIdOrderByOrdemAsc(10L))
                .thenReturn(List.of(etapa));

        trabalhoService.sincronizarStatusComEtapas(10L);

        assertEquals(StatusTrabalho.EM_ANDAMENTO, trabalho.getStatus());

        verify(trabalhoRepository, never()).save(any(Trabalho.class));
        verify(historicoTrabalhoRepository, never())
                .save(any(HistoricoTrabalho.class));
    }

    @Test
    void deveMontarLinhaDoTempoComEventosHistoricosEAlteracoesDeEtapas() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Maria");

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setCriadoPor(usuario);
        trabalho.setTipo(TipoTrabalho.PESSOAL);

        Etapa etapa = new Etapa();
        etapa.setId(20L);
        etapa.setTitulo("Desenvolvimento");
        etapa.setTrabalho(trabalho);

        LocalDateTime dataEvento = LocalDateTime.of(2026, 11, 1, 10, 0);
        LocalDateTime dataHistorico = LocalDateTime.of(2026, 11, 2, 10, 0);
        LocalDateTime dataEtapa = LocalDateTime.of(2026, 11, 3, 10, 0);

        EventoTrabalho evento = new EventoTrabalho();
        evento.setId(30L);
        evento.setTipo(TipoEventoTrabalho.TRABALHO_EDITADO);
        evento.setDescricao("Título do trabalho alterado");
        evento.setValorAnterior("Título antigo");
        evento.setValorNovo("Título novo");
        evento.setDataEvento(dataEvento);
        evento.setUsuario(usuario);
        evento.setEtapa(etapa);

        HistoricoTrabalho historicoInicial = new HistoricoTrabalho();
        historicoInicial.setStatusAnterior(null);
        historicoInicial.setStatusNovo(StatusTrabalho.PENDENTE);

        HistoricoTrabalho historico = new HistoricoTrabalho();
        historico.setId(40L);
        historico.setStatusAnterior(StatusTrabalho.PENDENTE);
        historico.setStatusNovo(StatusTrabalho.EM_ANDAMENTO);
        historico.setDataAlteracao(dataHistorico);
        historico.setUsuario(usuario);

        HistoricoEtapa historicoEtapaInicial = new HistoricoEtapa();
        historicoEtapaInicial.setStatusAnterior(null);
        historicoEtapaInicial.setStatusNovo(StatusTrabalho.PENDENTE);

        HistoricoEtapa historicoEtapa = new HistoricoEtapa();
        historicoEtapa.setId(50L);
        historicoEtapa.setStatusAnterior(StatusTrabalho.PENDENTE);
        historicoEtapa.setStatusNovo(StatusTrabalho.EM_ANDAMENTO);
        historicoEtapa.setDataAlteracao(dataEtapa);
        historicoEtapa.setUsuario(usuario);

        when(usuarioAutenticadoService.obterUsuario()).thenReturn(usuario);
        when(trabalhoRepository.findById(10L)).thenReturn(Optional.of(trabalho));

        when(eventoTrabalhoRepository.findByTrabalhoIdOrderByDataEventoDesc(10L))
                .thenReturn(List.of(evento));

        when(historicoTrabalhoRepository.findByTrabalhoIdOrderByDataAlteracaoDesc(10L))
                .thenReturn(List.of(historicoInicial, historico));

        when(etapaRepository.findByTrabalhoIdOrderByOrdemAsc(10L))
                .thenReturn(List.of(etapa));

        when(historicoEtapaRepository.findByEtapaIdOrderByDataAlteracaoDesc(20L))
                .thenReturn(List.of(historicoEtapaInicial, historicoEtapa));

        List<LinhaDoTempoRespostaDTO> resultado =
                trabalhoService.listarLinhaDoTempo(10L);

        assertEquals(3, resultado.size());

        assertEquals("STATUS_ETAPA", resultado.get(0).getTipo());
        assertEquals(50L, resultado.get(0).getId());
        assertEquals(20L, resultado.get(0).getEtapaId());
        assertEquals("Desenvolvimento", resultado.get(0).getEtapaTitulo());

        assertEquals("STATUS_TRABALHO", resultado.get(1).getTipo());
        assertEquals(40L, resultado.get(1).getId());

        assertEquals("TRABALHO_EDITADO", resultado.get(2).getTipo());
        assertEquals(30L, resultado.get(2).getId());
        assertEquals("Título antigo", resultado.get(2).getValorAnterior());
        assertEquals("Título novo", resultado.get(2).getValorNovo());
        assertEquals(20L, resultado.get(2).getEtapaId());

        assertEquals(dataEtapa, resultado.get(0).getData());
        assertEquals(dataHistorico, resultado.get(1).getData());
        assertEquals(dataEvento, resultado.get(2).getData());

        assertEquals(1L, resultado.get(0).getUsuarioId());
        assertEquals("Maria", resultado.get(0).getUsuarioNome());
    }

    @Test
    void deveRetornarLinhaDoTempoVaziaQuandoNaoHouverRegistros() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setCriadoPor(usuario);
        trabalho.setTipo(TipoTrabalho.PESSOAL);

        when(usuarioAutenticadoService.obterUsuario()).thenReturn(usuario);
        when(trabalhoRepository.findById(10L)).thenReturn(Optional.of(trabalho));

        when(eventoTrabalhoRepository.findByTrabalhoIdOrderByDataEventoDesc(10L))
                .thenReturn(List.of());

        when(historicoTrabalhoRepository.findByTrabalhoIdOrderByDataAlteracaoDesc(10L))
                .thenReturn(List.of());

        when(etapaRepository.findByTrabalhoIdOrderByOrdemAsc(10L))
                .thenReturn(List.of());

        List<LinhaDoTempoRespostaDTO> resultado =
                trabalhoService.listarLinhaDoTempo(10L);

        assertEquals(0, resultado.size());
    }



}
