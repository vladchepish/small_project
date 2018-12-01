package sample;

import java.util.List;

public class Document {
    private String name;
    private List<DocumentPemissions> permissions;

    public Document(String name, List<DocumentPemissions> permissions){
        this.name = name;
        this.permissions = permissions;
    }

    public String getName() {
        return name;
    }

    public List<DocumentPemissions> getPermissions() {
        return permissions;
    }
}
