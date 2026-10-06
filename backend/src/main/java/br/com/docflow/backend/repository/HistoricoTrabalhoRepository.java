package br.com.docflow.backend.repository;

import br.com.docflow.backend.entity.HistoricoTrabalho;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoTrabalhoRepository extends JpaRepository<HistoricoTrabalho, Long> {
    void deleteByTrabalhoId(Long trabalhoId);
    List<HistoricoTrabalho> findByTrabalhoIdOrderByDataAlteracaoDesc(Long trabalhoId);
}