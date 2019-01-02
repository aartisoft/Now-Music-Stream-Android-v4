package com.vpapps.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.vpapps.item.ItemAbout;
import com.vpapps.item.ItemMyPlayList;
import com.vpapps.item.ItemSong;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

public class DBHelper extends SQLiteOpenHelper {

    private static String DB_FAKE_NAME = "mp.db";
    private static String DB_NAME = "mp3.db";
    private static String TABLE_RECENT = "recent";
    private static String TABLE_OFFLINE_RECENT = "recent_off";
    private static String TABLE_OFFLINE_PLAYLIST = "playlist_offline";
    private static String TABLE_OFFLINE_PLAYLIST_SONG = "playlistsong_offline";
    private static String TABLE_PLAYLIST = "playlist";
    private static String TABLE_PLAYLIST_SONG = "playlistsong";
    private static String TAG_ID = "id";
    private static String TAG_SID = "sid";
    private static String TAG_PID = "pid";
    private static String TAG_TITLE = "title";
    private static String TAG_DESC = "descr";
    private static String TAG_ARTIST = "artist";
    private static String TAG_DURATION = "duration";
    private static String TAG_URL = "url";
    private static String TAG_IMAGE = "image";
    private static String TAG_IMAGE_SMALL = "image_small";
    private static String TAG_CID = "cid";
    private static String TAG_CNAME = "cname";
    private static String TAG_TOTAL_RATE = "total_rate";
    private static String TAG_AVG_RATE = "avg_rate";
    private static String TAG_VIEWS = "views";
    private static String TAG_DOWNLOADS = "downloads";
    private SQLiteDatabase db;
    private final Context context;
    private String DB_PATH;

    public DBHelper(Context context) {
        super(context, DB_FAKE_NAME, null, 4);
        this.context = context;
        DB_PATH = getReadableDatabase().getPath().substring(0,42);

//        DB_PATH = "/data/data/" + context.getPackageName() + "/" + "databases/";
    }


    public void createDataBase() throws IOException {
        boolean dbExist = checkDataBase();
        if (!dbExist) {
            getReadableDatabase();
            copyDataBase();
        }
    }

    private boolean checkDataBase() {
        File dbFile = new File(DB_PATH + DB_NAME);
        return dbFile.exists();
    }

    private void copyDataBase() throws IOException {

        InputStream myInput = context.getAssets().open(DB_FAKE_NAME);
        String outFileName = DB_PATH + DB_NAME;
        OutputStream myOutput = new FileOutputStream(outFileName);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        // Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    private Cursor getData(String Query) {
        String myPath = DB_PATH + DB_NAME;
        Cursor c = null;
        try {
            db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
            c = db.rawQuery(Query, null);
        } catch (Exception e) {
            Log.e("Err", e.toString());
        }
        return c;
    }

    private void dml(String Query) {
        String myPath = DB_PATH + DB_NAME;
        if (db == null)
            db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
        try {
            db.execSQL(Query);
        } catch (Exception e) {
            Log.e("Error", e.toString());
        }
    }

    public ArrayList<ItemMyPlayList> addPlayList(String playlist, Boolean isOnline) {
        String insert = "";
        if(isOnline) {
            insert = "insert into " + TABLE_PLAYLIST + "(name) values ('"+playlist+"')";
        } else {
            insert = "insert into " + TABLE_OFFLINE_PLAYLIST + "(name) values ('"+playlist+"')";
        }

        dml(insert);
        return loadPlayList(isOnline);
    }

    public void addToFav(ItemSong itemSong) {
        String a = DatabaseUtils.sqlEscapeString(itemSong.getDescription());
        String name = itemSong.getTitle().replace("'","%27");
        String cat_name = itemSong.getCatName().replace("'","%27");
        String insert = "insert into song (sid,title,desc,artist,duration,url,image,image_small,cid,cname,total_rate,avg_rate,views,downloads) values ('" + itemSong.getId() + "', '" + name + "', " + a + ", '" +itemSong.getArtist() + "', '" + itemSong.getDuration() + "', '" + itemSong.getUrl() + "', '" + itemSong.getImageBig() + "', '" + itemSong.getImageSmall() + "', '" + itemSong.getCatId() + "', '" + cat_name + "', '" + itemSong.getTotalRate() + "', '" + itemSong.getAverageRating() + "', '" + itemSong.getViews() + "', '" + itemSong.getDownloads() + "')";
        dml(insert);
    }

