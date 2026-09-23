package br.com.docflow.backend.repository;

import br.com.docflow.backend.entity.Trabalho;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrabalhoRepository extends JpaRepository<Trabalho, Long> {
    List<Trabalho> findByTituloContainingIgnoreCase(String titulo);

}
