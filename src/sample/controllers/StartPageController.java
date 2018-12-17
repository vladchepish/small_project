package sample.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import sample.DriveService;
import sample.ReportFileManager;

import static java.lang.Thread.sleep;

public class StartPageController {
    private static final String CHANGE_OWNER_REPORT = "ChangeOwnerReport.txt";
    private static final String REPORT_FILE_NAME = "Report.txt";

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button changeOwnerBtn;

    @FXML
    private TextArea changedOwnerResultArea;

    @FXML
    private TextField changingOwnerFolderID;

    @FXML
    private TextField changingOwnerEmail;

    private Stage startStage;

    @FXML
    private Button deleteBtn;

    @FXML
    private TextArea resultArea;

    @FXML
    private TextField deletingPermissionFolderID;

    @FXML
    private TextField deletingPermissionEmail;


    @FXML
    void initialize() {
    }

    public void setStartStage(Stage startStage){
        this.startStage = startStage;
    }

    @FXML
    private void changeOwner() throws IOException, InterruptedException {
        if (isInputValid(changingOwnerFolderID, changingOwnerEmail)){
            changedOwnerResultArea.setText("");

            String email = changingOwnerEmail.getText().trim();
            String folderId = changingOwnerFolderID.getText().trim();

            List<File> googleRootFolders = DriveService.getGoogleRootFolders(folderId);
            List<File> googleRootFiles = DriveService.getGoogleRootFiles(googleRootFolders, folderId);

            List<File> finalFoldersList = DriveService.sortElementsByOwner(googleRootFolders);
            List<File> finalFilesList = DriveService.sortElementsByOwner(googleRootFiles);

            System.out.println("Общее количество папок для обработки: " + finalFoldersList.size());
            System.out.println("Общее количество файлов для обработки: " + finalFilesList.size());

            java.io.File reportFile = ReportFileManager.createAndCleanReportFile(CHANGE_OWNER_REPORT);

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
                changedOwnerResultArea.appendText(listItem + "\n");
            }

            googleRootFiles.clear();
            googleRootFolders.clear();
        }
    }

    @FXML
    private void deletePermission() throws IOException {
        if (isInputValid(deletingPermissionFolderID, deletingPermissionEmail)){
            resultArea.setText("");

            String folderId = deletingPermissionFolderID.getText().trim();
            String email = deletingPermissionEmail.getText().trim();

            List<com.google.api.services.drive.model.File> googleRootFolders = DriveService.getGoogleRootFolders(folderId);
            List<com.google.api.services.drive.model.File> googleRootFiles = DriveService.getGoogleRootFiles(googleRootFolders, folderId);

            HashMap<String, List<File>> grapesOfFolders = DriveService.separateBigList(googleRootFolders, email);
            HashMap<String, List<com.google.api.services.drive.model.File>> grapesOfFiles = DriveService.separateBigList(googleRootFiles, email);

            List<com.google.api.services.drive.model.File> listFoldersForDeletingPermission = new ArrayList<>();
            List<com.google.api.services.drive.model.File> listFilesForDeletingPermission = new ArrayList<>();

            for (com.google.api.services.drive.model.File f : grapesOfFolders.get("myFolders")){
                listFoldersForDeletingPermission.add(f);
            }
            for (com.google.api.services.drive.model.File f : grapesOfFolders.get("otherFolders")){
                listFoldersForDeletingPermission.add(f);
            }

            for (com.google.api.services.drive.model.File f : grapesOfFiles.get("myFolders")){
                listFilesForDeletingPermission.add(f);
            }
            for (com.google.api.services.drive.model.File f : grapesOfFiles.get("otherFolders")){
                listFilesForDeletingPermission.add(f);
            }

            List<Permission> permissionList;

            java.io.File reportFile = ReportFileManager.createAndCleanReportFile(REPORT_FILE_NAME);

            // This part deleting permissions from files
            ReportFileManager.addIformationToFile(reportFile, "Работа с файлами");
            for (com.google.api.services.drive.model.File file : listFilesForDeletingPermission) {

                permissionList = file.getPermissions();
                //delete permissions
                for (Permission permission : permissionList) {
                    //this part deleting permission by link
                    if (permission.getId().contains("anyone")) {
                        DriveService.deletePermissions(file.getId(), permission.getId());
                        ReportFileManager.addIformationToFile(reportFile, "Удалён доступ '" + permission.getId() + "' из файла: '" + file.getName() + "'");
                        System.out.println("Deleted permission '" + permission.getId() + "' from file: '" + file.getName() + "'");
                    } //this part deleting permission by e-mail
                    else if (permission.getEmailAddress().contains(email)) {
                        DriveService.deletePermissions(file.getId(), permission.getId());
                        ReportFileManager.addIformationToFile(reportFile, "Удалён e-mail " + email + " из доступов к файлу: '" + file.getName() + "'");
                        System.out.println("Deleted email " + email + " from file: '" + file.getName() + "'");
                    }

                }
            }

            // This part deleting permissions from folders
            ReportFileManager.addIformationToFile(reportFile, "Работа с папками");
            //for (com.google.api.services.drive.model.File folder : googleRootFolders) {
            for (int i = listFoldersForDeletingPermission.size() - 1; i >=0; i--){

                com.google.api.services.drive.model.File folder = listFoldersForDeletingPermission.get(i);
                permissionList = folder.getPermissions();

                //delete permissions
                for (Permission permission : permissionList) {
                    //this part deleting permission by link
                    if (permission.getId().contains("anyone")){
                        DriveService.deletePermissions(folder.getId(), permission.getId());
                        ReportFileManager.addIformationToFile(reportFile, "Удалён доступ '" + permission.getId() + "' из папки: '" + folder.getName() + "'");
                        System.out.println("Deleted permission '" + permission.getId() + "' from folder: '" + folder.getName() + "'");
                    } //this part deleting permission by e-mail
                    else if (permission.getEmailAddress().contains(email)) {
                        DriveService.deletePermissions(folder.getId(), permission.getId());
                        ReportFileManager.addIformationToFile(reportFile, "Удален e-mail " + email + " из досутпов к папке: '" + folder.getName() + "'");
                        System.out.println("Deleted email " + email + " from folder: '" + folder.getName() + "'");
                    }
                }
            }


            List<String> finalList = ReportFileManager.addReportFileIntoOneList(reportFile);
            for(String listItem : finalList){
                resultArea.appendText(listItem + "\n");
            }

            googleRootFiles.clear();
            googleRootFolders.clear();
        }
    }

    public static String getPropertyPath() {
        java.io.File jarPath = new java.io.File(DriveService.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        return jarPath.getParentFile().getAbsolutePath();
    }

    private boolean isInputValid(TextField folderID, TextField email){
        String errorMessage = "";

        if (folderID.getText() == null || folderID.getText().length() == 0 ){
            errorMessage += "Некорректный id папки! \n";
        }
        if (email.getText() == null || email.getText().length() == 0){
            errorMessage += "Некорректный e-mail! \n";
        }
        if (errorMessage.length() == 0){
            return true;
        }else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(startStage);
            alert.setTitle("Указаны некорректные данные");
            alert.setHeaderText("Пожалуйста, проверьте корректность указанных данных");
            alert.setContentText(errorMessage);
            alert.showAndWait();
            return false;
        }
    }


}
