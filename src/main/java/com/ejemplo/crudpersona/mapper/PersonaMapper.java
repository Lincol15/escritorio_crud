package com.ejemplo.crudpersona.mapper;

import com.ejemplo.crudpersona.dto.PersonaDto;
import com.ejemplo.crudpersona.model.Persona;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * MapStruct genera la implementacion al compilar (target/generated-sources).
 * Los campos de id y auditoria nunca se copian desde la vista.
 */
@Mapper(componentModel = "spring")
public interface PersonaMapper {

    PersonaDto toDto(Persona persona);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creadoEn", ignore = true)
    @Mapping(target = "creadoPor", ignore = true)
    @Mapping(target = "modificadoEn", ignore = true)
    @Mapping(target = "modificadoPor", ignore = true)
    Persona toEntity(PersonaDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creadoEn", ignore = true)
    @Mapping(target = "creadoPor", ignore = true)
    @Mapping(target = "modificadoEn", ignore = true)
    @Mapping(target = "modificadoPor", ignore = true)
    void actualizar(PersonaDto dto, @MappingTarget Persona persona);
}
