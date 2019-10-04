package tretornesp.clickerchat3;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.xwray.passwordview.PasswordView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewUser extends AppCompatActivity {

    private static final int PASSWORD_MIN_LENGHT = 8;

    private Calendar myCalendar = Calendar.getInstance();
    private EditText pickDate;
    private ColorStateList oldColors;
    private FirebaseAuth auth;
    private Context mContext;
    private boolean nameless;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_user_layout);

        mContext = this;
        auth = FirebaseAuth.getInstance();

        saveColor();
        loadDatePicker();
        loadLink();
        loadButton();
        loadToolbar();
    }

    private void loadToolbar() {
        Objects.requireNonNull(getSupportActionBar()).setTitle("Registro");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    private void loadDatePicker() {
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };

        pickDate = findViewById(R.id.new_user_date);
        pickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(NewUser.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void updateLabel() {
        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());

        pickDate.setText(sdf.format(myCalendar.getTime()));
    }

    private void loadLink() {
        TextView link = findViewById(R.id.new_user_link);
        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateTerms();
            }
        });
    }

    private void saveColor() {
        TextView terms = findViewById(R.id.new_user_link);
        oldColors =  terms.getTextColors();
    }

    private void loadButton() {
        final Button create = findViewById(R.id.new_user_create);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                create();
            }
        });
    }

    private void create() {

        EditText username = findViewById(R.id.new_user_username);
        EditText mail = findViewById(R.id.new_user_mail);
        PasswordView password = findViewById(R.id.new_user_password);
        PasswordView password_confirmation = findViewById(R.id.new_user_password_confirmation);
        EditText phone = findViewById(R.id.new_user_phone);
        EditText country = findViewById(R.id.new_user_county);
        EditText pc = findViewById(R.id.new_user_pc);
        EditText birthday = findViewById(R.id.new_user_date);
        CheckBox accept = findViewById(R.id.new_user_check);
        TextView terms = findViewById(R.id.new_user_link);

        String username_string  = username.getText().toString();
        String mail_string = mail.getText().toString();
        String password_string = password.getText().toString();
        String password_confirmation_string = password.getText().toString();
        String phone_string = phone.getText().toString();
        String county_string = country.getText().toString();
        String pc_string = pc.getText().toString();
        String birthday_string = birthday.getText().toString();

        username.getBackground().setColorFilter(getResources().getColor(R.color.mainColor), PorterDuff.Mode.SRC_IN);
        mail.getBackground().setColorFilter(getResources().getColor(R.color.mainColor), PorterDuff.Mode.SRC_IN);
        password.getBackground().setColorFilter(getResources().getColor(R.color.mainColor), PorterDuff.Mode.SRC_IN);
        password_confirmation.getBackground().setColorFilter(getResources().getColor(R.color.mainColor), PorterDuff.Mode.SRC_IN);
        phone.getBackground().setColorFilter(getResources().getColor(R.color.mainColor), PorterDuff.Mode.SRC_IN);
        country.getBackground().setColorFilter(getResources().getColor(R.color.mainColor), PorterDuff.Mode.SRC_IN);
        pc.getBackground().setColorFilter(getResources().getColor(R.color.mainColor), PorterDuff.Mode.SRC_IN);
        birthday.getBackground().setColorFilter(getResources().getColor(R.color.mainColor), PorterDuff.Mode.SRC_IN);

        terms.setTextColor(oldColors);

        boolean valid = true;

        if (isStringNullOrWhiteSpace(username_string)) {
            phone.getBackground().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
            valid = false;
        }

        if (isStringNullOrWhiteSpace(mail_string) || !isEmailValid(mail_string)) {
            phone.getBackground().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
            valid = false;
        }

        if (isStringNullOrWhiteSpace(password_string)) {
            password.getBackground().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
            valid = false;
        } else if (password_string.length() < PASSWORD_MIN_LENGHT) {
            LinearLayout layout = findViewById(R.id.new_user);
            if (layout!=null) {
                Snackbar.make(layout, "Las contraseñas deben tener un mínimo de " + PASSWORD_MIN_LENGHT + "caracteres", Snackbar.LENGTH_LONG)
                        .show();
                password.getBackground().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
                valid = false;
            }
        }

        if (!password_confirmation_string.equals(password_string)) {
            password_confirmation.getBackground().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
            valid = false;
        }

        if (isStringNullOrWhiteSpace(phone_string)) {
            phone.getBackground().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
            valid = false;
        }

        if (isStringNullOrWhiteSpace(county_string)) {
            country.getBackground().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
            valid = false;
        }

        if (isStringNullOrWhiteSpace(pc_string)) {
            pc.getBackground().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
            valid = false;
        }

        if (isStringNullOrWhiteSpace(birthday_string)) {
            birthday.getBackground().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
            valid = false;
        }

        if (!accept.isChecked()) {
            terms.setTextColor(getResources().getColor(R.color.colorAccent));
            valid = false;
        }

        if (valid) {
            create_user(username_string, mail_string, password_string, phone_string , county_string, pc_string, birthday_string);
        }
    }

    private static boolean isStringNullOrWhiteSpace(String value) {
        if (value == null) {
            return true;
        }

        for (int i = 0; i < value.length(); i++) {
            if (!Character.isWhitespace(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private void create_user(final String username, final String mail, final String password, final String phone, final String country, final String pc, final String birthday) {
        auth.createUserWithEmailAndPassword(mail, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            nameless = false;
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username).build();
                            auth = FirebaseAuth.getInstance();
                            Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        create_user_data(username, mail, password, phone , country, pc, birthday);

                                    } else {
                                        nameless = true;
                                    }
                                }
                            });
                        } else {
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                new AlertDialog.Builder(mContext)
                                        .setIcon(R.drawable.user_already_exists)
                                        .setTitle("Error")
                                        .setMessage("El usuario ya existe, introduzca otro email")
                                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        })
                                        .show();
                            }
                        }
                    }
                });
    }

    private void create_user_data(String username, String mail, String password, String phone, String country, String pc, String birthday) {

        if (auth == null || auth.getCurrentUser() == null || auth.getCurrentUser().getDisplayName()==null && !nameless) {
            System.err.println("LINEA 274: algo es null");
            new AlertDialog.Builder(this)
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
        } else {
            String name = Objects.requireNonNull(auth.getCurrentUser()).getDisplayName();

            Map<String, Object> parsed = new HashMap<>();
            parsed.put("name", name);
            parsed.put("mail", auth.getCurrentUser().getEmail());
            parsed.put("phone", phone);
            parsed.put("country", country);
            parsed.put("pc", pc);
            parsed.put("birthday", birthday);

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("user_data").document(auth.getCurrentUser().getUid())
                    .set(parsed)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            register();
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
                                        }
                                    })
                                    .show();
                        }
                    });
        }
    }

    private void register() {

        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        ActiveUser.activeuser = new User(currentFirebaseUser.getUid(), currentFirebaseUser.getDisplayName(), "", "", "", new ArrayList<String>(), new ArrayList<String>());

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
                            Intent intent = new Intent(getApplicationContext(), CreateUser.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            e.printStackTrace();
                            new AlertDialog.Builder(getApplicationContext())
                                    .setIcon(R.drawable.image_error)
                                    .setTitle("Error")
                                    .setMessage("El usuario no se ha creado por un error desconocido, inténtelo de nuevo mas tarde")
                                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            System.exit(0);
                                        }
                                    })
                                    .show();
                        }
                    });
        }
    }

    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private void navigateTerms() {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference docRef = db.collection("urls").document("urls");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String terms = document.getString("terms");
                        Intent browse = new Intent( Intent.ACTION_VIEW , Uri.parse( terms ) );
                        startActivity( browse );
                    }
                }
            }
        });
    }
}