    public void addToRecent(ItemSong itemSong, Boolean isOnline) {
        String table = "";
        if(isOnline) {
            table = TABLE_RECENT;
        } else {
            table = TABLE_OFFLINE_RECENT;
        }
        if(checkRecent(itemSong.getId(), isOnline)) {
            dml("delete from "+table+" where sid = '"+itemSong.getId()+"'");
        }
        String a = DatabaseUtils.sqlEscapeString(itemSong.getDescription());
        String name = itemSong.getTitle().replace("'","%27");
        String cat_name = itemSong.getCatName().replace("'","%27");
        String insert = "insert into "+table+" (sid,title,desc,artist,duration,url,image,image_small,cid,cname,total_rate,avg_rate,views,downloads) values ('" + itemSong.getId() + "', '" + name + "', " + a + ", '" + itemSong.getArtist() + "', '" + itemSong.getDuration() + "', '" + itemSong.getUrl() + "', '" + itemSong.getImageBig() + "', '" + itemSong.getImageSmall() + "', '" + itemSong.getCatId() + "', '" + cat_name + "', '" + itemSong.getTotalRate() + "', '" + itemSong.getAverageRating() + "', '" + itemSong.getViews() + "', '" + itemSong.getDownloads() + "')";
        dml(insert);
    }

    public void addToPlayList(ItemSong itemSong, String pid, Boolean isOnline) {
        String tableName = "";
        if(isOnline) {
            tableName = TABLE_PLAYLIST_SONG;
        } else {
            tableName = TABLE_OFFLINE_PLAYLIST_SONG;
        }
        if(checkPlaylist(itemSong.getId(), isOnline)) {
            dml("delete from "+ tableName +" where sid = '"+itemSong.getId()+"'");
        }
        String a = DatabaseUtils.sqlEscapeString(itemSong.getDescription());
        String name = itemSong.getTitle().replace("'","%27");
        String cat_name = itemSong.getCatName().replace("'","%27");
        String insert = "insert into "+ tableName +"("+TAG_SID+","+TAG_TITLE+","+TAG_DESC+","+TAG_ARTIST+","+TAG_DURATION+","+TAG_URL+","+TAG_IMAGE+","+TAG_IMAGE_SMALL+","+TAG_CID+","+TAG_CNAME+","+TAG_PID+","+TAG_TOTAL_RATE+","+TAG_AVG_RATE+","+TAG_VIEWS+","+TAG_DOWNLOADS+") values ('" + itemSong.getId() + "', '" + name + "', " + a + ", '" + itemSong.getArtist() + "', '" + itemSong.getDuration() + "', '" + itemSong.getUrl() + "', '" + itemSong.getImageBig() + "', '" + itemSong.getImageSmall() + "', '" + itemSong.getCatId() + "', '" + cat_name + "', '" + pid + "', '" + itemSong.getTotalRate() + "', '" + itemSong.getAverageRating() + "', '" + itemSong.getViews() + "', '" + itemSong.getDownloads() + "')";
        dml(insert);
    }

    public ArrayList<ItemMyPlayList> loadPlayList(Boolean isOnline) {
        ArrayList<ItemMyPlayList> arrayList = new ArrayList<>();

        String tableName = "";
        if(isOnline) {
            tableName = TABLE_PLAYLIST;
        } else {
            tableName = TABLE_OFFLINE_PLAYLIST;
        }

        String select = "select * from " + tableName + " order by name asc";
        Cursor cursor = getData(select);
        if(cursor!=null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            for(int i=0; i<cursor.getCount();i++) {

                String id = cursor.getString(cursor.getColumnIndex("id"));
                String name = cursor.getString(cursor.getColumnIndex("name"));

                ItemMyPlayList objItem = new ItemMyPlayList(id,name, loadPlaylistImages(id,isOnline));
                arrayList.add(objItem);

                cursor.moveToNext();
            }
        }
        return arrayList;
    }

