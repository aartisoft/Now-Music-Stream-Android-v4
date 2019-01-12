package com.nowmusicstream.interfaces;

import com.nowmusicstream.item.ItemArtist;

import java.util.ArrayList;

public interface ArtistListener {
    void onStart();
    void onEnd(String success, ArrayList<ItemArtist> arrayList);
}