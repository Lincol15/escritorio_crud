package com.ejemplo.crudpersona.service;

import com.ejemplo.crudpersona.dto.PersonaDto;
import com.ejemplo.crudpersona.exception.NegocioException;
import com.ejemplo.crudpersona.mapper.PersonaMapper;
import com.ejemplo.crudpersona.model.Persona;
import com.ejemplo.crudpersona.repository.PersonaRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PersonaService {

    private static final int TAMANIO_PAGINA = 15;
    private static final Sort ORDEN = Sort.by("id").ascending();

    private final PersonaRepository repository;
    private final PersonaMapper mapper;

    public Page<PersonaDto> listar(String filtro, int pagina) {
        return consultar(filtro, PageRequest.of(pagina, TAMANIO_PAGINA, ORDEN)).map(mapper::toDto);
    }

    /** Todos los registros que cumplen el filtro, sin paginar (para exportar). */
    public List<PersonaDto> listarTodos(String filtro) {
        return consultar(filtro, Pageable.unpaged(ORDEN)).map(mapper::toDto).getContent();
    }

    @Transactional
    public PersonaDto guardar(@Valid PersonaDto dto) {
        repository.findByDni(dto.getDni()).ifPresent(existente -> {
            boolean esOtraPersona = dto.getId() == null
                                 || !existente.getId().equals(dto.getId());
            if (esOtraPersona) {
                throw new NegocioException(
                        "Ya existe una persona registrada con el DNI " + dto.getDni());
            }
        });

        Persona persona;
        if (dto.getId() == null) {
            persona = mapper.toEntity(dto);
        } else {
            // Se parte del registro guardado para no perder los datos de auditoria
            persona = repository.findById(dto.getId())
                    .orElseThrow(() -> new NegocioException("La persona ya no existe en la base de datos."));
            mapper.actualizar(dto, persona);
        }

        Persona guardada = repository.saveAndFlush(persona);
        log.info("Persona guardada: id={} dni={}", guardada.getId(), guardada.getDni());
        return mapper.toDto(guardada);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new NegocioException("La persona ya no existe en la base de datos.");
        }
        repository.deleteById(id);
    }

    public long contar() {
        return repository.count();
    }

    private Page<Persona> consultar(String filtro, Pageable pageable) {
        if (filtro == null || filtro.isBlank()) {
            return repository.findAll(pageable);
        }
        return repository.buscar(filtro.trim(), pageable);
    }
}
