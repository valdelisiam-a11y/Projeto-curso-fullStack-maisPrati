package br.com.docflow.backend.repository;

import br.com.docflow.backend.entity.HistoricoTrabalho;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoricoTrabalhoRepository extends JpaRepository<HistoricoTrabalho, Long> {
}