package com.example.proyecto_hibernate.controllers;

import com.example.proyecto_hibernate.CRUD.*;
import com.example.proyecto_hibernate.classes.*;
import com.example.proyecto_hibernate.util.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.*;
import javafx.event.ActionEvent;
import org.hibernate.Session;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;


public class ListaPartesController implements Initializable {

    @FXML
    private Button bt_borrar;

    @FXML
    private Button bt_buscar;

    @FXML
    private Button bt_buscarFecha;

    @FXML
    private DatePicker dt_fechaFin;

    @FXML
    private DatePicker dt_fechaInicio;

    @FXML
    private ImageView imagen_fondo;

    @FXML
    private Pagination pagination;

    @FXML
    private TableColumn<PartesIncidencia, Button> tc_botones;

    @FXML
    private TableColumn<PartesIncidencia, String> tc_descripcion;

    @FXML
    private TableColumn<PartesIncidencia, String> tc_expediente;

    @FXML
    private TableColumn<PartesIncidencia, String> tc_fecha;

    @FXML
    private TableColumn<PartesIncidencia, String> tc_grupo;

    @FXML
    private TableColumn<PartesIncidencia, String> tc_nombreAlumno;

    @FXML
    private TableColumn<PartesIncidencia, String> tc_profesor;

    @FXML
    private TableColumn<PartesIncidencia, String> tc_sancion;

    @FXML
    private TableView<PartesIncidencia> tv_partes;

    @FXML
    private TextField txt_numExpediente;

    private PartesCRUD partesCRUD = new PartesCRUD();

    private PartesIncidencia parte = new PartesIncidencia();

    private FilteredList<PartesIncidencia> listaFiltrada;

    private Session session;

    private ObservableList<PartesIncidencia> listaPartesObservable; // Lista observable para la tabla

    Configurable ParteVerdeController = new ParteVerdeController();

    Configurable ParteNaranjaController = new ParteNaranjaController();

    Configurable ParteRojoController = new ParteRojoController();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        session = HibernateUtil.getSessionFactory().openSession();

        // Configurar columnas de la tabla
        tc_expediente.setCellValueFactory(cellData -> {
            Alumnos alumnos = cellData.getValue().getAlumno();
            return new SimpleStringProperty(alumnos.getNumero_expediente());
        });

        tc_nombreAlumno.setCellValueFactory(cellData -> {
            Alumnos alumnos = cellData.getValue().getAlumno();
            return new SimpleStringProperty(alumnos.getNombre_alum());
        });

        tc_grupo.setCellValueFactory(cellData -> {
            Grupos grupo = cellData.getValue().getGrupo();
            return new SimpleStringProperty(grupo.getNombreGrupo());
        });

        tc_profesor.setCellValueFactory(cellData -> {
            Profesores profesor = cellData.getValue().getProfesor();
            return new SimpleStringProperty(profesor.getNombre());
        });

        tc_fecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        tc_descripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        tc_sancion.setCellValueFactory(new PropertyValueFactory<>("sancion"));

