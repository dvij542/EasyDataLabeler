package com.example.easydatalabeler;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class trial extends AppCompatActivity {
    LabelEdit labelEdit;
    State Current_state = State.Unselected;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        labelEdit = new LabelEdit(this);
        setContentView(labelEdit);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    public void rectangle_click(MenuItem item){
        Current_state = State.Rectangle;
        Log.i("MainActivity","Current state has been shifted to rectangle form");
        return;
    }

    public void polygon_click(MenuItem item){
        Current_state = State.Polygon;
        Log.i("MainActivity","Current state has been shifted to polygon form");
        return;
    }
}
