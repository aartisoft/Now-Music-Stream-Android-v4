package com.nowmusicstream.interfaces;

import com.nowmusicstream.item.ItemAlbums;

import java.util.ArrayList;

public interface AlbumsListener {
    void onStart();

    void onEnd(String success, ArrayList<ItemAlbums> arrayList);
}