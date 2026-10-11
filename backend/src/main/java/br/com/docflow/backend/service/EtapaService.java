package br.com.docflow.backend.service;

import br.com.docflow.backend.dto.EtapaAtualizacaoDTO;
import br.com.docflow.backend.dto.EtapaCadastroDTO;
import br.com.docflow.backend.dto.EtapaOrdemDTO;
import br.com.docflow.backend.dto.EtapaPercentualDTO;
import br.com.docflow.backend.dto.EtapaRespostaDTO;
import br.com.docflow.backend.dto.EtapaResponsavelDTO;
import br.com.docflow.backend.dto.EtapaStatusDTO;
import br.com.docflow.backend.entity.Etapa;
import br.com.docflow.backend.entity.StatusEtapa;
import br.com.docflow.backend.entity.Trabalho;
import br.com.docflow.backend.entity.Usuario;
import br.com.docflow.backend.repository.EtapaRepository;
import br.com.docflow.backend.repository.TrabalhoRepository;
import br.com.docflow.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class EtapaService {

    private final EtapaRepository etapaRepository;
    private final TrabalhoRepository trabalhoRepository;
    private final UsuarioRepository usuarioRepository;

    public EtapaService(
            EtapaRepository etapaRepository,
            TrabalhoRepository trabalhoRepository,
            UsuarioRepository usuarioRepository) {

        this.etapaRepository = etapaRepository;
        this.trabalhoRepository = trabalhoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // @Transactional: se algo falhar no meio, tudo é desfeito (como DB::transaction no Laravel)
    @Transactional
    public EtapaRespostaDTO cadastrar(Long trabalhoId, EtapaCadastroDTO dto) {

        Trabalho trabalho = trabalhoRepository.findById(trabalhoId)
                .orElseThrow(() -> new RuntimeException("Trabalho não encontrado"));

        Etapa etapa = new Etapa();

        etapa.setTitulo(dto.getTitulo());
        etapa.setDescricao(dto.getDescricao());
        etapa.setTrabalho(trabalho);

        // Responsável é opcional
        if (dto.getResponsavelId() != null) {
            Usuario responsavel = usuarioRepository.findById(dto.getResponsavelId())
                    .orElseThrow(() -> new RuntimeException("Usuário responsável não encontrado"));
            etapa.setResponsavel(responsavel);
        }

        // Entra sempre no final; se uma ordem foi pedida, reposicionamos em seguida
        etapa.setOrdem(etapaRepository.buscarMaiorOrdem(trabalhoId) + 1);

        // status (PENDENTE), percentual (0) e datas são preenchidos pelo @PrePersist da entidade
        Etapa etapaSalva = etapaRepository.save(etapa);

        if (dto.getOrdem() != null) {
            posicionar(etapaSalva, dto.getOrdem());
        }

        return converterParaResposta(etapaSalva);
    }

    public List<EtapaRespostaDTO> listarPorTrabalho(Long trabalhoId) {

        if (!trabalhoRepository.existsById(trabalhoId)) {
            throw new RuntimeException("Trabalho não encontrado");
        }

        return etapaRepository.findByTrabalhoIdOrderByOrdemAsc(trabalhoId)
                .stream()
                .map(this::converterParaResposta)
                .toList();
    }

    public EtapaRespostaDTO buscarPorId(Long id) {

        return converterParaResposta(buscarEntidade(id));
    }

    public EtapaRespostaDTO atualizar(Long id, EtapaAtualizacaoDTO dto) {

        Etapa etapa = buscarEntidade(id);

        etapa.setTitulo(dto.getTitulo());
        etapa.setDescricao(dto.getDescricao());

        // dataAtualizacao é atualizada pelo @PreUpdate da entidade
        Etapa etapaAtualizada = etapaRepository.save(etapa);

        return converterParaResposta(etapaAtualizada);
    }

    public EtapaRespostaDTO alterarStatus(Long id, EtapaStatusDTO dto) {

        Etapa etapa = buscarEntidade(id);

        etapa.setStatus(dto.getStatus());

        // Regra: etapa concluída tem sempre 100% de conclusão
        if (dto.getStatus() == StatusEtapa.CONCLUIDA) {
            etapa.setPercentualConclusao(100);
        }

        Etapa etapaAtualizada = etapaRepository.save(etapa);

        return converterParaResposta(etapaAtualizada);
    }

    public EtapaRespostaDTO atualizarPercentual(Long id, EtapaPercentualDTO dto) {

        Etapa etapa = buscarEntidade(id);

        int percentual = dto.getPercentualConclusao();

        etapa.setPercentualConclusao(percentual);

        if (percentual == 100) {
            // Regra: ao chegar em 100%, a etapa é concluída
            etapa.setStatus(StatusEtapa.CONCLUIDA);
        } else if (etapa.getStatus() == StatusEtapa.CONCLUIDA) {
            // Se estava concluída e o percentual baixou, a etapa é reaberta
            etapa.setStatus(StatusEtapa.EM_ANDAMENTO);
        }

        Etapa etapaAtualizada = etapaRepository.save(etapa);

        return converterParaResposta(etapaAtualizada);
    }

    public EtapaRespostaDTO definirResponsavel(Long id, EtapaResponsavelDTO dto) {

        Etapa etapa = buscarEntidade(id);

        if (dto.getResponsavelId() == null) {
            // Sem ID: remove o responsável
            etapa.setResponsavel(null);
        } else {
            Usuario responsavel = usuarioRepository.findById(dto.getResponsavelId())
                    .orElseThrow(() -> new RuntimeException("Usuário responsável não encontrado"));
            etapa.setResponsavel(responsavel);
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

        etapaRepository.delete(etapa);

        // Fecha o "buraco" deixado na sequência (1, 3, 4 vira 1, 2, 3)
        renumerar(new ArrayList<>(etapaRepository.findByTrabalhoIdOrderByOrdemAsc(trabalhoId)));
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

    // Busca a entidade ou lança erro; evita repetir o mesmo trecho em vários métodos
    private Etapa buscarEntidade(Long id) {

        return etapaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Etapa não encontrada"));
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
}