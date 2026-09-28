package br.com.docflow.backend.repository;

import br.com.docflow.backend.entity.UsuarioTrabalho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioTrabalhoRepository extends JpaRepository<UsuarioTrabalho, Long> {
}