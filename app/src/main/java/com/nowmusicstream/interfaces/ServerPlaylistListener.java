package com.nowmusicstream.interfaces;

import com.nowmusicstream.item.ItemArtist;
import com.nowmusicstream.item.ItemServerPlayList;

import java.util.ArrayList;

public interface ServerPlaylistListener {
    void onStart();
    void onEnd(String success, ArrayList<ItemServerPlayList> arrayList);
}