package com.nowmusicstream.ionicdev;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.telephony.TelephonyManager;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.nowmusicstream.item.ItemAlbums;
import com.nowmusicstream.item.MessageEvent;
import com.nowmusicstream.utils.Constant;
import com.nowmusicstream.utils.DBHelper;
import com.nowmusicstream.utils.GlobalBus;
import com.nowmusicstream.utils.JsonUtils;
import com.nowmusicstream.utils.Methods;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class PlayerService extends IntentService implements Player.EventListener {

    public static final String ACTION_TOGGLE = "action.ACTION_TOGGLE";
    public static final String ACTION_PLAY = "action.ACTION_PLAY";
    public static final String ACTION_NEXT = "action.ACTION_NEXT";
    public static final String ACTION_PREVIOUS = "action.ACTION_PREVIOUS";
    public static final String ACTION_STOP = "action.ACTION_STOP";
    public static final String ACTION_SEEKTO = "action.ACTION_SEEKTO";

    static ExoPlayer exoPlayer = null;
    NotificationManager mNotificationManager;
    NotificationCompat.Builder notification;
    RemoteViews bigViews, smallViews;

    DataSource.Factory dataSourceFactory;
    ExtractorsFactory extractorsFactory;
    private String NOTIFICATION_CHANNEL_ID = "ionicdev_ch_1";

    static PlayerService playerService;
    Methods methods;
    DBHelper dbHelper;
    Boolean isNewSong = false;
    Bitmap bitmap;

    public PlayerService() {
        super(null);
    }

    static public PlayerService getInstance() {
        if (playerService == null) {
            playerService = new PlayerService();
        }
        return playerService;
    }

    public static Boolean getIsPlayling() {
        return exoPlayer != null && exoPlayer.getPlayWhenReady();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
    }

    @Override
    public void onCreate() {
        methods = new Methods(getApplicationContext());
        dbHelper = new DBHelper(getApplicationContext());
        try {
            registerReceiver(onCallIncome, new IntentFilter("android.intent.action.PHONE_STATE"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        dataSourceFactory = new DefaultDataSourceFactory(getApplicationContext(), Util.getUserAgent(getApplicationContext(), "ionicdev"), bandwidthMeter);
        extractorsFactory = new DefaultExtractorsFactory();

        exoPlayer = ExoPlayerFactory.newSimpleInstance(getApplicationContext(), trackSelector);
        exoPlayer.addListener(this);

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        super.onCreate();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        String action = intent.getAction();
        switch (action) {

            case ACTION_PLAY:
                startNewSong();
                break;
            case ACTION_TOGGLE:
                togglePlay();
                break;
            case ACTION_SEEKTO:
                seekTo(intent.getExtras().getLong("seekto"));
                break;
            case ACTION_STOP:
                stop(intent);
                break;
            case ACTION_PREVIOUS:
                if (!Constant.isOnline || methods.isNetworkAvailable()) {
                    previous();
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.err_internet_not_conn), Toast.LENGTH_SHORT).show();
                }
                break;
            case ACTION_NEXT:
                if (!Constant.isOnline || methods.isNetworkAvailable()) {
                    next();
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.err_internet_not_conn), Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return START_STICKY;
    }

    private void startNewSong() {
        isNewSong = true;
        setBuffer(true);

        String url = Constant.arrayList_play.get(Constant.playPos).getUrl().replace(" ", "%20");

        MediaSource videoSource = new ExtractorMediaSource(Uri.parse(url),
                dataSourceFactory, extractorsFactory, null, null);
        exoPlayer.prepare(videoSource);
        exoPlayer.setPlayWhenReady(true);

        dbHelper.addToRecent(Constant.arrayList_play.get(Constant.playPos), Constant.isOnline);
    }

    private void togglePlay() {
        if (exoPlayer.getPlayWhenReady()) {
            exoPlayer.setPlayWhenReady(false);
        } else {
            exoPlayer.setPlayWhenReady(true);
        }
        changePlayPause(exoPlayer.getPlayWhenReady());
        updateNotiPlay(exoPlayer.getPlayWhenReady());
    }

    private void previous() {
        setBuffer(true);
        if (Constant.isSuffle) {
            Random rand = new Random();
            Constant.playPos = rand.nextInt((Constant.arrayList_play.size() - 1) + 1);
        } else {
            if (Constant.playPos > 0) {
                Constant.playPos = Constant.playPos - 1;
            } else {
                Constant.playPos = Constant.arrayList_play.size() - 1;
            }
        }
        startNewSong();
    }

    private void next() {
        setBuffer(true);
        if (Constant.isSuffle) {
            Random rand = new Random();
            Constant.playPos = rand.nextInt((Constant.arrayList_play.size() - 1) + 1);
        } else {
            if (Constant.playPos < (Constant.arrayList_play.size() - 1)) {
                Constant.playPos = Constant.playPos + 1;
            } else {
                Constant.playPos = 0;
            }
        }
        startNewSong();
    }

    private void seekTo(long seek) {
        exoPlayer.seekTo((int) seek);
    }

    private void onCompletion() {
        if (Constant.isRepeat) {
            exoPlayer.seekTo(0);
        } else {
            if (Constant.isSuffle) {
                Random rand = new Random();
                Constant.playPos = rand.nextInt((Constant.arrayList_play.size() - 1) + 1);
            } else {
                next();
            }
        }

        startNewSong();
    }

    private void changePlayPause(Boolean flag) {
        changeEquilizer();
        changeImageAnimation();
        GlobalBus.getBus().postSticky(new MessageEvent(flag, "playicon"));
    }

    private void setBuffer(Boolean isBuffer) {
        if (!isBuffer) {
            changeEquilizer();
        }
        GlobalBus.getBus().postSticky(new MessageEvent(isBuffer, "buffer"));
    }

    private void changeEquilizer() {
        GlobalBus.getBus().postSticky(new ItemAlbums("", "", "", ""));
    }

    private void changeImageAnimation() {
        ((BaseActivity) Constant.context).changeImageAnimation(exoPlayer.getPlayWhenReady());
    }

    private void stop(Intent intent) {
        Constant.isPlaying = false;
        Constant.isPlayed = false;

        exoPlayer.setPlayWhenReady(false);
        changePlayPause(false);
        exoPlayer.stop();
        exoPlayer.release();
        exoPlayer = null;
        try {
            unregisterReceiver(onCallIncome);
        } catch (Exception e) {
            e.printStackTrace();
        }
        stopService(intent);
        stopForeground(true);
    }

    private void createNoti() {
        bigViews = new RemoteViews(getPackageName(), R.layout.layout_notification);
        smallViews = new RemoteViews(getPackageName(), R.layout.layout_noti_small);

        Intent notificationIntent = new Intent(this, SplashActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.putExtra("isnoti", true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent previousIntent = new Intent(this, PlayerService.class);
        previousIntent.setAction(ACTION_PREVIOUS);
        PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,
                previousIntent, 0);

        Intent playIntent = new Intent(this, PlayerService.class);
        playIntent.setAction(ACTION_TOGGLE);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                playIntent, 0);

        Intent nextIntent = new Intent(this, PlayerService.class);
        nextIntent.setAction(ACTION_NEXT);
        PendingIntent pnextIntent = PendingIntent.getService(this, 0,
                nextIntent, 0);

        Intent closeIntent = new Intent(this, PlayerService.class);
        closeIntent.setAction(ACTION_STOP);
        PendingIntent pcloseIntent = PendingIntent.getService(this, 0,
                closeIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        notification = new NotificationCompat.Builder(this)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.app_icon))
                .setContentTitle(getString(R.string.app_name))
                .setPriority(Notification.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_action_name)
                .setTicker(Constant.arrayList_play.get(Constant.playPos).getTitle())
                .setChannelId(NOTIFICATION_CHANNEL_ID)
                .setOnlyAlertOnce(true);

        NotificationChannel mChannel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Online Song";// The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_LOW;
            mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            mNotificationManager.createNotificationChannel(mChannel);

            MediaSessionCompat mMediaSession;
            mMediaSession = new MediaSessionCompat(getApplicationContext(), "ionicdev");
            mMediaSession.setFlags(
                    MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                            MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

            notification.setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mMediaSession.getSessionToken())
                    .setShowCancelButton(true)
                    .setShowActionsInCompactView(0, 1, 2)
                    .setCancelButtonIntent(
                            MediaButtonReceiver.buildMediaButtonPendingIntent(getApplicationContext(), PlaybackStateCompat.ACTION_STOP)))
                    .addAction(new NotificationCompat.Action(
                            R.mipmap.ic_noti_previous, "Previous",
                            ppreviousIntent))
                    .addAction(new NotificationCompat.Action(
                            R.mipmap.ic_noti_pause, "Pause",
                            pplayIntent))
                    .addAction(new NotificationCompat.Action(
                            R.mipmap.ic_noti_next, "Next",
                            pnextIntent))
                    .addAction(new NotificationCompat.Action(
                            R.mipmap.ic_noti_close, "Close",
                            pcloseIntent));

            notification.setContentTitle(Constant.arrayList_play.get(Constant.playPos).getTitle());
            notification.setContentText(Constant.arrayList_play.get(Constant.playPos).getArtist());
        } else {
            bigViews.setOnClickPendingIntent(R.id.imageView_noti_play, pplayIntent);

            bigViews.setOnClickPendingIntent(R.id.imageView_noti_next, pnextIntent);

            bigViews.setOnClickPendingIntent(R.id.imageView_noti_prev, ppreviousIntent);

            bigViews.setOnClickPendingIntent(R.id.imageView_noti_close, pcloseIntent);
            smallViews.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent);

            bigViews.setImageViewResource(R.id.imageView_noti_play, android.R.drawable.ic_media_pause);

            bigViews.setTextViewText(R.id.textView_noti_name, Constant.arrayList_play.get(Constant.playPos).getTitle());
            smallViews.setTextViewText(R.id.status_bar_track_name, Constant.arrayList_play.get(Constant.playPos).getTitle());

            bigViews.setTextViewText(R.id.textView_noti_artist, Constant.arrayList_play.get(Constant.playPos).getArtist());
            smallViews.setTextViewText(R.id.status_bar_artist_name, Constant.arrayList_play.get(Constant.playPos).getArtist());

            notification.setCustomContentView(smallViews).setCustomBigContentView(bigViews);
        }

        startForeground(101, notification.build());
        updateNotiImage();
    }

    private void updateNotiImage() {
        new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... strings) {
                JsonUtils.okhttpGET(Constant.URL_SONG_1 + Constant.arrayList_play.get(Constant.playPos).getId() + Constant.URL_SONG_2 + "");
                getBitmapFromURL(Constant.arrayList_play.get(Constant.playPos).getImageSmall());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    notification.setLargeIcon(bitmap);
                } else {
                    bigViews.setImageViewBitmap(R.id.imageView_noti, bitmap);
                    smallViews.setImageViewBitmap(R.id.status_bar_album_art, bitmap);
                }
                mNotificationManager.notify(101, notification.build());
                return null;
            }
        }.execute();
    }

    private void updateNoti() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification.setContentTitle(Constant.arrayList_play.get(Constant.playPos).getTitle());
            notification.setContentText(Constant.arrayList_play.get(Constant.playPos).getArtist());
        } else {
            bigViews.setTextViewText(R.id.textView_noti_name, Constant.arrayList_play.get(Constant.playPos).getTitle());
            bigViews.setTextViewText(R.id.textView_noti_artist, Constant.arrayList_play.get(Constant.playPos).getArtist());
            smallViews.setTextViewText(R.id.status_bar_artist_name, Constant.arrayList_play.get(Constant.playPos).getArtist());
            smallViews.setTextViewText(R.id.status_bar_track_name, Constant.arrayList_play.get(Constant.playPos).getTitle());
        }
        updateNotiImage();
        updateNotiPlay(exoPlayer.getPlayWhenReady());
        changeImageAnimation();
    }

    private void updateNotiPlay(Boolean isPlay) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification.mActions.remove(1);
            Intent playIntent = new Intent(this, PlayerService.class);
            playIntent.setAction(ACTION_TOGGLE);
            PendingIntent ppreviousIntent = PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            if (isPlay) {
                notification.mActions.add(1, new NotificationCompat.Action(
                        R.mipmap.ic_noti_pause, "Pause",
                        ppreviousIntent));

            } else {
                notification.mActions.add(1, new NotificationCompat.Action(
                        R.mipmap.ic_noti_play, "Play",
                        ppreviousIntent));
            }
        } else {
            if (isPlay) {
                bigViews.setImageViewResource(R.id.imageView_noti_play, android.R.drawable.ic_media_pause);
            } else {
                bigViews.setImageViewResource(R.id.imageView_noti_play, android.R.drawable.ic_media_play);
            }
        }
        mNotificationManager.notify(101, notification.build());
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == Player.STATE_ENDED) {
            onCompletion();
        }
        if (playbackState == Player.STATE_READY && playWhenReady) {
            if (isNewSong) {
                isNewSong = false;
                Constant.isPlayed = true;
                setBuffer(false);
                GlobalBus.getBus().postSticky(Constant.arrayList_play.get(Constant.playPos));
                if (notification == null) {
                    createNoti();
                    changeImageAnimation();
                } else {
                    updateNoti();
                }
            } else {
                updateNotiPlay(exoPlayer.getPlayWhenReady());
            }
        }
    }

    private void getBitmapFromURL(String src) {
        try {
            if (Constant.isOnline) {
                URL url = new URL(src);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(input);
            } else {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), methods.getAlbumArtUri(Integer.parseInt(src)));
                } catch (Exception e) {
                    bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.placeholder_song);
                }
            }
        } catch (IOException e) {
            // Log exception
            e.printStackTrace();
        }
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        exoPlayer.setPlayWhenReady(false);
        setBuffer(false);
        changePlayPause(false);
    }

    @Override
    public void onPositionDiscontinuity(int reason) {
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }

    BroadcastReceiver onCallIncome = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String a = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

            if (Constant.isPlaying) {
                if (a.equals(TelephonyManager.EXTRA_STATE_OFFHOOK) || a.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    exoPlayer.setPlayWhenReady(false);
                } else if (a.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    exoPlayer.setPlayWhenReady(true);
                }
            }
        }
    };
}