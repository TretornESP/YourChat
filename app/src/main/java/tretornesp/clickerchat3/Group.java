package tretornesp.clickerchat3;

import java.io.StringBufferInputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class Group {

    private static final int MAX_USERS = 200;

    private String name;
    private String description;
    private String category;
    private String imageURL;
    private String database;
    private boolean updated;
    private ArrayList<String> users;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public ArrayList<String> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<String> users) {
        this.users = users;
    }

    public ArrayList<String> getAdmins() {
        return admins;
    }

    public void setAdmins(ArrayList<String> admins) {
        this.admins = admins;
    }

    private ArrayList<String> admins;
    private String id;

    public Group(String name, String description, String id, ArrayList<String> users, ArrayList<String> admins, String imageURL, String database, String category) {
        this.name = name;
        this.description = description;
        this.imageURL = imageURL;
        this.id = id;
        this.database = database;
        this.users = users;
        this.admins = admins;
        this.category = category;

        updated = true;
    }

    public Group(String id) {
        this.id = id;
        updated = true;
    }
    public Group() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public int getNumero_usuarios() {
        return users.size()+admins.size();
    }

    public String getNumero_usuarios_asString() {
        return "Usuarios: " + getNumero_usuarios();
    }

    public void updated() {
        updated = true;
    }

    public boolean isUpdated() {
        boolean r = updated;
        updated = false;
        return r;

    }
}
