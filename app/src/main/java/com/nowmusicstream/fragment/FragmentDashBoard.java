package com.nowmusicstream.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.irfaan008.irbottomnavigation.SpaceItem;
import com.irfaan008.irbottomnavigation.SpaceNavigationView;
import com.irfaan008.irbottomnavigation.SpaceOnClickListener;
import com.nowmusicstream.ionicdev.MainActivity;
import com.nowmusicstream.ionicdev.OfflineMusicActivity;
import com.nowmusicstream.ionicdev.R;
import com.nowmusicstream.utils.Methods;

public class FragmentDashBoard extends Fragment {

    Methods methods;
    static SpaceNavigationView spaceNavigationView;
    FragmentManager fm;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);
        setHasOptionsMenu(true);

        methods = new Methods(getActivity());
        fm = getFragmentManager();

        spaceNavigationView = rootView.findViewById(R.id.space);
        spaceNavigationView.initWithSaveInstanceState(savedInstanceState);
        spaceNavigationView.addSpaceItem(new SpaceItem(getString(R.string.home), R.mipmap.ic_home_bottom));
        spaceNavigationView.addSpaceItem(new SpaceItem(getString(R.string.recent), R.mipmap.ic_recent));
        spaceNavigationView.addSpaceItem(new SpaceItem(getString(R.string.categories), R.mipmap.ic_categories));
        spaceNavigationView.addSpaceItem(new SpaceItem(getString(R.string.latest), R.mipmap.ic_latest));

        FragmentHome f1 = new FragmentHome();
        loadFrag(f1, getString(R.string.home));

        spaceNavigationView.setSpaceOnClickListener(new SpaceOnClickListener() {
            @Override
            public void onCentreButtonClick() {
                Intent intent = new Intent(getActivity(), OfflineMusicActivity.class);
                startActivity(intent);
            }

            @Override
            public void onItemClick(int itemIndex, String itemName) {
                switch (itemIndex) {
                    case 0:
                        FragmentHome f1 = new FragmentHome();
                        loadFrag(f1, getString(R.string.home));
                        break;
                    case 1:
                        FragmentRecentSongs frecent = new FragmentRecentSongs();
                        loadFrag(frecent, getString(R.string.recently_played));
                        break;
                    case 2:
                        FragmentServerPlaylist fcat = new FragmentServerPlaylist();
                        loadFrag(fcat, getString(R.string.categories));
                        break;
                    case 3:
                        FragmentLatest flatest = new FragmentLatest();
                        loadFrag(flatest, getString(R.string.latest));
                        break;
                }
            }

            @Override
            public void onItemReselected(int itemIndex, String itemName) {

            }
        });

        return rootView;
    }

    public void loadFrag(Fragment f1, String name) {
//        selectedFragment = name;
//        if (!name.equals(getString(R.string.search))) {
//            for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
//                fm.popBackStack();
//            }
//        }

        FragmentTransaction ft = fm.beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        if (name.equals(getString(R.string.search))) {
            ft.hide(fm.getFragments().get(fm.getBackStackEntryCount()));
            ft.add(R.id.fragment_dash, f1, name);
            ft.addToBackStack(name);
        } else {
            ft.replace(R.id.fragment_dash, f1, name);
        }
        ft.commit();

        ((MainActivity) getActivity()).getSupportActionBar().setTitle(name);
    }
}