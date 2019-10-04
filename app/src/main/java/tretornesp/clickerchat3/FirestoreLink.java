package tretornesp.clickerchat3;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class FirestoreLink {
    private static FirebaseFirestore db;
    private static FirebaseUser currentFirebaseUser;

    private static final int TIMEOUT = 5000;

    private static User downloadedUser;

    private static boolean flag;

    public FirestoreLink() {
    }

    public static boolean init() {
        db = FirebaseFirestore.getInstance();
        currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (db == null || currentFirebaseUser == null) {
            return false;
        } else {
            return true;
        }
    }

    public static User downloadUser(final String uid) {

        downloadedUser = null;
        flag = false;

        DocumentReference docRef = db.collection("users").document(uid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String name = document.getString("name");
                        String imageURL = document.getString("imageURL");
                        String status = document.getString("status");
                        String location = document.getString("location");

                        ArrayList<String> groups = (ArrayList<String>) document.get("groups");
                        ArrayList<String> categories = (ArrayList<String>) document.get("categories");

                        User user = new User(uid, name, imageURL, status, location, groups, categories);
                        if (checkIntegrity(user)) downloadedUser = user;
                        System.out.println("GENERAdO USUARIO");
                    }
                }
            }
        });
        long timeout = System.currentTimeMillis()+TIMEOUT;
        while (downloadedUser == null && System.currentTimeMillis() < timeout) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e ) {
                e.printStackTrace();
            }
        }
        return downloadedUser;
    }

    public static User getDownloadedUser() {
        return downloadedUser;
    }

    public static boolean checkIntegrity(User user) {
        if (user.getUid() == null || user.getUid().equals("")) {
            return false;
        }
        if (user.getName() == null) {
            return false;
        }
        if (user.getImageURL() == null) {
            return false;
        }
        if (user.getStatus() == null) {
            return false;
        }
        if (user.getLocation() == null) {
            return false;
        }
        if (user.getGroups() == null) {
            return false;
        }
        if (user.getCategories() == null) {
            return false;
        }
        return true;
    }

    public static boolean checkIntegrity(Group group) {
        if (group.getCategory() == null || group.getCategory().equals("")) {
            return false;
        }
        if (group.getName() == null || group.getName().equals("")) {
            return false;
        }
        if (group.getDatabase() == null || group.getDatabase().equals("")) {
            return false;
        }
        if (group.getDescription() == null) {
            return false;
        }
        if (group.getAdmins() == null || group.getAdmins().size() == 0) {
            return false;
        }
        if (group.getImageURL() == null) {
            return false;
        }
        if (group.getUsers() == null) {
            return false;
        }
        return true;
    }

    public static boolean checkIntegrity(CategoryAndURL category) {
        if (category.getUrl() == null) {
            return false;
        }
        if (category.getCategory().getName() == null) {
            return false;
        }
        return true;
    }
}
