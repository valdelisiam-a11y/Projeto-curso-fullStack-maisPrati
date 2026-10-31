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
import br.com.docflow.backend.entity.StatusEtapa;
import br.com.docflow.backend.entity.Trabalho;
import br.com.docflow.backend.entity.Usuario;
import br.com.docflow.backend.repository.EtapaRepository;
import br.com.docflow.backend.repository.HistoricoEtapaRepository;
import br.com.docflow.backend.repository.TrabalhoRepository;
import br.com.docflow.backend.repository.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public EtapaService(
            EtapaRepository etapaRepository,
            HistoricoEtapaRepository historicoEtapaRepository,
            TrabalhoRepository trabalhoRepository,
            UsuarioRepository usuarioRepository,
            UsuarioAutenticadoService usuarioAutenticadoService) {

        this.etapaRepository = etapaRepository;
        this.historicoEtapaRepository = historicoEtapaRepository;
        this.trabalhoRepository = trabalhoRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
    }

    @Transactional
    public EtapaRespostaDTO cadastrar(Long trabalhoId, EtapaCadastroDTO dto) {

        Trabalho trabalho = validarAcessoTrabalho(trabalhoId);

        Etapa etapa = new Etapa();

        etapa.setTitulo(dto.getTitulo());
        etapa.setDescricao(dto.getDescricao());
        etapa.setStatus(StatusEtapa.PENDENTE);
        etapa.setPercentualConclusao(0);
        etapa.setTrabalho(trabalho);

        // Responsável é opcional, mas precisa ser o criador ou um participante do trabalho
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

        // O usuário do histórico vem sempre do JWT; o usuarioId do corpo é ignorado
        Usuario usuario = usuarioAutenticadoService.obterUsuario();

        StatusEtapa statusAnterior = etapa.getStatus();
        StatusEtapa statusNovo = dto.getStatus();

        if (statusAnterior == statusNovo) {
            throw new EtapaStatusInalteradoException("A etapa já está com este status");
        }

        if (statusNovo == StatusEtapa.CONCLUIDO) {
            // Regra: etapa concluída tem sempre 100% de conclusão
            etapa.setPercentualConclusao(100);
        } else if (statusAnterior == StatusEtapa.CONCLUIDO) {
            // Regra: etapa que deixa de estar concluída não pode permanecer com 100%.
            // O usuário informa o percentual ao qual ela retorna.
            etapa.setPercentualConclusao(
                    validarPercentualDeRetorno(statusNovo, dto.getPercentualConclusao()));
        }

        etapa.setStatus(statusNovo);

        Etapa etapaAtualizada = etapaRepository.save(etapa);

        registrarHistorico(etapaAtualizada, statusAnterior, statusNovo, usuario);

        return converterParaResposta(etapaAtualizada);
    }

    @Transactional
    public EtapaRespostaDTO atualizarPercentual(Long id, EtapaPercentualDTO dto) {

        Etapa etapa = buscarEntidade(id);

        // O usuário do histórico vem sempre do JWT; o usuarioId do corpo é ignorado
        Usuario usuario = usuarioAutenticadoService.obterUsuario();

        StatusEtapa statusAnterior = etapa.getStatus();
        int percentual = dto.getPercentualConclusao();

        etapa.setPercentualConclusao(percentual);

        if (percentual == 100) {
            // Regra: ao atingir 100%, a etapa é concluída
            etapa.setStatus(StatusEtapa.CONCLUIDO);
        } else if (statusAnterior == StatusEtapa.CONCLUIDO) {
            // Regra: se o percentual for reduzido, a etapa concluída volta para EM_ANDAMENTO
            etapa.setStatus(StatusEtapa.EM_ANDAMENTO);
        } else if (statusAnterior == StatusEtapa.PENDENTE && percentual > 0) {
            // Regra: etapa pendente que passa a ter progresso entra em andamento
            etapa.setStatus(StatusEtapa.EM_ANDAMENTO);
        }
        // IMPEDIDO e EM_ANDAMENTO abaixo de 100% mantêm o status (sem restrições adicionais)

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

    private void registrarHistorico(Etapa etapa, StatusEtapa statusAnterior,
                                    StatusEtapa statusNovo, Usuario usuario) {

        HistoricoEtapa historico = new HistoricoEtapa();

        historico.setStatusAnterior(statusAnterior);
        historico.setStatusNovo(statusNovo);
        historico.setDataAlteracao(LocalDateTime.now());
        historico.setEtapa(etapa);
        historico.setUsuario(usuario);

        historicoEtapaRepository.save(historico);
    }

    // Percentual informado ao reabrir uma etapa concluída: obrigatório, de 0 a 99,
    // e PENDENTE só aceita 0% (percentual inicial da etapa)
    private int validarPercentualDeRetorno(StatusEtapa statusNovo, Integer percentual) {

        if (percentual == null) {
            throw new EtapaPercentualInvalidoException(
                    "Informe o percentual de conclusão ao reabrir uma etapa concluída");
        }

        if (percentual < 0 || percentual > 99) {
            throw new EtapaPercentualInvalidoException(
                    "O percentual de retorno deve estar entre 0 e 99");
        }

        if (statusNovo == StatusEtapa.PENDENTE && percentual > 0) {
            throw new EtapaPercentualInvalidoException(
                    "Uma etapa pendente deve ter 0% de conclusão");
        }

        return percentual;
    }

    // Busca o usuário responsável e garante que ele é o criador ou um participante do trabalho
    // (o mesmo critério que dá acesso à etapa)
    private Usuario buscarResponsavel(Long usuarioId, Trabalho trabalho) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário responsável não encontrado"));

        if (!possuiAcesso(usuario, trabalho)) {
            throw new ResponsavelSemAcessoException(
                    "O responsável deve ser o criador ou um participante do trabalho");
        }

        return usuario;
    }

    // Acesso ao trabalho: somente o criador ou um participante autorizado
    private boolean possuiAcesso(Usuario usuario, Trabalho trabalho) {

        if (trabalho.getCriadoPor().getId().equals(usuario.getId())) {
            return true;
        }

        return trabalhoRepository
                .findAcessiveisPorUsuario(usuario)
                .stream()
                .anyMatch(t -> t.getId().equals(trabalho.getId()));
    }

    private void exigirAcesso(Trabalho trabalho, String mensagem) {

        Usuario usuario = usuarioAutenticadoService.obterUsuario();

        if (!possuiAcesso(usuario, trabalho)) {
            throw new AccessDeniedException(mensagem);
        }
    }

    private Trabalho validarAcessoTrabalho(Long trabalhoId) {

        Trabalho trabalho = trabalhoRepository.findById(trabalhoId)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Trabalho não encontrado"));

        exigirAcesso(trabalho, "Você não tem permissão para acessar este trabalho.");

        return trabalho;
    }

    // Busca a etapa e verifica se o usuário autenticado tem acesso ao trabalho dela
    private Etapa buscarEntidade(Long id) {

        Etapa etapa = etapaRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Etapa não encontrada"));

        exigirAcesso(etapa.getTrabalho(), "Você não tem permissão para acessar esta etapa.");

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
