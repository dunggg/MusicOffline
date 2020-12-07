package vn.poly.musicoffline.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import vn.poly.musicoffline.R;
import vn.poly.musicoffline.adapter.DanhSach_Adapter;
import vn.poly.musicoffline.manage.DanhSach_Activity;
import vn.poly.musicoffline.model.PlayList;
import vn.poly.musicoffline.sql.PlayList_Dao;

public class DanhSach_Fragment extends Fragment {
    FloatingActionButton float_add_danhSach;
    ListView lv_frag_danhSach;
    List<PlayList> playLists;
    PlayList_Dao playList_dao;

    public DanhSach_Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_danh_sach_, container, false);

        playList_dao = new PlayList_Dao(getContext());
        //ánh xạ
        float_add_danhSach = view.findViewById(R.id.float_add_danhSach);
        lv_frag_danhSach = view.findViewById(R.id.lv_frag_danhSach);

        playLists = new ArrayList<>();
        playLists = playList_dao.getAllPlayList();
        DanhSach_Adapter danhSach_adapter = new DanhSach_Adapter(playLists,getContext());
        lv_frag_danhSach.setAdapter(danhSach_adapter);

        //chuyển tới danh sách
        lv_frag_danhSach.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getContext(),DanhSach_Activity.class);
                intent.putExtra("idPlayList",playLists.get(i).getId());
                startActivity(intent);
            }
        });

        //tạo dannh sách mới
        float_add_danhSach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                View view1 = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_folder, null);
                builder.setCancelable(false);
                builder.setView(view1);

                TextView tv_dialog_title_folder = view1.findViewById(R.id.tv_dialog_title_folder);
                EditText txt_dialog_name_folder = view1.findViewById(R.id.txt_dialog_name_folder);
                EditText txt_dialog_id_folder = view1.findViewById(R.id.txt_dialog_id_folder);
                TextView tv_dialog_add_folder = view1.findViewById(R.id.tv_dialog_add_folder);
                TextView tv_dialog_cancel_folder = view1.findViewById(R.id.tv_dialog_cancel_folder);

                AlertDialog alertDialog = builder.show();

                tv_dialog_title_folder.setText("Thêm danh sách phát mới");
                tv_dialog_add_folder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String name = txt_dialog_name_folder.getText().toString().trim();
                        String id = txt_dialog_id_folder.getText().toString().trim();
                        playList_dao.insertPlayList(Long.parseLong(id),name);
                        playLists.add(new PlayList(id,name));
                        danhSach_adapter.notifyDataSetChanged();
                        alertDialog.cancel();

                    }
                });

                tv_dialog_cancel_folder.setOnClickListener(view2 -> alertDialog.cancel());

            }
        });

        return view;
    }
}