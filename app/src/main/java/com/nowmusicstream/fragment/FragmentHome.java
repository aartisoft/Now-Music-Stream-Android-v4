package com.nowmusicstream.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nowmusicstream.adapter.AdapterAlbumsHome;
import com.nowmusicstream.adapter.AdapterArtistHome;
import com.nowmusicstream.adapter.AdapterHomeBanner;
import com.nowmusicstream.adapter.AdapterRecent;
import com.nowmusicstream.interfaces.ClickListenerPlayList;
import com.nowmusicstream.interfaces.HomeListener;
import com.nowmusicstream.interfaces.InterAdListener;
import com.nowmusicstream.item.ItemAlbums;
import com.nowmusicstream.item.ItemArtist;
import com.nowmusicstream.item.ItemHomeBanner;
import com.nowmusicstream.item.ItemSong;
import com.nowmusicstream.ionicdev.BaseActivity;
import com.nowmusicstream.ionicdev.MainActivity;
import com.nowmusicstream.ionicdev.PlayerService;
import com.nowmusicstream.ionicdev.R;
import com.nowmusicstream.ionicdev.SongByCatActivity;
import com.nowmusicstream.utils.Constant;
import com.nowmusicstream.utils.DBHelper;
import com.nowmusicstream.utils.Methods;
import com.nowmusicstream.utils.RecyclerItemClickListener;

import java.util.ArrayList;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;

public class FragmentHome extends Fragment {

    DBHelper dbHelper;
    Methods methods;
    RecyclerView rv_banner, rv_artist, rv_songs, rv_albums;
    ArrayList<ItemSong> arrayList_recent;
    ArrayList<ItemHomeBanner> arrayList_banner;
    ArrayList<ItemArtist> arrayList_artist;
    ArrayList<ItemAlbums> arrayList_albums;
    ArrayList<ItemSong> arrayList_trend_songs;
    AdapterRecent adapterTrending;
    AdapterHomeBanner adapterHomeBanner;
    AdapterArtistHome adapterArtistHome;
    AdapterAlbumsHome adapterAlbumsHome;
    CircularProgressBar progressBar;
    FrameLayout frameLayout;

    TextView tv_songs_all, tv_albums_all, tv_artist_all, tv_empty_artist, tv_empty_albums, tv_empty_songs;
    LinearLayout ll_home;
    String errr_msg;
    SearchView searchView;

    LinearLayout ll_adView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        setHasOptionsMenu(true);

        methods = new Methods(getActivity(), new InterAdListener() {
            @Override
            public void onClick(int position, String type) {
                if (type.equals(getString(R.string.songs))) {
                    Constant.isOnline = true;
                    Constant.arrayList_play.clear();
                    Constant.arrayList_play.addAll(arrayList_trend_songs);
                    Constant.playPos = position;

                    Intent intent = new Intent(getActivity(), PlayerService.class);
                    intent.setAction(PlayerService.ACTION_PLAY);
                    getActivity().startService(intent);
                } else if (type.equals(getString(R.string.artist))) {
                    Intent intent = new Intent(getActivity(), SongByCatActivity.class);
                    intent.putExtra("type", getString(R.string.artist));
                    intent.putExtra("id", arrayList_artist.get(position).getId());
                    intent.putExtra("name", arrayList_artist.get(position).getName());
                    startActivity(intent);
                } else if (type.equals(getString(R.string.albums))) {
                    Intent intent = new Intent(getActivity(), SongByCatActivity.class);
                    intent.putExtra("type", getString(R.string.albums));
                    intent.putExtra("id", arrayList_albums.get(position).getId());
                    intent.putExtra("name", arrayList_albums.get(position).getName());
                    startActivity(intent);
                } else if (type.equals(getString(R.string.banner))) {
                    Intent intent = new Intent(getActivity(), SongByCatActivity.class);
                    intent.putExtra("type", getString(R.string.banner));
                    intent.putExtra("id", arrayList_banner.get(position).getId());
                    intent.putExtra("name", arrayList_banner.get(position).getTitle());
                    intent.putExtra("songs", arrayList_banner.get(position).getArrayListSongs());
                    startActivity(intent);
                }
            }
        });
        dbHelper = new DBHelper(getActivity());

