package br.com.docflow.backend.repository;

import br.com.docflow.backend.entity.EventoTrabalho;
import org.springframework.data.jpa.repository.JpaRepository;
import br.com.docflow.backend.entity.TipoEventoTrabalho;
import java.util.Optional;

import java.util.List;

public interface EventoTrabalhoRepository extends JpaRepository<EventoTrabalho, Long> {

    List<EventoTrabalho> findByTrabalhoIdOrderByDataEventoDesc(Long trabalhoId);

    Optional<EventoTrabalho> findFirstByEtapaIdAndTipoOrderByDataEventoDesc(
            Long etapaId,
            TipoEventoTrabalho tipo
    );
}

