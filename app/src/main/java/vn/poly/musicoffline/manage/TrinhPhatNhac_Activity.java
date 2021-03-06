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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
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
    Animation a1, a2;
    Music music;
    final int REQUEST_CODE_ACTION_PICK = 123;
    PlayList_Dao playList_dao;
    Favorite_Dao favorite_dao;
    List<PlayList> playLists;
    MyBroadCastUpdateUiTrinhPhat myBroadCastUpdateUiTrinhPhat;
    public static final String BROADCAST_ACTION_TRINHPHAT = "updateUITrinhPhat";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trinh_phat_nhac_);
        anhXa();
        toolbar_trinhPhatNhac();

        a1 = AnimationUtils.loadAnimation(this, R.anim.music_rorate);
        a2 = AnimationUtils.loadAnimation(this, R.anim.music_stop);

        if (MainActivity.playerMusicService.mediaPlayer.isPlaying()) {
            img_logo_trinhPhatNhac.startAnimation(a1);
        }

        playList_dao = new PlayList_Dao(this);
        favorite_dao = new Favorite_Dao(this);

        // lấy ra bài hát hiện tại
        music = MainActivity.checkListMusic.get(Music_Fragment.positionBaiHat);
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

                case R.id.menu_ngheSi_trinhPhatNhac:
                    startActivity(new Intent(TrinhPhatNhac_Activity.this, NgheSi_Activity.class).putExtra("artist", music.getArtist()));
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

        //set tên nghệ sĩ,tên bài hát, tổng thời gian, thời gian chạy
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
        img_uaThich_trinhPhatNhac.setOnClickListener(view -> {
            if (favorite_dao.searchSongFavorite(music.getId())) {
                // xóa và cập nhật lại danh sách nếu list và adapter của ưa thích đã đc tạo
                if (MainActivity.music_uaThich_adapter != null) {
                    MainActivity.favoriteList.remove(music);
                    MainActivity.music_uaThich_adapter.notifyDataSetChanged();

                    // đang phát ở list yêu thích mà trong bài hát xóa tim bài cuối cùng thì chuyển sang list bài hát
                    if (MainActivity.checkListMusic.equals(MainActivity.favoriteList) && MainActivity.favoriteList.size() == 0) {
                        MainActivity.checkListMusic = MainActivity.listSong;
                        Music_Fragment.positionBaiHat = 0;
                    }

                }
                // == true là đã nằm trong danh sách favorite
                //1 là xóa khỏi danh sách yêu thích
                favorite_dao.update(music.getId(), 1);
                img_uaThich_trinhPhatNhac.setImageResource(R.drawable.ic_favorite);
            } else {

                // thêm và cập nhật lại danh sách nếu list và adapter của ưa thích đã đc tạo
                if (MainActivity.music_uaThich_adapter != null) {
                    MainActivity.favoriteList.add(music);
                    MainActivity.music_uaThich_adapter.notifyDataSetChanged();
                }

                // ngược lại chưa nằm trong danh sách
                // 2 là thêm vào danh sách yêu thích
                favorite_dao.update(music.getId(), 2);
                img_uaThich_trinhPhatNhac.setImageResource(R.drawable.ic_favorite_blue);
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
            if (Music_Fragment.positionBaiHat == MainActivity.checkListMusic.size()) {
                Music_Fragment.positionBaiHat = 0;
            }
            music = MainActivity.checkListMusic.get(Music_Fragment.positionBaiHat);
            MainActivity.playerMusicService.play(music, MainActivity.checkListMusic);
        });

        // nhấn vào quay lại
        img_quayLai_trinhPhatNhac.setOnClickListener(v -> {
            Music_Fragment.positionBaiHat--;
            // khi cick quay lại mà bé hơn 0 thì cho về vị trí cuối cùng của mảng
            if (Music_Fragment.positionBaiHat < 0) {
                Music_Fragment.positionBaiHat = MainActivity.checkListMusic.size() - 1;
            }
            music = MainActivity.checkListMusic.get(Music_Fragment.positionBaiHat);
            MainActivity.playerMusicService.play(music, MainActivity.checkListMusic);
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

        DanhSachBaiHat_Adapter adapter = new DanhSachBaiHat_Adapter(MainActivity.checkListMusic);
        lv_dialog_dangPhat.setAdapter(adapter);

        tv_dialog_soLuong_dangPhat.setText("Hiện đang phát " + "(" + MainActivity.checkListMusic.size() + ")");

        lv_dialog_dangPhat.setOnItemClickListener((parent, view2, position, id) -> {
            Music_Fragment.positionBaiHat = position;
            MainActivity.playerMusicService.play(MainActivity.checkListMusic.get(position), MainActivity.checkListMusic);
        });

        AlertDialog alertDialog = builder.show();
    }

    //khi nhấn vào danh sách phát
    public void danhSachPhat(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view1 = LayoutInflater.from(this).inflate(R.layout.dialog_list_danh_sach, null);
        builder.setView(view1);

        ListView lv_dialog_danhSach = view1.findViewById(R.id.lv_dialog_danhSach);
        TextView tv_dialog_danhSach = view1.findViewById(R.id.tv_dialog_danhSach);
        FrameLayout fl_dialog_danhSach = view1.findViewById(R.id.fl_dialog_danhSach);

        Dialog dialog = builder.create();

        playLists = new ArrayList<>();
        playLists = playList_dao.getAllPlayList();

        if (playLists.size() == 0) {
            fl_dialog_danhSach.setVisibility(View.VISIBLE);
        }

        List_DanhSach_Adapter arrayAdapter = new List_DanhSach_Adapter(TrinhPhatNhac_Activity.this, playLists);
        lv_dialog_danhSach.setAdapter(arrayAdapter);

        tv_dialog_danhSach.setText("Danh sách phát của tôi " + "(" + playLists.size() + ")");

        lv_dialog_danhSach.setOnItemClickListener((parent, view2, position, id) -> {
            // khi nhấn vào thì thêm bài hát vào danh sách
            // nếu bài hát chưa tồn tại trong danh sách thì thêm vào
            if (!playList_dao.checkSongInPlayList(playLists.get(position).getId(), music.getId())) {
                playList_dao.addTrackToPlaylist(TrinhPhatNhac_Activity.this, music.getId(), Long.parseLong(playLists.get(position).getId()));

                // nếu danh sách đang phát bằng danh sách hiện tại vừa thêm vào
                if (MainActivity.songPlayList.equals(MainActivity.checkListMusic)) {
                    MainActivity.songPlayList.add(music);
                }
                Toast.makeText(getApplicationContext(), "Thêm bài hát " + music.getTitle() + " vào danh sách " + playLists.get(position).getName(), Toast.LENGTH_SHORT).show();
                dialog.cancel();

            } else {
                Toast.makeText(getApplicationContext(), "Bài hát " + music.getTitle() + " đã tồn tại trong danh sách " + playLists.get(position).getName(), Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
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
                tv_tongThoiGian_trinhPhatNhac.setText(convertDuration(MediaPlayer.create(context, Uri.parse(music.getUri())).getDuration()));
                img_play_trinhPhatNhac.setImageResource(R.drawable.ic_pause2);
                img_logo_trinhPhatNhac.startAnimation(a1);

                // kiểm tra xem bài hát có nằm trong mục yêu thích không
                if (favorite_dao.searchSongFavorite(music.getId())) {
                    img_uaThich_trinhPhatNhac.setImageResource(R.drawable.ic_favorite_blue);
                } else {
                    img_uaThich_trinhPhatNhac.setImageResource(R.drawable.ic_favorite);
                }

            } else if (changePlay != -1) {
                if (changePlay == 0) {
                    img_play_trinhPhatNhac.setImageResource(R.drawable.ic_play2);
                    img_logo_trinhPhatNhac.startAnimation(a2);
                } else {
                    img_play_trinhPhatNhac.setImageResource(R.drawable.ic_pause2);
                    img_logo_trinhPhatNhac.startAnimation(a1);
                }
            }
        }

        private String convertDuration(int duration) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
            return simpleDateFormat.format(duration);
        }
    }

}