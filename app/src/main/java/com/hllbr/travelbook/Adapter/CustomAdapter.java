package com.hllbr.travelbook.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hllbr.travelbook.Model.Place;
import com.hllbr.travelbook.R;

import java.util.ArrayList;

public class CustomAdapter extends ArrayAdapter<Place> {

    ArrayList<Place> places ;
    Context context ;

    public CustomAdapter(@NonNull Context context, ArrayList<Place> placeList) {//Nerede ve Hangi layoutla olacak
        super(context, R.layout.custom_list_row,placeList);
        this.context = context;
        this.places = placeList ;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        //Hangi Görünümü göstereceğimizi belirten Method
        //Metodumuzun soununda bir görünüm döndürülüyor
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View customView = layoutInflater.inflate(R.layout.custom_list_row,parent,false);
        TextView nameTextView = customView.findViewById(R.id.nameTextView);
        nameTextView.setText(places.get(position).name);
        return customView;

    }
}
