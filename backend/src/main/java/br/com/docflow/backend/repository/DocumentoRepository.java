package br.com.docflow.backend.repository;

import br.com.docflow.backend.entity.Documento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    List<Documento> findByEtapaIdOrderByDataUploadDesc(Long etapaId);

    Optional<Documento> findByIdAndEtapaId(Long id, Long etapaId);

}