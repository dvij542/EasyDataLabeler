package com.example.easydatalabeler;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.easydatalabeler.dataLabeler.DlLogin;
import com.example.easydatalabeler.dataScientist.DsLogin;


public class SignUpActivity extends AppCompatActivity {
    public static Person temp = new Person();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        Log.i("MainActivity","Yaha to aa gaya");
    }

    public void onSubmit(View view){
        TextView name = (TextView) findViewById(R.id.Name);
        TextView phone_no = (TextView) findViewById(R.id.number);
        TextView email = (TextView) findViewById(R.id.username);
        TextView password = (TextView) findViewById(R.id.password);

        temp.name = (String) name.getText().toString();
        temp.phone_no = (String) phone_no.getText().toString();
        temp.email = (String) email.getText().toString();
        temp.password = (String) password.getText().toString();
        RadioGroup grp = (RadioGroup) findViewById(R.id.group);
        int itype = grp.getCheckedRadioButtonId();
        RadioButton acc_type = (RadioButton) findViewById(itype);

        if(acc_type.getText().equals("Data Labeler")) {
            Intent intent = new Intent(SignUpActivity.this, DlLogin.class);
            startActivity(intent);
            Log.i("MainActivity", "Registered as data labeler");
            finish();
        }
        if(acc_type.getText().equals("Data Scientist")){
            Intent intent = new Intent(SignUpActivity.this, DsLogin.class);
            startActivity(intent);
            Log.i("MainActivity", "Registered as data scientist");
            finish();
        }
    }

    public static void kill(){

    }


}