package com.nowmusicstream.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nowmusicstream.adapter.AdapterMyPlaylist;
import com.nowmusicstream.interfaces.ClickListenerPlayList;
import com.nowmusicstream.interfaces.InterAdListener;
import com.nowmusicstream.item.ItemMyPlayList;
import com.nowmusicstream.ionicdev.R;
import com.nowmusicstream.ionicdev.SongByMyPlaylistActivity;
import com.nowmusicstream.utils.DBHelper;
import com.nowmusicstream.utils.Methods;

import java.util.ArrayList;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class FragmentMyPlaylist extends Fragment {

    DBHelper dbHelper;
    Methods methods;
    RecyclerView rv;
    Button button_add;
    AdapterMyPlaylist adapterMyPlaylist;
    ArrayList<ItemMyPlayList> arrayList;
    FrameLayout frameLayout;
    SearchView searchView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_my_playlist, container, false);

        dbHelper = new DBHelper(getActivity());

        methods = new Methods(getActivity(), new InterAdListener() {
            @Override
            public void onClick(int position, String type) {
                Intent intent = new Intent(getActivity(), SongByMyPlaylistActivity.class);
                intent.putExtra("item", adapterMyPlaylist.getItem(position));
                startActivity(intent);
            }
        });

        arrayList = new ArrayList<>();
        arrayList.addAll(dbHelper.loadPlayList(true));

        button_add = rootView.findViewById(R.id.button_add_myplaylist);
        frameLayout = rootView.findViewById(R.id.fl_empty);
//        progressBar = rootView.findViewById(R.id.pb_mysong_by_cat);
//        progressBar.setVisibility(View.GONE);

        rv = rootView.findViewById(R.id.rv_myplaylist);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        rv.setLayoutManager(gridLayoutManager);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setHasFixedSize(true);
        rv.setNestedScrollingEnabled(false);

        button_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAddPlaylistDialog();
            }
        });

        adapterMyPlaylist = new AdapterMyPlaylist(getActivity(), arrayList, new ClickListenerPlayList() {
            @Override
            public void onClick(int position) {
                methods.showInterAd(position, "");
            }

            @Override
            public void onItemZero() {
                setEmpty();
            }
        }, true);

        rv.setAdapter(adapterMyPlaylist);
        setEmpty();

        setHasOptionsMenu(true);
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
            if (adapterMyPlaylist != null) {
                if (!searchView.isIconified()) {
                    adapterMyPlaylist.getFilter().filter(s);
                    adapterMyPlaylist.notifyDataSetChanged();
                }
            }
            return true;
        }
    };

    private void setEmpty() {
        if (arrayList.size() > 0) {
            frameLayout.setVisibility(View.GONE);
            rv.setVisibility(View.VISIBLE);
        } else {
            frameLayout.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);

            frameLayout.removeAllViews();
            LayoutInflater infltr = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View myView = infltr.inflate(R.layout.layout_err_nodata, null);

            TextView textView = myView.findViewById(R.id.tv_empty_msg);
            textView.setText(getString(R.string.err_no_playlist_found));

            myView.findViewById(R.id.btn_empty_try).setVisibility(View.GONE);
            frameLayout.addView(myView);
        }
    }

    private void openAddPlaylistDialog() {
        final InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_dialog_add_playlist);

        final EditText editText = dialog.findViewById(R.id.et_dialog_addplay);
        final ImageView iv_close = dialog.findViewById(R.id.iv_addplay_close);
        final Button button = dialog.findViewById(R.id.button_addplay_add);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!editText.getText().toString().trim().isEmpty()) {
                    arrayList.clear();
                    arrayList.addAll(dbHelper.addPlayList(editText.getText().toString(), true));
                    Toast.makeText(getActivity(), getString(R.string.playlist_added), Toast.LENGTH_SHORT).show();
                    adapterMyPlaylist.notifyDataSetChanged();
                    setEmpty();
                    dialog.dismiss();
                }
            }
        });

        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        new Handler().post(
                new Runnable() {
                    public void run() {
                        inputMethodManager.showSoftInput(editText, 0);
                        editText.requestFocus();
                    }
                });
    }
}