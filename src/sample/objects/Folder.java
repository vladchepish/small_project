package sample.objects;

import java.util.List;

public class Folder {
    private String name;
    private List<FolderPermissions> permissions;

    public Folder(String name, List<FolderPermissions> permissions) {
        this.name = name;
        this.permissions = permissions;
    }

    public String getName() {
        return name;
    }

    public List<FolderPermissions> getPermissions() {
        return permissions;
    }
}
