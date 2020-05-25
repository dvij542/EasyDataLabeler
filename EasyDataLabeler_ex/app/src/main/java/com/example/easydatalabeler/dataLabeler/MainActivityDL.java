package com.example.easydatalabeler.dataLabeler;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.easydatalabeler.Image;
import com.example.easydatalabeler.ImageEditActivity;
import com.example.easydatalabeler.Project;
import com.example.easydatalabeler.R;
import com.example.easydatalabeler.dataScientist.AddActivity;
import com.example.easydatalabeler.dataScientist.ui.home.ImageDSHolder;
import com.example.easydatalabeler.dataScientist.ui.login.LoginActivity;
import com.example.easydatalabeler.projectHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class MainActivityDL extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private StorageReference imagesStorageRef;
    private DatabaseReference infoStorageRef;
    private ProgressDialog progressDialog;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    public static Image curr_image = new Image();
    private static DatabaseReference projectStorageRef;
    private enum mode{
        ProjectSelect ,
        ImageSelect
    }
    private mode curr_mode;
    public static Project curr_project = new Project();

    int count = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_d_l);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        recyclerView = findViewById(R.id.recycler_menu);
        recyclerView.setHasFixedSize(true);
        layoutManager = new GridLayoutManager(getApplicationContext(),3, LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);
        progressDialog = new ProgressDialog(this);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        imagesStorageRef = FirebaseStorage.getInstance().getReference().child("Data Images");
        infoStorageRef = FirebaseDatabase.getInstance().getReference().child("Projects");
        projectStorageRef = FirebaseDatabase.getInstance().getReference().child("ProjectsData");
        NavigationView navigationView = findViewById(R.id.nav_view);
        curr_mode=mode.ProjectSelect;
        Select_project();


        View headerView = navigationView.getHeaderView(0);
        ImageView dp = headerView.findViewById(R.id.profile_picture);
        TextView username = headerView.findViewById(R.id.user_profile_name);
        TextView emailid = headerView.findViewById(R.id.emailid_profile_name);
        username.setText(LoginActivity.curr_user_dl.name);
        emailid.setText(LoginActivity.curr_user_dl.email);
        //username.setText(LoginDataSource.curr_user.getDisplayName());
        //emailid.setText(LoginDataSource.curr_user.getDataScientist().email);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

    }

    public void Select_project(){
        FirebaseRecyclerOptions<Project> options_project = new FirebaseRecyclerOptions.Builder<Project>().setQuery(projectStorageRef, Project.class).build();
        FirebaseRecyclerAdapter<Project, projectHolder> adapter_project = new FirebaseRecyclerAdapter<Project, projectHolder>(options_project) {
            @Override
            protected void onBindViewHolder(@NonNull projectHolder Holder, int i, @NonNull final Project project) {
                Holder.image.setScaleType(ImageView.ScaleType.FIT_CENTER);
                Holder.project_name.setText(project.name);
                Holder.image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        curr_project.name = project.name;
                        curr_project.description = project.description;
                        curr_project.author = project.author;
                        curr_project.Date_of_creation = project.Date_of_creation;
                        Log.i("MainActivity", "This button was pressed ");
                        Access_data_images(project);
                        curr_mode= mode.ImageSelect;
                    }
                });
            }

            @NonNull
            @Override
            public projectHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.folder_layout,parent,false);
                projectHolder imageDSHolder = new projectHolder(view);

                return imageDSHolder;
            }
        };
        recyclerView.setAdapter(adapter_project);
        adapter_project.startListening();
    }
    public void Access_data_images(Project project){
        FirebaseRecyclerOptions<Image> options = new FirebaseRecyclerOptions.Builder<Image>().setQuery(infoStorageRef.child(project.name),Image.class).build();
        FirebaseRecyclerAdapter<Image, ImageDSHolder> adapter = new FirebaseRecyclerAdapter<Image, ImageDSHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ImageDSHolder imageDSHolder, int i, @NonNull final Image image) {
                if(!image.isLabeled) imageDSHolder.isLabelled.setVisibility(View.VISIBLE);
                else imageDSHolder.isLabelled.setVisibility(View.INVISIBLE);
                Picasso.get().load(image.downloadUrl).into(imageDSHolder.image);
                imageDSHolder.image.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageDSHolder.image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i("MainActivity", "This button was pressed ");
                        curr_image.downloadUrl = image.downloadUrl;
                        curr_image.key = image.key;
                        curr_image.Time_of_creation = image.Time_of_creation;
                        curr_image.LabeledBy = image.LabeledBy;
                        Intent intent = new Intent(MainActivityDL.this, ImageEditActivity.class);
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public ImageDSHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_ds_layout,parent,false);
                ImageDSHolder imageDSHolder = new ImageDSHolder(view);

                return imageDSHolder;
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();

    }

    @Override
    protected void onStart() {

        super.onStart();

    }

    public void add_files_click(View view){
        OpenGallery();

    }
    private String downloadUrl;


    void Validate(final Image image, int i){
        if(image.location==null) return;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        image.Time_of_creation = currentDate.format(new Date()).toString();
        image.key = image.Time_of_creation + currentTime.format(new Date()) + i;
        image.filePath = imagesStorageRef.child(image.location.getLastPathSegment() + image.key + ".jpg");
        final UploadTask uploadTask = image.filePath.putFile(image.location);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String message = e.toString();
                Toast.makeText(MainActivityDL.this,"Error : " + message,Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if(!task.isSuccessful()){
                            throw task.getException();
                        }
                        image.downloadUrl = image.filePath.getDownloadUrl().toString();
                        return image.filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            image.downloadUrl = task.getResult().toString();
                            SaveImageInfoToDatabase(image);

                        }
                    }
                });
            }
        });
    }

    private void SaveImageInfoToDatabase(Image image) {
        HashMap<String,Object> productMap = new HashMap<>();
        //image.LabeledBy = LoginDataSource.curr_user.getDisplayName();
        image.isLabeled = true;
        productMap.put("LabeledBy",image.LabeledBy);
        productMap.put("downloadUrl",image.downloadUrl);
        productMap.put("isLabeled",true);
        productMap.put("key",image.key);
        productMap.put("Time_of_creation",image.Time_of_creation);
        infoStorageRef.child(image.key).updateChildren(productMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    count--;
                    if(count<=0) progressDialog.dismiss();
                }
                else{
                    String msg = task.getException().toString();
                    Toast.makeText(MainActivityDL.this,"Error : " + msg,Toast.LENGTH_SHORT).show();
                }
            }


        });

    }
    @Override
    public void onBackPressed(){
        if(curr_mode== mode.ImageSelect){
            curr_mode= mode.ProjectSelect;
            Select_project();
        }
        else if(curr_mode== mode.ProjectSelect)
        {
            super.onBackPressed();
        }
    }
    int GalleryPick = 1;
    void OpenGallery(){
        Intent Galleryintent = new Intent();
        Galleryintent.setAction(Intent.ACTION_GET_CONTENT);
        Galleryintent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        Galleryintent.setType("image/*");

        startActivityForResult(Galleryintent,GalleryPick);
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == GalleryPick) {
            if(resultCode == Activity.RESULT_OK) {
                if(data.getClipData() != null) {
                    count = data.getClipData().getItemCount(); //evaluate the count before the for loop --- otherwise, the count is evaluated every loop.
                    progressDialog.setTitle("Uploading images...");
                    progressDialog.setMessage("Please have some patience");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    for(int i = 0; i < count; i++) {
                        Image temp = new Image();
                        temp.location = data.getClipData().getItemAt(i).getUri();
                        Validate(temp,i);
                    }
                    //do something with the image (save it to some directory or whatever you need to do with it here)
                }
            } else if(data.getData() != null) {
                String imagePath = data.getData().getPath();
                //do something with the image (save it to some directory or whatever you need to do with it here)
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
