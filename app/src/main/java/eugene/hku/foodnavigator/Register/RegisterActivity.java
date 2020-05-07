package eugene.hku.foodnavigator.Register;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import eugene.hku.foodnavigator.Login.LoginActivity;
import eugene.hku.foodnavigator.R;
import eugene.hku.foodnavigator.dataClass.User;
import eugene.hku.foodnavigator.helper.InputValidation;

public class RegisterActivity extends AppCompatActivity {

    private RegisterViewModel registerViewModel;
    private FirebaseFirestore db;
    private InputValidation inputValidation;
    private User user = new User();
    private Boolean exist = false;

    private Intent loginIntent;

    //UI
    private TextInputEditText loginEditText;
    private TextInputEditText pwEditText;
    private TextInputEditText confirmEditText;
    private Button regButton;
    private TextInputLayout textInputLayoutLogin;
    private TextInputLayout textInputLayoutConfirm;
    private TextInputLayout textInputLayoutPW;
    private NestedScrollView nestedScrollView;
    private AppCompatTextView loginLink;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // init ViewModel
        registerViewModel = ViewModelProviders.of(this).get(RegisterViewModel.class);

        inputValidation = new InputValidation(this);

        loginIntent = new Intent(this, LoginActivity.class);

        //UI
        loginEditText = findViewById(R.id.textInputEditTextLoginName);
        pwEditText = findViewById(R.id.textInputEditTextPassword);
        confirmEditText = findViewById(R.id.textInputEditTextConfirmPassword);
        regButton = findViewById(R.id.appCompatButtonRegister);
        textInputLayoutLogin = findViewById(R.id.textInputLayoutLoginName);
        textInputLayoutConfirm= findViewById(R.id.textInputLayoutConfirmPassword);
        textInputLayoutPW= findViewById(R.id.textInputLayoutPassword);
        nestedScrollView = findViewById(R.id.nestedScrollView);
        loginLink = findViewById(R.id.appCompatTextViewLoginLink);

        //initialise firestore
        db = FirebaseFirestore.getInstance();

        loginEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                registerViewModel.setLogin(charSequence.toString());
            }

            @Override public void afterTextChanged(Editable editable) {
            }
        });
        pwEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                registerViewModel.setPassword(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        confirmEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                registerViewModel.setConfirmPW(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CollectionReference users = db.collection("User");
                Query query = users.whereEqualTo("userName",loginEditText.getText().toString().trim());
                // retrieve  query results asynchronously using query.get()
                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                exist = true;
                            }
                            postDataToDB();
                        }

                    }
                });
            }
        });

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(loginIntent);
                finish();
            }
        });



    }

    private void postDataToDB() {
        if (!inputValidation.isInputEditTextFilled(loginEditText, textInputLayoutLogin, "Please input username")) {
            return;
        }
        if (!inputValidation.isInputEditTextFilled(pwEditText, textInputLayoutPW, "Please input password")) {
            return;
        }
        if (!inputValidation.isInputEditTextMatches(pwEditText, confirmEditText,
                textInputLayoutConfirm, "Please confirm password")) {
            return;
        }
        if (!exist) { // app stopping running this
            user.setUserName(loginEditText.getText().toString().trim());
            user.setPassword(pwEditText.getText().toString().trim());
            user.setUserName(loginEditText.getText().toString().trim()); // Username equals to login name by default
            String userRefID = db.collection("User").document().getId(); // The new id
            db.collection("User").add(user).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    // Snack Bar to show success message that record saved successfully
                    db.collection("User").document(documentReference.getId()).update("userID",documentReference.getId());
                    Snackbar.make(nestedScrollView, "Registered successfully", Snackbar.LENGTH_LONG).show();
                    emptyInputEditText();


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Snackbar.make(nestedScrollView, "Registration failed", Snackbar.LENGTH_LONG).show();
                }
            });


        } else {
            // Snack Bar to show error message that record already exists
            Snackbar.make(nestedScrollView, "User Exists", Snackbar.LENGTH_LONG).show();
        }
    }

    private void emptyInputEditText() {
        loginEditText.setText(null);
        pwEditText.setText(null);
        confirmEditText.setText(null);
    }


}
