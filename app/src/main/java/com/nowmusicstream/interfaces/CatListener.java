package com.nowmusicstream.interfaces;

import com.nowmusicstream.item.ItemCat;

import java.util.ArrayList;

public interface CatListener {
    void onStart();
    void onEnd(String success, ArrayList<ItemCat> arrayList);
}
