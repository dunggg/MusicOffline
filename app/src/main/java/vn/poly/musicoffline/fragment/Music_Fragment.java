package vn.poly.musicoffline.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import vn.poly.musicoffline.adapter.Music_Adapter;
import vn.poly.musicoffline.MainActivity;
import vn.poly.musicoffline.model.Music;
import vn.poly.musicoffline.R;

import java.text.SimpleDateFormat;

public class Music_Fragment extends Fragment {
    TextView tv_soLuong_music;
    ListView lv_frag_music;
    Music_Adapter musicAdapter;
    // view bên activity
    ImageView img_icon, img_play;
    TextView tvBaiHat, tvNgheSi;
    LinearLayout linearLayout_main;

    // biến để lưu vị trí của bài hát
    public static int positionBaiHat = -1;

    public Music_Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_music_, container, false);

        //ánh xạ
        tv_soLuong_music = view.findViewById(R.id.tv_soLuong_music);
        lv_frag_music = view.findViewById(R.id.lv_frag_music);

        tv_soLuong_music.setText(MainActivity.listSong.size() + " bài hát");
        // set customlistview vào listview
        musicAdapter = new Music_Adapter(MainActivity.listSong, getContext());
        lv_frag_music.setAdapter(musicAdapter);

        // ánh xạ đến các view bên activiy
        img_icon = getActivity().findViewById(R.id.img_logo_main);
        img_play = getActivity().findViewById(R.id.img_play_main);
        tvBaiHat = getActivity().findViewById(R.id.tv_music_main);
        tvNgheSi = getActivity().findViewById(R.id.tv_ngheSi_main);
        linearLayout_main = getActivity().findViewById(R.id.linear_music_main);

        lv_frag_music.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // khi nhấn vào item cho hiện trình phát nhạc mini lên
                linearLayout_main.setVisibility(View.VISIBLE);
                Music music = MainActivity.listSong.get(position);
                positionBaiHat = position;
                MainActivity.playerMusicService.play(music, MainActivity.listSong);
                MainActivity.checkListMusic = MainActivity.listSong;
            }
        });

        return view;
    }

    // hàm lấy ảnh từ uri bài hát
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

    // chuyển đổi thời gian của bài hát vì mặc định thời gian của bài hát là kiểu int
    private String convertDuration(int duration) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
        return simpleDateFormat.format(duration);
    }
}