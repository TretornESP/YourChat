package tretornesp.clickerchat3;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    private Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_layout);

        mContext = getApplicationContext();

        loadRegisterButton();
    }

    @Override
    public void onBackPressed() {
        return;
    }

    private void loadRegisterButton() {
        Button button = findViewById(R.id.register_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                create();
                register();
            }
        });
    }


    private void create() {
        EditText text = findViewById(R.id.register_name);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String name = text.getText().toString();
        String status = "";
        String location = "";
        String imageURL = "";
        ArrayList<String> groups = new ArrayList<>();
        ArrayList<String> categories = new ArrayList<>();

        ActiveUser.activeuser = new User(uid, name, imageURL, status, location, groups, categories);
    }

    private void register() {
        Map<String, Object> parsed = new HashMap<>();
        parsed.put("categories", ActiveUser.activeuser.getCategories());
        parsed.put("groups", ActiveUser.activeuser.getGroups());
        parsed.put("imageURL", ActiveUser.activeuser.getImageURL());
        parsed.put("location", ActiveUser.activeuser.getLocation());
        parsed.put("name", ActiveUser.activeuser.getName());
        parsed.put("status", ActiveUser.activeuser.getStatus());

        User tmp_user = new User(
                ActiveUser.activeuser.getUid(),
                ActiveUser.activeuser.getName(),
                ActiveUser.activeuser.getImageURL(),
                ActiveUser.activeuser.getStatus(),
                ActiveUser.activeuser.getLocation(),
                ActiveUser.activeuser.getGroups(),
                ActiveUser.activeuser.getCategories()
        );

        if (FirestoreLink.checkIntegrity(tmp_user)) {

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("users").document(ActiveUser.activeuser.getUid())
                    .set(parsed)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            new AlertDialog.Builder(getApplicationContext())
                                    .setIcon(R.drawable.image_error)
                                    .setTitle("Error")
                                    .setMessage("El usuario no se ha creado por un error desconocido, inténtelo de nuevo mas tarde")
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    })
                                    .show();
                        }
                    });
        }
    }
}
