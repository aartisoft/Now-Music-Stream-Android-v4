package com.nowmusicstream.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.nowmusicstream.item.ItemHomeBanner;
import com.nowmusicstream.ionicdev.R;
import com.nowmusicstream.utils.Methods;

import java.util.ArrayList;

public class AdapterHomeBanner extends RecyclerView.Adapter<AdapterHomeBanner.MyViewHolder> {

    private Context context;
    private ArrayList<ItemHomeBanner> arrayList;
    private Methods methods;

    public AdapterHomeBanner(Context context, ArrayList<ItemHomeBanner> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
        methods = new Methods(context);
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_title, tv_desc, tv_total;
        ImageView iv_banner, iv_forward, iv_play;
        View view_gradient;

        MyViewHolder(View view) {
            super(view);
            tv_title = view.findViewById(R.id.tv_home_banner);
            tv_desc = view.findViewById(R.id.tv_home_banner_desc);
            tv_total = view.findViewById(R.id.tv_home_banner_total);
            iv_banner = view.findViewById(R.id.iv_home_banner);
            iv_forward = view.findViewById(R.id.iv3);
            iv_play = view.findViewById(R.id.iv_play_banner);
            view_gradient = view.findViewById(R.id.view_home_banner);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_home_banner, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {

        holder.iv_forward.setColorFilter(Color.WHITE);
        holder.tv_title.setText(arrayList.get(position).getTitle());
        holder.tv_desc.setText(arrayList.get(position).getDesc());
        holder.tv_total.setText(arrayList.get(position).getTotalSong() + " " + context.getString(R.string.songs));
        Picasso.get()
                .load(arrayList.get(position).getImage())
                .placeholder(R.drawable.placeholder_song)
                .into(holder.iv_banner);

        new LoadColor(holder.view_gradient, holder.tv_title).execute(arrayList.get(position).getImage());
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class LoadColor extends AsyncTask<String, String, String> {

        Bitmap bitmap;
        TextView textView;
        View view;

        LoadColor(View view, TextView textView) {
            this.view = view;
            this.textView = textView;
        }

        @Override
        protected String doInBackground(String... strings) {
            bitmap = methods.getBitmapFromURL(strings[0]);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(@NonNull Palette palette) {
                        int defaultValue = 0x000000;
                        int vibrant = palette.getVibrantColor(defaultValue);

                        view.setBackground(methods.getGradientDrawable(vibrant, Color.parseColor("#00000000")));
                    }
                });
                super.onPostExecute(s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}