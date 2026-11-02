# Sistema CRUD de Personas

Aplicacion de escritorio para registrar personas (crear, leer, actualizar y eliminar)
con **JavaFX + Spring Boot + JPA + Lombok + SQLite**.

## Tecnologias

| Tecnologia | Version |
|---|---|
| Java (JDK) | 17 |
| Spring Boot | 3.3.4 |
| JavaFX | 21.0.4 |
| AtlantaFX | 2.0.1 |
| SQLite | 3.45.3.0 |

## Arquitectura

```
persona-view.fxml   VISTA        estructura visual y estilos
PersonaController   CONTROLADOR  recibe eventos y muestra resultados
PersonaService      SERVICIO     reglas de negocio y transacciones
PersonaRepository   REPOSITORIO  acceso a datos (sin SQL manual)
persona.db          BASE DE DATOS SQLite
```

## Ejecutar

```
mvn clean javafx:run
```

La base de datos se guarda en `%USERPROFILE%\.gestion-personas\persona.db`.

## Generar el instalador

```
generar-instalador.bat            (instalador .exe, requiere WiX Toolset 3.11)
generar-instalador.bat portable   (carpeta con GestionPersonas.exe, sin WiX)
```

El resultado queda en la carpeta `instalador\`. El usuario final no necesita tener Java instalado.
