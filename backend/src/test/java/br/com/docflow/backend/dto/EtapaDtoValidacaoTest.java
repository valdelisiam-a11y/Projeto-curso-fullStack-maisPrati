package br.com.docflow.backend.dto;

import br.com.docflow.backend.entity.StatusEtapa;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Valida as anotações de Bean Validation dos DTOs de Etapa, sem subir o Spring
 * (e sem tocar no banco de dados).
 */
class EtapaDtoValidacaoTest {

    private static ValidatorFactory fabrica;
    private static Validator validador;

    @BeforeAll
    static void iniciar() {
        fabrica = Validation.buildDefaultValidatorFactory();
        validador = fabrica.getValidator();
    }

    @AfterAll
    static void encerrar() {
        fabrica.close();
    }

    // ---- EtapaPercentualDTO ----

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 50, 99, 100})
    void percentual_valoresDe0a100_saoValidos(int valor) {
        EtapaPercentualDTO dto = new EtapaPercentualDTO();
        dto.setPercentualConclusao(valor);

        assertThat(validador.validate(dto)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 101, 1000})
    void percentual_foraDoIntervalo_ehInvalido(int valor) {
        EtapaPercentualDTO dto = new EtapaPercentualDTO();
        dto.setPercentualConclusao(valor);

        assertThat(validador.validate(dto)).hasSize(1);
    }

    @Test
    void percentual_nulo_ehInvalido() {
        assertThat(validador.validate(new EtapaPercentualDTO())).hasSize(1);
    }

    @Test
    void percentual_usuarioIdAusente_naoEhMaisObrigatorio() {
        EtapaPercentualDTO dto = new EtapaPercentualDTO();
        dto.setPercentualConclusao(10);
        dto.setUsuarioId(null);

        assertThat(validador.validate(dto)).isEmpty();
    }

    // ---- EtapaStatusDTO ----

    @Test
    void status_semStatus_ehInvalido() {
        assertThat(validador.validate(new EtapaStatusDTO())).hasSize(1);
    }

    @Test
    void status_semPercentualEsemUsuarioId_ehValido() {
        EtapaStatusDTO dto = new EtapaStatusDTO();
        dto.setStatus(StatusEtapa.IMPEDIDO);

        assertThat(validador.validate(dto)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 60, 99})
    void status_percentualDeRetornoAte99_ehValido(int valor) {
        EtapaStatusDTO dto = new EtapaStatusDTO();
        dto.setStatus(StatusEtapa.EM_ANDAMENTO);
        dto.setPercentualConclusao(valor);

        assertThat(validador.validate(dto)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 100, 150})
    void status_percentualDeRetornoForaDe0a99_ehInvalido(int valor) {
        EtapaStatusDTO dto = new EtapaStatusDTO();
        dto.setStatus(StatusEtapa.EM_ANDAMENTO);
        dto.setPercentualConclusao(valor);

        assertThat(validador.validate(dto)).hasSize(1);
    }
}
