package sample;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import sample.controllers.DeletePermissionController;
import sample.controllers.StartPageController;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.*;

public class DriveService {

    private static Drive service;
    private static final String APPLICATION_NAME = "Обработчик результатов опросника";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "driveTokens";
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    private static final String CREDENTIALS_FILE_NAME = "credentialsDrive.json";
    private static List<File> fileList = new ArrayList<>();
    private static List<com.google.api.services.drive.model.File> documentsList = new ArrayList<>();

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        //InputStream in = Main.class.getResourceAsStream(CREDENTIALS_FILE_PATH);

        InputStream in = new FileInputStream(new java.io.File(StartPageController.getPropertyPath() + java.io.File.separator + CREDENTIALS_FILE_NAME));
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    private static Drive getService() {
        if (service == null) {
            try {
                final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
                service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                        .setApplicationName(APPLICATION_NAME)
                        .build();
            } catch (GeneralSecurityException | IOException firstEx) {
                firstEx.printStackTrace();
            }
        }
        return service;
    }

    public static List<File> getGoogleSubFolders(String googleFolderIdParent) throws IOException {

        Drive driveService = getService();
        String pageToken = null;
        String query = null;

        if (googleFolderIdParent == null) {
            query = " mimeType = 'application/vnd.google-apps.folder' "
                    + " and 'root' in parents";
        } else {
            query = " mimeType = 'application/vnd.google-apps.folder' "
                    + " and '" + googleFolderIdParent + "' in parents";
        }

        do {
            FileList result = driveService.files().list().setQ(query).setSpaces("drive")
                    .setFields("nextPageToken, files(id, name, permissions, owners, capabilities, ownedByMe)")
                    .setPageToken(pageToken).execute();
            fileList.addAll(result.getFiles());
            for (File file : result.getFiles()) {
                getGoogleSubFolders(file.getId());
            }
            pageToken = result.getNextPageToken();
        } while (pageToken != null);
        return fileList;
    }

    public static List<File> getGoogleRootFolders(String folderId) throws IOException {
        return getGoogleSubFolders(folderId);
    }

    public static void deletePermissions(String fileId, String permissionId) throws IOException {

        Drive driveService = getService();
        driveService.permissions().delete(fileId, permissionId).execute();
    }

    // create permission
    public static Permission createPermissions(String fileId, String email) throws IOException {
        Permission userPermission = new Permission()
                .setType("user")
                .setRole("owner")
                .setEmailAddress(email);

        Drive driveService = getService();
        Permission createdPermission = driveService.permissions().create(fileId, userPermission).setTransferOwnership(true).execute();
        return createdPermission;
    }

    // working with files
    // This method takes list folder Files and id root folder
    public static List<com.google.api.services.drive.model.File> getGoogleRootFiles(List<com.google.api.services.drive.model.File> googleRootFoldersd, String googleFolderIdParent) throws IOException {
        Drive driveService = getService();
        String pageToken = null;

        // Here cycle checking documents in each folder from the list. If some files finding they adding into List documentsList
        for (int i = 0; i < googleRootFoldersd.size(); i++) {
            do {
                FileList result = driveService.files().list().setQ(" mimeType != 'application/vnd.google-apps.folder' "
                        + " and '" + googleRootFoldersd.get(i).getId() + "' in parents").setSpaces("drive")
                        .setFields("nextPageToken, files(id, name, permissions, owners, capabilities, ownedByMe)")
                        .setPageToken(pageToken).execute();
                documentsList.addAll(result.getFiles());
                pageToken = result.getNextPageToken();
            } while (pageToken != null);
        }


        // Here is checking documents in root folder. If some documents finding - they adding into documentsList
        do {
            FileList result = driveService.files().list().setQ(" mimeType != 'application/vnd.google-apps.folder' "
                    + " and '" + googleFolderIdParent + "' in parents").setSpaces("drive")
                    .setFields("nextPageToken, files(id, name, permissions, owners, capabilities, ownedByMe)")
                    .setPageToken(pageToken).execute();
            documentsList.addAll(result.getFiles());
            for (File file : result.getFiles()) {
                getGoogleSubFolders(file.getId());
            }
            pageToken = result.getNextPageToken();
        } while (pageToken != null);

        // Here returning final documents list
        return documentsList;
    }

    public static HashMap<String, List<File>> separateBigList(List<File> bigList, String deletedUserEmail){
        HashMap<String, List<File>> grapes = new HashMap<>();

        List<com.google.api.services.drive.model.File> foldersWithOwnerCurrentUser = new ArrayList<>();
        List<com.google.api.services.drive.model.File> foldersWithOwnerUserThatShouldBeDeleted = new ArrayList<>();
        List<com.google.api.services.drive.model.File> foldersWithOnlyReadPermission = new ArrayList<>();
        List<com.google.api.services.drive.model.File> foldersWithOtherOwnres = new ArrayList<>();

        List<Permission> permissionList;

        for(File folder : bigList){
            if (folder.getCapabilities().getCanEdit().equals(false)){
                foldersWithOnlyReadPermission.add(folder);
            } else if (folder.getOwnedByMe().equals(true)) {
                foldersWithOwnerCurrentUser.add(folder);
            } else {
                permissionList = folder.getPermissions();
                for (Permission permission : permissionList) {
                    if (permission.getRole().contains("owner")){
                        if (permission.getEmailAddress().contains(deletedUserEmail)) {
                            foldersWithOwnerUserThatShouldBeDeleted.add(folder);
                        } else {
                            foldersWithOtherOwnres.add(folder);
                        }
                    }
                }
            }
        }

        grapes.put("myFolders", foldersWithOwnerCurrentUser);
        grapes.put("deletedUserFolders", foldersWithOwnerUserThatShouldBeDeleted);
        grapes.put("onlyReadFolders", foldersWithOnlyReadPermission);
        grapes.put("otherFolders", foldersWithOtherOwnres);

        return grapes;
    }

    public static List<File> sortElementsByOwner(List<File> bigList){
        List<File> myElementsList = new ArrayList<>();
        List<File> notMyElementsList = new ArrayList<>();
        for (File f : bigList){
            if (f.getOwnedByMe().equals(true)){
                myElementsList.add(f);
            } else {
                notMyElementsList.add(f);
            }
        }
        return myElementsList;
    }
}
