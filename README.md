# Sistema CRUD de Personas

Aplicacion de escritorio para registrar personas (crear, leer, actualizar y eliminar)
con **JavaFX + Spring Boot + JPA + Lombok + SQLite**.

## Tecnologias

| Tecnologia | Version | Uso |
|---|---|---|
| Java (JDK) | 17 | Lenguaje |
| Spring Boot | 3.3.4 | Inyeccion de dependencias y arranque |
| Spring Data JPA / Hibernate | incluida | Acceso a datos sin SQL manual |
| Lombok | incluida | Getters, setters, constructores |
| MapStruct | 1.6.2 | Conversion entidad <-> DTO |
| Flyway | incluida | Migraciones versionadas |
| JavaFX | 21.0.4 | Interfaz grafica |
| AtlantaFX | 2.0.1 | Tema visual |
| SQLite | 3.45.3.0 | Base de datos (por defecto) |
| PostgreSQL | driver incluido | Base de datos alternativa (perfil `postgres`) |
| Apache POI / OpenPDF | 5.3.0 / 1.3.43 | Exportar a Excel y PDF |

## Arquitectura

```
persona-view.fxml   VISTA        estructura visual y estilos
PersonaController   CONTROLADOR  recibe eventos, consultas en segundo plano (Task)
PersonaDto          DTO          datos que viajan entre vista y servicio
PersonaService      SERVICIO     reglas de negocio, validacion y transacciones
PersonaMapper       MAPPER       MapStruct: convierte entidad <-> DTO
PersonaRepository   REPOSITORIO  acceso a datos (sin SQL manual)
Persona             ENTIDAD      tabla persona + auditoria
db/migration        FLYWAY       scripts SQL versionados (V1, V2, ...)
```

```
src/main/java/com/ejemplo/crudpersona/
  Launcher.java, JavaFxApplication.java, CrudPersonaApplication.java
  config/AuditoriaConfig.java         @EnableJpaAuditing + usuario de Windows
  controller/PersonaController.java
  dto/PersonaDto.java
  exception/NegocioException.java
  mapper/PersonaMapper.java
  model/Persona.java
  repository/PersonaRepository.java
  service/PersonaService.java, ExportacionService.java
src/main/resources/
  application.properties, application-postgres.properties
  db/migration/sqlite/       V1__crear_tabla_persona.sql, V2__auditoria_persona.sql
  db/migration/postgresql/   los mismos scripts para PostgreSQL
  view/persona-view.fxml, css/app.css, icono.ico
```

## Ejecutar

```
mvn clean javafx:run
```

La base de datos se guarda en `%USERPROFILE%\.gestion-personas\persona.db`.

## Pruebas automatizadas

```
mvn test
```

| Clase | Tipo | Que prueba |
|---|---|---|
| `PersonaRepositoryTest` | `@DataJpaTest` | Consultas derivadas, busqueda JPQL y auditoria contra SQLite |
| `PersonaServiceTest` | Mockito | Reglas de negocio (DNI duplicado, actualizar, eliminar) sin base de datos |
| `PersonaServiceIntegracionTest` | `@SpringBootTest` | Validaciones `@Valid` y paginacion de 15 en 15 |
| `ExportacionServiceTest` | Unitaria | El Excel y el PDF se generan con el contenido correcto |

Las pruebas usan su propia base (`target/test.db`) y nunca tocan los datos reales.

## Mejoras implementadas (seccion 17 del manual)

1. **DTO con MapStruct**: la vista trabaja con `PersonaDto`; la entidad no sale del servicio.
2. **Auditoria**: `@CreatedDate`, `@LastModifiedDate`, `@CreatedBy`, `@LastModifiedBy` con `@EnableJpaAuditing`. Se muestra debajo del formulario y en la columna *Modificado*.
3. **Flyway**: reemplaza `ddl-auto=update`. Si la base ya existia se toma como version 1 (`baseline-on-migrate`).
4. **Pruebas**: `@DataJpaTest` para el repositorio y Mockito para el servicio (15 pruebas).
5. **Exportar** a Excel (Apache POI) y PDF (OpenPDF), respetando el filtro de busqueda.
6. **Segundo plano**: las consultas y exportaciones corren en un `javafx.concurrent.Task`; la ventana no se congela.
7. **PostgreSQL**: crear la base `personas` y arrancar con el perfil `postgres`:

```
set SPRING_PROFILES_ACTIVE=postgres
mvn javafx:run
```

## Generar el instalador

```
generar-instalador.bat            (instalador .exe, requiere WiX Toolset 3.11 en el PATH)
generar-instalador.bat portable   (carpeta con GestionPersonas.exe, sin WiX)
```

El resultado queda en la carpeta `instalador\`. El usuario final no necesita tener Java instalado.
