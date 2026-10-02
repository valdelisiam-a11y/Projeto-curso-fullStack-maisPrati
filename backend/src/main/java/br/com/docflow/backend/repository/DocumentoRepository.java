package br.com.docflow.backend.repository;

import br.com.docflow.backend.entity.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Long> {
    List<Documento> findByEtapaId(Long etapaId);
}