    public void removeFromFav(String id) {
        String delete = "delete from song where sid = '"+id+"'";
        dml(delete);
    }

    public void removeFromPlayList(String id, Boolean isOnline) {
        String delete  = "";
        if(isOnline) {
            delete = "delete from "+TABLE_PLAYLIST_SONG+" where sid = '"+id+"'";
        } else {
            delete = "delete from "+TABLE_OFFLINE_PLAYLIST_SONG+" where sid = '"+id+"'";
        }

        dml(delete);
    }

    public void removePlayList(String pid, Boolean isOnline) {
        String tableName = "";
        if(isOnline) {
            tableName = TABLE_PLAYLIST;
        } else {
            tableName = TABLE_OFFLINE_PLAYLIST;
        }

        String delete = "delete from "+tableName+" where id = '"+pid+"'";
        dml(delete);
        removePlayListAllSongs(pid, isOnline);
    }

    private void removePlayListAllSongs(String pid, Boolean isOnline) {
        String tableName = "";
        if(isOnline) {
            tableName = TABLE_PLAYLIST_SONG;
        } else {
            tableName = TABLE_OFFLINE_PLAYLIST_SONG;
        }

        String delete = "delete from "+tableName+" where pid = '"+pid+"'";
        dml(delete);
    }

    public Boolean checkFav(String id) {
        String select = "select * from song where sid = '"+id+"'";
        Cursor cursor = getData(select);
        return cursor != null && cursor.getCount() > 0;
    }

    private Boolean checkRecent(String id, Boolean isOnline) {
        String table = "";
        if(isOnline) {
            table = TABLE_RECENT;
        } else {
            table = TABLE_OFFLINE_RECENT;
        }

        String select = "select * from "+table+" where sid = '"+id+"'";
        Cursor cursor = getData(select);
        return cursor != null && cursor.getCount() > 0;
    }

    private Boolean checkPlaylist(String id, Boolean isOnline) {
        String table = "";
        if(isOnline) {
            table = TABLE_PLAYLIST_SONG;
        } else {
            table = TABLE_OFFLINE_PLAYLIST_SONG;
        }

        String select = "select * from "+table+" where sid = '"+id+"'";
        Cursor cursor = getData(select);
        return cursor != null && cursor.getCount() > 0;
    }

