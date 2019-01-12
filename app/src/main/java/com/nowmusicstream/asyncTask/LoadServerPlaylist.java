package com.nowmusicstream.asyncTask;

import android.os.AsyncTask;

import com.nowmusicstream.interfaces.ServerPlaylistListener;
import com.nowmusicstream.item.ItemServerPlayList;
import com.nowmusicstream.utils.Constant;
import com.nowmusicstream.utils.JsonUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class LoadServerPlaylist extends AsyncTask<String, String, String> {

    private ServerPlaylistListener serverPlaylistListener;
    private ArrayList<ItemServerPlayList> arrayList = new ArrayList<>();

    public LoadServerPlaylist(ServerPlaylistListener serverPlaylistListener) {
        this.serverPlaylistListener = serverPlaylistListener;
    }

    @Override
    protected void onPreExecute() {
        serverPlaylistListener.onStart();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            JSONObject mainJson = new JSONObject(JsonUtils.okhttpGET(strings[0]));
            JSONArray jsonArray = mainJson.getJSONArray(Constant.TAG_ROOT);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject objJson = jsonArray.getJSONObject(i);

                String id = objJson.getString(Constant.TAG_PID);
                String name = objJson.getString(Constant.TAG_PLAYLIST_NAME);
                String image = objJson.getString(Constant.TAG_PLAYLIST_IMAGE);
                String thumb = objJson.getString(Constant.TAG_PLAYLIST_THUMB);

                ItemServerPlayList objItem = new ItemServerPlayList(id, name, image, thumb);
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
        serverPlaylistListener.onEnd(s, arrayList);
        super.onPostExecute(s);
    }
}