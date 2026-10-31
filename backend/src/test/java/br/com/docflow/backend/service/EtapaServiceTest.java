package br.com.docflow.backend.service;

import br.com.docflow.backend.dto.EtapaAtualizacaoDTO;
import br.com.docflow.backend.dto.EtapaCadastroDTO;
import br.com.docflow.backend.dto.EtapaOrdemDTO;
import br.com.docflow.backend.dto.EtapaPercentualDTO;
import br.com.docflow.backend.dto.EtapaRespostaDTO;
import br.com.docflow.backend.dto.EtapaResponsavelDTO;
import br.com.docflow.backend.dto.EtapaStatusDTO;
import br.com.docflow.backend.entity.Etapa;
import br.com.docflow.backend.entity.HistoricoEtapa;
import br.com.docflow.backend.entity.StatusEtapa;
import br.com.docflow.backend.entity.Trabalho;
import br.com.docflow.backend.entity.Usuario;
import br.com.docflow.backend.repository.EtapaRepository;
import br.com.docflow.backend.repository.HistoricoEtapaRepository;
import br.com.docflow.backend.repository.TrabalhoRepository;
import br.com.docflow.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Testes de regra de negócio do EtapaService.
 *
 * Cenário padrão: o trabalho 10 foi criado pelo usuário 1 (criador); o usuário 2 é
 * participante autorizado; o usuário 3 não tem relação com o trabalho.
 *
 * Atenção: com MockitoExtension (strict stubs) todo stub precisa ser usado no teste.
 * Por isso os stubs são feitos dentro de cada teste, e não no @BeforeEach.
 */
@ExtendWith(MockitoExtension.class)
class EtapaServiceTest {

    private static final Long TRABALHO_ID = 10L;
    private static final Long ETAPA_ID = 20L;

    @Mock
    private EtapaRepository etapaRepository;

    @Mock
    private HistoricoEtapaRepository historicoEtapaRepository;

    @Mock
    private TrabalhoRepository trabalhoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @InjectMocks
    private EtapaService etapaService;

    private Usuario criador;
    private Usuario participante;
    private Usuario estranho;
    private Trabalho trabalho;

