package sample.controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import com.google.api.services.drive.model.Permission;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import sample.DriveService;
import sample.ReportFileManager;

public class DeletePermissionController {

    private static final String REPORT_FILE_NAME = "Report.txt";

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField folderIdInput;

    @FXML
    private TextArea resultArea;

    @FXML
    private Label folderErrorLabel;

    @FXML
    private Label afterMethodLabel;

    @FXML
    private Label emailErrorLabel;

    @FXML
    private TextField emailInput;

    @FXML
    private Button deleteBtn;

    @FXML
    void initialize() {
        deleteBtn.setOnAction(event -> {
            String folderId = folderIdInput.getText().trim();
            String email = emailInput.getText().trim();

            if (folderId.equals("")){
                folderErrorLabel.setText("Поле 'FolderId' должно быть заполнено");
            } else if(email.equals("")){
                emailErrorLabel.setText("Поле 'E-mail' должно быть заполнено");
            } else if (folderId.equals("") && email.equals("")){
                folderErrorLabel.setText("Поле 'FolderId' должно быть заполнено");
                emailErrorLabel.setText("Поле 'E-mail' должно быть заполнено");
            } else {
                folderErrorLabel.setText("");
                emailErrorLabel.setText("");
                try {
                    deletingPermission(folderId, email);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void deletingPermission(String folderId, String email) throws IOException {
        List<com.google.api.services.drive.model.File> googleRootFolders = DriveService.getGoogleRootFolders(folderId);
        List<com.google.api.services.drive.model.File> googleRootFiles = DriveService.getGoogleRootFiles(googleRootFolders, folderId);

        HashMap<String, List<com.google.api.services.drive.model.File>> grapesOfFolders = DriveService.separateBigList(googleRootFolders, email);
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

        File reportFile = ReportFileManager.createAndCleanReportFile(REPORT_FILE_NAME);

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

    public static String getPropertyPath() {
        File jarPath = new File(DriveService.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        return jarPath.getParentFile().getAbsolutePath();
    }


}