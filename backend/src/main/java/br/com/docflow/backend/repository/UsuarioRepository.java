package br.com.docflow.backend.repository;

import br.com.docflow.backend.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import br.com.docflow.backend.entity.UsuarioEmpresa;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    @Query("""
    SELECT u FROM Usuario u
    WHERE u.ativo = true
    AND NOT EXISTS (
        SELECT ue.id FROM UsuarioEmpresa ue
        WHERE ue.usuario = u
        AND ue.empresa.id = :empresaId
    )
    ORDER BY u.nome
    """)
    List<Usuario> findUsuariosAtivosDisponiveisNaEmpresa(
            @Param("empresaId") Long empresaId
    );

}
