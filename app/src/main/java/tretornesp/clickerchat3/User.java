package tretornesp.clickerchat3;

import java.util.ArrayList;

public class User {
    private String uid;
    private String name;
    private String imageURL;
    private String status;
    private String location;
    private ArrayList<String> groups;
    private ArrayList<String> categories;

    public User(String uid, String name, String imageURL, String status, String location, ArrayList<String> groups, ArrayList<String> categories) {
        this.uid = uid;
        this.imageURL = imageURL;
        this.name = name;
        this.status = status;
        this.location = location;
        this.groups = groups;
        this.categories = categories;
    }

    public User() {

    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ArrayList<String> getGroups() {
        return groups;
    }

    public void setGroups(ArrayList<String> groups) {
        this.groups = groups;
    }

    public ArrayList<String> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<String> categories) {
        this.categories = categories;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
