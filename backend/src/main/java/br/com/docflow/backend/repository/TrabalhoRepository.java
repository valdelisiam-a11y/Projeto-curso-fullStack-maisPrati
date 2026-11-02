package br.com.docflow.backend.repository;

import br.com.docflow.backend.entity.Trabalho;
import org.springframework.data.jpa.repository.JpaRepository;
import br.com.docflow.backend.entity.Usuario;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import br.com.docflow.backend.entity.TipoTrabalho;

import java.util.List;

public interface TrabalhoRepository extends JpaRepository<Trabalho, Long> {
    List<Trabalho> findByTituloContainingIgnoreCase(String titulo);

    @Query("""
    SELECT DISTINCT t
    FROM Trabalho t
    LEFT JOIN UsuarioTrabalho ut ON ut.trabalho = t
    WHERE t.criadoPor = :usuario
       OR (t.tipo = :tipoEmpresa AND ut.usuario = :usuario)
    """)
    List<Trabalho> findAcessiveisPorUsuario(
            @Param("usuario") Usuario usuario,
            @Param("tipoEmpresa") TipoTrabalho tipoEmpresa
    );

}
