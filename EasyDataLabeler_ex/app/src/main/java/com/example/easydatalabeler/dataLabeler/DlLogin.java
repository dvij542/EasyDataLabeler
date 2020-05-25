package com.example.easydatalabeler.dataLabeler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.easydatalabeler.Image;
import com.example.easydatalabeler.R;
import com.example.easydatalabeler.SignUpActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;


public class DlLogin extends AppCompatActivity {
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref = database.getReference("server");
    DatabaseReference usersRef = ref.child("users").child("Data Labeler");
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dl_login);
        progressDialog = new ProgressDialog(this);

    }



    public void OnSubmit(View view) {
        DataLabeler labeler = new DataLabeler();
        TextView education = (TextView) findViewById(R.id.education);
        TextView experience = (TextView) findViewById(R.id.publications);
        TextView expertise = (TextView) findViewById(R.id.expertise);
        labeler.education = education.getText().toString();
        labeler.experience = experience.getText().toString();
        if (!expertise.getText().toString().equals(""))
            labeler.expertise = expertise.getText().toString();
        labeler.email = SignUpActivity.temp.email;
        labeler.name = SignUpActivity.temp.name;
        labeler.phone_no = SignUpActivity.temp.phone_no;
        labeler.password = SignUpActivity.temp.password;
        Log.i("MainActivity",labeler.education);
        Log.i("MainActivity",labeler.experience);
        progressDialog.setTitle("Updating User info...");
        progressDialog.setMessage("Please have some patience");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        SaveUserInfoToDatabase(labeler);

    }

    private void SaveUserInfoToDatabase(final DataLabeler labeler) {
        HashMap<String,Object> productMap = new HashMap<>();
        //image.LabeledBy = LoginDataSource.curr_user.getDisplayName();
        productMap.put("education",labeler.education);
        productMap.put("experience",labeler.experience);
        productMap.put("name",labeler.name);
        productMap.put("email",labeler.email.replace('.',','));
        productMap.put("phone_no",labeler.phone_no);
        productMap.put("password",labeler.password);
        productMap.put("expertise",labeler.expertise);


        usersRef.child(labeler.email.replace('.',',')).updateChildren(productMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    progressDialog.dismiss();
                    Toast.makeText(DlLogin.this, "You have successfully registered as " + labeler.email.replace('.',',') + ". Now login to your account", Toast.LENGTH_SHORT).show();
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

