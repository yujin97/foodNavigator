package eugene.hku.foodnavigator.Login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.widget.NestedScrollView;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import eugene.hku.foodnavigator.MainActivity;
import eugene.hku.foodnavigator.R;
import eugene.hku.foodnavigator.Register.RegisterActivity;
import eugene.hku.foodnavigator.dataClass.User;
import eugene.hku.foodnavigator.helper.InputValidation;

public class LoginActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private Intent intent;
    private Intent registerIntent;

    private LoginViewModel loginViewModel;
    private InputValidation inputValidation;
    private String userId;
    private String userName;
    private User user;

    //UI
    TextInputEditText loginEditText;
    TextInputEditText pwEditText;
    Button loginButton;
    TextInputLayout textInputLayoutLogin;
    TextInputLayout textInputLayoutPassword;
    NestedScrollView nestedScrollView;
    AppCompatTextView registerLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        intent = new Intent(this, MainActivity.class);
        registerIntent = new Intent(this,RegisterActivity.class);

        // initialise firestore
        db = FirebaseFirestore.getInstance();

        //validation
        inputValidation = new InputValidation(this);

        //UI
        loginEditText = findViewById(R.id.textInputEditTextLogin);
        pwEditText = findViewById(R.id.textInputEditTextPassword);
        loginButton = findViewById(R.id.appCompatButtonLogin);
        textInputLayoutLogin = findViewById(R.id.textInputLayoutLogin);
        textInputLayoutPassword = findViewById(R.id.textInputLayoutPassword);
        nestedScrollView = findViewById(R.id.nestedScrollView);
        registerLink = findViewById(R.id.textViewLinkRegister);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyFromDB();
            }
        });
        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(registerIntent);
                finish();
            }
        });


    }

    private void emptyInputEditText() {
        loginEditText.setText(null);
        pwEditText.setText(null);

    }

    private void verifyFromDB() {
        if (!inputValidation.isInputEditTextFilled(loginEditText, textInputLayoutLogin, "Please input username!")) {
            return;
        }
        if (!inputValidation.isInputEditTextFilled(pwEditText, textInputLayoutPassword, "Pleases input password!")) {
            return;
        }

        CollectionReference users = db.collection("User");

        Query query = users.whereEqualTo("userName",loginEditText.getText().toString().trim()).whereEqualTo("password",pwEditText.getText().toString().trim());

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    if (!task.getResult().isEmpty()){
                        for (QueryDocumentSnapshot document : task.getResult()) {

                            user = document.toObject(User.class);
                            SharedPreferences.Editor editor = getSharedPreferences("userInfo", MODE_PRIVATE).edit();

                            userId = user.getUserID();
                            userName = user.getUserName();

                            editor.putBoolean("IsLogin", true);
                            editor.putString("userID", userId);
                            editor.putString("userName", userName);
                            editor.apply();

                            startActivity(intent);
                            finish();


                        }
                    }
                    else {
                        // Snack Bar to show success message that record is wrong
                        Log.d("User","No such user");
                        Snackbar.make(nestedScrollView, "User does not exist!", Snackbar.LENGTH_LONG).show();
                    }
                }
                else {
                    Log.d("FireStore", "Error getting documents: ", task.getException());
                }
            }
        });

    }
}
