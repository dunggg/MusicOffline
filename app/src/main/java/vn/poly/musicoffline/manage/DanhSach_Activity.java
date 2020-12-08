package vn.poly.musicoffline.manage;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import vn.poly.musicoffline.MainActivity;
import vn.poly.musicoffline.R;
import vn.poly.musicoffline.adapter.Music_PlayList_Adapter;
import vn.poly.musicoffline.fragment.Music_Fragment;
import vn.poly.musicoffline.model.Music;
import vn.poly.musicoffline.sql.PlayList_Dao;

public class DanhSach_Activity extends AppCompatActivity {
    TextView tv_soLuong_danhSachPhat;
    ListView lv_danhSachPhat;
    PlayList_Dao playList_dao;
    public static List<Music> musicList;
    Music_PlayList_Adapter music_playList_adapter;
    private final int REQUEST_CODE_ACTION_PICK = 345;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danh_sach);
        toolbar_danhSachPhat();
        anhXa();

        Intent intent = getIntent();
        String idPlaylist = intent.getStringExtra("idPlayList");

        playList_dao = new PlayList_Dao(this);
        musicList = new ArrayList<>();
        musicList = playList_dao.getAllSongInPlayList(idPlaylist);
        music_playList_adapter = new Music_PlayList_Adapter(this, musicList, idPlaylist);
        lv_danhSachPhat.setAdapter(music_playList_adapter);

        tv_soLuong_danhSachPhat.setText(musicList.size() + " bài hát");

        lv_danhSachPhat.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                MainActivity.checkListMusic = musicList;
                Music_Fragment.positionBaiHat = position;
                MainActivity.playerMusicService.play(musicList.get(position), musicList);
                startActivity(new Intent(DanhSach_Activity.this, TrinhPhatNhac_Activity.class));
            }
        });

    }

    // hàm toolbar xử lý chức năng quay lại và tìm kiếm
    public void toolbar_danhSachPhat() {
        Toolbar toolbar = findViewById(R.id.toolbar_danhSachPhat);
        toolbar.setTitle("Danh sách phát");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.ic_more_vert));
        toolbar.setNavigationOnClickListener(view -> startActivity(new Intent(getBaseContext(), MainActivity.class)));

        //tìm kiếm
        toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_timKiem:
                    startActivity(new Intent(getBaseContext(), TimKiem_Activity.class));
                    break;
                case R.id.menu_chiaSe:
                    try {
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType("audio/*");
                        startActivityForResult(intent, REQUEST_CODE_ACTION_PICK);
                    } catch (Exception ex) {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("audio/*");
                        startActivityForResult(intent, REQUEST_CODE_ACTION_PICK);
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
        tv_soLuong_danhSachPhat = findViewById(R.id.tv_soLuong_danhSachPhat);
        lv_danhSachPhat = findViewById(R.id.lv_danhSachPhat);
    }
}