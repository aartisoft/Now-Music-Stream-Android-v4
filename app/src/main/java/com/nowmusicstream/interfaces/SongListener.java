package com.nowmusicstream.interfaces;

import com.nowmusicstream.item.ItemCat;
import com.nowmusicstream.item.ItemSong;

import java.util.ArrayList;

public interface SongListener {
    void onStart();
    void onEnd(String success, ArrayList<ItemSong> arrayList);
}
