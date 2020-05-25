package com.example.easydatalabeler.dataScientist.ui.home;

import android.view.View;
import android.widget.CheckedTextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easydatalabeler.R;
import com.example.easydatalabeler.dataScientist.ui.Interface.ItemClickListener;

public class ImageDSHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public ImageView image = null;
    public CheckedTextView isLabelled = null;
    public ItemClickListener listener;

    public ImageDSHolder(@NonNull View itemView) {
        super(itemView);
        image = itemView.findViewById(R.id.image_view);
        isLabelled = itemView.findViewById(R.id.islabelled);
    }

    @Override
    public void onClick(View v) {
        listener.onCLick(v,getAdapterPosition(),false);
    }

    public void setItemClickListener(ItemClickListener listener){
        this.listener = listener;
    }
}
