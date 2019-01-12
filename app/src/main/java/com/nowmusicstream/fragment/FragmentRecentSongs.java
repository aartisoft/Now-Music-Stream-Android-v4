package com.nowmusicstream.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.nowmusicstream.adapter.AdapterAllSongList;
import com.nowmusicstream.interfaces.ClickListenerPlayList;
import com.nowmusicstream.interfaces.InterAdListener;
import com.nowmusicstream.item.ItemAlbums;
import com.nowmusicstream.item.ItemSong;
import com.nowmusicstream.ionicdev.PlayerService;
import com.nowmusicstream.ionicdev.R;
import com.nowmusicstream.utils.Constant;
import com.nowmusicstream.utils.DBHelper;
import com.nowmusicstream.utils.GlobalBus;
import com.nowmusicstream.utils.Methods;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class FragmentRecentSongs extends Fragment {

    DBHelper dbHelper;
    Methods methods;
    RecyclerView rv;
    ArrayList<ItemSong> arrayList;
    AdapterAllSongList adapterSongList;
    FrameLayout frameLayout;
    SearchView searchView;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_recent_songs, container, false);
        setHasOptionsMenu(true);

        dbHelper = new DBHelper(getActivity());
        methods = new Methods(getActivity(), new InterAdListener() {
            @Override
            public void onClick(int position, String type) {
                Constant.isOnline = true;
                Constant.arrayList_play.clear();
                Constant.arrayList_play.addAll(arrayList);
                Constant.playPos = position;

                Intent intent = new Intent(getActivity(), PlayerService.class);
                intent.setAction(PlayerService.ACTION_PLAY);
                getActivity().startService(intent);
            }
        });

        arrayList = new ArrayList<>();

        frameLayout = rootView.findViewById(R.id.fl_empty);
        rv = rootView.findViewById(R.id.rv_recent);
        LinearLayoutManager llm_banner = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm_banner);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setHasFixedSize(true);
        rv.setNestedScrollingEnabled(false);

        new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... strings) {
                arrayList.addAll(dbHelper.loadDataRecent(true));
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                if (getActivity() != null) {

                    adapterSongList = new AdapterAllSongList(getActivity(), arrayList, new ClickListenerPlayList() {
                        @Override
                        public void onClick(int position) {
                            methods.showInterAd(position, "");
                        }

                        @Override
                        public void onItemZero() {

                        }
                    }, "online");

                    rv.setAdapter(adapterSongList);
                    setEmpty();
                }
                super.onPostExecute(s);
            }
        }.execute();

        return rootView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_search, menu);

        MenuItem item = menu.findItem(R.id.menu_search);
        MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setOnQueryTextListener(queryTextListener);
        super.onCreateOptionsMenu(menu, inflater);
    }

    SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            if (adapterSongList != null) {
                if (!searchView.isIconified()) {
                    adapterSongList.getFilter().filter(s);
                    adapterSongList.notifyDataSetChanged();
                }
            }
            return true;
        }
    };

    public void setEmpty() {
        if (arrayList.size() > 0) {
            rv.setVisibility(View.VISIBLE);
            frameLayout.setVisibility(View.GONE);
        } else {
            rv.setVisibility(View.GONE);
            frameLayout.setVisibility(View.VISIBLE);

            frameLayout.removeAllViews();
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View myView = inflater.inflate(R.layout.layout_err_nodata, null);
            myView.findViewById(R.id.btn_empty_try).setVisibility(View.GONE);

            frameLayout.addView(myView);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEquilizerChange(ItemAlbums itemAlbums) {
        adapterSongList.notifyDataSetChanged();
        GlobalBus.getBus().removeStickyEvent(itemAlbums);
    }

    @Override
    public void onStart() {
        super.onStart();
        GlobalBus.getBus().register(this);
    }

    @Override
    public void onStop() {
        GlobalBus.getBus().unregister(this);
        super.onStop();
    }
}