package com.example.easydatalabeler;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.easydatalabeler.dataLabeler.MainActivityDL;
import com.example.easydatalabeler.dataScientist.AddActivity;
import com.github.chrisbanes.photoview.PhotoView;
import com.github.chrisbanes.photoview.PhotoViewAttacher;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import static com.example.easydatalabeler.dataLabeler.MainActivityDL.curr_image;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
enum State{
    Unselected,
    Rectangle,
    Circle,
    Polygon_first,
    Polygon
}

class MyPhotoAttacher extends PhotoViewAttacher implements View.OnLongClickListener,View.OnTouchListener {
    public MyPhotoAttacher(ImageView imageView) {
        super(imageView);
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        Log.d("Touch","touch happened -"+event.getAction());
        return super.onTouch(view, event);
    }

}

public class ImageEditActivity extends AppCompatActivity implements View.OnClickListener
{
    State Current_state = State.Unselected;
    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private StorageReference imagesStorageRef;
    private DatabaseReference infoStorageRef;
    private ProgressDialog progressDialog;

    private final Runnable mHidePart2Runnable =new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    Button poly_button,rect_button,poly_complete_button, undo_button;
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    FileOutputStream fos = null;
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            //hide();
        }
    };
    private Paint paint = new Paint();
    private Paint paint_translucent_fill = new Paint();
    Canvas tempCanvas,currCanvas;
    Bitmap tempBitmap,myBitmap,currBitmap;
    LinearLayout Labels = null;
    Path path = new Path();
    float init_x=0,init_y = 0,prev_x=0,prev_y=0;
    AlertDialog.Builder builder = null;
    MyPhotoAttacher myPhotoAttacher;
    int selected_label_id = -1;
    public class Label{
        String name;
        int id;
        ArrayList<Point> path = new ArrayList<Point>();
    }
    ArrayList<Label> list_of_labels = new ArrayList<Label>();
    Label curr_label = null;
    PhotoView imageView = null;
    int k=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_edi);
        poly_button = findViewById(R.id.polygon);
        rect_button = findViewById(R.id.rectangle);
        poly_complete_button = findViewById(R.id.finish);
        undo_button = findViewById(R.id.undo);
        poly_complete_button.setVisibility(View.INVISIBLE);
        imageView = (PhotoView) findViewById(R.id.image_view);
        myPhotoAttacher = new MyPhotoAttacher(imageView);
        Intent i = getIntent();
        imagesStorageRef = FirebaseStorage.getInstance().getReference().child("Data Images");
        infoStorageRef = FirebaseDatabase.getInstance().getReference().child("Projects").child(MainActivityDL.curr_project.name);
        progressDialog = new ProgressDialog(this);

        Labels = (LinearLayout) findViewById(R.id.Labels);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(5);
        paint_translucent_fill.setColor(0x330000FF);
        paint_translucent_fill.setStyle(Paint.Style.FILL);
        final float[] height = new float[1];
        final float[] width = new float[1];
        ViewTreeObserver viewTreeObserver = imageView.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    imageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    width[0] = imageView.getMeasuredWidth();
                    height[0] = imageView.getMeasuredHeight();
                }
            });
        }
        width[0] = 1080;height[0] = 1984;
        //To address clicks made on the image view
        View.OnTouchListener image_Listener = new View.OnTouchListener(){
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override

            public boolean onTouch(View v, MotionEvent event) {
                //myPhotoAttacher.onTouch(v,event);
                //original height and width of the bitmap

                final int index = event.getActionIndex();
                PointF p = new PointF(event.getX(), event.getY());
                Matrix m = new Matrix();
                imageView.getImageMatrix().invert(m);
                float[] point = new float[] {p.x,p.y};
                m.mapPoints(point);
                float x = point[0];
                float y = point[1];
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    if(Current_state==State.Polygon||Current_state==State.Rectangle||Current_state==State.Polygon_first) tempCanvas.drawCircle(x, y, 10, paint);
                    if(Current_state==State.Rectangle){
                        init_x = x;
                        init_y = y;
                    }
                    return true;
                }
                if(event.getAction() == MotionEvent.ACTION_UP||event.getAction() == MotionEvent.ACTION_MOVE) {
                    Log.i("MainActivity", String.valueOf(x) + " " +  String.valueOf(y));
                    if(Current_state==State.Polygon_first){
                        path.reset();
                        path.moveTo(x,y);
                        init_x = x;
                        init_y = y;
                        prev_x = x;
                        prev_y = y;
                        Current_state=State.Polygon;
                        Log.i("MainActivity","Current state has been shifted to polygon form");
                        curr_label.path.add(new Point((int)x,(int)y));
                    }
                    else if(Current_state==State.Polygon){
                        path.lineTo(x,y);
                        tempCanvas.drawLine(prev_x,prev_y,x,y,paint);
                        prev_x = x;
                        prev_y = y;
                        curr_label.path.add(new Point((int)x,(int)y));
                    }
                    if(Current_state==State.Rectangle){
                        drawRectangle((int)init_x,(int)init_y,(int) x,(int) y);
                    }
                    imageView.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
                    if(event.getAction() == MotionEvent.ACTION_UP && Current_state==State.Rectangle){
                        curr_label.path.clear();
                        curr_label.path.add(new Point((int)init_x,(int)init_y));
                        curr_label.path.add(new Point((int)x,(int)init_y));
                        curr_label.path.add(new Point((int)x,(int)y));
                        curr_label.path.add(new Point((int)init_x,(int)y));
                        curr_label.id = list_of_labels.size();
                        getLabelName();
                    }
                    return true;

                }

                return false;
            }
        };

        ImageAdapter imageAdapter = new ImageAdapter(this);


        imageView.setOnTouchListener(image_Listener);
        Picasso.get().load(curr_image.downloadUrl).into(new Target() {
            @Override
            public void onBitmapLoaded (final Bitmap bitmap, Picasso.LoadedFrom from){
                /* Save the bitmap or do something with it here */

                //Set it in the ImageView
                myBitmap = bitmap.copy(Bitmap.Config.ARGB_8888,false);
                tempBitmap = myBitmap.copy(Bitmap.Config.ARGB_8888,true);
                currBitmap = myBitmap.copy(Bitmap.Config.ARGB_8888,true);
                tempCanvas = new Canvas(tempBitmap);
                currCanvas = new Canvas(currBitmap);
                //Draw the image bitmap into the cavas
                tempCanvas.drawBitmap(myBitmap, 0, 0, null);

                //Draw everything else you want into the canvas, in this example a rectangle with rounded edges
                //tempCanvas.drawRoundRect(new RectF(x1,y1,x2,y2), 2, 2, myPaint);
                //Attach the canvas to the ImageView
                imageView.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
                //imageView.setImageResource(R.drawable.wallpaper);
                Drawable drawable = imageView.getDrawable();
                Matrix m = imageView.getImageMatrix();
                float scaleFactor = 0;
                if(imageView.getMeasuredHeight()/height[0] < drawable.getIntrinsicWidth()/width[0]) {
                    scaleFactor = width[0]*1.0f/imageView.getDrawable().getIntrinsicWidth();
                }
                else{
                    scaleFactor = height[0]*1.0f/imageView.getDrawable().getIntrinsicHeight();
                }

                Log.i("MainActivity", String.valueOf(scaleFactor));
                m.setTranslate(0,(height[0]*1.0f-imageView.getDrawable().getIntrinsicHeight()*scaleFactor)/2);
                m.preScale(scaleFactor,scaleFactor);
                //imageView.setImageMatrix(m);

            }

            @Override
            public void onBitmapFailed(Exception e,Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }

        });


        Log.i("MainActivity",height[0] + " " + width[0]);
        //Build the dialog box
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private void getLabelName(){
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Label name");

        final String[] m_Text = {""};
        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
        builder.setView(input);
        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                curr_label.name = input.getText().toString();
                Button tempButton = new Button(getApplicationContext());
                rect_button.setBackgroundResource(R.mipmap.ic_rect);
                poly_button.setBackgroundResource(R.mipmap.ic_poly);
                tempButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        selected_label_id = view.getId();
                        updateCurrBitmap();
                    }
                });
                selected_label_id = -1;
                tempButton.setText(input.getText().toString());
                curr_label.id = k++;
                tempButton.setId(curr_label.id);
                Current_state = State.Unselected;
                list_of_labels.add(curr_label);
                undo_button.setVisibility(View.VISIBLE);
                Labels.addView(tempButton, 40 * input.getText().toString().length() + 50, 200);
                updateCurrBitmap();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
    @Override
    public void onClick(View view){}
    private void updateCurrBitmap(){
        currBitmap = myBitmap.copy(Bitmap.Config.ARGB_8888,true);
        currCanvas = new Canvas(currBitmap);
        currCanvas.drawBitmap(myBitmap, 0, 0, null);
        for(int i=0;i<list_of_labels.size();i++){
            Path tempPath = new Path();
            if(list_of_labels.get(i).id==selected_label_id){
                paint_translucent_fill.setColor(0x3300FF00);
            }
            else paint_translucent_fill.setColor(0x330000FF);
            tempPath.moveTo(list_of_labels.get(i).path.get(0).x,list_of_labels.get(i).path.get(0).y);
            currCanvas.drawCircle(list_of_labels.get(i).path.get(0).x,list_of_labels.get(i).path.get(0).y, 10, paint);
            int j=1;
            for(j=1;j<list_of_labels.get(i).path.size();j++){
                tempPath.lineTo(list_of_labels.get(i).path.get(j).x,list_of_labels.get(i).path.get(j).y);
                currCanvas.drawCircle(list_of_labels.get(i).path.get(j).x,list_of_labels.get(i).path.get(j).y, 10, paint);
                currCanvas.drawLine(list_of_labels.get(i).path.get(j-1).x,list_of_labels.get(i).path.get(j-1).y,list_of_labels.get(i).path.get(j).x,list_of_labels.get(i).path.get(j).y,paint);

            }
            currCanvas.drawLine(list_of_labels.get(i).path.get(j-1).x,list_of_labels.get(i).path.get(j-1).y,list_of_labels.get(i).path.get(0).x,list_of_labels.get(i).path.get(0).y,paint);
            tempPath.lineTo(list_of_labels.get(i).path.get(0).x,list_of_labels.get(i).path.get(0).y);
            currCanvas.drawPath(tempPath,paint_translucent_fill);
        }
        tempBitmap = currBitmap.copy(Bitmap.Config.ARGB_8888,true);
        tempCanvas = new Canvas(tempBitmap);
        tempCanvas.drawBitmap(currBitmap, 0, 0, null);
        imageView.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));

        return;
    }

    public void rectangle_click(View view){
        Current_state = State.Rectangle;
        rect_button.setBackgroundResource(R.mipmap.ic_rectangle_selected);
        poly_button.setBackgroundResource(R.mipmap.ic_poly);
        curr_label = new Label();
        Log.i("MainActivity","Current state has been shifted to rectangle form");
        return;
    }

    private void drawRectangle(int x1,int y1,int x2,int y2){
        tempCanvas.drawBitmap(currBitmap,0,0,null);
        path.reset();
        path.moveTo(x1,y1);
        path.lineTo(x2,y1);
        path.lineTo(x2,y2);
        path.lineTo(x1,y2);
        path.lineTo(x1,y1);
        tempCanvas.drawPath(path,paint_translucent_fill);
        tempCanvas.drawLine(x1,y1,x2,y1,paint);
        tempCanvas.drawLine(x2,y1,x2,y2,paint);
        tempCanvas.drawLine(x2,y2,x1,y2,paint);
        tempCanvas.drawLine(x1,y2,x1,y1,paint);
        tempCanvas.drawCircle(x1,y1,10,paint);
        tempCanvas.drawCircle(x1,y2,10,paint);
        tempCanvas.drawCircle(x2,y1,10,paint);
        tempCanvas.drawCircle(x2,y2,10,paint);
        imageView.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
    }

    public void polygon_click(View view){
        Current_state = State.Polygon_first;
        rect_button.setBackgroundResource(R.mipmap.ic_rect);
        poly_button.setBackgroundResource(R.mipmap.ic_rect_selected);

        poly_complete_button.setVisibility(View.VISIBLE);
        curr_label = new Label();
        Log.i("MainActivity","Current state has been shifted to polygon form");
        return;
    }

    public void complete_polygon_click(View view){
        tempCanvas.drawLine(prev_x,prev_y,init_x,init_y,paint);
        path.lineTo(init_x,init_y);
        poly_complete_button.setVisibility(View.INVISIBLE);
        Log.i("MainACtivity","Polygon drawn");
        tempCanvas.drawPath(path,paint_translucent_fill);
        curr_label.id = list_of_labels.size();
        Current_state = State.Polygon_first;
        getLabelName();
    }

    public void undo_click(View view){
        Log.i("MainActivity","Undo button is clicked");
        findViewById(list_of_labels.get(list_of_labels.size()-1).id).setVisibility(View.GONE);
        Labels.removeView(findViewById(list_of_labels.get(list_of_labels.size()-1).id));
        list_of_labels.remove(list_of_labels.size()-1);
        if(list_of_labels.isEmpty()) undo_button.setVisibility(View.INVISIBLE);
        updateCurrBitmap();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void saveFile(View view){
        File file = null;
        try {
            //Text of the Document
            String textToWrite = "bla bla bla";

            //Checking the availability state of the External Storage.
            String state = Environment.getExternalStorageState();
            if (!Environment.MEDIA_MOUNTED.equals(state)) {

                //If it isn't mounted - we can't write into it.
                return;
            }
            progressDialog.setTitle("Uploading label to the image...");
            progressDialog.setMessage("Please have some patience");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            //Create a new file that points to the root directory, with the given name:
            file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "haha.xml");
            file.createNewFile();
            fos = new FileOutputStream(file,false);
            Log.i("MainActivity","File save ho gai kya ?");
            String text_start = "<annotation>\n<filename>1.jpg</filename>\n<folder>Project/</folder>\n<source>\n<submittedBy>Dvij</submittedBy>\n</source>\n<imageSize>\n<nrows>" + myBitmap.getHeight() + "</nrows>\n<ncols>" + myBitmap.getWidth() + "</ncols>\n</imageSize>\n";
            String text_mainbody = "";
            for(int i=0;i<list_of_labels.size();i++){
                text_mainbody += "<object>\n<name>" + list_of_labels.get(i).name + "</name>\n<deleted>0</deleted>\n<verified>0</verified>\n<occluded>True</occluded>\n<attributes/>\n<parts>\n<hasparts/>\n<ispartof/>\n</parts>\n";
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date today = Calendar.getInstance().getTime();
                text_mainbody += "<date>" + dateFormat.format(today) + "</date>\n<id>" + i + "</id>\n<polygon>\n<username>anonymous</username>\n";
                for(int j=0;j<list_of_labels.get(i).path.size();j++){
                    text_mainbody += "<pt>\n<x>" + list_of_labels.get(i).path.get(j).x + "</x>\n<y>" + list_of_labels.get(i).path.get(j).y + "</y>\n</pt>\n";
                }
                text_mainbody+="</polygon>\n</object>\n";
            }
            String text_conclusion = "</annotation>";
            String text = text_start + text_mainbody + text_conclusion;
            fos.write(text.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            if(fos!=null){
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Uri file_u = Uri.fromFile(file);

        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getPath() + "/haha.xml";
        curr_image.location = Uri.parse(path);
        curr_image.filePath = imagesStorageRef.child(curr_image.key + ".xml");
        final UploadTask uploadTask = curr_image.filePath.putFile(file_u);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String message = e.toString();
                Toast.makeText(ImageEditActivity.this,"Error : " + message,Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if(!task.isSuccessful()){
                            progressDialog.dismiss();
                            throw task.getException();

                        }
                        curr_image.labelDownloadUrl = curr_image.filePath.getDownloadUrl().toString();
                        return curr_image.filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            curr_image.labelDownloadUrl = task.getResult().toString();
                            UpdateImageInfoToDatabase(curr_image);

                        }

                    }
                });
            }
        });

    }
    private void UpdateImageInfoToDatabase(Image image) {
        HashMap<String,Object> productMap = new HashMap<>();
        //image.LabeledBy = LoginDataSource.curr_user.getDisplayName();
        image.isLabeled = false;
        productMap.put("LabeledBy",image.LabeledBy);
        productMap.put("downloadUrl",image.downloadUrl);
        productMap.put("labelDownloadUrl",image.labelDownloadUrl);
        productMap.put("isLabeled",false);
        productMap.put("key",image.key);
        productMap.put("Time_of_creation",image.Time_of_creation);
        infoStorageRef.child(image.key).updateChildren(productMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    progressDialog.dismiss();
                    finish();
                }
                else{
                    String msg = task.getException().toString();
                    Toast.makeText(ImageEditActivity.this,"Error : " + msg,Toast.LENGTH_SHORT).show();
                }
            }


        });

    }
}
