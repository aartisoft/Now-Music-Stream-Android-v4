package com.vpapps.onlinemp3;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.vpapps.asyncTask.LoadAbout;
import com.vpapps.fragment.FragmentAlbums;
import com.vpapps.fragment.FragmentArtist;
import com.vpapps.fragment.FragmentDashBoard;
import com.vpapps.fragment.FragmentDownloads;
import com.vpapps.fragment.FragmentFav;
import com.vpapps.fragment.FragmentMyPlaylist;
import com.vpapps.fragment.FragmentServerPlaylist;
import com.vpapps.fragment.FragmentSongs;
import com.vpapps.interfaces.AboutListener;
import com.vpapps.interfaces.AdConsentListener;
import com.vpapps.utils.AdConsent;
import com.vpapps.utils.Constant;
import com.vpapps.utils.Methods;

import java.util.EventListener;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, EventListener {

    Methods methods;
    FragmentManager fm;
    String selectedFragment = "";
    AdConsent adConsent;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.content_main, contentFrameLayout);

        Constant.isAppOpen = true;
        methods = new Methods(this);
        methods.forceRTLIfSupported(getWindow());

        fm = getSupportFragmentManager();

        navigationView.setNavigationItemSelectedListener(this);

        adConsent = new AdConsent(this, new AdConsentListener() {
            @Override
            public void onConsentUpdate() {
                methods.loadInter();
            }
        });


        if (methods.isNetworkAvailable()) {
            loadAboutData();
        } else {
            adConsent.checkForConsent();
            dbHelper.getAbout();
        }

        loadDashboardFrag();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_home:
                FragmentDashBoard f_home = new FragmentDashBoard();
                loadFrag(f_home, getString(R.string.dashboard), fm);
                break;
            case R.id.nav_albums:
                FragmentAlbums f_album = new FragmentAlbums();
                loadFrag(f_album, getString(R.string.albums), fm);
                break;
            case R.id.nav_artist:
                FragmentArtist f_art = new FragmentArtist();
                loadFrag(f_art, getString(R.string.artist), fm);
                break;
            case R.id.nav_allsongs:
                FragmentSongs f_all_songs = new FragmentSongs();
                loadFrag(f_all_songs, getString(R.string.all_songs), fm);
                break;
            case R.id.nav_myplaylist:
                FragmentMyPlaylist f_myplay = new FragmentMyPlaylist();
                loadFrag(f_myplay, getString(R.string.myplaylist), fm);
                break;
            case R.id.nav_music_library:
                Intent intent_music_lib = new Intent(MainActivity.this, OfflineMusicActivity.class);
                startActivity(intent_music_lib);
                break;
            case R.id.nav_downloads:
                if (checkPer()) {
                    FragmentDownloads f_download = new FragmentDownloads();
                    loadFrag(f_download, getString(R.string.downloads), fm);
                }
                break;
            case R.id.nav_favourite:
                FragmentFav f_fav = new FragmentFav();
                loadFrag(f_fav, getString(R.string.favourite), fm);
                break;
            case R.id.nav_settings:
                Intent intent_settings = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent_settings);
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadDashboardFrag() {
        FragmentDashBoard f1 = new FragmentDashBoard();
        loadFrag(f1, getResources().getString(R.string.dashboard), fm);
        navigationView.setCheckedItem(R.id.nav_home);
    }

    public void loadFrag(Fragment f1, String name, FragmentManager fm) {
        selectedFragment = name;
        for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }

        FragmentTransaction ft = fm.beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        if (!name.equals(getString(R.string.dashboard))) {
            ft.hide(fm.getFragments().get(fm.getBackStackEntryCount()));
            ft.add(R.id.fragment, f1, name);
            ft.addToBackStack(name);
        } else {
            ft.replace(R.id.fragment, f1, name);
        }
        ft.commit();

        getSupportActionBar().setTitle(name);

        if (mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
    }

    private void exitDialog() {
        AlertDialog.Builder alert;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            alert = new AlertDialog.Builder(MainActivity.this, R.style.ThemeDialog);
        } else {
            alert = new AlertDialog.Builder(MainActivity.this);
        }

        alert.setTitle(getString(R.string.exit));
        alert.setMessage(getString(R.string.sure_exit));
        alert.setPositiveButton(getString(R.string.exit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        alert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        alert.show();
    }

    public void loadAboutData() {
        LoadAbout loadAbout = new LoadAbout(new AboutListener() {
            @Override
            public void onStart() {
            }

            @Override
            public void onEnd(String success) {
                adConsent.checkForConsent();
                dbHelper.addtoAbout();
            }
        });
        loadAbout.execute(Constant.URL_APP_DETAILS);
    }

    @Override
    protected void onDestroy() {
        Constant.isAppOpen = false;
        if (PlayerService.exoPlayer != null && !PlayerService.exoPlayer.getPlayWhenReady()) {
            Intent intent = new Intent(getApplicationContext(), PlayerService.class);
            intent.setAction(PlayerService.ACTION_STOP);
            startService(intent);
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (dialog_desc != null && dialog_desc.isShowing()) {
            dialog_desc.dismiss();
        } else if (mLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else if (fm.getBackStackEntryCount() != 0) {
            String title = fm.getFragments().get(fm.getBackStackEntryCount()).getTag();
            if (title.equals(getString(R.string.dashboard)) || title.equals(getString(R.string.home))) {
                title = getString(R.string.home);
                navigationView.setCheckedItem(R.id.nav_home);
            }
            getSupportActionBar().setTitle(title);
            super.onBackPressed();
        } else {
            exitDialog();
        }
    }

    public Boolean checkPer() {
        if ((ContextCompat.checkSelfPermission(MainActivity.this, "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED)) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_PHONE_STATE"}, 1);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        boolean canUseExternalStorage = false;

        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    canUseExternalStorage = true;
                    FragmentDownloads f_download = new FragmentDownloads();
                    loadFrag(f_download, getString(R.string.downloads), fm);
                }

                if (!canUseExternalStorage) {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.err_cannot_use_features), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}