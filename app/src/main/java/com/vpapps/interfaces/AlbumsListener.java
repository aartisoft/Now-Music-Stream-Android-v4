package com.vpapps.interfaces;

import com.vpapps.item.ItemAlbums;

import java.util.ArrayList;

public interface AlbumsListener {
    void onStart();

    void onEnd(String success, ArrayList<ItemAlbums> arrayList);
}