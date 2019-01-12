package com.nowmusicstream.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.nowmusicstream.interfaces.ClickListenerPlayList;
import com.nowmusicstream.item.ItemMyPlayList;
import com.nowmusicstream.item.ItemSong;
import com.nowmusicstream.ionicdev.R;
import com.nowmusicstream.utils.Constant;
import com.nowmusicstream.utils.GlobalBus;
import com.nowmusicstream.utils.Methods;

import java.util.ArrayList;

public class AdapterRecent extends RecyclerView.Adapter<AdapterRecent.MyViewHolder> {

    private Methods methods;
    private Context context;
    private ArrayList<ItemSong> arrayList;
    private ClickListenerPlayList clickListenerPlayList;

    class MyViewHolder extends RecyclerView.ViewHolder {
        RoundedImageView iv_song;
        ImageView iv_more;
        TextView tv_title, tv_cat;

        MyViewHolder(View view) {
            super(view);
            iv_song = view.findViewById(R.id.iv_recent);
            iv_more = view.findViewById(R.id.iv_recent_more);
            tv_title = view.findViewById(R.id.tv_recent_song);
            tv_cat = view.findViewById(R.id.tv_recent_cat);
        }
    }

    public AdapterRecent(Context context, ArrayList<ItemSong> arrayList, ClickListenerPlayList clickListenerPlayList) {
        this.context = context;
        this.arrayList = arrayList;
        this.clickListenerPlayList = clickListenerPlayList;
        methods = new Methods(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_recent, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {

        holder.tv_title.setText(arrayList.get(position).getTitle());
        holder.tv_cat.setText(arrayList.get(position).getCatName());
        Picasso.get()
                .load(arrayList.get(position).getImageBig())
                .placeholder(R.drawable.placeholder_song)
                .into(holder.iv_song);

        holder.iv_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openOptionPopUp(holder.iv_more, holder.getAdapterPosition());
            }
        });

        holder.iv_song.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListenerPlayList.onClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public long getItemId(int id) {
        return id;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    private void openOptionPopUp(ImageView imageView, final int pos) {
        PopupMenu popup = new PopupMenu(context, imageView);
        popup.getMenuInflater().inflate(R.menu.popup_song, popup.getMenu());

        if (!Constant.isOnline) {
            popup.getMenu().findItem(R.id.popup_add_queue).setVisible(false);
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.popup_add_song:
                        methods.openPlaylists(arrayList.get(pos), true);
                        break;
                    case R.id.popup_add_queue:
                        Constant.arrayList_play.add(arrayList.get(pos));
                        GlobalBus.getBus().postSticky(new ItemMyPlayList("", "", null));
                        break;
                    case R.id.popup_youtube:
                        Intent intent = new Intent(Intent.ACTION_SEARCH);
                        intent.setPackage("com.google.android.youtube");
                        intent.putExtra("query", arrayList.get(pos).getTitle());
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                        break;
                    case R.id.popup_share:
                        methods.shareSong(arrayList.get(pos), true);
                        break;
                }
                return true;
            }
        });
        popup.show();
    }
}