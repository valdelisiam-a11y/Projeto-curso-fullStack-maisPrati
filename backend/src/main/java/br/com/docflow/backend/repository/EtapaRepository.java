package br.com.docflow.backend.repository;

import br.com.docflow.backend.entity.Etapa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EtapaRepository extends JpaRepository<Etapa, Long> {

    // O Spring monta a consulta só pelo nome do método:
    // SELECT ... FROM etapa WHERE trabalho_id = ? ORDER BY ordem ASC
    List<Etapa> findByTrabalhoIdOrderByOrdemAsc(Long trabalhoId);

    // Maior ordem já usada no trabalho (0 se ainda não houver etapas)
    @Query("SELECT COALESCE(MAX(e.ordem), 0) FROM Etapa e WHERE e.trabalho.id = :trabalhoId")
    int buscarMaiorOrdem(@Param("trabalhoId") Long trabalhoId);
}