package vn.poly.musicoffline;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import vn.poly.musicoffline.fragment.Music_Fragment;
import vn.poly.musicoffline.manage.TrinhPhatNhac_Activity;
import vn.poly.musicoffline.model.Music;

import java.util.List;
import java.util.Random;

public class PlayerMusicService extends Service {

    public MediaPlayer mediaPlayer = new MediaPlayer();

    // khởi tạo LocalBindService thông qua ibinder để khi chạy đén hàm onbind của service thì trả về
    private IBinder iBinder = new LocalBindService();
    // kiểm tra lần đầu ấn vào play trên thông báo
    public boolean checkPause = false;
    // kiểm tra chế độ lặp lại đang bật
    public boolean checkLooping = false;
    // kiểm tra chế độ random
    public boolean checkRandom = false;
    // biến kiểm tra cập nhật lại nút pause -1 là trạng thái k cập nhật
    public int checkUiPauseMini = 0;

    RemoteViews notificationSmall;
    Notification customNotification;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    public void onCreate() {
        super.onCreate();
        // sharedPreferences dùng để lưu trữ data dưới dạng key-value trên file xml
        sharedPreferences = getSharedPreferences("DATA_MUSIC", MODE_PRIVATE);
        // ghi dữ liệu vào sharredPreferences
        editor = sharedPreferences.edit();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stop();
    }

