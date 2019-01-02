package com.vpapps.asyncTask;

import android.os.AsyncTask;

import com.vpapps.interfaces.AlbumsListener;
import com.vpapps.item.ItemAlbums;
import com.vpapps.utils.Constant;
import com.vpapps.utils.JsonUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class LoadAlbums extends AsyncTask<String, String, String> {

    private AlbumsListener albumsListener;
    private ArrayList<ItemAlbums> arrayList = new ArrayList<>();

    public LoadAlbums(AlbumsListener albumsListener) {
        this.albumsListener = albumsListener;
    }

    @Override
    protected void onPreExecute() {
        albumsListener.onStart();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            JSONObject mainJson = new JSONObject(JsonUtils.okhttpGET(strings[0]));
            JSONArray jsonArray = mainJson.getJSONArray(Constant.TAG_ROOT);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject objJson = jsonArray.getJSONObject(i);

                String id = objJson.getString(Constant.TAG_AID);
                String name = objJson.getString(Constant.TAG_ALBUM_NAME);
                String image = objJson.getString(Constant.TAG_ALBUM_IMAGE);
                String thumb = objJson.getString(Constant.TAG_ALBUM_THUMB);

                ItemAlbums objItem = new ItemAlbums(id, name, image, thumb);
                arrayList.add(objItem);
            }
            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
    }

    @Override
    protected void onPostExecute(String s) {
        albumsListener.onEnd(s, arrayList);
        super.onPostExecute(s);
    }
}