package br.com.docflow.backend.repository;

import br.com.docflow.backend.entity.HistoricoEtapa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoEtapaRepository extends JpaRepository<HistoricoEtapa, Long> {

    List<HistoricoEtapa> findByEtapaIdOrderByDataAlteracaoDesc(Long etapaId);
}
