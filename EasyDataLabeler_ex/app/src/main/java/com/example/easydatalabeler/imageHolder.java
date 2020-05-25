package com.example.easydatalabeler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ScrollView;

public class imageHolder extends ScrollView implements View.OnClickListener{
    public ImageView image = null;
    public CheckedTextView isLabelled = null;
    public imageHolder(Context context) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.image_dl_layout, this, true);
        image = findViewById(R.id.image_view);
        isLabelled = findViewById(R.id.islabelled);
    }

    @Override
    public void onClick(View view){
        super.callOnClick();
    }
}
