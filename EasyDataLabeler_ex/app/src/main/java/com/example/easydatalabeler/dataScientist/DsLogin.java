package com.example.easydatalabeler.dataScientist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.easydatalabeler.R;
import com.example.easydatalabeler.SignUpActivity;
import com.example.easydatalabeler.dataLabeler.DataLabeler;
import com.example.easydatalabeler.dataLabeler.DlLogin;
import com.example.easydatalabeler.dataScientist.DataScientist;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;


public class DsLogin extends AppCompatActivity {

    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref = database.getReference("server");
    DatabaseReference usersRef = ref.child("users").child("Data Scientist");
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ds_login);
        progressDialog = new ProgressDialog(this);
    }

    public void OnSubmit(View view){
        DataScientist scientist = new DataScientist();
        TextView education = (TextView) findViewById(R.id.education);
        TextView publications = (TextView) findViewById(R.id.publications);
        TextView expertise = (TextView) findViewById(R.id.expertise);
        TextView university = (TextView) findViewById(R.id.university);
        scientist.education = education.getText().toString();
        scientist.publications = publications.getText().toString();
        if(!expertise.getText().toString().equals("")) {
            scientist.expertise = expertise.getText().toString();
        }
        scientist.university = university.getText().toString();
        scientist.email = SignUpActivity.temp.email;
        scientist.name = SignUpActivity.temp.name;
        scientist.phone_no = SignUpActivity.temp.phone_no;
        scientist.password = SignUpActivity.temp.password;
        SaveUserInfoToDatabase(scientist);
        finish();
    }

    private void SaveUserInfoToDatabase(final DataScientist scientist) {
        HashMap<String,Object> productMap = new HashMap<>();
        //image.LabeledBy = LoginDataSource.curr_user.getDisplayName();
        productMap.put("education",scientist.education);
        productMap.put("experience",scientist.expertise);
        productMap.put("name",scientist.name);
        productMap.put("email",scientist.email.replace('.',','));
        productMap.put("phone_no",scientist.phone_no);
        productMap.put("password",scientist.password);
        productMap.put("university",scientist.university);
        productMap.put("publications",scientist.publications);


        usersRef.child(scientist.email.replace('.',',')).updateChildren(productMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    progressDialog.dismiss();
                    Toast.makeText(DsLogin.this, "You have successfully registered as " + scientist.email.replace('.',',') + ". Now login to your account", Toast.LENGTH_SHORT).show();

                    SignUpActivity.kill();
                    finish();
                }
                else{
                    String msg = task.getException().toString();

                }
            }


        });

    }
}
