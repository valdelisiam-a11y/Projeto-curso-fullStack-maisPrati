package br.com.docflow.backend.repository;

import br.com.docflow.backend.entity.Trabalho;
import br.com.docflow.backend.entity.Usuario;
import br.com.docflow.backend.entity.UsuarioTrabalho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioTrabalhoRepository extends JpaRepository<UsuarioTrabalho, Long> {

    @Query("SELECT ut FROM UsuarioTrabalho ut WHERE ut.trabalho = :trabalho AND ut.usuario = :usuario")
    Optional<UsuarioTrabalho> findByTrabalhoAndUsuario(@Param("trabalho") Trabalho trabalho, @Param("usuario") Usuario usuario);

    @Query("SELECT ut FROM UsuarioTrabalho ut WHERE ut.trabalho = :trabalho")
    List<UsuarioTrabalho> findByTrabalho(@Param("trabalho") Trabalho trabalho);

    void deleteByTrabalhoAndUsuario(Trabalho trabalho, Usuario usuario);
}