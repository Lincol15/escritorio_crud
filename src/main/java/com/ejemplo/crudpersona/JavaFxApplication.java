package com.ejemplo.crudpersona;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Objects;

public class JavaFxApplication extends Application {

    private ConfigurableApplicationContext context;

    @Override
    public void init() {
        this.context = new SpringApplicationBuilder(CrudPersonaApplication.class)
                .web(WebApplicationType.NONE)
                .run();
    }

    @Override
    public void start(Stage stage) throws Exception {
        // Tema visual: hay PrimerDark, NordLight, NordDark, Dracula, etc.
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/persona-view.fxml"));
        // LINEA CLAVE: Spring entrega los controladores ya inyectados
        loader.setControllerFactory(context::getBean);

        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/app.css")).toExternalForm());

        stage.setTitle("Gestion de Personas | Spring Boot + JavaFX");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }

    @Override
    public void stop() {
        context.close();
        Platform.exit();
    }
}
