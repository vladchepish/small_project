package sample.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import sample.Main;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class RootLayoutController {

    private Main main;

    public void setMainApp(Main main) {
        this.main = main;
    }

    @FXML
    private void handleExit() {
        System.exit(0);
    }

    @FXML
    private void handleAbout(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("About");
        alert.setContentText("Author: Olshanskiy Vladislav\nBeta\nVersion: 0.0.1\nQALab 2018");

        alert.showAndWait();
    }

    @FXML
    public void openReadMe() throws IOException {
        File htmlFile = new File("resources/readMe/index.html");
        Desktop.getDesktop().browse(htmlFile.toURI());
    }

}