        tc_botones.setCellFactory(column -> new TableCell<>() {
            private final Button bt_verMas = new Button("Ver más");
            {
                bt_verMas.setStyle("-fx-background-color: #3396ff; -fx-text-fill: white;");

                bt_verMas.setOnAction(event -> {
                    parte = getTableView().getItems().get(getIndex());
                    abrirParte(parte);
                });

            }

            @Override
            protected void updateItem(Button item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null); // Limpia la celda si está vacía
                } else {
                    setGraphic(bt_verMas); // Añade el botón si la celda contiene datos
                    setStyle("-fx-alignment: CENTER;"); // Centra el botón en la celda
                }
            }
        });

        tv_partes.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(PartesIncidencia parte, boolean empty) {
                super.updateItem(parte, empty);
                String estilo = "-fx-background-color: ";
                if (parte == null || empty) {
                    estilo = "";
                    setStyle(""); // Restablecer estilo si la fila está vacía
                } else {
                    // Aplicar colores según el color del parte
                    if(parte.getColor().equals(ColorParte.VERDE)){
                        estilo += ColorParte.VERDE.getCodigo_color() + ";";
                    } else if(parte.getColor().equals(ColorParte.NARANJA)){
                        estilo += ColorParte.NARANJA.getCodigo_color() + ";";
                    } else if(parte.getColor().equals(ColorParte.ROJO)){
                        estilo += ColorParte.ROJO.getCodigo_color() + ";";
                    }
                }
                setStyle(estilo);
            }
        });


        ArrayList<PartesIncidencia> listaPartesIncidencia = partesCRUD.obtenerPartes();
        listaPartesObservable = FXCollections.observableArrayList(listaPartesIncidencia);

        listaFiltrada = new FilteredList<>(listaPartesObservable, parte -> true);
        tv_partes.setItems(listaFiltrada);

        configurarPaginacion(listaFiltrada);
    }


    @FXML
    void onBorrarClick(ActionEvent event) {
        listaFiltrada.setPredicate(alumno -> true);
        txt_numExpediente.clear();
        dt_fechaInicio.setValue(null);
        dt_fechaFin.setValue(null);

        configurarPaginacion(listaFiltrada);
        pagination.setCurrentPageIndex(0);

        tv_partes.refresh();
    }


    @FXML
    void onBuscarClick(ActionEvent event) {
        String numeroExpediente = txt_numExpediente.getText();

        // Validar si el campo está vacío
        if (numeroExpediente == null || numeroExpediente.isEmpty()) {
            Alerta.mensajeError("Campo vacío", "Por favor, introduce un número de expediente válido.");
            return;
        }

        try {
            // Aplicar el filtro a la lista
            listaFiltrada.setPredicate(parte -> {
                Alumnos alumno = parte.getAlumno();
                if (alumno != null) {
                    return alumno.getNumero_expediente().equals(numeroExpediente);
                }
                return false;
            });

            // Si no se encontraron resultados
            if (listaFiltrada.isEmpty()) {
                Alerta.mensajeError("Parte no encontrado", "No se encontró ninguna parte con el expediente: " + numeroExpediente);

                // Restablecer el filtro para mostrar todo
                listaFiltrada.setPredicate(parte -> true);

                // Recalibrar la paginación para mostrar todos los resultados
                configurarPaginacion(listaFiltrada);

                return;
            }

            // Recalibrar la paginación para reflejar los resultados de la búsqueda
            configurarPaginacion(listaFiltrada);

            // Mostrar la primera página de los resultados
            pagination.setCurrentPageIndex(0);
        } catch (Exception e) {
            Alerta.mensajeError("Error inesperado", "Ocurrió un error al buscar: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @FXML
    void onBuscarFechaClick(ActionEvent event) {
        LocalDate fechaInicio = dt_fechaInicio.getValue();
        LocalDate fechaFin = dt_fechaFin.getValue();

        // Validar que ambas fechas estén seleccionadas
        if (fechaInicio == null || fechaFin == null) {
            Alerta.mensajeError("Campos vacíos", "Por favor, selecciona ambas fechas para buscar.");
            return;
        }

        // Validar que el rango de fechas sea válido
        if (fechaInicio.isAfter(fechaFin)) {
            Alerta.mensajeError("Rango de fechas inválido", "La fecha inicial no puede ser posterior a la fecha final.");
            return;
        }

        try {
            // Aplicar filtro para el rango de fechas
            listaFiltrada.setPredicate(parte -> {
                if (parte.getFecha() != null) {
                    return !parte.getFecha().isBefore(fechaInicio) && !parte.getFecha().isAfter(fechaFin);
                }
                return false;
            });

            // Si no se encuentran resultados
            if (listaFiltrada.isEmpty()) {
                Alerta.mensajeError("No se encontraron partes", "No se encontraron partes entre las fechas: " + fechaInicio + " y " + fechaFin);

                // Restablecer el filtro para mostrar todos los elementos
                listaFiltrada.setPredicate(parte -> true);

                // Recalibrar la paginación para mostrar todos los resultados
                configurarPaginacion(listaFiltrada);

                return;
            }

            // Recalibrar la paginación para reflejar los resultados filtrados
            configurarPaginacion(listaFiltrada);

            // Mostrar la primera página de los resultados
            pagination.setCurrentPageIndex(0);
        } catch (Exception e) {
            Alerta.mensajeError("Error inesperado", "Ocurrió un error al buscar por rango de fechas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void configurarPaginacion(ObservableList<PartesIncidencia> listaCompleta) {
        int filasPorPagina = 10; // Número de filas por página

        // Calcular el número total de páginas
        int totalPaginas = (int) Math.ceil((double) listaCompleta.size() / filasPorPagina);
        pagination.setPageCount(totalPaginas);

        // Eliminar cualquier listener anterior para evitar duplicados
        pagination.currentPageIndexProperty().removeListener((observable, oldValue, newValue) -> {
            cambiarPagina(listaCompleta, filasPorPagina, newValue.intValue());
        });

        // Añadir un nuevo listener para cambiar de página
        pagination.currentPageIndexProperty().addListener((observable, oldValue, newValue) -> {
            cambiarPagina(listaCompleta, filasPorPagina, newValue.intValue());
        });

        // Mostrar la primera página
        cambiarPagina(listaCompleta, filasPorPagina, pagination.getCurrentPageIndex());
    }


    private void cambiarPagina(ObservableList<PartesIncidencia> listaCompleta, int filasPorPagina, int paginaActual) {
        int desdeIndex = paginaActual * filasPorPagina;
        int hastaIndex = Math.min(desdeIndex + filasPorPagina, listaCompleta.size());

        // Extraer los elementos para la página actual
        ObservableList<PartesIncidencia> subLista = FXCollections.observableArrayList(listaCompleta.subList(desdeIndex, hastaIndex));

        // Actualizar solo los ítems del TableView sin reinicializarlo
        tv_partes.setItems(subLista);

        // Forzar una actualización visual para evitar que los botones desaparezcan
        tv_partes.refresh();
    }

    private void abrirParte(PartesIncidencia parte){
        GuardarParte.setParte(parte);
        boolean estado = GuardarProfesor.getProfesor().getTipo().equals(TipoProfesor.profesor);
        GuardarController.setController(this);
        if(parte.getColor().equals(ColorParte.VERDE)){
            CambioEscena.abrirEscena("parte-verde.fxml", "Ver parte", ParteVerdeController, estado);
        } else if (parte.getColor().equals(ColorParte.NARANJA)) {
            CambioEscena.abrirEscena("parte-naranja.fxml", "Ver parte", ParteNaranjaController, estado);
        } else if (parte.getColor().equals(ColorParte.ROJO)) {
            CambioEscena.abrirEscena("parte-rojo.fxml", "Ver parte", ParteRojoController, estado);
        }
    }


    public void recargarListaPartes() {
        Platform.runLater(() -> {
            // Recuperar datos actualizados
            ArrayList<PartesIncidencia> listaActualizada = partesCRUD.obtenerPartes();
            // Actualizar lista observable
            listaPartesObservable.setAll(listaActualizada);

            configurarPaginacion(listaFiltrada);
        });
    }
}