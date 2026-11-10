
package br.com.docflow.backend.repository;

import br.com.docflow.backend.entity.Empresa;
import br.com.docflow.backend.entity.Usuario;
import br.com.docflow.backend.entity.UsuarioEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioEmpresaRepository
        extends JpaRepository<UsuarioEmpresa, Long> {

    Optional<UsuarioEmpresa> findByUsuarioAndEmpresa(
            Usuario usuario, Empresa empresa);

    List<UsuarioEmpresa> findByUsuario(Usuario usuario);

    List<UsuarioEmpresa> findByEmpresa(Empresa empresa);

    long countByEmpresa(Empresa empresa);

    boolean existsByUsuarioAndEmpresa(
            Usuario usuario, Empresa empresa);
}
