
package br.com.docflow.backend.service;

import br.com.docflow.backend.entity.Empresa;
import br.com.docflow.backend.entity.Etapa;
import br.com.docflow.backend.entity.Trabalho;
import br.com.docflow.backend.entity.Usuario;
import br.com.docflow.backend.repository.EtapaRepository;
import br.com.docflow.backend.repository.HistoricoEtapaRepository;
import br.com.docflow.backend.repository.TrabalhoRepository;
import br.com.docflow.backend.repository.UsuarioRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import br.com.docflow.backend.dto.EtapaStatusDTO;
import br.com.docflow.backend.entity.HistoricoEtapa;
import br.com.docflow.backend.entity.StatusTrabalho;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

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
    private UsuarioAutenticadoService usuarioAutenticadoService;

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

        Etapa etapa = new Etapa();
        etapa.setId(20L);
        etapa.setTrabalho(trabalho);

        when(etapaRepository.findById(20L))
                .thenReturn(Optional.of(etapa));

        when(usuarioAutenticadoService.obterUsuario())
                .thenReturn(usuarioAutenticado);

        when(trabalhoRepository.findAcessiveisPorUsuario(usuarioAutenticado))
                .thenReturn(List.of());

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

        Etapa etapa = new Etapa();
        etapa.setId(20L);
        etapa.setTrabalho(trabalho);

        when(etapaRepository.findById(20L))
                .thenReturn(Optional.of(etapa));

        when(usuarioAutenticadoService.obterUsuario())
                .thenReturn(usuarioAutenticado);

        when(trabalhoRepository.findAcessiveisPorUsuario(usuarioAutenticado))
                .thenReturn(List.of());

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
        dto.setStatus(StatusTrabalho.EM_ANDAMENTO);
        dto.setUsuarioId(999L); // ID diferente do usuário autenticado

        when(etapaRepository.findById(20L))
                .thenReturn(Optional.of(etapa));

        when(usuarioAutenticadoService.obterUsuario())
                .thenReturn(usuarioAutenticado);

        when(trabalhoRepository.findAcessiveisPorUsuario(usuarioAutenticado))
                .thenReturn(List.of(trabalho));

        when(etapaRepository.save(any(Etapa.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        etapaService.alterarStatus(20L, dto);

        ArgumentCaptor<HistoricoEtapa> captor =
                ArgumentCaptor.forClass(HistoricoEtapa.class);

        verify(historicoEtapaRepository).save(captor.capture());

        HistoricoEtapa historicoSalvo = captor.getValue();

        assertEquals(2L, historicoSalvo.getUsuario().getId());
        assertNotEquals(999L, historicoSalvo.getUsuario().getId());
        assertEquals(StatusTrabalho.PENDENTE, historicoSalvo.getStatusAnterior());
        assertEquals(StatusTrabalho.EM_ANDAMENTO, historicoSalvo.getStatusNovo());
    }

}
