package com.nowmusicstream.interfaces;

import com.nowmusicstream.item.ItemAlbums;
import com.nowmusicstream.item.ItemArtist;
import com.nowmusicstream.item.ItemHomeBanner;
import com.nowmusicstream.item.ItemSong;

import java.util.ArrayList;

public interface HomeListener {
    void onStart();
    void onEnd(String success, ArrayList<ItemHomeBanner> arrayListBanner, ArrayList<ItemAlbums> arrayListAlbums, ArrayList<ItemArtist> arrayListArtist, ArrayList<ItemSong> arrayListSongs);
}
