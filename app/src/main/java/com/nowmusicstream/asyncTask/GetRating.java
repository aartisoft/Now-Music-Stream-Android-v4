package com.nowmusicstream.asyncTask;

import android.os.AsyncTask;

import com.nowmusicstream.interfaces.RatingListener;
import com.nowmusicstream.utils.Constant;
import com.nowmusicstream.utils.JsonUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GetRating extends AsyncTask<String, String, Boolean> {

    private String rate = "0";
    private RatingListener ratingListener;

    public GetRating(RatingListener ratingListener) {
        this.ratingListener = ratingListener;
    }

    @Override
    protected void onPreExecute() {
        ratingListener.onStart();
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        String url = strings[0];
        String json = JsonUtils.okhttpGET(url);
        try {
            JSONObject jOb = new JSONObject(json);
            JSONArray jsonArray = jOb.getJSONArray(Constant.TAG_ROOT);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject c = jsonArray.getJSONObject(i);
                rate = c.getString(Constant.TAG_USER_RATE);
            }
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        } catch (Exception ee) {
            ee.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean s) {
        ratingListener.onEnd(String.valueOf(s), "", Integer.parseInt(rate));
        super.onPostExecute(s);
    }
}
