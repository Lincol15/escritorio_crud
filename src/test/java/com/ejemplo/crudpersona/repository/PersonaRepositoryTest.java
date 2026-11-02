package com.ejemplo.crudpersona.repository;

import com.ejemplo.crudpersona.config.AuditoriaConfig;
import com.ejemplo.crudpersona.model.Persona;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba del repositorio contra SQLite real. Cada prueba se deshace al terminar.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(AuditoriaConfig.class)
class PersonaRepositoryTest {

    @Autowired
    private PersonaRepository repository;

    @BeforeEach
    void preparar() {
        repository.deleteAll();
        repository.save(persona("Ana", "Torres", "11111111"));
        repository.save(persona("Luis", "Quispe", "22222222"));
        repository.save(persona("Maria", "Anaya", "33333333"));
    }

    @Test
    void findByDniEncuentraLaPersona() {
        assertThat(repository.findByDni("22222222"))
                .isPresent()
                .get()
                .extracting(Persona::getNombre)
                .isEqualTo("Luis");
    }

    @Test
    void existsByDni() {
        assertThat(repository.existsByDni("11111111")).isTrue();
        assertThat(repository.existsByDni("99999999")).isFalse();
    }

    @Test
    void buscarIgnoraMayusculasYRevisaNombreApellidoYDni() {
        Page<Persona> porTexto = repository.buscar("ANA", PageRequest.of(0, 10));
        assertThat(porTexto.getContent())
                .extracting(Persona::getDni)
                .containsExactlyInAnyOrder("11111111", "33333333");

        Page<Persona> porDni = repository.buscar("2222", PageRequest.of(0, 10));
        assertThat(porDni.getContent()).extracting(Persona::getNombre).containsExactly("Luis");
    }

    @Test
    void laAuditoriaLlenaFechasYUsuario() {
        Persona guardada = repository.saveAndFlush(persona("Pedro", "Rojas", "44444444"));

        assertThat(guardada.getCreadoEn()).isNotNull();
        assertThat(guardada.getModificadoEn()).isNotNull();
        assertThat(guardada.getCreadoPor()).isEqualTo(System.getProperty("user.name"));
    }

    private Persona persona(String nombre, String apellido, String dni) {
        return Persona.builder().nombre(nombre).apellido(apellido).dni(dni).build();
    }
}
