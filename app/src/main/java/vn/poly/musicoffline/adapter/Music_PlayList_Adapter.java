package vn.poly.musicoffline.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import vn.poly.musicoffline.MainActivity;
import vn.poly.musicoffline.R;
import vn.poly.musicoffline.fragment.Music_Fragment;
import vn.poly.musicoffline.model.Music;
import vn.poly.musicoffline.sql.PlayList_Dao;

public class Music_PlayList_Adapter extends BaseAdapter {
    Context context;
    List<Music> musicList;
    String idPlayList;
    PlayList_Dao playList_dao;

    public Music_PlayList_Adapter(Context context, List<Music> musicList, String idPlayList) {
        this.context = context;
        this.musicList = musicList;
        this.idPlayList = idPlayList;
        playList_dao = new PlayList_Dao(context);
    }

    @Override
    public int getCount() {
        return musicList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = LayoutInflater.from(context).inflate(R.layout.view_music_playlist, null);

        ImageView img_view_music = view.findViewById(R.id.img_view_music_playlist);
        ImageView img_view_more_music_playlist = view.findViewById(R.id.img_view_more_music_playlist);
        TextView tv_view_baiHat_music = view.findViewById(R.id.tv_view_baiHat_music_playlist);
        TextView tv_view_ngheSi_music = view.findViewById(R.id.tv_view_ngheSi_music_playlist);
        TextView tv_view_thoiGian_music = view.findViewById(R.id.tv_view_thoiGian_music_playlist);

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

        //khi nhấn vào img more
        img_view_more_music_playlist.setOnClickListener(view1 -> {
            // create popup menu
            PopupMenu popupMenu = new PopupMenu(view1.getContext(), img_view_more_music_playlist);
            popupMenu.getMenuInflater().inflate(R.menu.menu_delete_music, popupMenu.getMenu());
            // bắt sự kiện khi nhấn vào item ở menu
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.id.menu_delete:
                        //hiển thị dialog xóa
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(context);
                        View view2 = LayoutInflater.from(context).inflate(R.layout.dialog_delete, null);
                        builder2.setCancelable(false);
                        builder2.setView(view2);

                        TextView tv_dialog_title_delete = view2.findViewById(R.id.tv_dialog_title_delete);
                        TextView tv_dialog_delete = view2.findViewById(R.id.tv_dialog_delete);
                        TextView tv_dialog_cancel = view2.findViewById(R.id.tv_dialog_cancel);

                        tv_dialog_title_delete.setText("Xóa bài hát: " + music.getTitle() + " khỏi danh sách?");

                        final AlertDialog alertDialog2 = builder2.show();

                        tv_dialog_cancel.setOnClickListener(view3 -> alertDialog2.dismiss());

                        tv_dialog_delete.setOnClickListener(view32 -> {
                            playList_dao.deletSongInPlayList(musicList.get(position).getIdMemberPlayList(), idPlayList);
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

                            Toast.makeText(context, "Xóa bài hát " + music.getTitle(), Toast.LENGTH_SHORT).show();
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