    public void stop() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE);
        }

    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public void seekTo(int position) {
        mediaPlayer.seekTo(position);
    }

    // truyền list vào để kiểm tra xem đang phát trong list nào
    public void play(Music music, List<Music> musicList) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        // nếu đang bật chế độ random
        if (checkRandom) {
            // lấy ra 1 số ngẫu nhiên từ 0 đến size của list
            int rd = randomBaiHat(musicList);
            // lấy ra bài hát theo vị trí random
            music = musicList.get(rd);
            // gán vị trí vừa random vào để cập nhật lại giao diện
            Music_Fragment.positionBaiHat = rd;
        }
        mediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(music.getUri()));
        mediaPlayer.start();
        showNotification(music);
        // nếu looping vẫn đc bật
        if (checkLooping) {
            mediaPlayer.setLooping(true);
        }

        // ghi dữ liệu vào sharredPreferences
        editor.putString("title", music.getTitle());
        editor.putString("artist", music.getArtist());
        editor.putString("uri", music.getUri());
        editor.putBoolean("check", true);
        // lưu dữ liệu
        editor.apply();

        // kiểm tra xem nhạc đã đc phát lần nào chưa
        checkPause = true;

        // khi chạy nhạc xong thì chạy sang bài mới
        updateMusic(musicList);
        // cập nhật lại ui của trình phát nhạc
        checkUiPauseMini = 1;
        // để nút play về trạng thái nhạc đang chạy
        setImageButtonPlayNotification(R.drawable.ic_pause_black);

        putBroadCastUpdateUi(getApplicationContext(), Music_Fragment.positionBaiHat, MainActivity.BROADCAST_ACTION_MAIN);
        putBroadCastUpdateUi(getApplicationContext(), Music_Fragment.positionBaiHat, TrinhPhatNhac_Activity.BROADCAST_ACTION_TRINHPHAT);
    }

    public void clearData() {
        editor.clear();
        editor.apply();
    }

    public void hideNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE);
        }
    }

    public void showNotification(Music music) {
        createNotificationChannel();

        // khi nhấn vào thông báo thì chạy đến activity trình phát nhạc
        Intent notificationIntent = new Intent(getApplicationContext(), TrinhPhatNhac_Activity.class);
        // pendingintent cho phép ứng dụng bên ngoài truy cập vào ứng dụng để thực hiện 1 chức năng nào đó
        // pendingintent tồn tại đến lúc ứng dụng bị đóng hoàn toàn
        PendingIntent pendingIntent =
                PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);

        // custom notification
        // get layout user notification
        notificationSmall = new RemoteViews(getPackageName(), R.layout.notification_small);

        // lấy ảnh từ bài hát
        Bitmap bitmap = getImage(music.getUri());
        // nếu bitmap khác null
        if (bitmap != null) {
            // gán vào imageview trên notification
            notificationSmall.setImageViewBitmap(R.id.img_notificationImage, bitmap);
        }
        // gán giá trị vào textview trong notification
        notificationSmall.setTextViewText(R.id.tv_notificationBaiHat, music.getTitle());
        notificationSmall.setTextViewText(R.id.tv_notificationNgheSi, music.getArtist());
        // setOnclick cho 4 button của notification
        notificationSmall.setOnClickPendingIntent(R.id.img_notificationPause, onButtonNotificationClick(R.id.img_notificationPause));
        notificationSmall.setOnClickPendingIntent(R.id.img_notificationPrev, onButtonNotificationClick(R.id.img_notificationPrev));
        notificationSmall.setOnClickPendingIntent(R.id.img_notificationNext, onButtonNotificationClick(R.id.img_notificationNext));
        notificationSmall.setOnClickPendingIntent(R.id.img_notificationClose, onButtonNotificationClick(R.id.img_notificationClose));

        // apply the layout to the notification
        customNotification = new NotificationCompat.Builder(getApplicationContext(), "8")
                .setSmallIcon(R.drawable.logo)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationSmall)
                .setContentIntent(pendingIntent)
                .setCustomBigContentView(notificationSmall)
                .setShowWhen(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();

        // gán notification vào service
        // Notification ID cannot be 0.
        startForeground(8, customNotification);

    }

    // hàm khởi tạo pendingIntent broadcast
    private PendingIntent onButtonNotificationClick(int id) {
        Intent intent = new Intent(MyBroadcastReceiver.RECEIVER_ACTION);
        intent.putExtra("idbutton", id);
        return PendingIntent.getBroadcast(this, id, intent, 0);
    }

    public void pause() {
        if (checkPause == false) {
            // checkUiPause == 1 là trạng thái lúc đang chạy và ngược lại
            checkUiPauseMini = 1;
            play(MainActivity.listSong.get(Music_Fragment.positionBaiHat), MainActivity.listSong);
            setImageButtonPlayNotification(R.drawable.ic_pause_black);
        } else {
            // nếu đang phát nhạc thì dừng lại
            if (mediaPlayer.isPlaying()) {
                checkUiPauseMini = 0;
                mediaPlayer.pause();
                setImageButtonPlayNotification(R.drawable.ic_play_black);
            } else {
                // ngược lại sẽ chạy nhạc
                mediaPlayer.start();
                checkUiPauseMini = 1;
                setImageButtonPlayNotification(R.drawable.ic_pause_black);
            }
        }
        putPlayBroadCastUpdateUi(getApplicationContext(), checkUiPauseMini, MainActivity.BROADCAST_ACTION_MAIN);
        putPlayBroadCastUpdateUi(getApplicationContext(), checkUiPauseMini, TrinhPhatNhac_Activity.BROADCAST_ACTION_TRINHPHAT);
    }

    public void looping() {
        if (checkPause == false) {
            Toast.makeText(this, "Bạn không thể dùng chức năng này khi chưa phát nhạc lần nào", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mediaPlayer.isLooping()) {
            checkLooping = false;
            mediaPlayer.setLooping(false);
        } else {
            checkLooping = true;
            mediaPlayer.setLooping(true);
        }
    }

    // lấy ảnh từ url bài hát
    public Bitmap getImage(String uri) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(uri);
        byte arr[] = mmr.getEmbeddedPicture();
        if (arr != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(arr, 0, arr.length);
            return bitmap;
        }
        return null;
    }

    // truyền list vào để kiểm tra xem đang phát trong list nào
    public void updateMusic(List<Music> musicList) {
        // khi chạy xong bài hát
        mediaPlayer.setOnCompletionListener(mp -> {

            ++Music_Fragment.positionBaiHat;
            //nếu vị trí của bài hát bằng với size của list thì gán vị trí bằng 0
            if (Music_Fragment.positionBaiHat == musicList.size()) {
                Music_Fragment.positionBaiHat = 0;
            }
            Music music = musicList.get(Music_Fragment.positionBaiHat);

            // cập nhật lại thông báo
            play(music, musicList);
        });
    }

    // set ảnh mới cho nút play
    public void setImageButtonPlayNotification(int res) {
        notificationSmall.setImageViewResource(R.id.img_notificationPause, res);
        // cập nhật lại notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(8, customNotification);
    }

    // từ android 8 phải đăng kí notificationChannerl id
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "playermusic";
            String description = "description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("8", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getApplication().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // truyền list vào để kiểm tra xem đang random trong list nào
    private int randomBaiHat(List<Music> musicList) {
        Random rd = new Random();
        return rd.nextInt(musicList.size());
    }

    // lớp trả về thể hiện của service để sử dụng các hàm có trong service
    public class LocalBindService extends Binder {
        PlayerMusicService getService() {
            return PlayerMusicService.this;
        }
    }

    // cập nhật lại nút play trình phát bên main thông qua broadcast
    private void putPlayBroadCastUpdateUi(Context context, int changePlay, String action) {
        Intent intent = new Intent(action);
        intent.putExtra("btnPlay", changePlay);
        context.sendBroadcast(intent);
    }

    // cập nhật lại trình phát bên main thông qua broadcast
    private void putBroadCastUpdateUi(Context context, int position, String action) {
        Intent intent = new Intent(action);
        intent.putExtra("position", position);
        context.sendBroadcast(intent);
    }

}
