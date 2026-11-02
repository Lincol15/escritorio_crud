package com.ejemplo.crudpersona.service;

import com.ejemplo.crudpersona.exception.NegocioException;
import com.ejemplo.crudpersona.model.Persona;
import com.ejemplo.crudpersona.repository.PersonaRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PersonaService {

    private static final int TAMANIO_PAGINA = 15;

    private final PersonaRepository repository;

    public Page<Persona> listar(String filtro, int pagina) {
        Pageable pageable = PageRequest.of(pagina, TAMANIO_PAGINA, Sort.by("id").ascending());
        if (filtro == null || filtro.isBlank()) {
            return repository.findAll(pageable);
        }
        return repository.buscar(filtro.trim(), pageable);
    }

    @Transactional
    public Persona guardar(@Valid Persona persona) {
        repository.findByDni(persona.getDni()).ifPresent(existente -> {
            boolean esOtraPersona = persona.getId() == null
                                 || !existente.getId().equals(persona.getId());
            if (esOtraPersona) {
                throw new NegocioException(
                        "Ya existe una persona registrada con el DNI " + persona.getDni());
            }
        });
        Persona guardada = repository.save(persona);
        log.info("Persona guardada: id={} dni={}", guardada.getId(), guardada.getDni());
        return guardada;
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
}
