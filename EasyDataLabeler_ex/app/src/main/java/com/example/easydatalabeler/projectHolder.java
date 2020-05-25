package com.example.easydatalabeler;

import android.view.View;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easydatalabeler.R;
import com.example.easydatalabeler.dataScientist.ui.Interface.ItemClickListener;

public class projectHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public ImageView image = null;
    public ItemClickListener listener;
    public TextView project_name;

    public projectHolder(@NonNull View itemView) {
        super(itemView);
        image = itemView.findViewById(R.id.image_view);
        image.setImageResource(R.mipmap.ic_folder);
        project_name = itemView.findViewById(R.id.label);
    }


    @Override
    public void onClick(View v) {
        listener.onCLick(v,getAdapterPosition(),false);
    }

    public void setItemClickListener(ItemClickListener listener){
        this.listener = listener;
    }
}
