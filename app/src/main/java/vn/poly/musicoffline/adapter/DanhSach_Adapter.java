package vn.poly.musicoffline.adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import vn.poly.musicoffline.MainActivity;
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
    public View getView(int position, View view, ViewGroup viewGroup) {
        view = LayoutInflater.from(context).inflate(R.layout.view_danh_sach, null);

        TextView tv_view_name_playList = view.findViewById(R.id.tv_view_name_playList);
        ImageView img_view_more_playList = view.findViewById(R.id.img_view_more_playList);

        PlayList playList = playLists.get(position);

        tv_view_name_playList.setText(playList.getName());

        img_view_more_playList.setOnClickListener(view12 -> {
            PopupMenu popupMenu = new PopupMenu(view12.getContext(), img_view_more_playList);
            popupMenu.getMenuInflater().inflate(R.menu.menu_edit_folder, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
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

                        AlertDialog alertDialog = builder.show();

                        tv_dialog_title_folder.setText("Sửa danh sách " + playList.getName());
                        tv_dialog_add_folder.setText("Sửa");

                        tv_dialog_add_folder.setOnClickListener(view23 -> {
                            // sửa danh sách
                            String name = txt_dialog_name_folder.getText().toString().trim();
                            if (playList_dao.checkNamePlayList(name) && !name.equals("") || name.equals(playList.getName())) {
                                playList.setName(name);
                                playList_dao.updatePlayList(playList.getId(), name);
                                notifyDataSetChanged();
                                Toast.makeText(context, "Sửa danh sách " + playList.getName(), Toast.LENGTH_SHORT).show();
                                alertDialog.cancel();

                            } else if (name.isEmpty()) {
                                txt_dialog_name_folder.setError("Không được nhập trống");
                                return;

                            } else {
                                txt_dialog_name_folder.setError("Danh sách " + name + " đã tồn tại");
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

                        tv_dialog_delete.setOnClickListener(view32 -> {
                            if (MainActivity.idPlayListDangPhat.equals(playList.getId())) {
                                Toast.makeText(context, "Danh sách " + playList.getName() + " không thể xóa khi đang phát bài hát trong danh sách", Toast.LENGTH_SHORT).show();

                            } else {
                                // xóa danh sách
                                playList_dao.deletePlayList(playList.getId());
                                playLists.remove(position);
                                notifyDataSetChanged();
                                Toast.makeText(context, "Xóa danh sách " + playList.getName(), Toast.LENGTH_SHORT).show();
                            }
                            alertDialog1.cancel();
                        });
                        break;
                }
                return false;
            });
            popupMenu.show();
        });

        return view;
    }

}
