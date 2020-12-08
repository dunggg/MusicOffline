package vn.poly.musicoffline;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import vn.poly.musicoffline.adapter.DanhSach_Adapter_DiaLog;
import vn.poly.musicoffline.adapter.ViewPager_Adapter;
import vn.poly.musicoffline.fragment.Music_Fragment;
import vn.poly.musicoffline.manage.GioiThieu_Activity;
import vn.poly.musicoffline.manage.TimKiem_Activity;
import vn.poly.musicoffline.manage.TrinhPhatNhac_Activity;
import vn.poly.musicoffline.model.Music;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import vn.poly.musicoffline.model.PlayList;
import vn.poly.musicoffline.sql.PlayList_Dao;

public class MainActivity extends AppCompatActivity {
    LinearLayout linear_music_main;
    CircleImageView img_logo_main;
    ImageView img_play_main, img_menuOpen_main;
    TextView tv_music_main, tv_ngheSi_main;
    ViewPager viewPager;
    BottomNavigationView bottomNavigationView;
    MyBroadcastReceiver myBroadcastReceiver;
    MyBroadcastUpdateUi myBroadcastUpdateUi;
    ServiceConnection serviceConnection;
    SharedPreferences sharedPreferences;
    public static PlayerMusicService playerMusicService;
    // list để kiểm tra xem đang phát ở phần nhạc nào
    public static List<Music> checkListMusic;
    public static List<Music> listSong;
    final int REQUEST_CODE_ACTION_PICK = 345;

    PlayList_Dao playList_dao;

    public static final String BROADCAST_ACTION_MAIN = "updateUI";

    boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        anhXa();
        toolbar();
        musicPlay();

        playList_dao = new PlayList_Dao(this);
        checkListMusic = new ArrayList<>();
        listSong = new ArrayList<>();

        // mặc định checklist sẽ có giá  trị của list bài hát

        // nếu lần đầu vào app thì ẩn trình phát mini
        linear_music_main.setVisibility(View.GONE);

        // lấy dữ liệu từ sharedPreferences
        sharedPreferences = getSharedPreferences("DATA_MUSIC", MODE_PRIVATE);

        registerMyBroadCastUpdateNotification();

        registerBroadCastUpdateUi();

        Intent intent = new Intent(this, PlayerMusicService.class);

        // nếu đc cấp quyền thì load fragment vào activity
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            setupViewPager();
            listSong = getDataMusic();
            checkListMusic = listSong;
            // serviceconnection theo dõi kết nối đến dịch vụ
            serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    PlayerMusicService.LocalBindService localBindService = (PlayerMusicService.LocalBindService) service;
                    playerMusicService = localBindService.getService();

                    // lấy giá trị của key check nếu check bằng true thì lấy giá trị crua các key khác
                    boolean check = sharedPreferences.getBoolean("check", false);
                    String tenBaiHat, ngheSi, uri;
                    int position;

                    if (check == true) {
                        // từ lần thứ 2 thì hiện trình phát
                        linear_music_main.setVisibility(View.VISIBLE);
                        tenBaiHat = sharedPreferences.getString("title", null);
                        ngheSi = sharedPreferences.getString("artist", null);
                        uri = sharedPreferences.getString("uri", null);
                        position = searchPosition(uri);
                        Music_Fragment.positionBaiHat = position;

                        // gán giá trị vào các view
                        tv_music_main.setText(tenBaiHat);
                        tv_ngheSi_main.setText(ngheSi);

                        // lấy ảnh từ uri đổi ra bitmap nếu bitmap khác null thì gán ảnh vào view
                        Bitmap bitmap = getImage(uri);
                        if (bitmap != null) {
                            img_logo_main.setImageBitmap(bitmap);
                        }
                        playerMusicService.showNotification(new Music("", uri, tenBaiHat, ngheSi, ""));
                    }

