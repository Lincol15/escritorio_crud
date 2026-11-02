package com.ejemplo.crudpersona.service;

import com.ejemplo.crudpersona.dto.PersonaDto;
import com.ejemplo.crudpersona.exception.NegocioException;
import com.ejemplo.crudpersona.mapper.PersonaMapper;
import com.ejemplo.crudpersona.model.Persona;
import com.ejemplo.crudpersona.repository.PersonaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Prueba unitaria del servicio: el repositorio es un simulacro (mock) de Mockito,
 * asi se prueban solo las reglas de negocio, sin base de datos.
 */
@ExtendWith(MockitoExtension.class)
class PersonaServiceTest {

    @Mock
    private PersonaRepository repository;

    private PersonaService service;

    @BeforeEach
    void preparar() {
        service = new PersonaService(repository, Mappers.getMapper(PersonaMapper.class));
    }

    @Test
    void guardarNuevoInsertaYDevuelveElId() {
        when(repository.findByDni("12345678")).thenReturn(Optional.empty());
        when(repository.saveAndFlush(any(Persona.class))).thenAnswer(inv -> {
            Persona p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        PersonaDto guardada = service.guardar(dto(null, "12345678"));

        assertThat(guardada.getId()).isEqualTo(1L);
        assertThat(guardada.getNombre()).isEqualTo("Ana");
    }

    @Test
    void guardarConDniDeOtraPersonaLanzaError() {
        when(repository.findByDni("12345678")).thenReturn(Optional.of(entidad(5L, "12345678")));

        assertThatThrownBy(() -> service.guardar(dto(null, "12345678")))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("12345678");
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void actualizarConservaLaAuditoriaYCambiaLosDatos() {
        Persona existente = entidad(5L, "12345678");
        existente.setCreadoPor("admin");
        when(repository.findByDni("12345678")).thenReturn(Optional.of(existente));
        when(repository.findById(5L)).thenReturn(Optional.of(existente));
        when(repository.saveAndFlush(any(Persona.class))).thenAnswer(inv -> inv.getArgument(0));

        PersonaDto cambios = dto(5L, "12345678");
        cambios.setNombre("Ana Maria");
        service.guardar(cambios);

        ArgumentCaptor<Persona> captor = ArgumentCaptor.forClass(Persona.class);
        verify(repository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getNombre()).isEqualTo("Ana Maria");
        assertThat(captor.getValue().getCreadoPor()).isEqualTo("admin");
    }

    @Test
    void eliminarInexistenteLanzaError() {
        when(repository.existsById(9L)).thenReturn(false);

        assertThatThrownBy(() -> service.eliminar(9L)).isInstanceOf(NegocioException.class);
        verify(repository, never()).deleteById(any());
    }

    @Test
    void eliminarExistenteBorra() {
        when(repository.existsById(9L)).thenReturn(true);

        service.eliminar(9L);

        verify(repository).deleteById(9L);
    }

    private PersonaDto dto(Long id, String dni) {
        return PersonaDto.builder().id(id).nombre("Ana").apellido("Torres").dni(dni)
                .email("").telefono("").build();
    }

    private Persona entidad(Long id, String dni) {
        return Persona.builder().id(id).nombre("Ana").apellido("Torres").dni(dni).build();
    }
}
