package br.com.docflow.backend.repository;
import br.com.docflow.backend.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
}
