package com.vpapps.asyncTask;

import android.os.AsyncTask;

import com.vpapps.interfaces.ArtistListener;
import com.vpapps.interfaces.CatListener;
import com.vpapps.item.ItemArtist;
import com.vpapps.item.ItemCat;
import com.vpapps.utils.Constant;
import com.vpapps.utils.JsonUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class LoadArtist extends AsyncTask<String, String, String> {

    private ArtistListener artistListener;
    private ArrayList<ItemArtist> arrayList = new ArrayList<>();

    public LoadArtist(ArtistListener artistListener) {
        this.artistListener = artistListener;
    }

    @Override
    protected void onPreExecute() {
        artistListener.onStart();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            JSONObject mainJson = new JSONObject(JsonUtils.okhttpGET(strings[0]));
            JSONArray jsonArray = mainJson.getJSONArray(Constant.TAG_ROOT);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject objJson = jsonArray.getJSONObject(i);

                String id = objJson.getString(Constant.TAG_ID);
                String name = objJson.getString(Constant.TAG_ARTIST_NAME);
                String image = objJson.getString(Constant.TAG_ARTIST_IMAGE);
                String thumb = objJson.getString(Constant.TAG_ARTIST_THUMB);

                ItemArtist objItem = new ItemArtist(id, name, image, thumb);
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
        artistListener.onEnd(s, arrayList);
        super.onPostExecute(s);
    }
}