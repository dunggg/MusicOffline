package vn.poly.musicoffline.manage;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import vn.poly.musicoffline.MainActivity;
import vn.poly.musicoffline.R;
import vn.poly.musicoffline.adapter.DanhSachBaiHat_Adapter;
import vn.poly.musicoffline.fragment.Music_Fragment;
import vn.poly.musicoffline.model.Music;

public class TimKiem_Activity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    SearchView searchView;
    ListView lv_music_timKiem;
    DanhSachBaiHat_Adapter baiHat_adapter;
    List<Music> listSearchMusic;
    FrameLayout frameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tim_kiem_);
        anhXa();
        toolbar_timKiem();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(this, "Bạn phải cấp quyền truy cập vào bộ nhớ mới có thể sử dụng chức năng này", Toast.LENGTH_SHORT).show();
            searchView.setVisibility(View.GONE);
        }

        searchView.setOnQueryTextListener(this);

        listSearchMusic = new ArrayList<>();
        baiHat_adapter = new DanhSachBaiHat_Adapter(listSearchMusic);
        lv_music_timKiem.setAdapter(baiHat_adapter);

        lv_music_timKiem.setOnItemClickListener((parent, view, position, id) -> {
            MainActivity.checkListMusic = MainActivity.listSong;
            Music_Fragment.positionBaiHat = searchPosition(listSearchMusic.get(position).getUri());
            MainActivity.playerMusicService.play(listSearchMusic.get(position), MainActivity.listSong);
            startActivity(new Intent(TimKiem_Activity.this, TrinhPhatNhac_Activity.class));
        });
    }

    // hàm toolbar
    public void toolbar_timKiem() {
        Toolbar toolbar = findViewById(R.id.toolbar_timKiem);
        toolbar.setTitle("");
        toolbar.setNavigationOnClickListener(view -> startActivity(new Intent(getBaseContext(), MainActivity.class)));
    }

    //ánh xạ
    public void anhXa() {
        frameLayout = findViewById(R.id.frameLayout);
        searchView = findViewById(R.id.SearchView);
        lv_music_timKiem = findViewById(R.id.lv_music_timKiem);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if (!searchTenBaiHat(query)) {
            baiHat_adapter.notifyDataSetChanged();
            Toast.makeText(this, "Không tìm thấy bài hát " + query, Toast.LENGTH_SHORT).show();
        }
        searchView.clearFocus();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        filterMusic(newText);
        if (listSearchMusic.size() == 0) {
            frameLayout.setVisibility(View.GONE);
        } else {
            frameLayout.setVisibility(View.VISIBLE);
        }

        return false;
    }

    // hàm tìm kiếm bài hát theo kí tự
    private void filterMusic(String text) {
        if (text.trim().length() == 0) {
            listSearchMusic.clear();
        } else {
            listSearchMusic.clear();
            listSearchMusic.addAll(MainActivity.listSong);
            for (int i = 0; i < listSearchMusic.size(); i++) {
                if (listSearchMusic.get(i).getTitle().contains(text) == false) {
                    listSearchMusic.remove(i);
                    i--;
                }
            }
        }
        baiHat_adapter.notifyDataSetChanged();
    }

    private boolean searchTenBaiHat(String ten) {
        listSearchMusic.clear();
        for (Music music : MainActivity.listSong) {
            if (music.getTitle().equalsIgnoreCase(ten.trim())) {
                listSearchMusic.add(music);
                return true;
            }
        }
        return false;
    }

    // lấy vị trí của bài hát theo url
    private int searchPosition(String uri) {
        for (int i = 0; i < MainActivity.listSong.size(); i++) {
            if (uri.equals(MainActivity.listSong.get(i).getUri())) {
                return i;
            }
        }
        return -1;
    }

}