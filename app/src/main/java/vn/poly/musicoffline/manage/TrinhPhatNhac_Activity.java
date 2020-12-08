package vn.poly.musicoffline.manage;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import vn.poly.musicoffline.adapter.DanhSachBaiHat_Adapter;
import vn.poly.musicoffline.adapter.List_DanhSach_Adapter;
import vn.poly.musicoffline.fragment.Music_Fragment;
import vn.poly.musicoffline.MainActivity;
import vn.poly.musicoffline.model.Music;
import vn.poly.musicoffline.R;
import vn.poly.musicoffline.model.PlayList;
import vn.poly.musicoffline.sql.Favorite_Dao;
import vn.poly.musicoffline.sql.PlayList_Dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class TrinhPhatNhac_Activity extends AppCompatActivity {
    TextView tv_music_trinhPhatNhac, tv_ngheSi_trinhPhatNhac, tv_thoiGianChay_trinhPhatNhac, tv_tongThoiGian_trinhPhatNhac;
    ImageView img_logo_trinhPhatNhac, img_uaThich_trinhPhatNhac, img_danhSachPhat_trinhPhatNhac, img_danhSachBaiHat_trinhPhatNhac,
            img_lapLai_trinhPhatNhac, img_quayLai_trinhPhatNhac, img_play_trinhPhatNhac, img_tiepTheo_trinhPhatNhac, img_random_trinhPhatNhac;
    SeekBar seekbar;
    Music music;
    final int REQUEST_CODE_ACTION_PICK = 123;
    List<Music> musicList = MainActivity.checkListMusic;
    MyBroadCastUpdateUiTrinhPhat myBroadCastUpdateUiTrinhPhat;

    PlayList_Dao playList_dao;
    Favorite_Dao favorite_dao;

    List<PlayList> playLists;

    // đơn vị là mili giây
    private final long tenMinutes = 36000L;
    private final long thirtyMinutes = 108000L;
    private final long sixtyMinutes = 216000L;

    Handler handler;
    Runnable runnable;

    public static final String BROADCAST_ACTION_TRINHPHAT = "updateUITrinhPhat";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trinh_phat_nhac_);
        anhXa();
        toolbar_trinhPhatNhac();

        playList_dao = new PlayList_Dao(this);
        favorite_dao = new Favorite_Dao(this);

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                MainActivity.playerMusicService.stop();
            }
        };

        // lấy ra bài hát hiện tại
        music = musicList.get(Music_Fragment.positionBaiHat);
        setDataView();
        musicPlay();

        // nếu bài hát nằm trong danh sách yêu thích thì nút yêu thích đổi màu
        if (favorite_dao.searchSongFavorite(music.getId())) {
            img_uaThich_trinhPhatNhac.setImageResource(R.drawable.ic_favorite_blue);
        }

        //nếu đang bật chế độ random
        if (MainActivity.playerMusicService.checkRandom) {
            img_random_trinhPhatNhac.setImageResource(R.drawable.ic_random_blue);
        }

        //nếu đang bật chế độ lặp lại
        if (MainActivity.playerMusicService.checkLooping) {
            img_lapLai_trinhPhatNhac.setImageResource(R.drawable.ic_replay_blue);
        }

        registerBroadCast();
    }

    private void registerBroadCast() {
        myBroadCastUpdateUiTrinhPhat = new MyBroadCastUpdateUiTrinhPhat();
        IntentFilter intentFilter = new IntentFilter(BROADCAST_ACTION_TRINHPHAT);
        registerReceiver(myBroadCastUpdateUiTrinhPhat, intentFilter);
    }

    //ánh xạ
    private void anhXa() {
        tv_music_trinhPhatNhac = findViewById(R.id.tv_music_trinhPhatNhac);
        tv_ngheSi_trinhPhatNhac = findViewById(R.id.tv_ngheSi_trinhPhatNhac);
        tv_thoiGianChay_trinhPhatNhac = findViewById(R.id.tv_thoiGianChay_trinhPhatNhac);
        tv_tongThoiGian_trinhPhatNhac = findViewById(R.id.tv_tongThoiGian_trinhPhatNhac);
        img_logo_trinhPhatNhac = findViewById(R.id.img_logo_trinhPhatNhac);
        img_uaThich_trinhPhatNhac = findViewById(R.id.img_uaThich_trinhPhatNhac);
        img_danhSachPhat_trinhPhatNhac = findViewById(R.id.img_danhSachPhat_trinhPhatNhac);
        img_danhSachBaiHat_trinhPhatNhac = findViewById(R.id.img_danhSachBaiHat_trinhPhatNhac);
        img_lapLai_trinhPhatNhac = findViewById(R.id.img_lapLai_trinhPhatNhac);
        img_quayLai_trinhPhatNhac = findViewById(R.id.img_quayLai_trinhPhatNhac);
        img_play_trinhPhatNhac = findViewById(R.id.img_play_trinhPhatNhac);
        img_tiepTheo_trinhPhatNhac = findViewById(R.id.img_tiepTheo_trinhPhatNhac);
        img_random_trinhPhatNhac = findViewById(R.id.img_random_trinhPhatNhac);
        seekbar = findViewById(R.id.seekbar);
    }

    private void toolbar_trinhPhatNhac() {
        Toolbar toolbar = findViewById(R.id.toolbar_trinhPhatNhac);
        toolbar.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.ic_more_vert));
        toolbar.setNavigationOnClickListener(view -> startActivity(new Intent(getBaseContext(), MainActivity.class)));

        toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_chiaSe_trinhPhatNhac:
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

                case R.id.menu_henGio_trinhPhatNhac:
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    View view = LayoutInflater.from(this).inflate(R.layout.dialog_hen_gio_ngu, null);
                    builder.setCancelable(false);
                    builder.setView(view);

                    RadioButton rb_dialog_10p_henGioNgu = view.findViewById(R.id.rb_dialog_10p_henGioNgu);
                    RadioButton rb_dialog_30p_henGioNgu = view.findViewById(R.id.rb_dialog_30p_henGioNgu);
                    RadioButton rb_dialog_60p_henGioNgu = view.findViewById(R.id.rb_dialog_60p_henGioNgu);
                    RadioButton rb_dialog_tat_henGioNgu = view.findViewById(R.id.rb_dialog_tat_henGioNgu);
                    TextView tv_dialog_confirm_henGioNgu = view.findViewById(R.id.tv_dialog_confirm_henGioNgu);
                    TextView tv_dialog_cancel_henGioNgu = view.findViewById(R.id.tv_dialog_cancel_henGioNgu);

                    AlertDialog alertDialog = builder.show();

                    tv_dialog_cancel_henGioNgu.setOnClickListener(view1 -> alertDialog.dismiss());

                    tv_dialog_confirm_henGioNgu.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (rb_dialog_10p_henGioNgu.isChecked()) {
                                musicOffTime(36000);
                            } else if (rb_dialog_30p_henGioNgu.isChecked()) {
                                musicOffTime(thirtyMinutes);
                            } else if (rb_dialog_60p_henGioNgu.isChecked()) {
                                musicOffTime(sixtyMinutes);
                            } else {
                                handler.removeCallbacks(runnable);
                            }
                            alertDialog.cancel();
                        }

                    });

                    break;
            }
            return false;
        });
    }

    private void setDataView() {
        // kiểm tra xem nhạc đang phát hay dừng để gán ảnh cho nút play
        if (MainActivity.playerMusicService.mediaPlayer.isPlaying()) {
            img_play_trinhPhatNhac.setImageResource(R.drawable.ic_pause2);
        }

        // gán ảnh bài hát
        Bitmap bitmap = getImage(music.getUri());
        if (bitmap != null) {
            img_logo_trinhPhatNhac.setImageBitmap(bitmap);
        } else {
            img_logo_trinhPhatNhac.setImageResource(R.drawable.ic_no_music);
        }

        //set tên nghệ sĩ,tên bài hát, tổng thời gian,thoigianchay
        tv_ngheSi_trinhPhatNhac.setText(music.getArtist());
        tv_music_trinhPhatNhac.setText(music.getTitle());
        tv_tongThoiGian_trinhPhatNhac.setText(music.getDuration());

        int duration = MediaPlayer.create(TrinhPhatNhac_Activity.this, Uri.parse(music.getUri())).getDuration();
        seekbar.setMax(duration);

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // khi giá trị của seekbar thay đổi
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            // bắt đầu thao tác chạm
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            // khi hoàn thành thao tác chạm
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // tua đến vị trí đc kéo đến
                MainActivity.playerMusicService.seekTo(seekBar.getProgress());
            }
        });

        // cập nhật giao diện và thanh seekbar
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (MainActivity.playerMusicService.checkPause) {
                    // cập nhật tv thời gian phát cả seebar theo từng giây
                    tv_thoiGianChay_trinhPhatNhac.setText(getCurrentPosition(MainActivity.playerMusicService.getCurrentPosition()));
                    seekbar.setProgress(MainActivity.playerMusicService.getCurrentPosition());
                }

                new Handler().postDelayed(this, 1000);
            }
        }, 1000);

    }

    public void musicPlay() {
        // khi nhấn vào ưa thích
        img_uaThich_trinhPhatNhac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (favorite_dao.searchSongFavorite(music.getId())) {
                    // == true là đã nằm trong danh sách favorite
                    //1 là xóa khỏi danh sách yêu thích
                    favorite_dao.update(music.getId(), 1);
                    img_uaThich_trinhPhatNhac.setImageResource(R.drawable.ic_favorite);
                } else {
                    // ngược lại chưa nằm trong danh sách
                    // 2 là thêm vào danh sách yêu thích
                    favorite_dao.update(music.getId(), 2);
                    img_uaThich_trinhPhatNhac.setImageResource(R.drawable.ic_favorite_blue);
                }
            }
        });

        // khi nhấn vào play
        img_play_trinhPhatNhac.setOnClickListener(view -> {
            MainActivity.playerMusicService.pause();
        });

        // nhấn vào bài tiếp theo
        img_tiepTheo_trinhPhatNhac.setOnClickListener(v -> {
            Music_Fragment.positionBaiHat++;
            // khi click lên mà vượt quá size của mảng thì gán về 0
            if (Music_Fragment.positionBaiHat == musicList.size()) {
                Music_Fragment.positionBaiHat = 0;
            }
            music = musicList.get(Music_Fragment.positionBaiHat);
            MainActivity.playerMusicService.play(music, musicList);

        });

        // nhấn vào quay lại
        img_quayLai_trinhPhatNhac.setOnClickListener(v -> {
            Music_Fragment.positionBaiHat--;
            // khi cick quay lại mà bé hơn 0 thì cho về vị trí cuối cùng của mảng
            if (Music_Fragment.positionBaiHat < 0) {
                Music_Fragment.positionBaiHat = musicList.size() - 1;
            }
            music = musicList.get(Music_Fragment.positionBaiHat);
            MainActivity.playerMusicService.play(music, musicList);

        });

        // nhấn vào lặp lại
        img_lapLai_trinhPhatNhac.setOnClickListener(view -> {
            MainActivity.playerMusicService.looping();
            if (MainActivity.playerMusicService.mediaPlayer.isLooping()) {
                img_lapLai_trinhPhatNhac.setImageResource(R.drawable.ic_replay_blue);

                // bật chế độ lặp lại thì tắt chế độ random
                img_random_trinhPhatNhac.setImageResource(R.drawable.ic_random);
                MainActivity.playerMusicService.checkRandom = false;

            } else {
                img_lapLai_trinhPhatNhac.setImageResource(R.drawable.ic_replay);

            }
        });

        // sự kiện random
        img_random_trinhPhatNhac.setOnClickListener(v -> {
            // nếu chưa bật chế độ random
            if (MainActivity.playerMusicService.checkRandom == false) {
                MainActivity.playerMusicService.checkRandom = true;
                img_random_trinhPhatNhac.setImageResource(R.drawable.ic_random_blue);
                // bật chế độ random thì phải tắt chế độ lặp lại

                if (MainActivity.playerMusicService.checkLooping) {
                    // tắt lặp lại
                    MainActivity.playerMusicService.checkLooping = false;
                    img_lapLai_trinhPhatNhac.setImageResource(R.drawable.ic_replay);
                }

            } else {
                // Nếu đã bật chế độ random
                MainActivity.playerMusicService.checkRandom = false;
                img_random_trinhPhatNhac.setImageResource(R.drawable.ic_random);
            }
        });
    }

    //khi nhấn vào hàng chờ
    public void hangCho(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view1 = LayoutInflater.from(this).inflate(R.layout.dialog_dang_phat, null);
        builder.setView(view1);

        TextView tv_dialog_soLuong_dangPhat = view1.findViewById(R.id.tv_dialog_soLuong_dangPhat);
        ListView lv_dialog_dangPhat = view1.findViewById(R.id.lv_dialog_dangPhat);

        DanhSachBaiHat_Adapter adapter = new DanhSachBaiHat_Adapter(MainActivity.checkListMusic, R.layout.view_danh_sach_bai_hat);
        lv_dialog_dangPhat.setAdapter(adapter);

        tv_dialog_soLuong_dangPhat.setText("Hiện đang phát " + "(" + MainActivity.checkListMusic.size() + ")");

        lv_dialog_dangPhat.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Music_Fragment.positionBaiHat = position;
                MainActivity.playerMusicService.play(MainActivity.checkListMusic.get(position), MainActivity.checkListMusic);
            }
        });

        AlertDialog alertDialog = builder.show();
    }

    //khi nhấn vào danh sách phát
    public void danhSachPhat(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view1 = LayoutInflater.from(this).inflate(R.layout.dialog_menu_danh_sach_phat, null);
        builder.setView(view1);

        ListView lv_dialog_menu_danhSachPhat = view1.findViewById(R.id.lv_dialog_menu_danhSachPhat);
        TextView tv_dialog_soLuong_danhSach = view1.findViewById(R.id.tv_dialog_soLuong_danhSach);

        Dialog dialog = builder.create();

        playLists = new ArrayList<>();
        playLists = playList_dao.getAllPlayList();

        List_DanhSach_Adapter arrayAdapter = new List_DanhSach_Adapter(TrinhPhatNhac_Activity.this, playLists);
        lv_dialog_menu_danhSachPhat.setAdapter(arrayAdapter);

        tv_dialog_soLuong_danhSach.setText("Danh sách phát của tôi " + "(" + playLists.size() + ")");

        lv_dialog_menu_danhSachPhat.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // khi nhấn vào thì thêm bài hát vào danh sách
                // nếu bài hát chưa tồn tại trong danh sách thì thêm vào
                if (!playList_dao.checkSongInPlayList(playLists.get(position).getId(), music.getId())) {
                    playList_dao.addTrackToPlaylist(TrinhPhatNhac_Activity.this, music.getId(), Long.parseLong(playLists.get(position).getId()));

                    // nếu danh sách đang phát bằng danh sách hiện tại vừa thêm vào
                    if (MainActivity.songPlayList.equals(MainActivity.checkListMusic)) {
                        MainActivity.songPlayList.add(music);
                    }
                    Toast.makeText(TrinhPhatNhac_Activity.this, "Thêm vào danh sách thành công", Toast.LENGTH_SHORT).show();
                    dialog.cancel();

                } else {
                    Toast.makeText(TrinhPhatNhac_Activity.this, "Bài hát đã tồn tại trong danh sách", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.show();

    }

    private void musicOffTime(long milliseconds) {
        handler.postDelayed(runnable, milliseconds);
    }

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

    private String getCurrentPosition(int currentPosition) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
        return simpleDateFormat.format(currentPosition);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(TrinhPhatNhac_Activity.this, MainActivity.class));
    }

    // hàm lắng nghe sự kiện sau khi chọn bài hát xong
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
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myBroadCastUpdateUiTrinhPhat);
    }

    private class MyBroadCastUpdateUiTrinhPhat extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int i = intent.getIntExtra("position", -1);
            int changePlay = intent.getIntExtra("btnPlay", -1);
            if (i != -1) {
                music = MainActivity.checkListMusic.get(i);
                Bitmap bitmap = getImage(music.getUri());
                // nếu bitmap khác null thì gán ảnh
                if (bitmap != null) {
                    img_logo_trinhPhatNhac.setImageBitmap(bitmap);
                } else {
                    img_logo_trinhPhatNhac.setImageResource(R.drawable.ic_no_music);
                }
                tv_music_trinhPhatNhac.setText(music.getTitle());
                tv_ngheSi_trinhPhatNhac.setText(music.getArtist());
                img_play_trinhPhatNhac.setImageResource(R.drawable.ic_pause2);

                // kiểm tra xem bài hát có nằm trong mục yêu thích không
                if (favorite_dao.searchSongFavorite(music.getId())) {
                    img_uaThich_trinhPhatNhac.setImageResource(R.drawable.ic_favorite_blue);
                } else {
                    img_uaThich_trinhPhatNhac.setImageResource(R.drawable.ic_favorite);
                }

            } else if (changePlay != -1) {
                if (changePlay == 0) {
                    img_play_trinhPhatNhac.setImageResource(R.drawable.ic_play2);
                } else {
                    img_play_trinhPhatNhac.setImageResource(R.drawable.ic_pause2);
                }
            }
        }
    }

}