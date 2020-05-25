package com.example.easydatalabeler;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.View;

public class LabelEdit extends View {
    public LabelEdit(Context context){
        super(context);
        //setBackgroundResource(R.drawable.wallpaper);
        Drawable myDrawable = getResources().getDrawable(R.drawable.wallpaper);
        setBackground(myDrawable);
    }
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
    }
}