    @BeforeEach
    void criarCenario() {
        criador = usuario(1L);
        participante = usuario(2L);
        estranho = usuario(3L);

        trabalho = new Trabalho();
        trabalho.setId(TRABALHO_ID);
        trabalho.setCriadoPor(criador);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static Usuario usuario(Long id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        return usuario;
    }

    private Etapa etapa(Long id, StatusEtapa status, int percentual, int ordem) {
        Etapa etapa = new Etapa();
        etapa.setId(id);
        etapa.setTitulo("Etapa " + id);
        etapa.setStatus(status);
        etapa.setPercentualConclusao(percentual);
        etapa.setOrdem(ordem);
        etapa.setTrabalho(trabalho);
        return etapa;
    }

    private Etapa etapaExistente(StatusEtapa status, int percentual) {
        Etapa etapa = etapa(ETAPA_ID, status, percentual, 1);
        when(etapaRepository.findById(ETAPA_ID)).thenReturn(Optional.of(etapa));
        return etapa;
    }

    private void autenticarComo(Usuario usuario) {
        when(usuarioAutenticadoService.obterUsuario()).thenReturn(usuario);
    }

    private void permitirAcessoAoTrabalho(Usuario usuario) {
        when(trabalhoRepository.findAcessiveisPorUsuario(usuario)).thenReturn(List.of(trabalho));
    }

    private void negarAcessoAoTrabalho(Usuario usuario) {
        when(trabalhoRepository.findAcessiveisPorUsuario(usuario)).thenReturn(List.of());
    }

    private void salvarRetornandoOArgumento() {
        when(etapaRepository.save(any(Etapa.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));
    }

    private void trabalhoExistente() {
        when(trabalhoRepository.findById(TRABALHO_ID)).thenReturn(Optional.of(trabalho));
    }

    private EtapaCadastroDTO cadastroDto() {
        EtapaCadastroDTO dto = new EtapaCadastroDTO();
        dto.setTitulo("Levantar requisitos");
        dto.setDescricao("Reunião com o cliente");
        return dto;
    }

    // ------------------------------------------------------------------
    // CADASTRO
    // ------------------------------------------------------------------

    @Test
    void cadastrar_criadorDoTrabalho_criaEtapaPendenteComZeroPorCentoNoFinalDaLista() {
        trabalhoExistente();
        autenticarComo(criador);
        when(etapaRepository.buscarMaiorOrdem(TRABALHO_ID)).thenReturn(2);
        when(etapaRepository.save(any(Etapa.class))).thenAnswer(invocacao -> {
            Etapa salva = invocacao.getArgument(0);
            salva.setId(99L);
            return salva;
        });

        EtapaRespostaDTO resposta = etapaService.cadastrar(TRABALHO_ID, cadastroDto());

        ArgumentCaptor<Etapa> captor = ArgumentCaptor.forClass(Etapa.class);
        verify(etapaRepository).save(captor.capture());
        Etapa salva = captor.getValue();

        assertThat(salva.getTitulo()).isEqualTo("Levantar requisitos");
        assertThat(salva.getDescricao()).isEqualTo("Reunião com o cliente");
        assertThat(salva.getStatus()).isEqualTo(StatusEtapa.PENDENTE);
        assertThat(salva.getPercentualConclusao()).isZero();
        assertThat(salva.getOrdem()).isEqualTo(3);
        assertThat(salva.getTrabalho()).isSameAs(trabalho);
        assertThat(salva.getResponsavel()).isNull();

        assertThat(resposta.getId()).isEqualTo(99L);
        assertThat(resposta.getStatus()).isEqualTo(StatusEtapa.PENDENTE);
        assertThat(resposta.getTrabalhoId()).isEqualTo(TRABALHO_ID);
    }

    @Test
    void cadastrar_participanteAutorizado_consegueCadastrar() {
        trabalhoExistente();
        autenticarComo(participante);
        permitirAcessoAoTrabalho(participante);
        when(etapaRepository.buscarMaiorOrdem(TRABALHO_ID)).thenReturn(0);
        salvarRetornandoOArgumento();

        EtapaRespostaDTO resposta = etapaService.cadastrar(TRABALHO_ID, cadastroDto());

        assertThat(resposta.getOrdem()).isEqualTo(1);
        verify(etapaRepository).save(any(Etapa.class));
    }

    @Test
    void cadastrar_usuarioSemAcesso_negaESemSalvar() {
        trabalhoExistente();
        autenticarComo(estranho);
        negarAcessoAoTrabalho(estranho);

        assertThatThrownBy(() -> etapaService.cadastrar(TRABALHO_ID, cadastroDto()))
                .isInstanceOf(AccessDeniedException.class);

        verify(etapaRepository, never()).save(any(Etapa.class));
    }

    @Test
    void cadastrar_trabalhoInexistente_lancaRecursoNaoEncontrado() {
        when(trabalhoRepository.findById(TRABALHO_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> etapaService.cadastrar(TRABALHO_ID, cadastroDto()))
                .isInstanceOf(RecursoNaoEncontradoException.class);

        verify(etapaRepository, never()).save(any(Etapa.class));
    }

    @Test
    void cadastrar_comOrdemInformada_reposicionaEDesceAsDemaisEtapas() {
        Etapa primeira = etapa(1L, StatusEtapa.PENDENTE, 0, 1);
        Etapa segunda = etapa(2L, StatusEtapa.PENDENTE, 0, 2);
        Etapa[] nova = new Etapa[1];

        trabalhoExistente();
        autenticarComo(criador);
        when(etapaRepository.buscarMaiorOrdem(TRABALHO_ID)).thenReturn(2);
        when(etapaRepository.save(any(Etapa.class))).thenAnswer(invocacao -> {
            Etapa salva = invocacao.getArgument(0);
            salva.setId(99L);
            nova[0] = salva;
            return salva;
        });
        when(etapaRepository.findByTrabalhoIdOrderByOrdemAsc(TRABALHO_ID))
                .thenAnswer(invocacao -> List.of(primeira, segunda, nova[0]));

        EtapaCadastroDTO dto = cadastroDto();
        dto.setOrdem(1);

        EtapaRespostaDTO resposta = etapaService.cadastrar(TRABALHO_ID, dto);

        assertThat(nova[0].getOrdem()).isEqualTo(1);
        assertThat(primeira.getOrdem()).isEqualTo(2);
        assertThat(segunda.getOrdem()).isEqualTo(3);
        assertThat(resposta.getOrdem()).isEqualTo(1);
    }

    @Test
    void cadastrar_comOrdemMaiorQueOTotal_vaiParaOFinal() {
        Etapa primeira = etapa(1L, StatusEtapa.PENDENTE, 0, 1);
        Etapa[] nova = new Etapa[1];

        trabalhoExistente();
        autenticarComo(criador);
        when(etapaRepository.buscarMaiorOrdem(TRABALHO_ID)).thenReturn(1);
        when(etapaRepository.save(any(Etapa.class))).thenAnswer(invocacao -> {
            Etapa salva = invocacao.getArgument(0);
            salva.setId(99L);
            nova[0] = salva;
            return salva;
        });
        when(etapaRepository.findByTrabalhoIdOrderByOrdemAsc(TRABALHO_ID))
                .thenAnswer(invocacao -> List.of(primeira, nova[0]));

        EtapaCadastroDTO dto = cadastroDto();
        dto.setOrdem(50);

        etapaService.cadastrar(TRABALHO_ID, dto);

        assertThat(primeira.getOrdem()).isEqualTo(1);
        assertThat(nova[0].getOrdem()).isEqualTo(2);
    }

    @Test
    void cadastrar_comResponsavelParticipante_associaOResponsavel() {
        trabalhoExistente();
        autenticarComo(criador);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(participante));
        permitirAcessoAoTrabalho(participante);
        when(etapaRepository.buscarMaiorOrdem(TRABALHO_ID)).thenReturn(0);
        salvarRetornandoOArgumento();

        EtapaCadastroDTO dto = cadastroDto();
        dto.setResponsavelId(2L);

        EtapaRespostaDTO resposta = etapaService.cadastrar(TRABALHO_ID, dto);

        assertThat(resposta.getResponsavelId()).isEqualTo(2L);
    }

    @Test
    void cadastrar_comResponsavelSemAcessoAoTrabalho_rejeitaESemSalvar() {
        trabalhoExistente();
        autenticarComo(criador);
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(estranho));
        negarAcessoAoTrabalho(estranho);

        EtapaCadastroDTO dto = cadastroDto();
        dto.setResponsavelId(3L);

        assertThatThrownBy(() -> etapaService.cadastrar(TRABALHO_ID, dto))
                .isInstanceOf(ResponsavelSemAcessoException.class);

        verify(etapaRepository, never()).save(any(Etapa.class));
    }

    // ------------------------------------------------------------------
    // EDIÇÃO
    // ------------------------------------------------------------------

    @Test
    void atualizar_criadorDoTrabalho_alteraTituloEDescricaoSemMexerEmStatusEPercentual() {
        Etapa etapa = etapaExistente(StatusEtapa.EM_ANDAMENTO, 40);
        autenticarComo(criador);
        salvarRetornandoOArgumento();

        EtapaAtualizacaoDTO dto = new EtapaAtualizacaoDTO();
        dto.setTitulo("Novo título");
        dto.setDescricao("Nova descrição");

        EtapaRespostaDTO resposta = etapaService.atualizar(ETAPA_ID, dto);

        assertThat(etapa.getTitulo()).isEqualTo("Novo título");
        assertThat(etapa.getDescricao()).isEqualTo("Nova descrição");
        assertThat(resposta.getTitulo()).isEqualTo("Novo título");
        assertThat(resposta.getStatus()).isEqualTo(StatusEtapa.EM_ANDAMENTO);
        assertThat(resposta.getPercentualConclusao()).isEqualTo(40);
    }

    @Test
    void atualizar_participanteAutorizado_consegueEditar() {
        etapaExistente(StatusEtapa.PENDENTE, 0);
        autenticarComo(participante);
        permitirAcessoAoTrabalho(participante);
        salvarRetornandoOArgumento();

        EtapaAtualizacaoDTO dto = new EtapaAtualizacaoDTO();
        dto.setTitulo("Editado pelo participante");
        dto.setDescricao("ok");

        EtapaRespostaDTO resposta = etapaService.atualizar(ETAPA_ID, dto);

        assertThat(resposta.getTitulo()).isEqualTo("Editado pelo participante");
    }

    @Test
    void atualizar_etapaInexistente_lancaRecursoNaoEncontrado() {
        when(etapaRepository.findById(ETAPA_ID)).thenReturn(Optional.empty());

        EtapaAtualizacaoDTO dto = new EtapaAtualizacaoDTO();
        dto.setTitulo("x");
        dto.setDescricao("y");

        assertThatThrownBy(() -> etapaService.atualizar(ETAPA_ID, dto))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    // ------------------------------------------------------------------
    // STATUS x PERCENTUAL - alterarStatus
    // ------------------------------------------------------------------

    @ParameterizedTest(name = "{0} ({1}%) -> {2}, retorno informado={3} => {4}%")
    @CsvSource({
            // anterior,   %anterior, novo,         %informado, %esperado
            "PENDENTE,     0,         EM_ANDAMENTO, ,          0",
            "PENDENTE,     0,         IMPEDIDO,     ,          0",
            "PENDENTE,     0,         CONCLUIDO,    ,          100",
            "EM_ANDAMENTO, 40,        IMPEDIDO,     ,          40",
            "EM_ANDAMENTO, 40,        CONCLUIDO,    ,          100",
            "IMPEDIDO,     40,        EM_ANDAMENTO, ,          40",
            "IMPEDIDO,     40,        CONCLUIDO,    ,          100",
            // saindo de CONCLUIDO o usuário informa o percentual de retorno
            "CONCLUIDO,    100,       EM_ANDAMENTO, 60,        60",
            "CONCLUIDO,    100,       EM_ANDAMENTO, 99,        99",
            "CONCLUIDO,    100,       EM_ANDAMENTO, 0,         0",
            "CONCLUIDO,    100,       IMPEDIDO,     30,        30",
            "CONCLUIDO,    100,       PENDENTE,     0,         0"
    })
    void alterarStatus_mantemStatusEPercentualConsistentes(
            StatusEtapa anterior, int percentualAnterior, StatusEtapa novo,
            Integer percentualInformado, int percentualEsperado) {

        Etapa etapa = etapaExistente(anterior, percentualAnterior);
        autenticarComo(criador);
        salvarRetornandoOArgumento();

        EtapaStatusDTO dto = new EtapaStatusDTO();
        dto.setStatus(novo);
        dto.setPercentualConclusao(percentualInformado);

        EtapaRespostaDTO resposta = etapaService.alterarStatus(ETAPA_ID, dto);

        assertThat(etapa.getStatus()).isEqualTo(novo);
        assertThat(etapa.getPercentualConclusao()).isEqualTo(percentualEsperado);
        assertThat(resposta.getStatus()).isEqualTo(novo);
        assertThat(resposta.getPercentualConclusao()).isEqualTo(percentualEsperado);

        ArgumentCaptor<HistoricoEtapa> captor = ArgumentCaptor.forClass(HistoricoEtapa.class);
        verify(historicoEtapaRepository).save(captor.capture());
        assertThat(captor.getValue().getStatusAnterior()).isEqualTo(anterior);
        assertThat(captor.getValue().getStatusNovo()).isEqualTo(novo);
    }

    @ParameterizedTest(name = "reabrir CONCLUIDO como {0} com percentual {1} deve ser rejeitado")
    @CsvSource({
            "EM_ANDAMENTO, ",     // percentual obrigatório
            "IMPEDIDO,     ",
            "EM_ANDAMENTO, 100",  // continuaria concluída
            "EM_ANDAMENTO, -1",
            "EM_ANDAMENTO, 150",
            "PENDENTE,     10"    // pendente só aceita 0%
    })
    void alterarStatus_saindoDeConcluidoComPercentualInvalido_rejeitaSemAlterarNada(
            StatusEtapa novo, Integer percentualInformado) {

        Etapa etapa = etapaExistente(StatusEtapa.CONCLUIDO, 100);
        autenticarComo(criador);

        EtapaStatusDTO dto = new EtapaStatusDTO();
        dto.setStatus(novo);
        dto.setPercentualConclusao(percentualInformado);

        assertThatThrownBy(() -> etapaService.alterarStatus(ETAPA_ID, dto))
                .isInstanceOf(EtapaPercentualInvalidoException.class);

        assertThat(etapa.getStatus()).isEqualTo(StatusEtapa.CONCLUIDO);
        assertThat(etapa.getPercentualConclusao()).isEqualTo(100);
        verify(etapaRepository, never()).save(any(Etapa.class));
        verifyNoInteractions(historicoEtapaRepository);
    }

    @Test
    void alterarStatus_mesmoStatus_lancaStatusInalterado() {
        etapaExistente(StatusEtapa.EM_ANDAMENTO, 50);
        autenticarComo(criador);

        EtapaStatusDTO dto = new EtapaStatusDTO();
        dto.setStatus(StatusEtapa.EM_ANDAMENTO);

        assertThatThrownBy(() -> etapaService.alterarStatus(ETAPA_ID, dto))
                .isInstanceOf(EtapaStatusInalteradoException.class);

        verify(etapaRepository, never()).save(any(Etapa.class));
        verifyNoInteractions(historicoEtapaRepository);
    }

    @Test
    void alterarStatus_historicoUsaUsuarioDoJwtEIgnoraUsuarioIdDoCorpo() {
        etapaExistente(StatusEtapa.PENDENTE, 0);
        autenticarComo(participante);
        permitirAcessoAoTrabalho(participante);
        salvarRetornandoOArgumento();

        EtapaStatusDTO dto = new EtapaStatusDTO();
        dto.setStatus(StatusEtapa.EM_ANDAMENTO);
        dto.setUsuarioId(999L);

        etapaService.alterarStatus(ETAPA_ID, dto);

        ArgumentCaptor<HistoricoEtapa> captor = ArgumentCaptor.forClass(HistoricoEtapa.class);
        verify(historicoEtapaRepository).save(captor.capture());

        assertThat(captor.getValue().getUsuario()).isSameAs(participante);
        assertThat(captor.getValue().getUsuario().getId()).isNotEqualTo(999L);
    }

    @Test
    void alterarStatus_paraImpedido_naoImpoeNovasRestricoes() {
        // Regra da líder: IMPEDIDO não cria restrições novas; só muda o status
        Etapa etapa = etapaExistente(StatusEtapa.EM_ANDAMENTO, 70);
        autenticarComo(criador);
        salvarRetornandoOArgumento();

        EtapaStatusDTO dto = new EtapaStatusDTO();
        dto.setStatus(StatusEtapa.IMPEDIDO);

        etapaService.alterarStatus(ETAPA_ID, dto);

        assertThat(etapa.getStatus()).isEqualTo(StatusEtapa.IMPEDIDO);
        assertThat(etapa.getPercentualConclusao()).isEqualTo(70);
    }

    // ------------------------------------------------------------------
    // STATUS x PERCENTUAL - atualizarPercentual
    // ------------------------------------------------------------------

    @ParameterizedTest(name = "{0} ({1}%) -> {2}% => {3}")
    @CsvSource({
            // anterior,   %anterior, %novo, statusEsperado
            "PENDENTE,     0,         10,    EM_ANDAMENTO",
            "PENDENTE,     0,         0,     PENDENTE",
            "PENDENTE,     0,         100,   CONCLUIDO",
            "EM_ANDAMENTO, 10,        50,    EM_ANDAMENTO",
            "EM_ANDAMENTO, 10,        100,   CONCLUIDO",
            "EM_ANDAMENTO, 50,        0,     EM_ANDAMENTO",
            "IMPEDIDO,     30,        60,    IMPEDIDO",
            "IMPEDIDO,     30,        100,   CONCLUIDO",
            "CONCLUIDO,    100,       80,    EM_ANDAMENTO",
            "CONCLUIDO,    100,       0,     EM_ANDAMENTO",
            "CONCLUIDO,    100,       100,   CONCLUIDO"
    })
    void atualizarPercentual_ajustaStatusConformeARegra(
            StatusEtapa anterior, int percentualAnterior, int percentualNovo,
            StatusEtapa statusEsperado) {

        Etapa etapa = etapaExistente(anterior, percentualAnterior);
        autenticarComo(criador);
        salvarRetornandoOArgumento();

        EtapaPercentualDTO dto = new EtapaPercentualDTO();
        dto.setPercentualConclusao(percentualNovo);

        EtapaRespostaDTO resposta = etapaService.atualizarPercentual(ETAPA_ID, dto);

        assertThat(etapa.getPercentualConclusao()).isEqualTo(percentualNovo);
        assertThat(etapa.getStatus()).isEqualTo(statusEsperado);
        assertThat(resposta.getStatus()).isEqualTo(statusEsperado);
        assertThat(resposta.getPercentualConclusao()).isEqualTo(percentualNovo);

        if (anterior == statusEsperado) {
            verifyNoInteractions(historicoEtapaRepository);
        } else {
            ArgumentCaptor<HistoricoEtapa> captor = ArgumentCaptor.forClass(HistoricoEtapa.class);
            verify(historicoEtapaRepository).save(captor.capture());
            assertThat(captor.getValue().getStatusAnterior()).isEqualTo(anterior);
            assertThat(captor.getValue().getStatusNovo()).isEqualTo(statusEsperado);
        }
    }

    @Test
    void atualizarPercentual_historicoUsaUsuarioDoJwtEIgnoraUsuarioIdDoCorpo() {
        etapaExistente(StatusEtapa.PENDENTE, 0);
        autenticarComo(participante);
        permitirAcessoAoTrabalho(participante);
        salvarRetornandoOArgumento();

        EtapaPercentualDTO dto = new EtapaPercentualDTO();
        dto.setPercentualConclusao(30);
        dto.setUsuarioId(999L);

        etapaService.atualizarPercentual(ETAPA_ID, dto);

        ArgumentCaptor<HistoricoEtapa> captor = ArgumentCaptor.forClass(HistoricoEtapa.class);
        verify(historicoEtapaRepository).save(captor.capture());
        assertThat(captor.getValue().getUsuario()).isSameAs(participante);
    }

    // ------------------------------------------------------------------
    // RESPONSÁVEL (precisa ser criador ou participante do trabalho)
    // ------------------------------------------------------------------

    @Test
    void definirResponsavel_participanteDoTrabalho_associa() {
        Etapa etapa = etapaExistente(StatusEtapa.PENDENTE, 0);
        autenticarComo(criador);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(participante));
        permitirAcessoAoTrabalho(participante);
        salvarRetornandoOArgumento();

        EtapaResponsavelDTO dto = new EtapaResponsavelDTO();
        dto.setResponsavelId(2L);

        EtapaRespostaDTO resposta = etapaService.definirResponsavel(ETAPA_ID, dto);

        assertThat(etapa.getResponsavel()).isSameAs(participante);
        assertThat(resposta.getResponsavelId()).isEqualTo(2L);
    }

    @Test
    void definirResponsavel_criadorDoTrabalho_associa() {
        Etapa etapa = etapaExistente(StatusEtapa.PENDENTE, 0);
        autenticarComo(participante);
        permitirAcessoAoTrabalho(participante);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(criador));
        salvarRetornandoOArgumento();

        EtapaResponsavelDTO dto = new EtapaResponsavelDTO();
        dto.setResponsavelId(1L);

        etapaService.definirResponsavel(ETAPA_ID, dto);

        assertThat(etapa.getResponsavel()).isSameAs(criador);
    }

