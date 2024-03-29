package com.vpapps.interfaces;

import com.vpapps.item.ItemArtist;
import com.vpapps.item.ItemServerPlayList;

import java.util.ArrayList;

public interface ServerPlaylistListener {
    void onStart();
    void onEnd(String success, ArrayList<ItemServerPlayList> arrayList);
}