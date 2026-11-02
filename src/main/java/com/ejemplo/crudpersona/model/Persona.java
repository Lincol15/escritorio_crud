package com.ejemplo.crudpersona.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "persona",
       uniqueConstraints = @UniqueConstraint(name = "uk_persona_dni", columnNames = "dni"))
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(of = "id")
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 60, message = "El nombre no puede superar 60 caracteres")
    @Column(nullable = false, length = 60)
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String apellido;

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "\\d{8}", message = "El DNI debe tener exactamente 8 digitos")
    @Column(nullable = false, length = 8)
    private String dni;

    @Email(message = "El correo no tiene un formato valido")
    @Size(max = 120)
    @Column(length = 120)
    private String email;

    @Pattern(regexp = "|\\d{9}", message = "El telefono debe tener 9 digitos")
    @Column(length = 9)
    private String telefono;

    // Auditoria: Spring Data llena estos campos al insertar y al actualizar
    @CreatedDate
    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @CreatedBy
    @Column(name = "creado_por", length = 60, updatable = false)
    private String creadoPor;

    @LastModifiedDate
    @Column(name = "modificado_en")
    private LocalDateTime modificadoEn;

    @LastModifiedBy
    @Column(name = "modificado_por", length = 60)
    private String modificadoPor;

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
}