        ll_adView = rootView.findViewById(R.id.ll_adView);
        methods.showBannerAd(ll_adView);

        arrayList_recent = new ArrayList<>();
        arrayList_artist = new ArrayList<>();
        arrayList_banner = new ArrayList<>();
        arrayList_albums = new ArrayList<>();
        arrayList_trend_songs = new ArrayList<>();

        progressBar = rootView.findViewById(R.id.pb_home);
        frameLayout = rootView.findViewById(R.id.fl_empty);

        LayoutInflater inflat = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View myView = inflat.inflate(R.layout.layout_err_internet, null);
        frameLayout.addView(myView);
        myView.findViewById(R.id.btn_empty_try).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadHome();
            }
        });

        rv_banner = rootView.findViewById(R.id.rv_home_banner);
        LinearLayoutManager llm_banner = new LinearLayoutManager(getActivity());
        rv_banner.setLayoutManager(llm_banner);
        rv_banner.setItemAnimator(new DefaultItemAnimator());
        rv_banner.setHasFixedSize(true);
        rv_banner.setNestedScrollingEnabled(false);

        adapterHomeBanner = new AdapterHomeBanner(getActivity(), arrayList_banner);
        rv_banner.setAdapter(adapterHomeBanner);

        rv_artist = rootView.findViewById(R.id.rv_home_artist);
        LinearLayoutManager llm_artist = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        rv_artist.setLayoutManager(llm_artist);
        rv_artist.setItemAnimator(new DefaultItemAnimator());
        rv_artist.setHasFixedSize(true);

        rv_albums = rootView.findViewById(R.id.rv_home_albums);
        LinearLayoutManager llm_albums = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        rv_albums.setLayoutManager(llm_albums);
        rv_albums.setItemAnimator(new DefaultItemAnimator());
        rv_albums.setHasFixedSize(true);

        rv_songs = rootView.findViewById(R.id.rv_home_songs);
        LinearLayoutManager llm_songs = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        rv_songs.setLayoutManager(llm_songs);
        rv_songs.setItemAnimator(new DefaultItemAnimator());
        rv_songs.setHasFixedSize(true);

        ll_home = rootView.findViewById(R.id.ll_home);

        tv_empty_artist = rootView.findViewById(R.id.tv_artist_empty);
        tv_empty_songs = rootView.findViewById(R.id.tv_songs_empty);
        tv_empty_albums = rootView.findViewById(R.id.tv_albums_empty);

        tv_artist_all = rootView.findViewById(R.id.tv_home_artist_all);
        tv_songs_all = rootView.findViewById(R.id.tv_home_songs_all);
        tv_albums_all = rootView.findViewById(R.id.tv_home_albums_all);

        loadHome();

        rv_artist.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                methods.showInterAd(position, getString(R.string.artist));
            }
        }));

        rv_albums.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                methods.showInterAd(position, getString(R.string.albums));
            }
        }));

        rv_banner.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                methods.showInterAd(position, getString(R.string.banner));
            }
        }));

        tv_artist_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentArtist f_art = new FragmentArtist();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.hide(getFragmentManager().getFragments().get(getFragmentManager().getBackStackEntryCount()));
                ft.add(R.id.fragment, f_art, getString(R.string.artist));
                ft.addToBackStack(getString(R.string.artist));
                ft.commit();
                ((MainActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.artist));
            }
        });

        tv_albums_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentAlbums f_albums = new FragmentAlbums();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.hide(getFragmentManager().getFragments().get(getFragmentManager().getBackStackEntryCount()));
                ft.add(R.id.fragment, f_albums, getString(R.string.albums));
                ft.addToBackStack(getString(R.string.albums));
                ft.commit();
                ((MainActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.albums));
            }
        });

        tv_songs_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentSongs f_all_songs = new FragmentSongs();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.hide(getFragmentManager().getFragments().get(getFragmentManager().getBackStackEntryCount()));
                ft.add(R.id.fragment, f_all_songs, getString(R.string.all_songs));
                ft.addToBackStack(getString(R.string.all_songs));
                ft.commit();
                ((MainActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.all_songs));
            }
        });
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
            Constant.search_item = s.replace(" ", "%20");
            FragmentSongBySearch fsearch = new FragmentSongBySearch();
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.hide(fm.findFragmentByTag(getString(R.string.home)));
            ft.add(R.id.fragment, fsearch, getString(R.string.search));
            ft.addToBackStack(getString(R.string.search));
            ft.commit();
            return true;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            return false;
        }
    };

    private void loadHome() {
        if (methods.isNetworkAvailable()) {
            com.nowmusicstream.asyncTask.LoadHome loadHome = new com.nowmusicstream.asyncTask.LoadHome(new HomeListener() {
                @Override
                public void onStart() {
                    frameLayout.setVisibility(View.GONE);
                    ll_home.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onEnd(String success, ArrayList<ItemHomeBanner> arrayListBanner, ArrayList<ItemAlbums> arrayListAlbums, ArrayList<ItemArtist> arrayListArtist, ArrayList<ItemSong> arrayListSongs) {
                    if (getActivity() != null) {
                        if (success.equals("1")) {
                            arrayList_banner.addAll(arrayListBanner);
                            arrayList_albums.addAll(arrayListAlbums);
                            arrayList_artist.addAll(arrayListArtist);
                            arrayList_trend_songs.addAll(arrayListSongs);
                            if (Constant.arrayList_play.size() == 0) {
                                Constant.arrayList_play.addAll(arrayListSongs);
                                ((BaseActivity) getActivity()).changeText(Constant.arrayList_play.get(0), "home");
                            }

                            loadEmpty();

                            adapterArtistHome = new AdapterArtistHome(getActivity(), arrayList_artist);
                            rv_artist.setAdapter(adapterArtistHome);

                            adapterTrending = new AdapterRecent(getActivity(), arrayList_trend_songs, new ClickListenerPlayList() {
                                @Override
                                public void onClick(int position) {
                                    if (methods.isNetworkAvailable()) {
                                        methods.showInterAd(position, getString(R.string.songs));
                                    } else {
                                        Toast.makeText(getActivity(), getResources().getString(R.string.err_internet_not_conn), Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onItemZero() {

                                }
                            });
                            rv_songs.setAdapter(adapterTrending);

                            adapterAlbumsHome = new AdapterAlbumsHome(getActivity(), arrayList_albums);
                            rv_albums.setAdapter(adapterAlbumsHome);

                            ll_home.setVisibility(View.VISIBLE);
                            errr_msg = getString(R.string.err_no_artist_found);
                        } else {
                            errr_msg = getString(R.string.err_server);
                        }

                        progressBar.setVisibility(View.GONE);
                    }
                }
            });
            loadHome.execute(Constant.URL_HOME);
        } else {
            errr_msg = getString(R.string.err_internet_not_conn);
            frameLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }

    private void loadEmpty() {
        arrayList_recent = dbHelper.loadDataRecent(true);

        if (arrayList_trend_songs.size() == 0) {
            rv_songs.setVisibility(View.GONE);
            tv_empty_songs.setVisibility(View.VISIBLE);
        } else {
            rv_songs.setVisibility(View.VISIBLE);
            tv_empty_songs.setVisibility(View.GONE);
        }

        if (arrayList_artist.size() == 0) {
            rv_artist.setVisibility(View.GONE);
            tv_empty_artist.setVisibility(View.VISIBLE);
        } else {
            rv_artist.setVisibility(View.VISIBLE);
            tv_empty_artist.setVisibility(View.GONE);
        }

        if (arrayList_albums.size() == 0) {
            rv_albums.setVisibility(View.GONE);
            tv_empty_albums.setVisibility(View.VISIBLE);
        } else {
            rv_albums.setVisibility(View.VISIBLE);
            tv_empty_albums.setVisibility(View.GONE);
        }
    }
}