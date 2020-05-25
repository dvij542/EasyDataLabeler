package com.example.easydatalabeler.dataScientist.ui.login;

import android.Manifest;
import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.example.easydatalabeler.SignUpActivity;
import com.example.easydatalabeler.UsernameIncorrect;
import com.example.easydatalabeler.data.Result;
import com.example.easydatalabeler.data.model.LoggedInUser;
import com.example.easydatalabeler.dataLabeler.DataLabeler;
import com.example.easydatalabeler.dataLabeler.MainActivityDL;
import com.example.easydatalabeler.dataScientist.AddActivity;
import com.example.easydatalabeler.dataScientist.DataScientist;
import com.example.easydatalabeler.LabelImageActivity;
import com.example.easydatalabeler.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.example.easydatalabeler.data.LoginDataSource.curr_user;
import static com.example.easydatalabeler.dataScientist.ui.login.LoginViewModel.loginFormState;


public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    public static boolean acc_type_dl;
    public static DataLabeler curr_user_dl = new DataLabeler();
    public static DataScientist curr_user_ds = new DataScientist();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("MainActivity", "Yaha to aa gaya");
        setContentView(R.layout.activity_login);
        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        isStoragePermissionGranted();
        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login2);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);


        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());
                }
                setResult(Activity.RESULT_OK);

                //Complete and destroy login activity once successful
                finish();
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.login(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString());
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioGroup grp = (RadioGroup) findViewById(R.id.group);
                int itype = grp.getCheckedRadioButtonId();
                RadioButton acc_type = (RadioButton) findViewById(itype);
                Log.i("MainActivity", acc_type.getText().toString());
                if (acc_type.getText().equals("Data Labeler")) {
                    acc_type_dl = true;

                } else if (acc_type.getText().equals("Data Scientist")) {
                    acc_type_dl = false;
                } else {
                    return;
                }
                loadingProgressBar.setVisibility(View.VISIBLE);

                final DataScientist[] required_user_ds = {null};
                final DataLabeler[] required_user_dl = {null};
                final boolean[] usernamIncorrect = {false, false};
                // TODO: handle loggedInUser authentication
                final FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference ref = database.getReference("server");
                DatabaseReference usersRef = ref.child("users");
                final String username = usernameEditText.getText().toString();
                final String password = passwordEditText.getText().toString();
                usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (acc_type_dl == false) {
                            if (dataSnapshot.child("Data Scientist").child(username.replace('.',',')).exists()) {
                                if (dataSnapshot.child("Data Scientist").child(username.replace('.',',')).child("password").getValue().toString().equals(password)) {
                                    required_user_ds[0] = (DataScientist) dataSnapshot.child("Data Scientist").child(username.replace('.',',')).getValue(DataScientist.class);
                                    Log.i("MainActivity", "Sahi hai password");
                                    Log.i("MainActivity", "Chalo ab password to sahi hai, logged in as data scientist");
                                    curr_user_ds = required_user_ds[0];
                                    Intent intent = new Intent(LoginActivity.this,AddActivity.class);
                                    loadingProgressBar.setVisibility(View.INVISIBLE);

                                    startActivity(intent);
                                } else {
                                    Log.i("MainActivity", "Abe, password galat hai");
                                    loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
                                    loadingProgressBar.setVisibility(View.INVISIBLE);

                                    return;
                                }
                            } else {
                                Log.i("MainActivity", "achha yeh ho rha hai ab samja");
                                loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
                                loadingProgressBar.setVisibility(View.INVISIBLE);
                                return;
                            }
                        } else {
                            if (dataSnapshot.child("Data Labeler").child(username.replace('.',',')).exists()) {
                                if (dataSnapshot.child("Data Labeler").child(username.replace('.',',')).child("password").getValue().toString().equals(password)) {
                                    required_user_dl[0] = (DataLabeler) dataSnapshot.child("Data Labeler").child(username.replace('.',',')).getValue(DataLabeler.class);
                                    curr_user_dl.name = required_user_dl[0].name;
                                    curr_user_dl.email = required_user_dl[0].email;
                                    Intent intent = new Intent(LoginActivity.this,MainActivityDL.class);
                                    loadingProgressBar.setVisibility(View.INVISIBLE);
                                    startActivity(intent);
                                } else {
                                    Log.i("MainActivity", "Abe, password galat hai");
                                    loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
                                    loadingProgressBar.setVisibility(View.INVISIBLE);
                                    return;
                                }

                            }
                            else{
                                Log.i("MainActivity", "achha yeh ho rha hai ab samja");
                                loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
                                loadingProgressBar.setVisibility(View.INVISIBLE);
                                return;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        // TODO : initiate successful logged in experience
        Intent intent = new Intent(LoginActivity.this, LabelImageActivity.class);
        startActivity(intent);
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    public void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    public void signup_click(View view){
        Log.i("MainActivity","Yaha to aa gaya");
        //Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
        Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivity(intent);
    }
    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.v("MainActivity","Permission is granted");
                return true;
            } else {

                Log.v("MainActivity","Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("MainActivity","Permission is granted");
            return true;
        }
    }
}
