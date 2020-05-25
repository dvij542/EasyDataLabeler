package com.example.easydatalabeler.data;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.easydatalabeler.dataLabeler.DataLabeler;
import com.example.easydatalabeler.dataScientist.DataScientist;
import com.example.easydatalabeler.R;
import com.example.easydatalabeler.UsernameIncorrect;
import com.example.easydatalabeler.data.model.LoggedInUser;
import com.example.easydatalabeler.dataScientist.ui.login.LoginFormState;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;

import static com.example.easydatalabeler.dataScientist.ui.login.LoginViewModel.loginFormState;

import static com.example.easydatalabeler.dataScientist.ui.login.LoginActivity.acc_type_dl;

public class LoginDataSource {
    static public LoggedInUser curr_user = null;
    public Result<LoggedInUser> login(final String username, final String password) {

        try {
            final DataScientist[] required_user_ds = {null};
            final DataLabeler[] required_user_dl = {null};
            final boolean[] usernamIncorrect = {false,false};
            // TODO: handle loggedInUser authentication
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("server");
            DatabaseReference usersRef = ref.child("users");
            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(acc_type_dl==false){
                        if(dataSnapshot.child("Data Scientist").child(username).exists()){
                            if(dataSnapshot.child("Data Scientist").child(username).child("password").getValue().equals(password)){
                                required_user_ds[0] = (DataScientist) dataSnapshot.child("Data Scientist").child(username).getValue(DataScientist.class);
                                Log.i("MainActivity","Sahi hai password");
                            }
                            else usernamIncorrect[1] = true;
                        }
                        else{
                            Log.i("MainActivity","achha yeh ho rha hai ab samja");
                            usernamIncorrect[0] = true;
                        }
                    }
                    else{
                        if(dataSnapshot.child("Data Labeler").child(username).exists()){
                            if(dataSnapshot.child("Data Labeler").child(username).child("password").getValue().equals(password)){
                                required_user_dl[0] = (DataLabeler) dataSnapshot.child("Data Labeler").child(username).getValue(DataLabeler.class);
                                Log.i("MainActivity","Sahi hai password");
                            }
                            else usernamIncorrect[1] = true;
                        }
                        else{
                            Log.i("MainActivity","achha yeh ho rha hai ab samja");
                            usernamIncorrect[0] = true;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            if(usernamIncorrect[0]){
                Log.i("MainActivity","achha yeh ho rha hai ab samja");
                loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
                throw new UsernameIncorrect("Username is incorrect");
            }

                // For Data labeler
                // For Data scientist
            if(acc_type_dl==false) {
                if (usernamIncorrect[1]) {
                    Log.i("MainActivity", "Abe, password galat hai");
                    loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
                    throw new UsernameIncorrect("Username and password doesn't match");

                } else {
                    Log.i("MainActivity", "Chalo ab password to sahi hai, logged in as data scientist");
                    curr_user = new LoggedInUser(required_user_ds[0].email, required_user_ds[0].name, required_user_ds[0]);
                }

            }
            else{
                if (usernamIncorrect[1]) {
                    Log.i("MainActivity", "Abe, password galat hai");
                    loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
                    throw new UsernameIncorrect("Username and password doesn't match");

                } else {
                    Log.i("MainActivity", "Chalo ab password to sahi hai, logged in as data scientist");
                    curr_user = new LoggedInUser(required_user_dl[0].email, required_user_dl[0].name, required_user_dl[0]);
                }
            }

            return new Result.Success<>(curr_user);
        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }
}