                    mBound = true;
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    playerMusicService.stop();
                    mBound = false;
                }
            };

            // chạy service
            bindService(intent, serviceConnection, BIND_AUTO_CREATE);

        } else {

            // không được cấp quyền thì hiện dialog hỏi
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("App chưa được cấp quyền, vui lòng cấp quyền truy cập vào bộ nhớ");
            builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            builder.show();
        }
    }

    // hàm toolbar xử lý chức năng navigation
    public void toolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        NavigationView navigationView = findViewById(R.id.nav_view);
        final DrawerLayout drawerLayout = findViewById(R.id.drawer);
        toolbar.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.ic_more_vert));
        toolbar.setNavigationOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));

        // xử lý khi click vào item trên menu navigation
        navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_gioiThieu:
                    startActivity(new Intent(getBaseContext(), GioiThieu_Activity.class));
                    break;

            }
            return false;
        });

        //tìm kiếm
        toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_timKiem:
                    startActivity(new Intent(MainActivity.this, TimKiem_Activity.class));
                    break;

                case R.id.menu_chiaSe:
                    // nếu đã cấp quyền
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("audio/*");
                            startActivityForResult(intent, REQUEST_CODE_ACTION_PICK);
                        } catch (Exception ex) {
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.setType("audio/*");
                            startActivityForResult(intent, REQUEST_CODE_ACTION_PICK);
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Bạn chưa cấp quyền truy cập vào bộ nhớ", Toast.LENGTH_SHORT).show();
                    }
                    break;

            }
            return false;
        });

    }

    // lấy ảnh từ đường dẫn uri
    private Bitmap getImage(String uri) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(uri);
        byte arr[] = mmr.getEmbeddedPicture();
        if (arr != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(arr, 0, arr.length);
            return bitmap;
        }
        return null;
    }

    // setup viewpager vào bottom navigation
    private void setupViewPager() {
        ViewPager_Adapter viewPagerAdapter = new ViewPager_Adapter(getSupportFragmentManager(),
                FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewPager.setAdapter(viewPagerAdapter);

        // bắt sự kiện khi có sự thay đổi của fragment trong viewpager
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                // khi viewpager selected
                switch (position) {
                    case 0:
                        bottomNavigationView.getMenu().findItem(R.id.menu_music).setChecked(true);
                        break;
                    case 1:
                        bottomNavigationView.getMenu().findItem(R.id.menu_danhSach).setChecked(true);
                        break;
                    case 2:
                        bottomNavigationView.getMenu().findItem(R.id.menu_ngheSi).setChecked(true);
                        break;
                    case 3:
                        bottomNavigationView.getMenu().findItem(R.id.menu_uaThich).setChecked(true);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // khi nhấn vào item trong bottom navigation
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_music:
                    viewPager.setCurrentItem(0);
                    break;
                case R.id.menu_danhSach:
                    viewPager.setCurrentItem(1);
                    break;
                case R.id.menu_ngheSi:
                    viewPager.setCurrentItem(2);
                    break;
                case R.id.menu_uaThich:
                    viewPager.setCurrentItem(3);
                    break;
            }
            item.setChecked(true);
            return false;
        });
    }

    //ánh xạ
    public void anhXa() {
        linear_music_main = findViewById(R.id.linear_music_main);
        img_logo_main = findViewById(R.id.img_logo_main);
        img_play_main = findViewById(R.id.img_play_main);
        img_menuOpen_main = findViewById(R.id.img_menuOpen_main);
        tv_music_main = findViewById(R.id.tv_music_main);
        tv_ngheSi_main = findViewById(R.id.tv_ngheSi_main);
        viewPager = findViewById(R.id.viewpager_main);
        bottomNavigationView = findViewById(R.id.bottom_view);

    }

    //hàm xử lý khi click vào layout thì chuyển đến trình phát nhạc, phát nhạc, mở danh sách nhạc
    public void musicPlay() {
        linear_music_main.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, TrinhPhatNhac_Activity.class));
        });

        //phát nhạc
        img_play_main.setOnClickListener(view -> {
            // sự kiện cho button play
            playerMusicService.pause();
            if (playerMusicService.mediaPlayer.isPlaying()) {
                img_play_main.setImageResource(R.drawable.ic_pause);
            } else {
                img_play_main.setImageResource(R.drawable.ic_play);
            }
        });

        //danh sách nhạc
        img_menuOpen_main.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view1 = LayoutInflater.from(this).inflate(R.layout.dialog_dang_phat, null);
            builder.setView(view1);

            TextView tv_dialog_soLuong_dangPhat = view1.findViewById(R.id.tv_dialog_soLuong_dangPhat);
            ImageView img_dialog_deleteAll_dangPhat = view1.findViewById(R.id.img_dialog_deleteAll_dangPhat);
            ListView lv_dialog_dangPhat = view1.findViewById(R.id.lv_dialog_dangPhat);

            List<PlayList> playLists = new ArrayList<>();
            playLists = playList_dao.getAllPlayList();

            DanhSach_Adapter_DiaLog arrayAdapter = new DanhSach_Adapter_DiaLog(MainActivity.this, playLists);
            lv_dialog_dangPhat.setAdapter(arrayAdapter);

            tv_dialog_soLuong_dangPhat.setText("Hiện đang phát (" + "" + ")");

            img_dialog_deleteAll_dangPhat.setOnClickListener(view2 -> {
                //code xóa

            });

            AlertDialog alertDialog = builder.show();

        });
    }

    // lấy bài hát từ trong máy
    public List<Music> getDataMusic() {
        List<Music> musicList = new ArrayList<>();
        // danh sách những cột cần lấy dữ liệu
        String projection[] = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST
        };
        // con trỏ truy cập vào file nhạc
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null);

        // vòng lặp sẽ dừng khi hết bản ghi
        while (cursor.moveToNext()) {
            String id = cursor.getString(0);
            String uri = cursor.getString(1);
            String title = cursor.getString(2);
            String artist = cursor.getString(3);
            int duration = MediaPlayer.create(MainActivity.this, Uri.parse(uri)).getDuration();
            musicList.add(new Music(id, uri, title, artist, convertDuration(duration)));
        }
        // đóng con trỏ
        cursor.close();
        return musicList;
    }

    // lấy vị trí của bài hát theo url
    private int searchPosition(String uri) {
        for (int i = 0; i < listSong.size(); i++) {
            if (uri.equals(listSong.get(i).getUri())) {
                return i;
            }
        }
        return -1;
    }

    // chuyển đổi thời gian của bài hát vì mặc định thời gian của bài hát là kiểu int
    private String convertDuration(int duration) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
        return simpleDateFormat.format(duration);
    }

    private void registerMyBroadCastUpdateNotification() {
        // đăng kí brodacastreceiver dùng để cập nhật thông báo
        myBroadcastReceiver = new MyBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MyBroadcastReceiver.RECEIVER_ACTION);
        registerReceiver(myBroadcastReceiver, intentFilter);
    }

    private void registerBroadCastUpdateUi() {
        // đăng kí broadcast receiver để cập nhật trình phát main
        myBroadcastUpdateUi = new MyBroadcastUpdateUi();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_ACTION_MAIN);
        registerReceiver(myBroadcastUpdateUi, intentFilter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ACTION_PICK && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("audio/*");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBound == true) {
            if (playerMusicService.mediaPlayer.isPlaying()) {
                img_play_main.setImageResource(R.drawable.ic_pause);
            } else {
                img_play_main.setImageResource(R.drawable.ic_play);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBound) {
            unbindService(serviceConnection);
        }
        unregisterReceiver(myBroadcastReceiver);
        unregisterReceiver(myBroadcastUpdateUi);
    }


    private class MyBroadcastUpdateUi extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int i = intent.getIntExtra("position", -1);
            int changePlay = intent.getIntExtra("btnPlay", -1);
            if (i != -1) {
                // cập nhật trình phát
                Music music = checkListMusic.get(i);
                Bitmap bitmap = getImage(music.getUri());
                // nếu bitmap khác null thì gán ảnh
                if (bitmap != null) {
                    img_logo_main.setImageBitmap(bitmap);
                } else {
                    img_logo_main.setImageResource(R.drawable.ic_no_music);
                }
                tv_music_main.setText(music.getTitle());
                tv_ngheSi_main.setText(music.getArtist());
                img_play_main.setImageResource(R.drawable.ic_pause);
                linear_music_main.setVisibility(View.VISIBLE);
            } else if (changePlay != -1) {
                // cập nhật nút play
                if (changePlay == 0) {
                    img_play_main.setImageResource(R.drawable.ic_play);
                } else {
                    img_play_main.setImageResource(R.drawable.ic_pause);
                }
            } else if (i == -1) {
                // nếu i =-1 thì có nghĩa là đã xóa hết bài hát
                linear_music_main.setVisibility(View.GONE);
            }
        }

    }
}