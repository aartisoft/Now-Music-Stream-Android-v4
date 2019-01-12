package com.nowmusicstream.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.nowmusicstream.item.ItemArtist;
import com.nowmusicstream.ionicdev.R;

import java.util.ArrayList;

public class AdapterArtistHome extends RecyclerView.Adapter<AdapterArtistHome.MyViewHolder> {

    private Context context;
    private ArrayList<ItemArtist> arrayList;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        RoundedImageView imageView_artist;

        MyViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.tv_home_artist);
            imageView_artist = view.findViewById(R.id.iv_home_artist);
        }
    }

    public AdapterArtistHome(Context context, ArrayList<ItemArtist> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_home_artist, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.textView.setText(arrayList.get(position).getName());
        Picasso.get()
                .load(arrayList.get(position).getImage())
                .placeholder(R.drawable.placeholder_artist)
                .into(holder.imageView_artist);

    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }
}