    public void addtoAbout() {
        try {
            dml("delete from about");
            dml("insert into about (name,logo,version,author,contact,email,website,desc,developed,privacy, ad_pub, ad_banner, ad_inter, isbanner, isinter, click, isdownload) values (" +
                    "'" + Constant.itemAbout.getAppName() + "','" + Constant.itemAbout.getAppLogo() + "','" + Constant.itemAbout.getAppVersion() + "'" +
                    ",'" + Constant.itemAbout.getAuthor() + "','" + Constant.itemAbout.getContact() + "','" + Constant.itemAbout.getEmail() + "'" +
                    ",'" + Constant.itemAbout.getWebsite() + "','" + Constant.itemAbout.getAppDesc() + "','" + Constant.itemAbout.getDevelopedby() + "'" +
                    ",'" + Constant.itemAbout.getPrivacy() + "','" + Constant.ad_publisher_id + "','" + Constant.ad_banner_id + "','" + Constant.ad_inter_id + "'" +
                    ",'" + Constant.isBannerAd + "','" + Constant.isInterAd + "','" + Constant.adDisplay + "','" + Constant.isSongDownload + "')");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Boolean getAbout() {
        String selectQuery = "SELECT * FROM about";

        Cursor c = getData(selectQuery);

        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            for (int i = 0; i < c.getCount(); i++) {
                String appname = c.getString(c.getColumnIndex("name"));
                String applogo = c.getString(c.getColumnIndex("logo"));
                String desc = c.getString(c.getColumnIndex("desc"));
                String appversion = c.getString(c.getColumnIndex("version"));
                String appauthor = c.getString(c.getColumnIndex("author"));
                String appcontact = c.getString(c.getColumnIndex("contact"));
                String email = c.getString(c.getColumnIndex("email"));
                String website = c.getString(c.getColumnIndex("website"));
                String privacy = c.getString(c.getColumnIndex("privacy"));
                String developedby = c.getString(c.getColumnIndex("developed"));

                Constant.ad_banner_id = c.getString(c.getColumnIndex("ad_banner"));
                Constant.ad_inter_id = c.getString(c.getColumnIndex("ad_inter"));
                Constant.isBannerAd = Boolean.parseBoolean(c.getString(c.getColumnIndex("isbanner")));
                Constant.isInterAd = Boolean.parseBoolean(c.getString(c.getColumnIndex("isinter")));
                Constant.ad_publisher_id = c.getString(c.getColumnIndex("ad_pub"));
                Constant.adDisplay = Integer.parseInt(c.getString(c.getColumnIndex("click")));
                Constant.isSongDownload= Boolean.parseBoolean(c.getString(c.getColumnIndex("isdownload")));

                Constant.itemAbout = new ItemAbout(appname, applogo, desc, appversion, appauthor, appcontact, email, website, privacy, developedby);
            }
            c.close();
            return true;
        } else {
            return false;
        }
    }

    public ArrayList<ItemSong> loadFavData() {
        ArrayList<ItemSong> arrayList = new ArrayList<>();
        String select = "select * from song";
        Cursor cursor = getData(select);
        if(cursor!=null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            for(int i=0; i<cursor.getCount();i++) {

                String id = cursor.getString(cursor.getColumnIndex("sid"));
                String cid = cursor.getString(cursor.getColumnIndex("cid"));
                String cname = cursor.getString(cursor.getColumnIndex("cname")).replace("%27","'");
                String artist = cursor.getString(cursor.getColumnIndex("artist"));
                String name = cursor.getString(cursor.getColumnIndex("title")).replace("%27","'");
                String url = cursor.getString(cursor.getColumnIndex("url"));
                String desc = cursor.getString(cursor.getColumnIndex("desc"));
                String duration = cursor.getString(cursor.getColumnIndex("duration"));
                String imagebig = cursor.getString(cursor.getColumnIndex("image"));
                String imagesmall = cursor.getString(cursor.getColumnIndex("image_small"));
                String total_rate = cursor.getString(cursor.getColumnIndex("total_rate"));
                String avg_rate = cursor.getString(cursor.getColumnIndex("avg_rate"));
                String views = cursor.getString(cursor.getColumnIndex("views"));
                String downloads = cursor.getString(cursor.getColumnIndex("downloads"));

                ItemSong objItem = new ItemSong(id,cid,cname,artist,url,imagebig,imagesmall,name,duration,desc,total_rate, avg_rate, views, downloads);
                arrayList.add(objItem);

                cursor.moveToNext();
            }
        }
        return arrayList;
    }

    public ArrayList<ItemSong> loadDataRecent(Boolean isOnline) {
        ArrayList<ItemSong> arrayList = new ArrayList<>();
        String table = "";
        if(isOnline) {
            table = TABLE_RECENT;
        } else {
            table = TABLE_OFFLINE_RECENT;
        }
        String select = "select * from "+table;
        Cursor cursor = getData(select);
        if(cursor!=null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            for(int i=0; i<cursor.getCount(); i++) {

                String id = cursor.getString(cursor.getColumnIndex("sid"));
                String cid = cursor.getString(cursor.getColumnIndex("cid"));
                String cname = cursor.getString(cursor.getColumnIndex("cname")).replace("%27","'");
                String artist = cursor.getString(cursor.getColumnIndex("artist"));
                String name = cursor.getString(cursor.getColumnIndex("title")).replace("%27","'");
                String url = cursor.getString(cursor.getColumnIndex("url"));
                String desc = cursor.getString(cursor.getColumnIndex("desc"));
                String duration = cursor.getString(cursor.getColumnIndex("duration"));
                String imagebig = cursor.getString(cursor.getColumnIndex("image"));
                String imagesmall = cursor.getString(cursor.getColumnIndex("image_small"));
                String total_rate = cursor.getString(cursor.getColumnIndex("total_rate"));
                String avg_rate = cursor.getString(cursor.getColumnIndex("avg_rate"));
                String views = cursor.getString(cursor.getColumnIndex("views"));
                String downloads = cursor.getString(cursor.getColumnIndex("downloads"));

                ItemSong objItem = new ItemSong(id,cid,cname,artist,url,imagebig,imagesmall,name,duration,desc,total_rate,avg_rate,views,downloads);
                arrayList.add(objItem);

                cursor.moveToNext();
            }
            Collections.reverse(arrayList);
        }
        return arrayList;
    }

    public ArrayList<ItemSong> loadDataPlaylist(String pid, Boolean isOnline) {
        String select = "";
        if(isOnline) {
            select = "select * from " + TABLE_PLAYLIST_SONG + " where pid=" + pid + " ORDER BY title ASC";
        } else {
            select = "select * from " + TABLE_OFFLINE_PLAYLIST_SONG + " where pid=" + pid + " ORDER BY title ASC";
        }
        ArrayList<ItemSong> arrayList = new ArrayList<>();

        Cursor cursor = getData(select);
        if(cursor!=null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            for(int i=0; i<cursor.getCount(); i++) {

                String id = cursor.getString(cursor.getColumnIndex(TAG_SID));
                String cid = cursor.getString(cursor.getColumnIndex(TAG_CID));
                String cname = cursor.getString(cursor.getColumnIndex(TAG_CNAME)).replace("%27","'");
                String artist = cursor.getString(cursor.getColumnIndex(TAG_ARTIST));
                String name = cursor.getString(cursor.getColumnIndex(TAG_TITLE)).replace("%27","'");
                String url = cursor.getString(cursor.getColumnIndex(TAG_URL));
                String desc = cursor.getString(cursor.getColumnIndex(TAG_DESC));
                String duration = cursor.getString(cursor.getColumnIndex(TAG_DURATION));
                String imagebig = cursor.getString(cursor.getColumnIndex(TAG_IMAGE));
                String imagesmall = cursor.getString(cursor.getColumnIndex(TAG_IMAGE_SMALL));
                String total_rate = cursor.getString(cursor.getColumnIndex(TAG_TOTAL_RATE));
                String avg_rate = cursor.getString(cursor.getColumnIndex(TAG_AVG_RATE));
                String views = cursor.getString(cursor.getColumnIndex(TAG_VIEWS));
                String downloads = cursor.getString(cursor.getColumnIndex(TAG_DOWNLOADS));

                ItemSong objItem = new ItemSong(id,cid,cname,artist,url,imagebig,imagesmall,name,duration,desc, total_rate, avg_rate, views, downloads);
                arrayList.add(objItem);

                cursor.moveToNext();
            }
//            Collections.reverse(arrayList);
        }
        return arrayList;
    }

    public ArrayList<String> loadPlaylistImages(String pid, Boolean isOnline) {
        String select = "";
        if(isOnline) {
            select = "select image_small from " + TABLE_PLAYLIST_SONG + " where pid=" + pid;
        } else {
            select = "select image_small from " + TABLE_OFFLINE_PLAYLIST_SONG + " where pid=" + pid;
        }
        ArrayList<String> arrayList = new ArrayList<>();

        Cursor cursor = getData(select);
        if(cursor!=null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            for(int i=0; i<4; i++) {

//                String imagebig = cursor.getString(cursor.getColumnIndex(TAG_IMAGE));
                try {
                    String imagesmall = cursor.getString(cursor.getColumnIndex(TAG_IMAGE_SMALL));
                    arrayList.add(imagesmall);
                    cursor.moveToNext();
                } catch (Exception e) {
                    cursor.moveToFirst();
                    arrayList.add(cursor.getString(cursor.getColumnIndex(TAG_IMAGE_SMALL)));
                }
            }
            Collections.reverse(arrayList);
        } else {
            arrayList.add("1");
            arrayList.add("1");
            arrayList.add("1");
            arrayList.add("1");
        }
        return arrayList;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
                String myPath = "/data/data/" + context.getPackageName() + "/" + "databases/" + DB_NAME;
                db = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
            switch (oldVersion) {
                case 1:
                    db.execSQL("ALTER TABLE song ADD 'total_rate' TEXT");
                    db.execSQL("ALTER TABLE song ADD 'avg_rate' TEXT");
                    db.execSQL("ALTER TABLE recent ADD 'total_rate' TEXT");
                    db.execSQL("ALTER TABLE recent ADD 'avg_rate' TEXT");

                    db.execSQL("CREATE TABLE "+ TABLE_PLAYLIST +"(id integer PRIMARY KEY AUTOINCREMENT,name TEXT)");
                    db.execSQL("CREATE TABLE "+ TABLE_PLAYLIST_SONG +"("+TAG_ID+" integer PRIMARY KEY AUTOINCREMENT,"+TAG_SID+" TEXT," +
                            ""+TAG_TITLE+" TEXT,"+TAG_DESC+" TEXT,"+TAG_ARTIST+" TEXT,"+TAG_DURATION+" TEXT,"+TAG_URL+" TEXT," +
                            ""+TAG_IMAGE+" TEXT,"+TAG_IMAGE_SMALL+" TEXT,"+TAG_CID+" TEXT,"+TAG_CNAME+" TEXT,"+TAG_PID+" TEXT,"+TAG_TOTAL_RATE+" TEXT,"+TAG_AVG_RATE+" TEXT)");

                    addPlayList("My Playlist", true);
                case 2:
                    db.execSQL("ALTER TABLE song ADD 'views' TEXT");
                    db.execSQL("ALTER TABLE song ADD 'downloads' TEXT");
                    db.execSQL("ALTER TABLE recent ADD 'views' TEXT");
                    db.execSQL("ALTER TABLE recent ADD 'downloads' TEXT");
                    db.execSQL("ALTER TABLE playlistsong ADD 'views' TEXT");
                    db.execSQL("ALTER TABLE playlistsong ADD 'downloads' TEXT");
                case 3:
                    db.execSQL("CREATE TABLE "+ TABLE_OFFLINE_PLAYLIST +"(id integer PRIMARY KEY AUTOINCREMENT,name TEXT)");

                    db.execSQL("CREATE TABLE "+ TABLE_OFFLINE_PLAYLIST_SONG +"("+TAG_ID+" integer PRIMARY KEY AUTOINCREMENT,"+TAG_SID+" TEXT," +
                            ""+TAG_TITLE+" TEXT,"+TAG_DESC+" TEXT,"+TAG_ARTIST+" TEXT,"+TAG_DURATION+" TEXT,"+TAG_URL+" TEXT," +
                            ""+TAG_IMAGE+" TEXT,"+TAG_IMAGE_SMALL+" TEXT,"+TAG_CID+" TEXT,"+TAG_CNAME+" TEXT,"+TAG_PID+" TEXT,"+TAG_TOTAL_RATE+" TEXT,"+TAG_AVG_RATE+" TEXT,"+TAG_VIEWS+" TEXT,"+TAG_DOWNLOADS+" TEXT)");

                    db.execSQL("CREATE TABLE "+ TABLE_OFFLINE_RECENT +"("+TAG_ID+" integer PRIMARY KEY AUTOINCREMENT,"+TAG_SID+" TEXT," +
                            ""+TAG_TITLE+" TEXT,"+TAG_DESC+" TEXT,"+TAG_ARTIST+" TEXT,"+TAG_DURATION+" TEXT,"+TAG_URL+" TEXT," +
                            ""+TAG_IMAGE+" TEXT,"+TAG_IMAGE_SMALL+" TEXT,"+TAG_CID+" TEXT,"+TAG_CNAME+" TEXT,"+TAG_PID+" TEXT,"+TAG_TOTAL_RATE+" TEXT,"+TAG_AVG_RATE+" TEXT,"+TAG_VIEWS+" TEXT,"+TAG_DOWNLOADS+" TEXT)");

                    addPlayList("My Playlist", false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}