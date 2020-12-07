package vn.poly.musicoffline.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import vn.poly.musicoffline.fragment.DanhSach_Fragment;
import vn.poly.musicoffline.fragment.Music_Fragment;
import vn.poly.musicoffline.fragment.NgheSi_Fragment;
import vn.poly.musicoffline.fragment.UaThich_Fragment;

public class ViewPager_Adapter extends FragmentStatePagerAdapter {

    // viewpage hỗ trợ bottom navigation khi kéo màn hình qua trái phải
    public ViewPager_Adapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    // tạo ra các fragment
    @NonNull
    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new Music_Fragment();
                break;
            case 1:
                fragment = new DanhSach_Fragment();
                break;
            case 2:
                fragment = new NgheSi_Fragment();
                break;
            case 3:
                fragment = new UaThich_Fragment();
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 4;
    }
}
