package vn.poly.musicoffline.manage;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ListView;
import android.widget.Toast;

import vn.poly.musicoffline.adapter.Music_NgheSi_Adapter;
import vn.poly.musicoffline.fragment.Music_Fragment;
import vn.poly.musicoffline.MainActivity;
import vn.poly.musicoffline.model.Music;
import vn.poly.musicoffline.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class NgheSi_Activity extends AppCompatActivity {
    ListView lv_ngheSi;
    Music_NgheSi_Adapter music_ngheSi_adapter;
    private List<Music> musicList;
    final int REQUEST_CODE_ACTION_PICK = 345;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nghe_si_);
        anhXa();

        Intent intent = getIntent();
        String artist = intent.getStringExtra("artist");
        toolbar_ngheSi(artist);

        musicList = new ArrayList<>();
        musicList = getDataMusicArtist(artist);
        music_ngheSi_adapter = new Music_NgheSi_Adapter(getBaseContext(), musicList);
        lv_ngheSi.setAdapter(music_ngheSi_adapter);

        lv_ngheSi.setOnItemClickListener((parent, view, position, id) -> {

//            MainActivity.idPlayListDangPhat = "";

            // nếu đang random thì tắt đi
            MainActivity.playerMusicService.checkRandom = false;
            MainActivity.checkListMusic = musicList;
            Music_Fragment.positionBaiHat = position;
            MainActivity.playerMusicService.play(musicList.get(position), MainActivity.checkListMusic);
            startActivity(new Intent(getBaseContext(), TrinhPhatNhac_Activity.class));
        });
    }

    // hàm toolbar xử lý chức năng quay lại và tìm kiếm
    public void toolbar_ngheSi(String title) {
        Toolbar toolbar = findViewById(R.id.toolbar_ngheSi);
        toolbar.setTitle(title);
        toolbar.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.ic_more_vert));
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationOnClickListener(view -> startActivity(new Intent(getBaseContext(), MainActivity.class)));

        //tìm kiếm
        toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_timKiem:
                    startActivity(new Intent(getBaseContext(), TimKiem_Activity.class));
                    break;
                case R.id.menu_chiaSe:
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
                        Toast.makeText(NgheSi_Activity.this, "Bạn chưa cấp quyền truy cập vào bộ nhớ", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
            return false;
        });

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

    //ánh xạ
    public void anhXa() {
        lv_ngheSi = findViewById(R.id.lv_ngheSi);
    }

    private List<Music> getDataMusicArtist(String artist) {
        String selection = MediaStore.Audio.Media.ARTIST + "= '" + artist + "'";
        // danh sách các cột cần lấy
        String projection[] = {
                MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST
        };
        List<Music> musicList = new ArrayList<>();
        // con trỏ truy cập vào file nhạc
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null);

        // vòng lặp sẽ dừng khi hết bản ghi
        while (cursor.moveToNext()) {
            String id = cursor.getString(0);
            String uri = cursor.getString(1);
            String title = cursor.getString(2);
            String artist1 = cursor.getString(3);
            int duration = MediaPlayer.create(getApplicationContext(), Uri.parse(uri)).getDuration();
            musicList.add(new Music(id, uri, title, artist1, convertDuration(duration)));
        }
        // đóng con trỏ
        cursor.close();
        return musicList;
    }

    // chuyển đổi thời gian của bài hát vì mặc định thời gian của bài hát là kiểu int
    private String convertDuration(int duration) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
        return simpleDateFormat.format(duration);
    }

}