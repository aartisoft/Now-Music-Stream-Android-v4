package com.vpapps.fragment;

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
import android.widget.FrameLayout;
import android.widget.TextView;

import com.vpapps.adapter.AdapterCat;
import com.vpapps.asyncTask.LoadCat;
import com.vpapps.interfaces.CatListener;
import com.vpapps.interfaces.InterAdListener;
import com.vpapps.item.ItemCat;
import com.vpapps.onlinemp3.R;
import com.vpapps.onlinemp3.SongByCatActivity;
import com.vpapps.utils.Constant;
import com.vpapps.utils.EndlessRecyclerViewScrollListener;
import com.vpapps.utils.Methods;
import com.vpapps.utils.RecyclerItemClickListener;

import java.util.ArrayList;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;

public class FragmentCategories extends Fragment {

    Methods methods;
    RecyclerView rv;
    AdapterCat adapterCat;
    ArrayList<ItemCat> arrayList;
    CircularProgressBar progressBar;
    FrameLayout frameLayout;
    GridLayoutManager glm_banner;
    Boolean isLoading = false;

    String errr_msg;
    SearchView searchView;
    int page = 1;
    Boolean isOver = false, isScroll = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_categories, container, false);

        methods = new Methods(getActivity(), new InterAdListener() {
            @Override
            public void onClick(int position, String type) {
                Intent intent = new Intent(getActivity(), SongByCatActivity.class);
                intent.putExtra("type", getString(R.string.categories));
                intent.putExtra("id", adapterCat.getItem(position).getId());
                intent.putExtra("name", adapterCat.getItem(position).getName());
                startActivity(intent);
            }
        });

        arrayList = new ArrayList<>();

        progressBar = rootView.findViewById(R.id.pb_cat);
        frameLayout = rootView.findViewById(R.id.fl_empty);

        rv = rootView.findViewById(R.id.rv_cat);
        glm_banner = new GridLayoutManager(getActivity(), 3);
        glm_banner.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapterCat.isHeader(position) ? glm_banner.getSpanCount() : 1;
            }
        });

        rv.setLayoutManager(glm_banner);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setHasFixedSize(true);

        rv.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                methods.showInterAd(position, "");
            }
        }));

        rv.addOnScrollListener(new EndlessRecyclerViewScrollListener(glm_banner) {
            @Override
            public void onLoadMore(int p, int totalItemsCount) {
                if (!isOver) {
                    if (!isLoading) {
                        isLoading = true;
                        arrayList.add(null);
                        adapterCat.notifyItemInserted(arrayList.size());
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                isScroll = true;
                                loadCategories();
                            }
                        }, 0);
                    }
                } else {
                    adapterCat.hideHeader();
                }
            }
        });

        loadCategories();

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
    }

    SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            if (adapterCat != null) {
                if (!searchView.isIconified()) {
                    adapterCat.getFilter().filter(s);
                    adapterCat.notifyDataSetChanged();
                }
            }
            return true;
        }
    };

    private void loadCategories() {
        if (methods.isNetworkAvailable()) {
            LoadCat loadCat = new LoadCat(new CatListener() {
                @Override
                public void onStart() {
                    if (arrayList.size() == 0) {
                        arrayList.clear();
                        frameLayout.setVisibility(View.GONE);
                        rv.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onEnd(String success, ArrayList<ItemCat> arrayListCat) {
                    if (getActivity() != null) {
                        if (success.equals("1")) {
                            if (isScroll) {
                                arrayList.remove(arrayList.size() - 1);
                                adapterCat.notifyItemRemoved(arrayList.size());
                            }
                            if (arrayListCat.size() == 0) {
                                errr_msg = getString(R.string.err_no_cat_found);
                                isOver = true;
                                try {
                                    adapterCat.hideHeader();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                setEmpty();
                            } else {
                                page = page + 1;
                                arrayList.addAll(arrayListCat);
                                setAdapter();
                            }

                        } else {
                            errr_msg = getString(R.string.err_server);
                            setEmpty();
                        }
                        progressBar.setVisibility(View.GONE);
                        isLoading = false;
                    }
                }
            });
            loadCat.execute(Constant.URL_CAT + page);
        } else {
            errr_msg = getString(R.string.err_internet_not_conn);
            setEmpty();
        }
    }

    private void setAdapter() {
        if (!isScroll) {
            adapterCat = new AdapterCat(getActivity(), arrayList);
            rv.setAdapter(adapterCat);
            setEmpty();
        } else {
            adapterCat.notifyDataSetChanged();
        }
    }

    public void setEmpty() {
        if (arrayList.size() > 0) {
            rv.setVisibility(View.VISIBLE);
            frameLayout.setVisibility(View.GONE);
        } else {
            rv.setVisibility(View.GONE);
            frameLayout.setVisibility(View.VISIBLE);

            frameLayout.removeAllViews();
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View myView = null;
            if (errr_msg.equals(getString(R.string.err_no_cat_found))) {
                myView = inflater.inflate(R.layout.layout_err_nodata, null);
            } else if (errr_msg.equals(getString(R.string.err_internet_not_conn))) {
                myView = inflater.inflate(R.layout.layout_err_internet, null);
            } else if (errr_msg.equals(getString(R.string.err_server))) {
                myView = inflater.inflate(R.layout.layout_err_server, null);
            }

            TextView textView = myView.findViewById(R.id.tv_empty_msg);
            textView.setText(errr_msg);

            myView.findViewById(R.id.btn_empty_try).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadCategories();
                }
            });


            frameLayout.addView(myView);
        }
    }
}