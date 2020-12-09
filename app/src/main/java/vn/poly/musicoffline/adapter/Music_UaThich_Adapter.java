package vn.poly.musicoffline.adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import vn.poly.musicoffline.MainActivity;
import vn.poly.musicoffline.R;
import vn.poly.musicoffline.fragment.Music_Fragment;
import vn.poly.musicoffline.model.Music;
import vn.poly.musicoffline.model.PlayList;
import vn.poly.musicoffline.sql.Favorite_Dao;
import vn.poly.musicoffline.sql.PlayList_Dao;

public class Music_UaThich_Adapter extends BaseAdapter {
    List<Music> musicList;
    Context context;
    List<PlayList> playLists;
    PlayList_Dao playList_dao;
    Favorite_Dao favorite_dao;

    public Music_UaThich_Adapter(List<Music> musicList, Context context) {
        this.musicList = musicList;
        this.context = context;
        playList_dao = new PlayList_Dao(context);
        favorite_dao = new Favorite_Dao(context);
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
        img_view_more_music.setOnClickListener(view12 -> {
            // create popup menu
            PopupMenu popupMenu = new PopupMenu(view12.getContext(), img_view_more_music);
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

                        Dialog dialog = builder.create();

                        playLists = new ArrayList<>();
                        playLists = playList_dao.getAllPlayList();

                        List_DanhSach_Adapter arrayAdapter = new List_DanhSach_Adapter(context, playLists);
                        lv_dialog_menu_danhSachPhat.setAdapter(arrayAdapter);

                        tv_dialog_soLuong_danhSach.setText("Danh sách phát của tôi " + "(" + playLists.size() + ")");

                        // nhấn vào playlist để thêm bài hát
                        lv_dialog_menu_danhSachPhat.setOnItemClickListener((parent, view1, i, id) -> {
                            // nếu bài hát chưa tồn tại trong danh sách thì thêm vào
                            if (!playList_dao.checkSongInPlayList(playLists.get(i).getId(), music.getId())) {
                                playList_dao.addTrackToPlaylist(context, music.getId(), Long.parseLong(playLists.get(i).getId()));

                                // nếu danh sashc đang phát bằng danh sách hiện tại vừa thêm vào
                                if (MainActivity.songPlayList.equals(MainActivity.checkListMusic)) {
                                    MainActivity.songPlayList.add(music);
                                }
                                Toast.makeText(context, "Thêm bài hát " + music.getTitle() + " vào danh sách " + playLists.get(i).getName(), Toast.LENGTH_SHORT).show();
                                dialog.cancel();

                            } else {
                                Toast.makeText(context, "Bài hát " + music.getTitle() + " đã tồn tại trong danh sách " + playLists.get(i).getName(), Toast.LENGTH_SHORT).show();
                            }
                        });
                        dialog.show();
                        break;

                    case R.id.menu_delete_music:
                        favorite_dao.update(music.getId(), 1);
                        musicList.remove(position);
                        notifyDataSetChanged();

                        // nếu vị trí của bát hát trc khi xóa bằng vị trí của bài hát trên thông báo thì thay đổi giao diện
                        // ngược lại thì chỉ xóa bài hát mà không thay đổi giao diện
                        // nếu list này bằng với list hiện tại đang phát thì xóa mới chạy nhạc mới
                        if (musicList.equals(MainActivity.checkListMusic)) {
                            if (position == Music_Fragment.positionBaiHat) {
                                if (position == musicList.size()) {
                                    // nếu là vị trí cuối cùng
                                    // so sánh vị trí bài hát trước khi xóa và size của list sau khi xóa nếu bằng nhau thì chứng tỏ bài hát đấy ở cuối cùng của list
                                    if (musicList.size() == 0) {
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
                                        MainActivity.playerMusicService.play(musicList.get(0), musicList);
                                    }
                                } else {
                                    // nếu không phải vị trí cuối cùng
                                    MainActivity.playerMusicService.play(musicList.get(Music_Fragment.positionBaiHat), musicList);
                                }
                            }
                        }

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
