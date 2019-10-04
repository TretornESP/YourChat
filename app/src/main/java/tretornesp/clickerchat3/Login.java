package tretornesp.clickerchat3;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.xwray.passwordview.PasswordView;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Login extends AppCompatActivity{

    private FirebaseAuth mAuth;
    private EditText mail, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        mAuth = FirebaseAuth.getInstance();

        loadMailPassword();
        loadToolbar();
        loadLogin();
        loadRegister();
    }

    @Override
    public void onBackPressed() {
        return;
    }

    private void loadMailPassword() {
        mail = findViewById(R.id.login_email);
        password = findViewById(R.id.login_password);
    }
    private void loadRegister() {
        TextView register = findViewById(R.id.login_register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, NewUser.class);
                startActivity(intent);
            }
        });
    }

    private void loadLogin() {

        Button login = findViewById(R.id.login_login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mail_string = mail.getText().toString();
                String password_string = password.getText().toString();

                if (!isEmailValid(mail_string)) {
                    mail.getBackground().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
                    return;
                }
                if (isStringNullOrWhiteSpace(password_string)) {
                    password.getBackground().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
                    return;
                }
                mAuth.signInWithEmailAndPassword(mail_string, password_string)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Intent intent = new Intent(Login.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    finish();
                                    startActivity(intent);
                                } else {
                                    mail.getBackground().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
                                    password.getBackground().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
                                }
                            }
                        });
            }
        });
    }

    private void loadToolbar() {
        Objects.requireNonNull(getSupportActionBar()).setTitle("Iniciar sesión");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
    }

    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
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


}
