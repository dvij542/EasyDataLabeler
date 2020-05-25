package com.example.easydatalabeler;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import com.example.easydatalabeler.dataScientist.AddActivity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class PopAddNewProject extends AppCompatDialogFragment {
    public EditText project_name;
    public EditText Description;
    public static String name = "";
    public static String description = "";
    public static boolean create = true;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_new_project, null);

        builder.setView(view).setTitle("Add new project").setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                create = false;
            }
        }).setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                name = project_name.getText().toString();
                description = Description.getText().toString();
                AddActivity.curr_project.name = name;
                AddActivity.curr_project.description = description;
                AddActivity.SaveProjectToDatabase(AddActivity.curr_project);
            }
        });
        project_name = view.findViewById(R.id.projectName);
        Description = view.findViewById(R.id.description);
        return builder.create();
    }
}
