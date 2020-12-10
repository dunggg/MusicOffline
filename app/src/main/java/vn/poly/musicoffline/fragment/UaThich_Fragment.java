package vn.poly.musicoffline.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

import vn.poly.musicoffline.MainActivity;
import vn.poly.musicoffline.R;
import vn.poly.musicoffline.adapter.Music_UaThich_Adapter;
import vn.poly.musicoffline.sql.Favorite_Dao;

public class UaThich_Fragment extends Fragment {
    ListView lv_uaThich;
    Favorite_Dao favorite_dao;

    public UaThich_Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ua_thich_, container, false);

        //ánh xạ
        lv_uaThich = view.findViewById(R.id.lv_uaThich);
        favorite_dao = new Favorite_Dao(getContext());

        //list
        if (MainActivity.checkListMusic.equals(MainActivity.favoriteList)==false) {
            MainActivity.favoriteList = new ArrayList<>();
            MainActivity.favoriteList = favorite_dao.getAllSongInFavorite();
        }

        MainActivity.music_uaThich_adapter = new Music_UaThich_Adapter(MainActivity.favoriteList, getContext());
        lv_uaThich.setAdapter(MainActivity.music_uaThich_adapter);

        lv_uaThich.setOnItemClickListener((parent, view1, position, id) -> {
            // nếu đang random thì tắt đi
            MainActivity.playerMusicService.checkRandom = false;
            MainActivity.checkListMusic = MainActivity.favoriteList;
            Music_Fragment.positionBaiHat = position;
            MainActivity.playerMusicService.play(MainActivity.favoriteList.get(position), MainActivity.favoriteList);
        });

        return view;
    }

    // kết thúc vòng đời của fragment
    @Override
    public void onDetach() {
        super.onDetach();
        MainActivity.music_uaThich_adapter = null;
    }

}