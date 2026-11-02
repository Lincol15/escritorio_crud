package com.ejemplo.crudpersona;

import javafx.application.Application;

import java.nio.file.Files;
import java.nio.file.Path;

public class Launcher {

    public static void main(String[] args) throws Exception {
        // SQLite no crea carpetas: se prepara la carpeta de datos del usuario
        Files.createDirectories(
                Path.of(System.getProperty("user.home"), ".gestion-personas"));
        Application.launch(JavaFxApplication.class, args);
    }
}