    @Test
    void definirResponsavel_usuarioForaDoTrabalho_rejeita() {
        // Mesmo que o usuário seja da mesma empresa, sem participação no trabalho não pode ser responsável
        Etapa etapa = etapaExistente(StatusEtapa.PENDENTE, 0);
        autenticarComo(criador);
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(estranho));
        negarAcessoAoTrabalho(estranho);

        EtapaResponsavelDTO dto = new EtapaResponsavelDTO();
        dto.setResponsavelId(3L);

        assertThatThrownBy(() -> etapaService.definirResponsavel(ETAPA_ID, dto))
                .isInstanceOf(ResponsavelSemAcessoException.class);

        assertThat(etapa.getResponsavel()).isNull();
        verify(etapaRepository, never()).save(any(Etapa.class));
    }

    @Test
    void definirResponsavel_usuarioInexistente_lancaRecursoNaoEncontrado() {
        etapaExistente(StatusEtapa.PENDENTE, 0);
        autenticarComo(criador);
        when(usuarioRepository.findById(77L)).thenReturn(Optional.empty());

        EtapaResponsavelDTO dto = new EtapaResponsavelDTO();
        dto.setResponsavelId(77L);

        assertThatThrownBy(() -> etapaService.definirResponsavel(ETAPA_ID, dto))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void definirResponsavel_semId_removeOResponsavel() {
        Etapa etapa = etapaExistente(StatusEtapa.PENDENTE, 0);
        etapa.setResponsavel(participante);
        autenticarComo(criador);
        salvarRetornandoOArgumento();

        etapaService.definirResponsavel(ETAPA_ID, new EtapaResponsavelDTO());

        assertThat(etapa.getResponsavel()).isNull();
    }

    // ------------------------------------------------------------------
    // AUTORIZAÇÃO
    // ------------------------------------------------------------------

    static Stream<Arguments> operacoesSobreEtapaExistente() {
        return Stream.of(
                operacao("buscarPorId", s -> s.buscarPorId(ETAPA_ID)),
                operacao("atualizar", s -> {
                    EtapaAtualizacaoDTO dto = new EtapaAtualizacaoDTO();
                    dto.setTitulo("x");
                    dto.setDescricao("y");
                    s.atualizar(ETAPA_ID, dto);
                }),
                operacao("alterarStatus", s -> {
                    EtapaStatusDTO dto = new EtapaStatusDTO();
                    dto.setStatus(StatusEtapa.EM_ANDAMENTO);
                    s.alterarStatus(ETAPA_ID, dto);
                }),
                operacao("atualizarPercentual", s -> {
                    EtapaPercentualDTO dto = new EtapaPercentualDTO();
                    dto.setPercentualConclusao(50);
                    s.atualizarPercentual(ETAPA_ID, dto);
                }),
                operacao("definirResponsavel", s -> {
                    EtapaResponsavelDTO dto = new EtapaResponsavelDTO();
                    dto.setResponsavelId(2L);
                    s.definirResponsavel(ETAPA_ID, dto);
                }),
                operacao("alterarOrdem", s -> {
                    EtapaOrdemDTO dto = new EtapaOrdemDTO();
                    dto.setOrdem(1);
                    s.alterarOrdem(ETAPA_ID, dto);
                }),
                operacao("listarHistorico", s -> s.listarHistorico(ETAPA_ID)),
                operacao("excluir", s -> s.excluir(ETAPA_ID))
        );
    }

    private static Arguments operacao(String nome, Consumer<EtapaService> acao) {
        return Arguments.of(nome, acao);
    }

    @ParameterizedTest(name = "{0}: usuário sem acesso ao trabalho recebe AccessDenied e nada é alterado")
    @MethodSource("operacoesSobreEtapaExistente")
    void operacoesSobreEtapa_usuarioSemAcesso_negam(String nome, Consumer<EtapaService> acao) {
        etapaExistente(StatusEtapa.PENDENTE, 0);
        autenticarComo(estranho);
        negarAcessoAoTrabalho(estranho);

        assertThatThrownBy(() -> acao.accept(etapaService))
                .isInstanceOf(AccessDeniedException.class);

        verify(etapaRepository, never()).save(any(Etapa.class));
        verify(etapaRepository, never()).saveAll(anyList());
        verify(etapaRepository, never()).delete(any(Etapa.class));
        verifyNoInteractions(historicoEtapaRepository);
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void listarPorTrabalho_usuarioSemAcesso_nega() {
        trabalhoExistente();
        autenticarComo(estranho);
        negarAcessoAoTrabalho(estranho);

        assertThatThrownBy(() -> etapaService.listarPorTrabalho(TRABALHO_ID))
                .isInstanceOf(AccessDeniedException.class);

        verify(etapaRepository, never()).findByTrabalhoIdOrderByOrdemAsc(any());
    }

    @Test
    void listarPorTrabalho_criadorEParticipante_veemAsEtapasEmOrdem() {
        Etapa primeira = etapa(1L, StatusEtapa.PENDENTE, 0, 1);
        Etapa segunda = etapa(2L, StatusEtapa.EM_ANDAMENTO, 20, 2);
        when(trabalhoRepository.findById(TRABALHO_ID)).thenReturn(Optional.of(trabalho));
        when(etapaRepository.findByTrabalhoIdOrderByOrdemAsc(TRABALHO_ID))
                .thenReturn(List.of(primeira, segunda));
        autenticarComo(participante);
        permitirAcessoAoTrabalho(participante);

        List<EtapaRespostaDTO> resposta = etapaService.listarPorTrabalho(TRABALHO_ID);

        assertThat(resposta).extracting(EtapaRespostaDTO::getId).containsExactly(1L, 2L);
    }

    @Test
    void buscarPorId_participanteAutorizado_retornaAEtapa() {
        etapaExistente(StatusEtapa.PENDENTE, 0);
        autenticarComo(participante);
        permitirAcessoAoTrabalho(participante);

        EtapaRespostaDTO resposta = etapaService.buscarPorId(ETAPA_ID);

        assertThat(resposta.getId()).isEqualTo(ETAPA_ID);
        assertThat(resposta.getTrabalhoId()).isEqualTo(TRABALHO_ID);
    }

    @Test
    void excluir_participanteAutorizado_excluiERenumeraAsDemais() {
        Etapa excluida = etapaExistente(StatusEtapa.PENDENTE, 0);
        Etapa restante = etapa(30L, StatusEtapa.PENDENTE, 0, 3);
        autenticarComo(participante);
        permitirAcessoAoTrabalho(participante);
        when(etapaRepository.findByTrabalhoIdOrderByOrdemAsc(TRABALHO_ID))
                .thenReturn(List.of(restante));

        etapaService.excluir(ETAPA_ID);

        verify(etapaRepository).delete(excluida);
        assertThat(restante.getOrdem()).isEqualTo(1);
    }

    @Test
    void buscarPorId_etapaInexistente_lancaRecursoNaoEncontrado() {
        when(etapaRepository.findById(ETAPA_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> etapaService.buscarPorId(ETAPA_ID))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
