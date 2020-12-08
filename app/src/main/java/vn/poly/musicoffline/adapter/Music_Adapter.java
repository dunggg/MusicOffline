package vn.poly.musicoffline.adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import vn.poly.musicoffline.fragment.Music_Fragment;
import vn.poly.musicoffline.MainActivity;
import vn.poly.musicoffline.model.Music;
import vn.poly.musicoffline.R;
import vn.poly.musicoffline.model.PlayList;
import vn.poly.musicoffline.sql.PlayList_Dao;

import java.util.ArrayList;
import java.util.List;

public class Music_Adapter extends BaseAdapter {
    List<Music> musicList;
    List<PlayList> playLists;
    PlayList_Dao playList_dao;
    Context context;

    public Music_Adapter(List<Music> musicList, Context context) {
        this.musicList = musicList;
        this.context = context;
        playList_dao = new PlayList_Dao(context);
    }

    @Override
    public int getCount() {
        return musicList.size();
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
        view = LayoutInflater.from(context).inflate(R.layout.view_music, null);

        ImageView img_view_music = view.findViewById(R.id.img_view_music);
        ImageView img_view_more_music = view.findViewById(R.id.img_view_more_music);
        TextView tv_view_baiHat_music = view.findViewById(R.id.tv_view_baiHat_music);
        TextView tv_view_ngheSi_music = view.findViewById(R.id.tv_view_ngheSi_music);
        TextView tv_view_thoiGian_music = view.findViewById(R.id.tv_view_thoiGian_music);

        // gán các giá trị cho view con trong layout
        Music music = musicList.get(position);
        tv_view_baiHat_music.setText(music.getTitle());
        tv_view_ngheSi_music.setText(music.getArtist());
        tv_view_thoiGian_music.setText(music.getDuration());

        // load ảnh trên 1 luồng khác
        AsyncTask<String, Void, Bitmap> asyncTask = new AsyncTask<String, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(String... strings) {
                return getImage(music.getUri());
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                if (bitmap != null) {
                    img_view_music.setImageBitmap(bitmap);
                } else {
                    img_view_music.setImageResource(R.drawable.ic_no_music);
                }
            }
        }.execute(music.getUri());

        // khi nhấn vào img more
        img_view_more_music.setOnClickListener(view1 -> {
            // create popup menu
            PopupMenu popupMenu = new PopupMenu(view1.getContext(), img_view_more_music);
            popupMenu.getMenuInflater().inflate(R.menu.menu_music, popupMenu.getMenu());
            // bắt sự kiện khi nhấn vào item ở menu
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.id.menu_add_danhSachPhat:
                        AlertDialog.Builder builder = new AlertDialog.Builder(viewGroup.getContext());
                        View viewa1 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.dialog_menu_danh_sach_phat, null);
                        builder.setView(viewa1);

                        ListView lv_dialog_menu_danhSachPhat = viewa1.findViewById(R.id.lv_dialog_menu_danhSachPhat);
                        TextView tv_dialog_soLuong_danhSach = viewa1.findViewById(R.id.tv_dialog_soLuong_danhSach);

                        List_DanhSach_Adapter arrayAdapter = new List_DanhSach_Adapter(context, playLists);
                        lv_dialog_menu_danhSachPhat.setAdapter(arrayAdapter);

                        Dialog dialog = builder.create();

                        playLists = new ArrayList<>();
                        playLists = playList_dao.getAllPlayList();

                        tv_dialog_soLuong_danhSach.setText("Danh sách phát của tôi " + "(" + playLists.size() + ")");

                        // nhấn vào playlist để thêm bài hát
                        lv_dialog_menu_danhSachPhat.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                                // nếu bài hát chưa tồn tại trong danh sách thì thêm vào
                                if (!playList_dao.checkSongInPlayList(playLists.get(i).getId(), music.getId())) {
                                    playList_dao.addTrackToPlaylist(context, music.getId(), Long.parseLong(playLists.get(i).getId()));

                                    // nếu danh sashc đang phát bằng danh sách hiện tại vừa thêm vào
                                    if (MainActivity.songPlayList.equals(MainActivity.checkListMusic)) {
                                        MainActivity.songPlayList.add(music);
                                    }
                                    Toast.makeText(context, "Thêm vào danh sách thành công", Toast.LENGTH_SHORT).show();
                                    dialog.cancel();

                                } else {
                                    Toast.makeText(context, "Bài hát đã tồn tại trong danh sách", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        dialog.show();
                        break;

                    case R.id.menu_delete_music:
                        // hiển thị dialog xóa
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(viewGroup.getContext());
                        View view2 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.dialog_delete, null);
                        builder2.setCancelable(false);
                        builder2.setView(view2);

                        TextView tv_dialog_title_delete = view2.findViewById(R.id.tv_dialog_title_delete);
                        TextView tv_dialog_delete = view2.findViewById(R.id.tv_dialog_delete);
                        TextView tv_dialog_cancel = view2.findViewById(R.id.tv_dialog_cancel);

                        tv_dialog_title_delete.setText("Xóa bài hát: " + music.getTitle());

                        final AlertDialog alertDialog2 = builder2.show();

                        tv_dialog_cancel.setOnClickListener(view3 -> alertDialog2.dismiss());

                        tv_dialog_delete.setOnClickListener(view32 -> {

                            String idBaiHat = music.getId();
                            String idBaiHatDangPhat = MainActivity.checkListMusic.get(Music_Fragment.positionBaiHat).getId();

                            // xóa bài hát theo id bài hát
                            context.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "_id=" + music.getId(), null);
                            musicList.remove(position);
                            notifyDataSetChanged();
                            // nếu id bài hát muốn xóa và bài hát đang phát trùng nhau
                            // ngược lại thì chỉ xóa bài hát mà không thay đổi giao diện
                            if (idBaiHat.equals(idBaiHatDangPhat)) {

                                if (musicList.equals(MainActivity.checkListMusic) == false) {
                                    MainActivity.checkListMusic.remove(Music_Fragment.positionBaiHat);
                                }

                                if (Music_Fragment.positionBaiHat == MainActivity.checkListMusic.size()) {
                                    // nếu là vị trí cuối cùng
                                    // so sánh vị trí bài hát trước khi xóa và size của list sau khi xóa nếu bằng nhau thì chứng tỏ bài hát đấy ở cuối cùng của list
                                    if (MainActivity.checkListMusic.size() == 0) {
                                        // nếu sau khi xóa size bằng 0 thì dừng nhạc tắt thông báo xóa dữ liệu lưu trữ
                                        MainActivity.playerMusicService.stop();
                                        MainActivity.playerMusicService.clearData();
                                        MainActivity.playerMusicService.hideNotification();
                                        Intent intent = new Intent(MainActivity.BROADCAST_ACTION_MAIN);
                                        intent.putExtra("position", -1);
                                        context.sendBroadcast(intent);
                                    } else {
                                        // nếu sau khi xóa vẫn còn nhạc
                                        Music_Fragment.positionBaiHat = 0;
                                        MainActivity.playerMusicService.play(MainActivity.checkListMusic.get(0), MainActivity.checkListMusic);
                                    }
                                } else {
                                    // nếu không phải vị trí cuối cùng
                                    MainActivity.playerMusicService.play(MainActivity.checkListMusic.get(Music_Fragment.positionBaiHat), MainActivity.checkListMusic);
                                }
                            }

                            Toast.makeText(context, "Xóa thành công", Toast.LENGTH_SHORT).show();
                            alertDialog2.dismiss();
                        });
                        break;

                }
                return false;
            });
            popupMenu.show();
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

}
