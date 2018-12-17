package sample.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.google.api.services.drive.model.File;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import sample.DriveService;
import sample.ReportFileManager;

import static java.lang.Thread.sleep;

public class ChangeOwnerController {
    private static final String REPORT_FILE_NAME = "ChangeOwnerReport.txt";

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button returnBackBtn;

    @FXML
    private Button changeOwnerBtn;

    @FXML
    private TextField changeOwnerField;

    @FXML
    private TextField rootFolderId;

    @FXML
    private TextArea resultArea;


    @FXML
    void initialize() {
        returnBackBtn.setOnAction(event -> {
            returnBackBtn.getScene().getWindow().hide();

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/sample/views/StartPage.fxml"));

            try {
                loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Parent root = loader.getRoot();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.showAndWait();
        });

        changeOwnerBtn.setOnAction(event -> {
            String email = changeOwnerField.getText().trim();
            String folderId = rootFolderId.getText().trim();
            try {
                changeOwner(folderId, email);
                System.out.println("Done!");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private void changeOwner(String folderId, String email) throws IOException, InterruptedException {
        List<File> googleRootFolders = DriveService.getGoogleRootFolders(folderId);
        List<File> googleRootFiles = DriveService.getGoogleRootFiles(googleRootFolders, folderId);

        List<File> finalFoldersList = DriveService.sortElementsByOwner(googleRootFolders);
        List<File> finalFilesList = DriveService.sortElementsByOwner(googleRootFiles);

        System.out.println("Общее количество папок для обработки: " + finalFoldersList.size());
        System.out.println("Общее количество файлов для обработки: " + finalFilesList.size());

        java.io.File reportFile = ReportFileManager.createAndCleanReportFile(REPORT_FILE_NAME);

        ReportFileManager.addIformationToFile(reportFile, "Работа с файлами:");
        for (File element : finalFilesList){
            DriveService.createPermissions(element.getId(), email);
            ReportFileManager.addIformationToFile(reportFile, "Изменён владелец файла: " + element.getName());
            System.out.println("Изменён владелец файла: " + element.getName());
            sleep(500);
        }

        ReportFileManager.addIformationToFile(reportFile, "Работа с папками:");
        for (File element : finalFoldersList){
            DriveService.createPermissions(element.getId(), email);
            ReportFileManager.addIformationToFile(reportFile, "Изменён владелец папки: " + element.getName());
            System.out.println("Изменён владелец папки: " + element.getName());
            sleep(500);
        }

        List<String> finalList = ReportFileManager.addReportFileIntoOneList(reportFile);
        for(String listItem : finalList){
            resultArea.appendText(listItem + "\n");
        }

        googleRootFiles.clear();
        googleRootFolders.clear();
    }
}
