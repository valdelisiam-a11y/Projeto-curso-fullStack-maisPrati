
package br.com.docflow.backend.service;

import br.com.docflow.backend.dto.*;
import br.com.docflow.backend.entity.*;
import br.com.docflow.backend.repository.*;
import br.com.docflow.backend.dto.EtapaCadastroDTO;
import br.com.docflow.backend.entity.Empresa;
import br.com.docflow.backend.dto.EtapaAtualizacaoDTO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.times;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;
import org.springframework.security.access.AccessDeniedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;

import br.com.docflow.backend.repository.UsuarioEmpresaRepository;
import br.com.docflow.backend.service.TrabalhoService;
import static org.mockito.Mockito.verify;

import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class EtapaServiceTest {

    @Mock
    private EtapaRepository etapaRepository;

    @Mock
    private HistoricoEtapaRepository historicoEtapaRepository;

    @Mock
    private TrabalhoRepository trabalhoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioEmpresaRepository usuarioEmpresaRepository;

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @Mock
    private UsuarioTrabalhoRepository usuarioTrabalhoRepository;

    @Mock
    private EventoTrabalhoRepository eventoTrabalhoRepository;

    @Mock
    private TrabalhoService trabalhoService;

    @InjectMocks
    private EtapaService etapaService;

    @Test
    void deveNegarConsultaDaEtapaQuandoUsuarioNaoTiverAcessoAoTrabalho() {
        // Usuário autenticado, mas sem acesso ao trabalho.
        Usuario usuarioAutenticado = new Usuario();
        usuarioAutenticado.setId(2L);

        // Criador do trabalho.
        Usuario criador = new Usuario();
        criador.setId(1L);

        Empresa empresa = new Empresa();
        empresa.setId(1L);

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setCriadoPor(criador);
        trabalho.setEmpresa(empresa);
        trabalho.setTipo(TipoTrabalho.EMPRESA);

        Etapa etapa = new Etapa();
        etapa.setId(20L);
        etapa.setTrabalho(trabalho);

        when(etapaRepository.findById(20L))
                .thenReturn(Optional.of(etapa));

        when(usuarioAutenticadoService.obterUsuario())
                .thenReturn(usuarioAutenticado);

        when(usuarioTrabalhoRepository.findByTrabalhoAndUsuario(
                trabalho,
                usuarioAutenticado
        )).thenReturn(Optional.empty());

        // O usuário não deve conseguir consultar a etapa.
        assertThrows(
                AccessDeniedException.class,
                () -> etapaService.buscarPorId(20L)
        );
    }

    @Test
    void deveNegarAlteracaoDaEtapaQuandoUsuarioNaoTiverAcessoAoTrabalho() {
        Usuario usuarioAutenticado = new Usuario();
        usuarioAutenticado.setId(2L);

        Usuario criador = new Usuario();
        criador.setId(1L);

        Empresa empresa = new Empresa();
        empresa.setId(1L);

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setCriadoPor(criador);
        trabalho.setEmpresa(empresa);
        trabalho.setTipo(TipoTrabalho.EMPRESA);

        Etapa etapa = new Etapa();
        etapa.setId(20L);
        etapa.setTrabalho(trabalho);

        when(etapaRepository.findById(20L))
                .thenReturn(Optional.of(etapa));

        when(usuarioAutenticadoService.obterUsuario())
                .thenReturn(usuarioAutenticado);

        when(usuarioTrabalhoRepository.findByTrabalhoAndUsuario(
                trabalho,
                usuarioAutenticado
        )).thenReturn(Optional.empty());

        assertThrows(
                AccessDeniedException.class,
                () -> etapaService.excluir(20L)
        );
    }


    @Test
    void deveRegistrarUsuarioAutenticadoNoHistoricoAoAlterarStatusDaEtapa() {
        Usuario usuarioAutenticado = new Usuario();
        usuarioAutenticado.setId(2L);

        Usuario criador = new Usuario();
        criador.setId(1L);

        Empresa empresa = new Empresa();
        empresa.setId(1L);

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setCriadoPor(criador);
        trabalho.setEmpresa(empresa);
        trabalho.setTipo(TipoTrabalho.EMPRESA);

        Etapa etapa = new Etapa();
        etapa.setId(20L);
        etapa.setTitulo("Etapa de teste");
        etapa.setStatus(StatusTrabalho.PENDENTE);
        etapa.setPercentualConclusao(0);
        etapa.setOrdem(1);
        etapa.setDataCriacao(LocalDateTime.now());
        etapa.setDataAtualizacao(LocalDateTime.now());
        etapa.setTrabalho(trabalho);

        EtapaStatusDTO dto = new EtapaStatusDTO();
        dto.setStatus(StatusTrabalho.EM_ANDAMENTO); // ID diferente do usuário autenticado

        when(etapaRepository.findById(20L))
                .thenReturn(Optional.of(etapa));

        when(usuarioAutenticadoService.obterUsuario())
                .thenReturn(usuarioAutenticado);

        when(usuarioTrabalhoRepository.findByTrabalhoAndUsuario(
                trabalho,
                usuarioAutenticado
        )).thenReturn(Optional.of(new UsuarioTrabalho()));

        when(etapaRepository.save(any(Etapa.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        etapaService.alterarStatus(20L, dto);

        ArgumentCaptor<HistoricoEtapa> captor =
                ArgumentCaptor.forClass(HistoricoEtapa.class);

        verify(historicoEtapaRepository).save(captor.capture());

        HistoricoEtapa historicoSalvo = captor.getValue();

        assertEquals(2L, historicoSalvo.getUsuario().getId());
        assertEquals(StatusTrabalho.PENDENTE, historicoSalvo.getStatusAnterior());
        assertEquals(StatusTrabalho.EM_ANDAMENTO, historicoSalvo.getStatusNovo());
    }

    @Test
    void deveDefinirResponsavelComVinculoNaEmpresaDoTrabalho() {
        Usuario criador = new Usuario();
        criador.setId(1L);

        Usuario usuarioAutenticado = new Usuario();
        usuarioAutenticado.setId(1L);

        Usuario responsavel = new Usuario();
        responsavel.setId(3L);
        // Não define usuario.setEmpresa(): o vínculo será verificado pelo repositório.

        Empresa empresa = new Empresa();
        empresa.setId(1L);

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setCriadoPor(criador);
        trabalho.setEmpresa(empresa);

        Etapa etapa = new Etapa();
        etapa.setId(20L);
        etapa.setTitulo("Etapa de teste");
        etapa.setStatus(StatusTrabalho.PENDENTE);
        etapa.setPercentualConclusao(0);
        etapa.setOrdem(1);
        etapa.setTrabalho(trabalho);

        EtapaResponsavelDTO dto = new EtapaResponsavelDTO();
        dto.setResponsavelId(3L);

        when(etapaRepository.findById(20L))
                .thenReturn(Optional.of(etapa));

        when(usuarioAutenticadoService.obterUsuario())
                .thenReturn(usuarioAutenticado);

        when(usuarioRepository.findById(3L))
                .thenReturn(Optional.of(responsavel));

        when(usuarioEmpresaRepository.existsByUsuarioAndEmpresa(
                responsavel, empresa))
                .thenReturn(true);

        when(etapaRepository.save(any(Etapa.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        etapaService.definirResponsavel(20L, dto);

        verify(usuarioEmpresaRepository)
                .existsByUsuarioAndEmpresa(responsavel, empresa);

        assertEquals(3L, etapa.getResponsavel().getId());
    }


    @Test
    void deveConcluirEtapaQuandoPercentualAtingir100() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Usuário teste");

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setCriadoPor(usuario);
        trabalho.setTipo(TipoTrabalho.PESSOAL);

        Etapa etapa = new Etapa();
        etapa.setId(20L);
        etapa.setTitulo("Etapa de teste");
        etapa.setStatus(StatusTrabalho.EM_ANDAMENTO);
        etapa.setPercentualConclusao(50);
        etapa.setOrdem(1);
        etapa.setTrabalho(trabalho);

        EtapaPercentualDTO dto = new EtapaPercentualDTO();
        dto.setPercentualConclusao(100);

        when(etapaRepository.findById(20L))
                .thenReturn(Optional.of(etapa));

        when(usuarioAutenticadoService.obterUsuario())
                .thenReturn(usuario);

        when(etapaRepository.save(any(Etapa.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        etapaService.atualizarPercentual(20L, dto);

        assertEquals(StatusTrabalho.CONCLUIDO, etapa.getStatus());
        assertEquals(100, etapa.getPercentualConclusao());

        ArgumentCaptor<HistoricoEtapa> historicoCaptor =
                ArgumentCaptor.forClass(HistoricoEtapa.class);

        verify(historicoEtapaRepository).save(historicoCaptor.capture());

        assertEquals(
                StatusTrabalho.EM_ANDAMENTO,
                historicoCaptor.getValue().getStatusAnterior()
        );

        assertEquals(
                StatusTrabalho.CONCLUIDO,
                historicoCaptor.getValue().getStatusNovo()
        );

        verify(eventoTrabalhoRepository).save(any(EventoTrabalho.class));

        verify(trabalhoService).sincronizarStatusComEtapas(10L);
    }


    @Test
    void deveRetornarEtapaParaPendenteQuandoPercentualForZero() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Usuário teste");

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setCriadoPor(usuario);
        trabalho.setTipo(TipoTrabalho.PESSOAL);

        Etapa etapa = new Etapa();
        etapa.setId(20L);
        etapa.setTitulo("Etapa de teste");
        etapa.setStatus(StatusTrabalho.EM_ANDAMENTO);
        etapa.setPercentualConclusao(50);
        etapa.setOrdem(1);
        etapa.setTrabalho(trabalho);

        EtapaPercentualDTO dto = new EtapaPercentualDTO();
        dto.setPercentualConclusao(0);

        when(etapaRepository.findById(20L))
                .thenReturn(Optional.of(etapa));

        when(usuarioAutenticadoService.obterUsuario())
                .thenReturn(usuario);

        when(etapaRepository.save(any(Etapa.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        etapaService.atualizarPercentual(20L, dto);

        assertEquals(StatusTrabalho.PENDENTE, etapa.getStatus());
        assertEquals(0, etapa.getPercentualConclusao());

        ArgumentCaptor<HistoricoEtapa> historicoCaptor =
                ArgumentCaptor.forClass(HistoricoEtapa.class);

        verify(historicoEtapaRepository).save(historicoCaptor.capture());

        assertEquals(
                StatusTrabalho.EM_ANDAMENTO,
                historicoCaptor.getValue().getStatusAnterior()
        );

        assertEquals(
                StatusTrabalho.PENDENTE,
                historicoCaptor.getValue().getStatusNovo()
        );

        verify(eventoTrabalhoRepository).save(any(EventoTrabalho.class));
        verify(trabalhoService).sincronizarStatusComEtapas(10L);
    }


    @Test
    void deveManterEtapaEmAndamentoQuandoPercentualForIntermediario() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Usuário teste");

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setCriadoPor(usuario);
        trabalho.setTipo(TipoTrabalho.PESSOAL);

        Etapa etapa = new Etapa();
        etapa.setId(20L);
        etapa.setTitulo("Etapa de teste");
        etapa.setStatus(StatusTrabalho.EM_ANDAMENTO);
        etapa.setPercentualConclusao(30);
        etapa.setOrdem(1);
        etapa.setTrabalho(trabalho);

        EtapaPercentualDTO dto = new EtapaPercentualDTO();
        dto.setPercentualConclusao(60);

        when(etapaRepository.findById(20L))
                .thenReturn(Optional.of(etapa));

        when(usuarioAutenticadoService.obterUsuario())
                .thenReturn(usuario);

        when(etapaRepository.save(any(Etapa.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        etapaService.atualizarPercentual(20L, dto);

        assertEquals(StatusTrabalho.EM_ANDAMENTO, etapa.getStatus());
        assertEquals(60, etapa.getPercentualConclusao());

        verify(eventoTrabalhoRepository).save(any(EventoTrabalho.class));
        verify(historicoEtapaRepository, org.mockito.Mockito.never())
                .save(any(HistoricoEtapa.class));

        verify(trabalhoService).sincronizarStatusComEtapas(10L);
    }


    @Test
    void deveImpedirAtualizacaoDoPercentualDeEtapaCancelada() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setCriadoPor(usuario);
        trabalho.setTipo(TipoTrabalho.PESSOAL);

        Etapa etapa = new Etapa();
        etapa.setId(20L);
        etapa.setStatus(StatusTrabalho.CANCELADO);
        etapa.setPercentualConclusao(0);
        etapa.setTrabalho(trabalho);

        EtapaPercentualDTO dto = new EtapaPercentualDTO();
        dto.setPercentualConclusao(50);

        when(etapaRepository.findById(20L))
                .thenReturn(Optional.of(etapa));

        when(usuarioAutenticadoService.obterUsuario())
                .thenReturn(usuario);

        ResponseStatusException excecao = assertThrows(
                ResponseStatusException.class,
                () -> etapaService.atualizarPercentual(20L, dto)
        );

        assertEquals(
                org.springframework.http.HttpStatus.CONFLICT,
                excecao.getStatusCode()
        );

        verify(etapaRepository, org.mockito.Mockito.never())
                .save(any(Etapa.class));

        verify(eventoTrabalhoRepository, org.mockito.Mockito.never())
                .save(any(EventoTrabalho.class));

        verify(historicoEtapaRepository, org.mockito.Mockito.never())
                .save(any(HistoricoEtapa.class));
    }


    @Test
    void deveImpedirConclusaoDiretaDeEtapaCancelada() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setCriadoPor(usuario);
        trabalho.setTipo(TipoTrabalho.PESSOAL);

        Etapa etapa = new Etapa();
        etapa.setId(20L);
        etapa.setStatus(StatusTrabalho.CANCELADO);
        etapa.setPercentualConclusao(0);
        etapa.setTrabalho(trabalho);

        EtapaStatusDTO dto = new EtapaStatusDTO();
        dto.setStatus(StatusTrabalho.CONCLUIDO);

        when(etapaRepository.findById(20L))
                .thenReturn(Optional.of(etapa));

        when(usuarioAutenticadoService.obterUsuario())
                .thenReturn(usuario);

        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> etapaService.alterarStatus(20L, dto)
        );

        assertEquals(
                "Uma etapa cancelada deve ser reaberta como pendente ou em andamento.",
                excecao.getMessage()
        );

        verify(etapaRepository, org.mockito.Mockito.never())
                .save(any(Etapa.class));

        verify(eventoTrabalhoRepository, org.mockito.Mockito.never())
                .save(any(EventoTrabalho.class));

        verify(historicoEtapaRepository, org.mockito.Mockito.never())
                .save(any(HistoricoEtapa.class));

        verify(trabalhoService, org.mockito.Mockito.never())
                .sincronizarStatusComEtapas(10L);
    }


    @Test
    void deveImpedirAlteracaoQuandoEtapaJaPossuiMesmoStatus() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setCriadoPor(usuario);
        trabalho.setTipo(TipoTrabalho.PESSOAL);

        Etapa etapa = new Etapa();
        etapa.setId(20L);
        etapa.setStatus(StatusTrabalho.EM_ANDAMENTO);
        etapa.setPercentualConclusao(50);
        etapa.setTrabalho(trabalho);

        EtapaStatusDTO dto = new EtapaStatusDTO();
        dto.setStatus(StatusTrabalho.EM_ANDAMENTO);

        when(etapaRepository.findById(20L))
                .thenReturn(Optional.of(etapa));

        when(usuarioAutenticadoService.obterUsuario())
                .thenReturn(usuario);

        assertThrows(
                EtapaStatusInalteradoException.class,
                () -> etapaService.alterarStatus(20L, dto)
        );

        verify(etapaRepository, org.mockito.Mockito.never())
                .save(any(Etapa.class));

        verify(eventoTrabalhoRepository, org.mockito.Mockito.never())
                .save(any(EventoTrabalho.class));

        verify(historicoEtapaRepository, org.mockito.Mockito.never())
                .save(any(HistoricoEtapa.class));

        verify(trabalhoService, org.mockito.Mockito.never())
                .sincronizarStatusComEtapas(10L);
    }


    @Test
    void deveDefinirPercentual100AoConcluirEtapaPeloStatus() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Usuário teste");

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setCriadoPor(usuario);
        trabalho.setTipo(TipoTrabalho.PESSOAL);

        Etapa etapa = new Etapa();
        etapa.setId(20L);
        etapa.setStatus(StatusTrabalho.EM_ANDAMENTO);
        etapa.setPercentualConclusao(50);
        etapa.setTrabalho(trabalho);

        EtapaStatusDTO dto = new EtapaStatusDTO();
        dto.setStatus(StatusTrabalho.CONCLUIDO);

        when(etapaRepository.findById(20L))
                .thenReturn(Optional.of(etapa));

        when(usuarioAutenticadoService.obterUsuario())
                .thenReturn(usuario);

        when(etapaRepository.save(any(Etapa.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        etapaService.alterarStatus(20L, dto);

        assertEquals(StatusTrabalho.CONCLUIDO, etapa.getStatus());
        assertEquals(100, etapa.getPercentualConclusao());

        ArgumentCaptor<HistoricoEtapa> historicoCaptor =
                ArgumentCaptor.forClass(HistoricoEtapa.class);

        verify(historicoEtapaRepository).save(historicoCaptor.capture());

        assertEquals(
                StatusTrabalho.EM_ANDAMENTO,
                historicoCaptor.getValue().getStatusAnterior()
        );

        assertEquals(
                StatusTrabalho.CONCLUIDO,
                historicoCaptor.getValue().getStatusNovo()
        );

        verify(eventoTrabalhoRepository).save(any(EventoTrabalho.class));
        verify(trabalhoService).sincronizarStatusComEtapas(10L);
    }


    @Test
    void deveZerarPercentualAoRetornarEtapaParaPendente() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Usuário teste");

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setCriadoPor(usuario);
        trabalho.setTipo(TipoTrabalho.PESSOAL);

        Etapa etapa = new Etapa();
        etapa.setId(20L);
        etapa.setStatus(StatusTrabalho.EM_ANDAMENTO);
        etapa.setPercentualConclusao(50);
        etapa.setTrabalho(trabalho);

        EtapaStatusDTO dto = new EtapaStatusDTO();
        dto.setStatus(StatusTrabalho.PENDENTE);

        when(etapaRepository.findById(20L))
                .thenReturn(Optional.of(etapa));

        when(usuarioAutenticadoService.obterUsuario())
                .thenReturn(usuario);

        when(etapaRepository.save(any(Etapa.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        etapaService.alterarStatus(20L, dto);

        assertEquals(StatusTrabalho.PENDENTE, etapa.getStatus());
        assertEquals(0, etapa.getPercentualConclusao());

        verify(eventoTrabalhoRepository).save(any(EventoTrabalho.class));
        verify(historicoEtapaRepository).save(any(HistoricoEtapa.class));
        verify(trabalhoService).sincronizarStatusComEtapas(10L);
    }


    @Test
    void deveDefinirPercentual1AoIniciarEtapaPendente() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Usuário teste");

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setCriadoPor(usuario);
        trabalho.setTipo(TipoTrabalho.PESSOAL);

        Etapa etapa = new Etapa();
        etapa.setId(20L);
        etapa.setStatus(StatusTrabalho.PENDENTE);
        etapa.setPercentualConclusao(0);
        etapa.setTrabalho(trabalho);

        EtapaStatusDTO dto = new EtapaStatusDTO();
        dto.setStatus(StatusTrabalho.EM_ANDAMENTO);

        when(etapaRepository.findById(20L))
                .thenReturn(Optional.of(etapa));

        when(usuarioAutenticadoService.obterUsuario())
                .thenReturn(usuario);

        when(etapaRepository.save(any(Etapa.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        etapaService.alterarStatus(20L, dto);

        assertEquals(StatusTrabalho.EM_ANDAMENTO, etapa.getStatus());
        assertEquals(1, etapa.getPercentualConclusao());

        verify(eventoTrabalhoRepository).save(any(EventoTrabalho.class));
        verify(historicoEtapaRepository).save(any(HistoricoEtapa.class));
        verify(trabalhoService).sincronizarStatusComEtapas(10L);
    }


    @Test
    void deveDefinirPercentual99AoReabrirEtapaConcluida() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Usuário teste");

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setCriadoPor(usuario);
        trabalho.setTipo(TipoTrabalho.PESSOAL);

        Etapa etapa = new Etapa();
        etapa.setId(20L);
        etapa.setStatus(StatusTrabalho.CONCLUIDO);
        etapa.setPercentualConclusao(100);
        etapa.setTrabalho(trabalho);

        EtapaStatusDTO dto = new EtapaStatusDTO();
        dto.setStatus(StatusTrabalho.EM_ANDAMENTO);

        when(etapaRepository.findById(20L))
                .thenReturn(Optional.of(etapa));

        when(usuarioAutenticadoService.obterUsuario())
                .thenReturn(usuario);

        when(etapaRepository.save(any(Etapa.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        etapaService.alterarStatus(20L, dto);

        assertEquals(StatusTrabalho.EM_ANDAMENTO, etapa.getStatus());
        assertEquals(99, etapa.getPercentualConclusao());

        verify(eventoTrabalhoRepository).save(any(EventoTrabalho.class));
        verify(historicoEtapaRepository).save(any(HistoricoEtapa.class));
        verify(trabalhoService).sincronizarStatusComEtapas(10L);
    }

    @Test
    void deveManterResponsavelQuandoNaoHouverAlteracao() {
        Usuario criador = new Usuario();
        criador.setId(1L);

        Usuario usuarioAutenticado = new Usuario();
        usuarioAutenticado.setId(1L);

        Usuario responsavel = new Usuario();
        responsavel.setId(3L);

        Empresa empresa = new Empresa();
        empresa.setId(1L);

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setCriadoPor(criador);
        trabalho.setEmpresa(empresa);

        Etapa etapa = new Etapa();
        etapa.setId(20L);
        etapa.setTitulo("Etapa de teste");
        etapa.setStatus(StatusTrabalho.PENDENTE);
        etapa.setPercentualConclusao(0);
        etapa.setOrdem(1);
        etapa.setTrabalho(trabalho);
        etapa.setResponsavel(responsavel);

        EtapaResponsavelDTO dto = new EtapaResponsavelDTO();
        dto.setResponsavelId(3L);

        when(etapaRepository.findById(20L))
                .thenReturn(Optional.of(etapa));

        when(usuarioAutenticadoService.obterUsuario())
                .thenReturn(usuarioAutenticado);

        when(usuarioRepository.findById(3L))
                .thenReturn(Optional.of(responsavel));

        when(usuarioEmpresaRepository.existsByUsuarioAndEmpresa(
                responsavel, empresa))
                .thenReturn(true);

        etapaService.definirResponsavel(20L, dto);

        verify(etapaRepository, org.mockito.Mockito.never())
                .save(any(Etapa.class));

        verify(eventoTrabalhoRepository, org.mockito.Mockito.never())
                .save(any(EventoTrabalho.class));
    }

    @Test
    void deveRemoverResponsavelERegistrarEvento() {
        Usuario criador = new Usuario();
        criador.setId(1L);

        Usuario usuarioAutenticado = new Usuario();
        usuarioAutenticado.setId(1L);

        Usuario responsavelAnterior = new Usuario();
        responsavelAnterior.setId(3L);
        responsavelAnterior.setNome("Maria");

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setCriadoPor(criador);

        Etapa etapa = new Etapa();
        etapa.setId(20L);
        etapa.setTitulo("Etapa de teste");
        etapa.setStatus(StatusTrabalho.PENDENTE);
        etapa.setPercentualConclusao(0);
        etapa.setOrdem(1);
        etapa.setTrabalho(trabalho);
        etapa.setResponsavel(responsavelAnterior);

        EtapaResponsavelDTO dto = new EtapaResponsavelDTO();
        dto.setResponsavelId(null);

        when(etapaRepository.findById(20L))
                .thenReturn(Optional.of(etapa));

        when(usuarioAutenticadoService.obterUsuario())
                .thenReturn(usuarioAutenticado);

        when(etapaRepository.save(any(Etapa.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        etapaService.definirResponsavel(20L, dto);

        assertEquals(null, etapa.getResponsavel());

        ArgumentCaptor<EventoTrabalho> captor =
                ArgumentCaptor.forClass(EventoTrabalho.class);

        verify(eventoTrabalhoRepository).save(captor.capture());

        EventoTrabalho eventoSalvo = captor.getValue();

        assertEquals(TipoEventoTrabalho.RESPONSAVEL_ALTERADO,
                eventoSalvo.getTipo());
        assertEquals("Maria", eventoSalvo.getValorAnterior());
        assertEquals("Sem responsável", eventoSalvo.getValorNovo());
        assertEquals("Responsável removido da etapa",
                eventoSalvo.getDescricao());

        verify(etapaRepository).save(etapa);
    }

    @Test
    void deveAlterarResponsavelERegistrarEvento() {
        Usuario criador = new Usuario();
        criador.setId(1L);

        Usuario usuarioAutenticado = new Usuario();
        usuarioAutenticado.setId(1L);

        Usuario responsavelAnterior = new Usuario();
        responsavelAnterior.setId(3L);
        responsavelAnterior.setNome("Maria");

        Usuario responsavelNovo = new Usuario();
        responsavelNovo.setId(4L);
        responsavelNovo.setNome("João");

        Empresa empresa = new Empresa();
        empresa.setId(1L);

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setCriadoPor(criador);
        trabalho.setEmpresa(empresa);

        Etapa etapa = new Etapa();
        etapa.setId(20L);
        etapa.setTitulo("Etapa de teste");
        etapa.setStatus(StatusTrabalho.PENDENTE);
        etapa.setPercentualConclusao(0);
        etapa.setOrdem(1);
        etapa.setTrabalho(trabalho);
        etapa.setResponsavel(responsavelAnterior);

        EtapaResponsavelDTO dto = new EtapaResponsavelDTO();
        dto.setResponsavelId(4L);

        when(etapaRepository.findById(20L))
                .thenReturn(Optional.of(etapa));

        when(usuarioAutenticadoService.obterUsuario())
                .thenReturn(usuarioAutenticado);

        when(usuarioRepository.findById(4L))
                .thenReturn(Optional.of(responsavelNovo));

        when(usuarioEmpresaRepository.existsByUsuarioAndEmpresa(
                responsavelNovo, empresa))
                .thenReturn(true);

        when(etapaRepository.save(any(Etapa.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        etapaService.definirResponsavel(20L, dto);

        assertEquals(4L, etapa.getResponsavel().getId());
        assertEquals("João", etapa.getResponsavel().getNome());

        ArgumentCaptor<EventoTrabalho> captor =
                ArgumentCaptor.forClass(EventoTrabalho.class);

        verify(eventoTrabalhoRepository).save(captor.capture());

        EventoTrabalho eventoSalvo = captor.getValue();

        assertEquals(TipoEventoTrabalho.RESPONSAVEL_ALTERADO,
                eventoSalvo.getTipo());
        assertEquals("Maria", eventoSalvo.getValorAnterior());
        assertEquals("João", eventoSalvo.getValorNovo());
        assertEquals("Responsável da etapa alterado",
                eventoSalvo.getDescricao());

        verify(etapaRepository).save(etapa);
    }

    @Test
    void deveAlterarOrdemDaEtapaERegistrarEvento() {
        Usuario criador = new Usuario();
        criador.setId(1L);

        Usuario usuarioAutenticado = new Usuario();
        usuarioAutenticado.setId(1L);

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setCriadoPor(criador);

        Etapa etapa1 = new Etapa();
        etapa1.setId(20L);
        etapa1.setTitulo("Etapa 1");
        etapa1.setOrdem(3);
        etapa1.setTrabalho(trabalho);

        Etapa etapa2 = new Etapa();
        etapa2.setId(21L);
        etapa2.setTitulo("Etapa 2");
        etapa2.setOrdem(1);
        etapa2.setTrabalho(trabalho);

        Etapa etapa3 = new Etapa();
        etapa3.setId(22L);
        etapa3.setTitulo("Etapa 3");
        etapa3.setOrdem(2);
        etapa3.setTrabalho(trabalho);

        EtapaOrdemDTO dto = new EtapaOrdemDTO();
        dto.setOrdem(1);

        when(etapaRepository.findById(20L))
                .thenReturn(Optional.of(etapa1));

        when(usuarioAutenticadoService.obterUsuario())
                .thenReturn(usuarioAutenticado);

        when(etapaRepository.findByTrabalhoIdOrderByOrdemAsc(10L))
                .thenReturn(List.of(etapa2, etapa3, etapa1));

        when(eventoTrabalhoRepository
                .findFirstByEtapaIdAndTipoOrderByDataEventoDesc(
                        20L, TipoEventoTrabalho.OBSERVACAO_REGISTRADA))
                .thenReturn(Optional.empty());

        etapaService.alterarOrdem(20L, dto);

        assertEquals(1, etapa1.getOrdem());
        assertEquals(2, etapa2.getOrdem());
        assertEquals(3, etapa3.getOrdem());

        verify(etapaRepository).saveAll(argThat(etapas -> {
            List<Etapa> lista = new ArrayList<>();
            etapas.forEach(lista::add);

            return lista.size() == 3
                    && lista.get(0).getId().equals(20L)
                    && lista.get(1).getId().equals(21L)
                    && lista.get(2).getId().equals(22L);
        }));

        ArgumentCaptor<EventoTrabalho> captor =
                ArgumentCaptor.forClass(EventoTrabalho.class);

        verify(eventoTrabalhoRepository).save(captor.capture());

        EventoTrabalho eventoSalvo = captor.getValue();

        assertEquals(TipoEventoTrabalho.ORDEM_ALTERADA,
                eventoSalvo.getTipo());
        assertEquals("3", eventoSalvo.getValorAnterior());
        assertEquals("1", eventoSalvo.getValorNovo());
        assertEquals(usuarioAutenticado, eventoSalvo.getUsuario());
    }

    @Test
    void deveManterOrdemSemRegistrarEventoQuandoPosicaoNaoMudar() {
        Usuario criador = new Usuario();
        criador.setId(1L);

        when(usuarioAutenticadoService.obterUsuario())
                .thenReturn(criador);

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setCriadoPor(criador);

        Etapa etapa1 = new Etapa();
        etapa1.setId(20L);
        etapa1.setTitulo("Etapa 1");
        etapa1.setOrdem(1);
        etapa1.setTrabalho(trabalho);

        Etapa etapa2 = new Etapa();
        etapa2.setId(21L);
        etapa2.setTitulo("Etapa 2");
        etapa2.setOrdem(2);
        etapa2.setTrabalho(trabalho);

        EtapaOrdemDTO dto = new EtapaOrdemDTO();
        dto.setOrdem(1);

        when(etapaRepository.findById(20L))
                .thenReturn(Optional.of(etapa1));

        when(etapaRepository.findByTrabalhoIdOrderByOrdemAsc(10L))
                .thenReturn(List.of(etapa1, etapa2));

        when(eventoTrabalhoRepository
                .findFirstByEtapaIdAndTipoOrderByDataEventoDesc(
                        20L, TipoEventoTrabalho.OBSERVACAO_REGISTRADA))
                .thenReturn(Optional.empty());

        etapaService.alterarOrdem(20L, dto);

        assertEquals(1, etapa1.getOrdem());
        assertEquals(2, etapa2.getOrdem());

        verify(etapaRepository).saveAll(anyList());

        verify(eventoTrabalhoRepository, never())
                .save(any(EventoTrabalho.class));

    }

    @Test
    void deveExcluirEtapaRenumerarRestantesESincronizarTrabalho() {

        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setCriadoPor(usuario);
        trabalho.setTipo(TipoTrabalho.PESSOAL);

        Etapa etapaExcluida = new Etapa();
        etapaExcluida.setId(20L);
        etapaExcluida.setOrdem(1);
        etapaExcluida.setTrabalho(trabalho);

        Etapa etapaRestante1 = new Etapa();
        etapaRestante1.setId(21L);
        etapaRestante1.setOrdem(2);
        etapaRestante1.setTrabalho(trabalho);

        Etapa etapaRestante2 = new Etapa();
        etapaRestante2.setId(22L);
        etapaRestante2.setOrdem(3);
        etapaRestante2.setTrabalho(trabalho);

        when(etapaRepository.findById(20L))
                .thenReturn(Optional.of(etapaExcluida));

        when(usuarioAutenticadoService.obterUsuario())
                .thenReturn(usuario);

        when(etapaRepository.findByTrabalhoIdOrderByOrdemAsc(10L))
                .thenReturn(List.of(etapaRestante1, etapaRestante2));

        // Executa
        etapaService.excluir(20L);

        // Verifica exclusão
        verify(etapaRepository).delete(etapaExcluida);

        // Verifica renumeração
        assertEquals(1, etapaRestante1.getOrdem());
        assertEquals(2, etapaRestante2.getOrdem());

        verify(etapaRepository).saveAll(argThat(etapas -> {
            List<Etapa> lista = new ArrayList<>();
            etapas.forEach(lista::add);

            return lista.size() == 2
                    && lista.get(0).getId().equals(21L)
                    && lista.get(1).getId().equals(22L);
        }));

        // Verifica sincronização do trabalho
        verify(trabalhoService).sincronizarStatusComEtapas(10L);
    }

    @Test
    void deveListarHistoricoDaEtapaComDadosDoUsuario() {

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Maria");

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setCriadoPor(usuario);
        trabalho.setTipo(TipoTrabalho.PESSOAL);

        Etapa etapa = new Etapa();
        etapa.setId(20L);
        etapa.setTrabalho(trabalho);

        HistoricoEtapa historico = new HistoricoEtapa();
        historico.setId(30L);
        historico.setStatusAnterior(StatusTrabalho.PENDENTE);
        historico.setStatusNovo(StatusTrabalho.EM_ANDAMENTO);
        historico.setDataAlteracao(LocalDateTime.of(2026, 11, 8, 10, 0));
        historico.setEtapa(etapa);
        historico.setUsuario(usuario);

        when(etapaRepository.findById(20L))
                .thenReturn(Optional.of(etapa));

        when(usuarioAutenticadoService.obterUsuario())
                .thenReturn(usuario);

        when(historicoEtapaRepository
                .findByEtapaIdOrderByDataAlteracaoDesc(20L))
                .thenReturn(List.of(historico));

        // Executa
        List<HistoricoEtapaRespostaDTO> resultado =
                etapaService.listarHistorico(20L);

        // Verifica consulta
        verify(historicoEtapaRepository)
                .findByEtapaIdOrderByDataAlteracaoDesc(20L);

        // Verifica conversão
        assertEquals(1, resultado.size());

        HistoricoEtapaRespostaDTO resposta = resultado.get(0);

        assertEquals(30L, resposta.getId());
        assertEquals(StatusTrabalho.PENDENTE, resposta.getStatusAnterior());
        assertEquals(StatusTrabalho.EM_ANDAMENTO, resposta.getStatusNovo());
        assertEquals(LocalDateTime.of(2026, 11, 8, 10, 0),
                resposta.getDataAlteracao());
        assertEquals(1L, resposta.getUsuarioId());
        assertEquals("Maria", resposta.getUsuarioNome());
    }

    @Test
    void deveRegistrarObservacaoComTextoTratadoEUsuarioAutenticado() {

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Maria");

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setCriadoPor(usuario);
        trabalho.setTipo(TipoTrabalho.PESSOAL);

        Etapa etapa = new Etapa();
        etapa.setId(20L);
        etapa.setTrabalho(trabalho);

        ObservacaoEtapaDTO dto = new ObservacaoEtapaDTO();
        dto.setTexto("  Documento revisado e aprovado.  ");

        when(etapaRepository.findById(20L))
                .thenReturn(Optional.of(etapa));

        when(usuarioAutenticadoService.obterUsuario())
                .thenReturn(usuario);

        when(eventoTrabalhoRepository
                .findFirstByEtapaIdAndTipoOrderByDataEventoDesc(
                        20L, TipoEventoTrabalho.OBSERVACAO_REGISTRADA))
                .thenReturn(Optional.empty());

        // Executa
        etapaService.registrarObservacao(20L, dto);

        // Captura o evento registrado
        ArgumentCaptor<EventoTrabalho> captor =
                ArgumentCaptor.forClass(EventoTrabalho.class);

        verify(eventoTrabalhoRepository).save(captor.capture());

        EventoTrabalho evento = captor.getValue();

        // Verifica os dados do evento
        assertEquals(
                TipoEventoTrabalho.OBSERVACAO_REGISTRADA,
                evento.getTipo()
        );

        assertEquals(
                "Documento revisado e aprovado.",
                evento.getDescricao()
        );

        assertEquals(etapa, evento.getEtapa());
        assertEquals(trabalho, evento.getTrabalho());
        assertEquals(usuario, evento.getUsuario());
    }

    @Test
    void deveCadastrarEtapaComStatusPendenteERegistrarEvento() {

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Maria");

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setCriadoPor(usuario);
        trabalho.setTipo(TipoTrabalho.PESSOAL);

        EtapaCadastroDTO dto = new EtapaCadastroDTO();
        dto.setTitulo("Revisar documentos");
        dto.setDescricao("Conferir documentação recebida");

        when(usuarioAutenticadoService.obterUsuario())
                .thenReturn(usuario);

        when(trabalhoRepository.findById(10L))
                .thenReturn(Optional.of(trabalho));

        when(etapaRepository.buscarMaiorOrdem(10L))
                .thenReturn(0);

        when(etapaRepository.save(any(Etapa.class)))
                .thenAnswer(invocation -> {
                    Etapa etapa = invocation.getArgument(0);
                    etapa.setId(20L);
                    return etapa;
                });

        when(eventoTrabalhoRepository
                .findFirstByEtapaIdAndTipoOrderByDataEventoDesc(
                        20L, TipoEventoTrabalho.OBSERVACAO_REGISTRADA))
                .thenReturn(Optional.empty());

        // Executa
        EtapaRespostaDTO resposta = etapaService.cadastrar(10L, dto);

        // Verifica a etapa cadastrada
        assertEquals(20L, resposta.getId());
        assertEquals("Revisar documentos", resposta.getTitulo());
        assertEquals("Conferir documentação recebida", resposta.getDescricao());
        assertEquals(StatusTrabalho.PENDENTE, resposta.getStatus());
        assertEquals(0, resposta.getPercentualConclusao());
        assertEquals(1, resposta.getOrdem());

        // Verifica o evento de criação
        ArgumentCaptor<EventoTrabalho> captor =
                ArgumentCaptor.forClass(EventoTrabalho.class);

        verify(eventoTrabalhoRepository).save(captor.capture());

        EventoTrabalho evento = captor.getValue();

        assertEquals(TipoEventoTrabalho.ETAPA_CRIADA, evento.getTipo());
        assertEquals(20L, evento.getEtapa().getId());
        assertEquals(trabalho, evento.getTrabalho());
        assertEquals(usuario, evento.getUsuario());

        verify(trabalhoService).sincronizarStatusComEtapas(10L);
    }

    @Test
    void deveCadastrarEtapaComResponsavelVinculadoAEmpresa() {

        Usuario criador = new Usuario();
        criador.setId(1L);
        criador.setNome("Maria");

        Usuario responsavel = new Usuario();
        responsavel.setId(2L);
        responsavel.setNome("João");

        Empresa empresa = new Empresa();
        empresa.setId(5L);

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setCriadoPor(criador);
        trabalho.setTipo(TipoTrabalho.EMPRESA);
        trabalho.setEmpresa(empresa);

        EtapaCadastroDTO dto = new EtapaCadastroDTO();
        dto.setTitulo("Revisar documentos");
        dto.setDescricao("Conferir documentação");
        dto.setResponsavelId(2L);

        when(usuarioAutenticadoService.obterUsuario())
                .thenReturn(criador);

        when(trabalhoRepository.findById(10L))
                .thenReturn(Optional.of(trabalho));

        when(etapaRepository.buscarMaiorOrdem(10L))
                .thenReturn(0);

        when(usuarioRepository.findById(2L))
                .thenReturn(Optional.of(responsavel));

        when(usuarioEmpresaRepository.existsByUsuarioAndEmpresa(
                responsavel, empresa))
                .thenReturn(true);

        when(etapaRepository.save(any(Etapa.class)))
                .thenAnswer(invocation -> {
                    Etapa etapa = invocation.getArgument(0);
                    etapa.setId(20L);
                    return etapa;
                });

        when(eventoTrabalhoRepository
                .findFirstByEtapaIdAndTipoOrderByDataEventoDesc(
                        20L, TipoEventoTrabalho.OBSERVACAO_REGISTRADA))
                .thenReturn(Optional.empty());

        // Executa
        etapaService.cadastrar(10L, dto);

        // Verifica o responsável associado à etapa
        ArgumentCaptor<Etapa> captor =
                ArgumentCaptor.forClass(Etapa.class);

        verify(etapaRepository).save(captor.capture());

        Etapa etapaSalva = captor.getValue();

        assertEquals(responsavel, etapaSalva.getResponsavel());
        assertEquals(20L, etapaSalva.getId());

        verify(usuarioEmpresaRepository)
                .existsByUsuarioAndEmpresa(responsavel, empresa);

        verify(trabalhoService).sincronizarStatusComEtapas(10L);
    }

    @Test
    void deveImpedirCadastroDeEtapaComResponsavelDeOutraEmpresa() {

        Usuario criador = new Usuario();
        criador.setId(1L);

        Usuario responsavel = new Usuario();
        responsavel.setId(2L);
        responsavel.setNome("João");

        Empresa empresa = new Empresa();
        empresa.setId(5L);

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setCriadoPor(criador);
        trabalho.setTipo(TipoTrabalho.EMPRESA);
        trabalho.setEmpresa(empresa);

        EtapaCadastroDTO dto = new EtapaCadastroDTO();
        dto.setTitulo("Revisar documentos");
        dto.setDescricao("Conferir documentação");
        dto.setResponsavelId(2L);

        when(usuarioAutenticadoService.obterUsuario())
                .thenReturn(criador);

        when(trabalhoRepository.findById(10L))
                .thenReturn(Optional.of(trabalho));

        when(usuarioRepository.findById(2L))
                .thenReturn(Optional.of(responsavel));

        when(usuarioEmpresaRepository.existsByUsuarioAndEmpresa(
                responsavel, empresa))
                .thenReturn(false);

        assertThrows(
                UsuarioEmpresaDiferenteException.class,
                () -> etapaService.cadastrar(10L, dto)
        );

        verify(etapaRepository, never()).save(any(Etapa.class));
        verify(eventoTrabalhoRepository, never()).save(any(EventoTrabalho.class));
        verify(trabalhoService, never()).sincronizarStatusComEtapas(10L);
    }

    @Test
    void deveAtualizarTituloEDescricaoDaEtapa() {

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Maria");

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setCriadoPor(usuario);
        trabalho.setTipo(TipoTrabalho.PESSOAL);

        Etapa etapa = new Etapa();
        etapa.setId(20L);
        etapa.setTitulo("Título antigo");
        etapa.setDescricao("Descrição antiga");
        etapa.setTrabalho(trabalho);

        EtapaAtualizacaoDTO dto = new EtapaAtualizacaoDTO();
        dto.setTitulo("Título atualizado");
        dto.setDescricao("Descrição atualizada");

        when(etapaRepository.findById(20L))
                .thenReturn(Optional.of(etapa));

        when(usuarioAutenticadoService.obterUsuario())
                .thenReturn(usuario);

        when(etapaRepository.save(any(Etapa.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(eventoTrabalhoRepository
                .findFirstByEtapaIdAndTipoOrderByDataEventoDesc(
                        20L, TipoEventoTrabalho.OBSERVACAO_REGISTRADA))
                .thenReturn(Optional.empty());

        // Executa
        EtapaRespostaDTO resposta = etapaService.atualizar(20L, dto);

        // Verifica atualização
        assertEquals("Título atualizado", etapa.getTitulo());
        assertEquals("Descrição atualizada", etapa.getDescricao());
        assertEquals("Título atualizado", resposta.getTitulo());
        assertEquals("Descrição atualizada", resposta.getDescricao());

        verify(etapaRepository).save(etapa);

        // Verifica os eventos de edição
        ArgumentCaptor<EventoTrabalho> captor =
                ArgumentCaptor.forClass(EventoTrabalho.class);

        verify(eventoTrabalhoRepository, times(2)).save(captor.capture());

        assertTrue(captor.getAllValues().stream()
                .allMatch(evento ->
                        evento.getTipo() == TipoEventoTrabalho.ETAPA_EDITADA));

        assertTrue(captor.getAllValues().stream()
                .allMatch(evento -> evento.getUsuario().equals(usuario)));
    }

    @Test
    void deveAtualizarSomenteTituloDaEtapa() {

        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setCriadoPor(usuario);
        trabalho.setTipo(TipoTrabalho.PESSOAL);

        Etapa etapa = new Etapa();
        etapa.setId(20L);
        etapa.setTitulo("Título antigo");
        etapa.setDescricao("Descrição original");
        etapa.setTrabalho(trabalho);

        EtapaAtualizacaoDTO dto = new EtapaAtualizacaoDTO();
        dto.setTitulo("Título novo");
        dto.setDescricao("Descrição original");

        when(etapaRepository.findById(20L)).thenReturn(Optional.of(etapa));
        when(usuarioAutenticadoService.obterUsuario()).thenReturn(usuario);
        when(etapaRepository.save(any(Etapa.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(eventoTrabalhoRepository
                .findFirstByEtapaIdAndTipoOrderByDataEventoDesc(
                        20L, TipoEventoTrabalho.OBSERVACAO_REGISTRADA))
                .thenReturn(Optional.empty());

        EtapaRespostaDTO resposta = etapaService.atualizar(20L, dto);

        assertEquals("Título novo", resposta.getTitulo());
        assertEquals("Descrição original", resposta.getDescricao());

        verify(etapaRepository).save(etapa);
        verify(eventoTrabalhoRepository).save(any(EventoTrabalho.class));
    }

    @Test
    void deveAtualizarSomenteDescricaoDaEtapa() {

        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Trabalho trabalho = new Trabalho();
        trabalho.setId(10L);
        trabalho.setCriadoPor(usuario);
        trabalho.setTipo(TipoTrabalho.PESSOAL);

        Etapa etapa = new Etapa();
        etapa.setId(20L);
        etapa.setTitulo("Título original");
        etapa.setDescricao("Descrição antiga");
        etapa.setTrabalho(trabalho);

        EtapaAtualizacaoDTO dto = new EtapaAtualizacaoDTO();
        dto.setTitulo("Título original");
        dto.setDescricao("Descrição nova");

        when(etapaRepository.findById(20L)).thenReturn(Optional.of(etapa));
        when(usuarioAutenticadoService.obterUsuario()).thenReturn(usuario);
        when(etapaRepository.save(any(Etapa.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(eventoTrabalhoRepository
                .findFirstByEtapaIdAndTipoOrderByDataEventoDesc(
                        20L, TipoEventoTrabalho.OBSERVACAO_REGISTRADA))
                .thenReturn(Optional.empty());

        EtapaRespostaDTO resposta = etapaService.atualizar(20L, dto);

        assertEquals("Título original", resposta.getTitulo());
        assertEquals("Descrição nova", resposta.getDescricao());

        verify(etapaRepository).save(etapa);
        verify(eventoTrabalhoRepository).save(any(EventoTrabalho.class));
    }

}
