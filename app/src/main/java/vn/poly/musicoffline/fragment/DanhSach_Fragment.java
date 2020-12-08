package vn.poly.musicoffline.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import vn.poly.musicoffline.R;
import vn.poly.musicoffline.adapter.DanhSach_Adapter;
import vn.poly.musicoffline.manage.DanhSach_Activity;
import vn.poly.musicoffline.model.PlayList;
import vn.poly.musicoffline.sql.PlayList_Dao;

public class DanhSach_Fragment extends Fragment {
    TextView tv_soLuong_danhSach;
    ImageView img_add_danhSach;
    ListView lv_frag_danhSach;
    List<PlayList> playLists;
    PlayList_Dao playList_dao;
    DanhSach_Adapter danhSach_adapter;
    Long idPlayList;

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
        tv_soLuong_danhSach = view.findViewById(R.id.tv_soLuong_danhSach);
        img_add_danhSach = view.findViewById(R.id.img_add_danhSach);
        lv_frag_danhSach = view.findViewById(R.id.lv_frag_danhSach);

        playLists = new ArrayList<>();
        playLists = playList_dao.getAllPlayList();

        danhSach_adapter = new DanhSach_Adapter(playLists, getContext());
        lv_frag_danhSach.setAdapter(danhSach_adapter);
        tv_soLuong_danhSach.setText(playLists.size() + " danh sách");

        if (playLists.size() != 0) {
            // nếu đã có playlist
            // id của playlist
            idPlayList = Long.parseLong(playLists.get(playLists.size() - 1).getId());
        } else {
            // nếu chưa có playlist nào thì id  == 0
            idPlayList = 999L;
        }

        //chuyển tới danh sách
        lv_frag_danhSach.setOnItemClickListener((adapterView, view13, i, l) -> {
            Intent intent = new Intent(getContext(), DanhSach_Activity.class);
            intent.putExtra("idPlayList", playLists.get(i).getId());
            intent.putExtra("name", playLists.get(i).getName());
            startActivity(intent);
        });

        //tạo dannh sách mới
        img_add_danhSach.setOnClickListener(view12 -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            View view1 = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_folder, null);
            builder.setCancelable(false);
            builder.setView(view1);

            TextView tv_dialog_title_folder = view1.findViewById(R.id.tv_dialog_title_folder);
            EditText txt_dialog_name_folder = view1.findViewById(R.id.txt_dialog_name_folder);
            TextView tv_dialog_add_folder = view1.findViewById(R.id.tv_dialog_add_folder);
            TextView tv_dialog_cancel_folder = view1.findViewById(R.id.tv_dialog_cancel_folder);

            AlertDialog alertDialog = builder.show();

            tv_dialog_title_folder.setText("Thêm danh sách phát mới");
            tv_dialog_add_folder.setOnClickListener(view22 -> {
                String name = txt_dialog_name_folder.getText().toString().trim();
                // kiểm tra tên đã tồn tại chưa == true là chưa tồn tại và không để trống
                if (playList_dao.checkNamePlayList(name) && !name.equals("")) {
                    idPlayList++;
                    playList_dao.insertPlayList(idPlayList, name);
                    playLists.add(new PlayList(idPlayList.toString(), name));
                    danhSach_adapter.notifyDataSetChanged();
                    alertDialog.cancel();
                    Toast.makeText(getContext(), "Thêm danh sách " + name, Toast.LENGTH_SHORT).show();
                } else if (name.isEmpty()) {
                    txt_dialog_name_folder.setError("Không được nhập trống");
                    return;
                } else {
                    txt_dialog_name_folder.setError("Danh sách " + name + " đã tồn tại");
                }
            });

            tv_dialog_cancel_folder.setOnClickListener(view2 -> alertDialog.cancel());
        });

        return view;
    }

}