package vn.poly.musicoffline.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import vn.poly.musicoffline.R;
import vn.poly.musicoffline.model.PlayList;
import vn.poly.musicoffline.sql.PlayList_Dao;

public class DanhSach_Adapter extends BaseAdapter {
    List<PlayList> playLists;
    PlayList_Dao playList_dao;
    Context context;

    public DanhSach_Adapter(List<PlayList> playLists, Context context) {
        this.playLists = playLists;
        this.context = context;
        playList_dao = new PlayList_Dao(context);
    }

    @Override
    public int getCount() {
        return playLists.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        int position = i;
        view = LayoutInflater.from(context).inflate(R.layout.view_danh_sach, null);

        TextView tv_view_name_playList = view.findViewById(R.id.tv_view_name_playList);
        ImageView img_view_playList = view.findViewById(R.id.img_view_playList);
        ImageView img_view_more_playList = view.findViewById(R.id.img_view_more_playList);

        PlayList playList = playLists.get(i);

        tv_view_name_playList.setText(playList.getName());
        if (img_view_playList == null) {
            img_view_playList.setImageResource(R.drawable.ic_no_music);
        }

        img_view_more_playList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(view.getContext(), img_view_more_playList);
                popupMenu.getMenuInflater().inflate(R.menu.menu_edit_folder, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.menu_edit_folder:
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                View view1 = LayoutInflater.from(context).inflate(R.layout.dialog_add_folder, null);
                                builder.setCancelable(false);
                                builder.setView(view1);

                                TextView tv_dialog_title_folder = view1.findViewById(R.id.tv_dialog_title_folder);
                                EditText txt_dialog_name_folder = view1.findViewById(R.id.txt_dialog_name_folder);
                                TextView tv_dialog_add_folder = view1.findViewById(R.id.tv_dialog_add_folder);
                                TextView tv_dialog_cancel_folder = view1.findViewById(R.id.tv_dialog_cancel_folder);
                                EditText txt_dialog_id_folder = view1.findViewById(R.id.txt_dialog_id_folder);
                                txt_dialog_id_folder.setVisibility(View.GONE);

                                AlertDialog alertDialog = builder.show();

                                tv_dialog_title_folder.setText("Sửa danh sách phát " + playList.getName());
                                tv_dialog_add_folder.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        // sửa danh sách
                                        String name = txt_dialog_name_folder.getText().toString().trim();
                                        playList.setName(name);
                                        playList_dao.updatePlayList(playList.getId(), name);
                                        notifyDataSetChanged();
                                        alertDialog.cancel();
                                    }
                                });

                                tv_dialog_cancel_folder.setOnClickListener(view22 -> alertDialog.cancel());

                                break;

                            case R.id.menu_delete_folder:
                                AlertDialog.Builder builder1 = new AlertDialog.Builder(viewGroup.getContext());
                                View view2 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.dialog_delete, null);
                                builder1.setCancelable(false);
                                builder1.setView(view2);

                                TextView tv_dialog_title_delete = view2.findViewById(R.id.tv_dialog_title_delete);
                                TextView tv_dialog_delete = view2.findViewById(R.id.tv_dialog_delete);
                                TextView tv_dialog_cancel = view2.findViewById(R.id.tv_dialog_cancel);

                                AlertDialog alertDialog1 = builder1.show();

                                tv_dialog_title_delete.setText("Xóa danh sách: " + playList.getName());
                                tv_dialog_cancel.setOnClickListener(view3 -> alertDialog1.cancel());

                                tv_dialog_delete.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        // xóa danh sách
                                        playList_dao.deletePlayList(playList.getId());
                                        playLists.remove(position);
                                        notifyDataSetChanged();
                                        Toast.makeText(context, "Xóa thành công", Toast.LENGTH_SHORT).show();
                                        alertDialog1.cancel();
                                    }
                                });

                                break;

                        }
                        return false;
                    }
                });
                popupMenu.show();
            }

        });

        return view;
    }

}
