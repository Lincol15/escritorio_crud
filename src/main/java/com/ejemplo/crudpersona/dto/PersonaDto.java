package com.ejemplo.crudpersona.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Objeto de transferencia: es lo que viaja entre la vista y el servicio,
 * de modo que la entidad JPA nunca sale de la capa de servicio.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PersonaDto {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 60, message = "El nombre no puede superar 60 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 60, message = "El apellido no puede superar 60 caracteres")
    private String apellido;

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "\\d{8}", message = "El DNI debe tener exactamente 8 digitos")
    private String dni;

    @Email(message = "El correo no tiene un formato valido")
    @Size(max = 120, message = "El correo no puede superar 120 caracteres")
    private String email;

    @Pattern(regexp = "|\\d{9}", message = "El telefono debe tener 9 digitos")
    private String telefono;

    // Solo lectura: los llena la auditoria
    private LocalDateTime creadoEn;
    private String creadoPor;
    private LocalDateTime modificadoEn;
    private String modificadoPor;

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
}
