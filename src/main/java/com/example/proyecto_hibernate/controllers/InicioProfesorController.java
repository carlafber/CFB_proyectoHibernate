package com.example.proyecto_hibernate.controllers;

import com.example.proyecto_hibernate.classes.TipoProfesor;
import com.example.proyecto_hibernate.util.CambioEscena;
import com.example.proyecto_hibernate.util.GuardarParte;
import com.example.proyecto_hibernate.util.GuardarProfesor;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class InicioProfesorController implements Initializable {

    @FXML
    private Button bt_crearParte;

    @FXML
    private Button bt_crearProfesor;

    @FXML
    private Button bt_listaAlumnos;

    @FXML
    private Button bt_listaPartes;

    @FXML
    private StackPane fondo;

    @FXML
    private ImageView imagen_fondo;

    private TipoProfesor tipoProfesor;

    @FXML
    void onCrearParteClick(ActionEvent event) {
        GuardarParte.resetParte();
        CambioEscena.abrirEscena("parte-verde.fxml", "Crear parte");
    }


    @FXML
    void onCrearProfesorClick(ActionEvent event) {
        CambioEscena.abrirEscena("crear-profesor.fxml", "Crear profesor");
    }


    @FXML
    void onListaAlumnosClick(ActionEvent event) {
        CambioEscena.abrirEscena("lista-alumnos.fxml", "Lista alumnos");
    }


    @FXML
    void onListaPartesClick(ActionEvent event) {
        CambioEscena.abrirEscena("lista-partes.fxml", "Lista partes");
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //adaptar la imagen al tamaño de la pantalla
        imagen_fondo.fitWidthProperty().bind(fondo.widthProperty());
        imagen_fondo.fitHeightProperty().bind(fondo.heightProperty());

        //obtener el tipo de profesor a través de los datos que se almacenaron al iniciar sesión
        tipoProfesor = GuardarProfesor.getProfesor().getTipo();
        if(tipoProfesor.equals(TipoProfesor.profesor)){
            //al ser profesor, no tiene acceso a la mayoria de métodos, por lo que se ocultan
            ocultarBotones();
        }
    }


    //método para ocultar los botones
    private void ocultarBotones(){
        bt_listaPartes.setVisible(false);
        bt_listaAlumnos.setVisible(false);
        bt_crearProfesor.setVisible(false);
    }
}

