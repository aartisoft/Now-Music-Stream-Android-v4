package com.nowmusicstream.asyncTask;

import android.os.AsyncTask;

import com.nowmusicstream.interfaces.CatListener;
import com.nowmusicstream.item.ItemCat;
import com.nowmusicstream.utils.Constant;
import com.nowmusicstream.utils.JsonUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class LoadCat extends AsyncTask<String, String, String> {

    private CatListener catListener;
    private ArrayList<ItemCat> arrayList = new ArrayList<>();

    public LoadCat(CatListener catListener) {
        this.catListener = catListener;
    }

    @Override
    protected void onPreExecute() {
        catListener.onStart();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            JSONObject mainJson = new JSONObject(JsonUtils.okhttpGET(strings[0]));
            JSONArray jsonArray = mainJson.getJSONArray(Constant.TAG_ROOT);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                String id = obj.getString(Constant.TAG_CID);
                String name = obj.getString(Constant.TAG_CAT_NAME);
                String image = obj.getString(Constant.TAG_CAT_IMAGE);

                ItemCat objItem = new ItemCat(id, name, image);
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
        catListener.onEnd(s, arrayList);
        super.onPostExecute(s);
    }
}