package com.vpapps.interfaces;

import com.vpapps.item.ItemArtist;

import java.util.ArrayList;

public interface ArtistListener {
    void onStart();
    void onEnd(String success, ArrayList<ItemArtist> arrayList);
}