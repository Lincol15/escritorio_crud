package com.ejemplo.crudpersona.service;

import com.ejemplo.crudpersona.dto.PersonaDto;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Prueba con todo Spring levantado: comprueba que @Validated / @Valid
 * frenan los datos invalidos antes de llegar a la base de datos.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PersonaServiceIntegracionTest {

    @Autowired
    private PersonaService service;

    @Test
    void dniDeCincoDigitosEsRechazado() {
        PersonaDto dto = PersonaDto.builder().nombre("Ana").apellido("Torres").dni("12345")
                .email("").telefono("").build();

        assertThatThrownBy(() -> service.guardar(dto))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("8 digitos");
    }

    @Test
    void nombreVacioEsRechazado() {
        PersonaDto dto = PersonaDto.builder().nombre("").apellido("Torres").dni("12345678")
                .email("").telefono("").build();

        assertThatThrownBy(() -> service.guardar(dto))
                .isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("obligatorio");
    }

    @Test
    void guardarYListarConPaginacion() {
        for (int i = 0; i < 20; i++) {
            service.guardar(PersonaDto.builder().nombre("Persona" + i).apellido("Prueba")
                    .dni(String.format("%08d", 50000000 + i)).email("").telefono("").build());
        }

        assertThat(service.listar("Prueba", 0).getContent()).hasSize(15);
        assertThat(service.listar("Prueba", 1).getContent()).hasSize(5);
        assertThat(service.listarTodos("Prueba")).hasSize(20);
    }
}
