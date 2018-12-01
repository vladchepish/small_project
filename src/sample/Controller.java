package sample;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import com.google.api.services.drive.model.Permission;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class Controller {

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

        List<Permission> permissionList;

        File reportFile = new File(REPORT_FILE_NAME);
        try{
            FileWriter writer = new FileWriter(reportFile);
            // Перезаписываем содержимое файла на случай, если файл не пуст
            writer.write("");
            writer.close();
        } catch (IOException ex){
            ex.printStackTrace();
        }

        // This part deleting permissions from files
        addTitleToFile(reportFile, "Работа с файлами");
        for (com.google.api.services.drive.model.File file : googleRootFiles) {
            permissionList = file.getPermissions();
            //delete permissions
            for (Permission permission : permissionList) {
                //this part deleting permission by link
                if (permission.getId().contains("anyone")) {
                    DriveService.deletePermissions(file.getId(), permission.getId());
                    addIformationToFile(reportFile, "Удалён доступ '" + permission.getId() + "' из файла: '" + file.getName() + "'\n");
                    System.out.println("Deleted permission '" + permission.getId() + "' from file: '" + file.getName() + "'");
                } //this part deleting permission by e-mail
                else if (permission.getEmailAddress().contains(email)) {
                    DriveService.deletePermissions(file.getId(), permission.getId());
                    addIformationToFile(reportFile, "Удалён e-mail " + email + " из доступов к файлу: '" + file.getName() + "'\n");
                    System.out.println("Deleted email " + email + " from file: '" + file.getName() + "'");
                }

            }
        }

        // This part deleting permissions from folders
        addTitleToFile(reportFile, "Работа с папками");
        //for (com.google.api.services.drive.model.File folder : googleRootFolders) {
        for (int i = googleRootFolders.size() - 1; i >=0; i--){
            com.google.api.services.drive.model.File folder = googleRootFolders.get(i);
            permissionList = folder.getPermissions();

            //delete permissions
            for (Permission permission : permissionList) {
                //this part deleting permission by link
                if (permission.getId().contains("anyone")){
                    DriveService.deletePermissions(folder.getId(), permission.getId());
                    addIformationToFile(reportFile, "Удалён доступ '" + permission.getId() + "' из папки: '" + folder.getName() + "'\n");
                    System.out.println("Deleted permission '" + permission.getId() + "' from folder: '" + folder.getName() + "'");
                } //this part deleting permission by e-mail
                else if (permission.getEmailAddress().contains(email)) {
                    DriveService.deletePermissions(folder.getId(), permission.getId());
                    addIformationToFile(reportFile, "Удален e-mail " + email + " из досутпов к папке: '" + folder.getName() + "'\n");
                    System.out.println("Deleted email " + email + " from folder: '" + folder.getName() + "'");
                }
            }
        }


        FileReader fr = new FileReader(reportFile);
        Scanner scanedFile = new Scanner(fr);
        List<String> finalList = new ArrayList<>();
        while (scanedFile.hasNextLine()){
            finalList.add(scanedFile.nextLine());
        }
        fr.close();

        for(String listItem : finalList){
            resultArea.appendText(listItem + "\n");
        }
    }

    public static String getPropertyPath() {
        File jarPath = new File(DriveService.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        return jarPath.getParentFile().getAbsolutePath();
    }

    public void addIformationToFile(File reportFile, String string){
        try{
            FileWriter writer = new FileWriter(reportFile, true);
            writer.write(string);
            writer.close();
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }

    public void addTitleToFile(File reportFile, String string){
        try{
            FileWriter writer = new FileWriter(reportFile, true);
            writer.write(string);
            writer.write("\n");
            writer.close();
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }
}