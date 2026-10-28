package br.com.docflow.backend.service;

import br.com.docflow.backend.dto.EtapaAtualizacaoDTO;
import br.com.docflow.backend.dto.EtapaCadastroDTO;
import br.com.docflow.backend.dto.EtapaOrdemDTO;
import br.com.docflow.backend.dto.EtapaPercentualDTO;
import br.com.docflow.backend.dto.EtapaRespostaDTO;
import br.com.docflow.backend.dto.EtapaResponsavelDTO;
import br.com.docflow.backend.dto.EtapaStatusDTO;
import br.com.docflow.backend.dto.HistoricoEtapaRespostaDTO;
import br.com.docflow.backend.entity.Etapa;
import br.com.docflow.backend.entity.HistoricoEtapa;
import br.com.docflow.backend.entity.StatusTrabalho;
import br.com.docflow.backend.entity.Trabalho;
import br.com.docflow.backend.entity.Usuario;
import br.com.docflow.backend.repository.EtapaRepository;
import br.com.docflow.backend.repository.HistoricoEtapaRepository;
import br.com.docflow.backend.repository.TrabalhoRepository;
import br.com.docflow.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.docflow.backend.repository.UsuarioEmpresaRepository;
import br.com.docflow.backend.entity.TipoTrabalho;
import br.com.docflow.backend.repository.UsuarioTrabalhoRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class EtapaService {

    private final EtapaRepository etapaRepository;
    private final HistoricoEtapaRepository historicoEtapaRepository;
    private final TrabalhoRepository trabalhoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final UsuarioEmpresaRepository usuarioEmpresaRepository;
    private final UsuarioTrabalhoRepository usuarioTrabalhoRepository;

    public EtapaService(
            EtapaRepository etapaRepository,
            HistoricoEtapaRepository historicoEtapaRepository,
            TrabalhoRepository trabalhoRepository,
            UsuarioRepository usuarioRepository,
            UsuarioAutenticadoService usuarioAutenticadoService,
            UsuarioEmpresaRepository usuarioEmpresaRepository,
            UsuarioTrabalhoRepository usuarioTrabalhoRepository) {

        this.etapaRepository = etapaRepository;
        this.historicoEtapaRepository = historicoEtapaRepository;
        this.trabalhoRepository = trabalhoRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
        this.usuarioEmpresaRepository = usuarioEmpresaRepository;
        this.usuarioTrabalhoRepository = usuarioTrabalhoRepository;
    }

    @Transactional
    public EtapaRespostaDTO cadastrar(Long trabalhoId, EtapaCadastroDTO dto) {

        Trabalho trabalho = validarAcessoTrabalho(trabalhoId);

        Etapa etapa = new Etapa();

        etapa.setTitulo(dto.getTitulo());
        etapa.setDescricao(dto.getDescricao());
        etapa.setStatus(StatusTrabalho.PENDENTE);
        etapa.setPercentualConclusao(0);
        etapa.setTrabalho(trabalho);

        // Responsável é opcional, mas precisa ser da mesma empresa do trabalho
        if (dto.getResponsavelId() != null) {
            etapa.setResponsavel(buscarResponsavel(dto.getResponsavelId(), trabalho));
        }

        // Entra sempre no final; se uma ordem foi pedida, reposicionamos em seguida
        etapa.setOrdem(etapaRepository.buscarMaiorOrdem(trabalhoId) + 1);

        Etapa etapaSalva = etapaRepository.save(etapa);

        if (dto.getOrdem() != null) {
            posicionar(etapaSalva, dto.getOrdem());
        }

        return converterParaResposta(etapaSalva);
    }

    @Transactional(readOnly = true)
    public List<EtapaRespostaDTO> listarPorTrabalho(Long trabalhoId) {

        validarAcessoTrabalho(trabalhoId);

        return etapaRepository.findByTrabalhoIdOrderByOrdemAsc(trabalhoId)
                .stream()
                .map(this::converterParaResposta)
                .toList();
    }

    @Transactional(readOnly = true)
    public EtapaRespostaDTO buscarPorId(Long id) {

        return converterParaResposta(buscarEntidade(id));
    }

    @Transactional
    public EtapaRespostaDTO atualizar(Long id, EtapaAtualizacaoDTO dto) {

        Etapa etapa = buscarEntidade(id);

        etapa.setTitulo(dto.getTitulo());
        etapa.setDescricao(dto.getDescricao());

        // dataAtualizacao é atualizada pelo @PreUpdate da entidade
        Etapa etapaAtualizada = etapaRepository.save(etapa);

        return converterParaResposta(etapaAtualizada);
    }

    @Transactional
    public EtapaRespostaDTO alterarStatus(Long id, EtapaStatusDTO dto) {

        Etapa etapa = buscarEntidade(id);

        Usuario usuario = usuarioAutenticadoService.obterUsuario();

        StatusTrabalho statusAnterior = etapa.getStatus();

        if (statusAnterior == dto.getStatus()) {
            throw new EtapaStatusInalteradoException("A etapa já está com este status");
        }

        etapa.setStatus(dto.getStatus());

        // Regra: etapa concluída tem sempre 100% de conclusão
        if (dto.getStatus() == StatusTrabalho.CONCLUIDO) {
            etapa.setPercentualConclusao(100);
        }

        Etapa etapaAtualizada = etapaRepository.save(etapa);

        registrarHistorico(etapaAtualizada, statusAnterior, dto.getStatus(), usuario);

        return converterParaResposta(etapaAtualizada);
    }

    @Transactional
    public EtapaRespostaDTO atualizarPercentual(Long id, EtapaPercentualDTO dto) {

        Etapa etapa = buscarEntidade(id);

        Usuario usuario = usuarioAutenticadoService.obterUsuario();

        StatusTrabalho statusAnterior = etapa.getStatus();
        int percentual = dto.getPercentualConclusao();

        etapa.setPercentualConclusao(percentual);

        if (percentual == 100) {
            // Regra: ao chegar em 100%, a etapa é concluída
            etapa.setStatus(StatusTrabalho.CONCLUIDO);
        } else if (statusAnterior == StatusTrabalho.CONCLUIDO) {
            // Se estava concluída e o percentual baixou, a etapa é reaberta
            etapa.setStatus(StatusTrabalho.EM_ANDAMENTO);
        }

        Etapa etapaAtualizada = etapaRepository.save(etapa);

        // Só registra histórico se o percentual mudou o status
        if (etapaAtualizada.getStatus() != statusAnterior) {
            registrarHistorico(etapaAtualizada, statusAnterior, etapaAtualizada.getStatus(), usuario);
        }

        return converterParaResposta(etapaAtualizada);
    }

    @Transactional
    public EtapaRespostaDTO definirResponsavel(Long id, EtapaResponsavelDTO dto) {

        Etapa etapa = buscarEntidade(id);

        if (dto.getResponsavelId() == null) {
            // Sem ID: remove o responsável
            etapa.setResponsavel(null);
        } else {
            etapa.setResponsavel(buscarResponsavel(dto.getResponsavelId(), etapa.getTrabalho()));
        }

        Etapa etapaAtualizada = etapaRepository.save(etapa);

        return converterParaResposta(etapaAtualizada);
    }

    @Transactional
    public EtapaRespostaDTO alterarOrdem(Long id, EtapaOrdemDTO dto) {

        Etapa etapa = buscarEntidade(id);

        posicionar(etapa, dto.getOrdem());

        return converterParaResposta(etapa);
    }

    @Transactional
    public void excluir(Long id) {

        Etapa etapa = buscarEntidade(id);
        Long trabalhoId = etapa.getTrabalho().getId();

        // O histórico da etapa é excluído pelo banco (ON DELETE CASCADE)
        etapaRepository.delete(etapa);

        // Fecha o "buraco" deixado na sequência (1, 3, 4 vira 1, 2, 3)
        renumerar(new ArrayList<>(etapaRepository.findByTrabalhoIdOrderByOrdemAsc(trabalhoId)));
    }

    @Transactional(readOnly = true)
    public List<HistoricoEtapaRespostaDTO> listarHistorico(Long etapaId) {

        Etapa etapa = buscarEntidade(etapaId);

        return historicoEtapaRepository
                .findByEtapaIdOrderByDataAlteracaoDesc(etapa.getId())
                .stream()
                .map(this::converterHistoricoParaResposta)
                .toList();
    }

    // Move a etapa para a nova posição; as demais descem ou sobem uma posição (comportamento de lista).
    // Se a ordem pedida for maior que o total de etapas, a etapa vai para o final.
    private void posicionar(Etapa etapa, int novaOrdem) {

        Long trabalhoId = etapa.getTrabalho().getId();

        List<Etapa> etapas = new ArrayList<>(etapaRepository.findByTrabalhoIdOrderByOrdemAsc(trabalhoId));

        etapas.removeIf(e -> e.getId().equals(etapa.getId()));

        int total = etapas.size() + 1;
        int indice = Math.min(novaOrdem, total) - 1;

        etapas.add(indice, etapa);

        renumerar(etapas);
    }

    // Reescreve a ordem como 1, 2, 3... conforme a posição na lista
    private void renumerar(List<Etapa> etapas) {

        for (int i = 0; i < etapas.size(); i++) {
            etapas.get(i).setOrdem(i + 1);
        }

        etapaRepository.saveAll(etapas);
    }

    private void registrarHistorico(Etapa etapa, StatusTrabalho statusAnterior,
                                    StatusTrabalho statusNovo, Usuario usuario) {

        HistoricoEtapa historico = new HistoricoEtapa();

        historico.setStatusAnterior(statusAnterior);
        historico.setStatusNovo(statusNovo);
        historico.setDataAlteracao(LocalDateTime.now());
        historico.setEtapa(etapa);
        historico.setUsuario(usuario);

        historicoEtapaRepository.save(historico);
    }

    // Busca o usuário responsável e garante que ele é da mesma empresa do trabalho
    private Usuario buscarResponsavel(Long usuarioId, Trabalho trabalho) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário responsável não encontrado"));

        if (!mesmaEmpresa(usuario, trabalho)) {
            throw new UsuarioEmpresaDiferenteException(
                    "O responsável deve pertencer à mesma empresa do trabalho");
        }

        return usuario;
    }

    private boolean mesmaEmpresa(Usuario usuario, Trabalho trabalho) {
        return trabalho.getEmpresa() != null
                && usuarioEmpresaRepository.existsByUsuarioAndEmpresa(
                usuario, trabalho.getEmpresa());
    }

    private Trabalho validarAcessoTrabalho(Long trabalhoId) {

        Usuario usuario = usuarioAutenticadoService.obterUsuario();

        Trabalho trabalho = trabalhoRepository.findById(trabalhoId)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Trabalho não encontrado"));

        boolean criador = trabalho.getCriadoPor().getId()
                .equals(usuario.getId());

        boolean participante = trabalho.getTipo() == TipoTrabalho.EMPRESA
                && usuarioTrabalhoRepository
                .findByTrabalhoAndUsuario(trabalho, usuario)
                .isPresent();

        if (!criador && !participante) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Você não tem permissão para acessar este trabalho.");
        }

        return trabalho;
    }

    // verifica se o usuário é o criador ou tem acesso ao trabalho por meio das associações existentes
    private Etapa buscarEntidade(Long id) {

        Etapa etapa = etapaRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Etapa não encontrada"));

        Trabalho trabalho = etapa.getTrabalho();
        Usuario usuario = usuarioAutenticadoService.obterUsuario();

        boolean criador = trabalho.getCriadoPor().getId()
                .equals(usuario.getId());

        boolean participante = trabalho.getTipo() == TipoTrabalho.EMPRESA
                && usuarioTrabalhoRepository
                .findByTrabalhoAndUsuario(trabalho, usuario)
                .isPresent();

        if (!criador && !participante) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Você não tem permissão para acessar esta etapa.");
        }

        return etapa;
    }

    private EtapaRespostaDTO converterParaResposta(Etapa etapa) {

        EtapaRespostaDTO resposta = new EtapaRespostaDTO();

        resposta.setId(etapa.getId());
        resposta.setTitulo(etapa.getTitulo());
        resposta.setDescricao(etapa.getDescricao());
        resposta.setStatus(etapa.getStatus());
        resposta.setPercentualConclusao(etapa.getPercentualConclusao());
        resposta.setOrdem(etapa.getOrdem());
        resposta.setDataCriacao(etapa.getDataCriacao());
        resposta.setDataAtualizacao(etapa.getDataAtualizacao());
        resposta.setTrabalhoId(etapa.getTrabalho().getId());

        if (etapa.getResponsavel() != null) {
            resposta.setResponsavelId(etapa.getResponsavel().getId());
        }

        return resposta;
    }

    private HistoricoEtapaRespostaDTO converterHistoricoParaResposta(HistoricoEtapa historico) {

        HistoricoEtapaRespostaDTO resposta = new HistoricoEtapaRespostaDTO();

        resposta.setId(historico.getId());
        resposta.setStatusAnterior(historico.getStatusAnterior());
        resposta.setStatusNovo(historico.getStatusNovo());
        resposta.setDataAlteracao(historico.getDataAlteracao());
        resposta.setUsuarioId(historico.getUsuario().getId());
        resposta.setUsuarioNome(historico.getUsuario().getNome());

        return resposta;
    }
}
