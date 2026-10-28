
package br.com.docflow.backend.service;

import br.com.docflow.backend.entity.*;
import br.com.docflow.backend.repository.*;

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
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import br.com.docflow.backend.dto.EtapaResponsavelDTO;
import br.com.docflow.backend.repository.UsuarioEmpresaRepository;
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
    private UsuarioEmpresaRepository usuarioEmpresaRepository;

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @Mock
    private UsuarioTrabalhoRepository usuarioTrabalhoRepository;

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
        dto.setStatus(StatusTrabalho.EM_ANDAMENTO);
        dto.setUsuarioId(999L); // ID diferente do usuário autenticado

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
        assertNotEquals(999L, historicoSalvo.getUsuario().getId());
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

}
