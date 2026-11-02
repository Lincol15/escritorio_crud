package com.ejemplo.crudpersona.controller;

import com.ejemplo.crudpersona.dto.PersonaDto;
import com.ejemplo.crudpersona.exception.NegocioException;
import com.ejemplo.crudpersona.service.ExportacionService;
import com.ejemplo.crudpersona.service.PersonaService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PersonaController {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PersonaService service;
    private final ExportacionService exportacion;

    /**
     * Hilo de fondo para las consultas: la interfaz no se congela mientras
     * se lee la base de datos. Al ser un solo hilo, los resultados llegan en orden.
     */
    private final ExecutorService ejecutor = Executors.newSingleThreadExecutor(r -> {
        Thread hilo = new Thread(r, "consultas-bd");
        hilo.setDaemon(true);
        return hilo;
    });

    @FXML private TextField txtId;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtDni;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtBuscar;

    @FXML private TableView<PersonaDto> tabla;
    @FXML private TableColumn<PersonaDto, String> colId;
    @FXML private TableColumn<PersonaDto, String> colNombre;
    @FXML private TableColumn<PersonaDto, String> colApellido;
    @FXML private TableColumn<PersonaDto, String> colDni;
    @FXML private TableColumn<PersonaDto, String> colEmail;
    @FXML private TableColumn<PersonaDto, String> colTelefono;
    @FXML private TableColumn<PersonaDto, String> colModificado;

    @FXML private Label lblEstado;
    @FXML private Label lblPagina;
    @FXML private Label lblAuditoria;
    @FXML private Button btnAnterior;
    @FXML private Button btnSiguiente;
    @FXML private ProgressIndicator indicador;

    private final ObservableList<PersonaDto> datos = FXCollections.observableArrayList();
    private int paginaActual = 0;
    private int totalPaginas = 1;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        colNombre.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getNombre()));
        colApellido.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getApellido()));
        colDni.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getDni()));
        colEmail.setCellValueFactory(c ->
                new SimpleStringProperty(textoODefecto(c.getValue().getEmail())));
        colTelefono.setCellValueFactory(c ->
                new SimpleStringProperty(textoODefecto(c.getValue().getTelefono())));
        colModificado.setCellValueFactory(c ->
                new SimpleStringProperty(formatear(c.getValue().getModificadoEn())));

        tabla.setItems(datos);
        tabla.getSelectionModel().selectedItemProperty()
                .addListener((obs, anterior, actual) -> mostrarEnFormulario(actual));

        txtBuscar.textProperty().addListener((obs, a, b) -> {
            paginaActual = 0;
            cargarTabla();
        });

        cargarTabla();
    }

    @FXML
    private void onNuevo() {
        tabla.getSelectionModel().clearSelection();
        limpiarFormulario();
        txtNombre.requestFocus();
        lblEstado.setText("Formulario limpio: listo para registrar.");
    }

    @FXML
    private void onGuardar() {
        PersonaDto persona = PersonaDto.builder()
                .id(txtId.getText().isBlank() ? null : Long.valueOf(txtId.getText()))
                .nombre(txtNombre.getText().trim())
                .apellido(txtApellido.getText().trim())
                .dni(txtDni.getText().trim())
                .email(txtEmail.getText().trim())
                .telefono(txtTelefono.getText().trim())
                .build();
        try {
            boolean esNuevo = persona.getId() == null;
            PersonaDto guardada = service.guardar(persona);
            lblEstado.setText(esNuevo
                    ? "Registrado correctamente (id " + guardada.getId() + ")."
                    : "Actualizado correctamente (id " + guardada.getId() + ").");
            limpiarFormulario();
            cargarTabla();
        } catch (ConstraintViolationException e) {
            String detalle = e.getConstraintViolations().stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("\n"));
            alerta(Alert.AlertType.WARNING, "Revisa los datos", detalle);
        } catch (NegocioException e) {
            alerta(Alert.AlertType.WARNING, "No se pudo guardar", e.getMessage());
        } catch (Exception e) {
            alerta(Alert.AlertType.ERROR, "Error inesperado", String.valueOf(e.getMessage()));
        }
    }

    @FXML
    private void onEliminar() {
        PersonaDto seleccionada = tabla.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            alerta(Alert.AlertType.INFORMATION, "Sin seleccion",
                    "Selecciona una fila de la tabla para eliminar.");
            return;
        }
        Alert confirmar = new Alert(Alert.AlertType.CONFIRMATION,
                "Se eliminara a " + seleccionada.getNombreCompleto() + ".",
                ButtonType.CANCEL, ButtonType.OK);
        confirmar.setTitle("Confirmar eliminacion");
        confirmar.setHeaderText("Eliminar registro");

        Optional<ButtonType> respuesta = confirmar.showAndWait();
        if (respuesta.isPresent() && respuesta.get() == ButtonType.OK) {
            try {
                service.eliminar(seleccionada.getId());
                limpiarFormulario();
                cargarTabla();
                lblEstado.setText("Registro eliminado.");
            } catch (NegocioException e) {
                alerta(Alert.AlertType.WARNING, "No se pudo eliminar", e.getMessage());
            }
        }
    }

    @FXML
    private void onAnterior() {
        if (paginaActual > 0) {
            paginaActual--;
            cargarTabla();
        }
    }

    @FXML
    private void onSiguiente() {
        if (paginaActual < totalPaginas - 1) {
            paginaActual++;
            cargarTabla();
        }
    }

    @FXML
    private void onExportarExcel() {
        exportar("Excel", "*.xlsx", "personas.xlsx", exportacion::aExcel);
    }

    @FXML
    private void onExportarPdf() {
        exportar("PDF", "*.pdf", "personas.pdf", exportacion::aPdf);
    }

    private void cargarTabla() {
        String filtro = txtBuscar.getText();
        int pagina = paginaActual;
        enSegundoPlano(() -> service.listar(filtro, pagina), this::mostrarPagina, "No se pudo cargar la tabla");
    }

    private void mostrarPagina(Page<PersonaDto> pagina) {
        totalPaginas = Math.max(pagina.getTotalPages(), 1);
        // Si se elimino el ultimo registro de la ultima pagina, se retrocede una
        if (paginaActual >= totalPaginas) {
            paginaActual = totalPaginas - 1;
            cargarTabla();
            return;
        }
        datos.setAll(pagina.getContent());
        lblPagina.setText("Pagina " + (paginaActual + 1) + " de " + totalPaginas
                + "  |  " + pagina.getTotalElements() + " persona(s)");
        btnAnterior.setDisable(paginaActual == 0);
        btnSiguiente.setDisable(paginaActual >= totalPaginas - 1);
    }

    private void exportar(String formato, String extension, String nombreInicial, Exportador exportador) {
        FileChooser selector = new FileChooser();
        selector.setTitle("Exportar a " + formato);
        selector.setInitialFileName(nombreInicial);
        selector.getExtensionFilters().add(new FileChooser.ExtensionFilter(formato, extension));
        File archivo = selector.showSaveDialog(tabla.getScene().getWindow());
        if (archivo == null) {
            return;
        }
        String filtro = txtBuscar.getText();
        enSegundoPlano(() -> {
            List<PersonaDto> personas = service.listarTodos(filtro);
            try {
                exportador.exportar(personas, archivo.toPath());
            } catch (Exception e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
            return personas.size();
        }, total -> lblEstado.setText("Se exportaron " + total + " registro(s) a " + archivo.getName() + "."),
                "No se pudo exportar a " + formato);
    }

    /** Ejecuta la consulta en el hilo de fondo y entrega el resultado en el hilo de JavaFX. */
    private <T> void enSegundoPlano(Supplier<T> trabajo, Consumer<T> alTerminar, String tituloError) {
        Task<T> tarea = new Task<>() {
            @Override
            protected T call() {
                return trabajo.get();
            }
        };
        indicador.visibleProperty().bind(tarea.runningProperty());
        tarea.setOnSucceeded(e -> alTerminar.accept(tarea.getValue()));
        tarea.setOnFailed(e ->
                alerta(Alert.AlertType.ERROR, tituloError, String.valueOf(tarea.getException().getMessage())));
        ejecutor.submit(tarea);
    }

    private void mostrarEnFormulario(PersonaDto p) {
        if (p == null) {
            lblAuditoria.setText("");
            return;
        }
        txtId.setText(String.valueOf(p.getId()));
        txtNombre.setText(p.getNombre());
        txtApellido.setText(p.getApellido());
        txtDni.setText(p.getDni());
        txtEmail.setText(textoODefecto(p.getEmail()));
        txtTelefono.setText(textoODefecto(p.getTelefono()));
        lblAuditoria.setText("Creado por " + textoODefecto(p.getCreadoPor(), "-")
                + " el " + formatear(p.getCreadoEn())
                + "   |   Modificado por " + textoODefecto(p.getModificadoPor(), "-")
                + " el " + formatear(p.getModificadoEn()));
    }

    private void limpiarFormulario() {
        txtId.clear();
        txtNombre.clear();
        txtApellido.clear();
        txtDni.clear();
        txtEmail.clear();
        txtTelefono.clear();
        lblAuditoria.setText("");
    }

    private String textoODefecto(String valor) {
        return textoODefecto(valor, "");
    }

    private String textoODefecto(String valor, String defecto) {
        return valor == null ? defecto : valor;
    }

    private String formatear(LocalDateTime fecha) {
        return fecha == null ? "-" : fecha.format(FECHA);
    }

    private void alerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert a = new Alert(tipo, mensaje, ButtonType.OK);
        a.setTitle(titulo);
        a.setHeaderText(titulo);
        a.getDialogPane().setMinWidth(420);
        a.showAndWait();
    }

    @FunctionalInterface
    private interface Exportador {
        void exportar(List<PersonaDto> personas, Path destino) throws Exception;
    }
}
