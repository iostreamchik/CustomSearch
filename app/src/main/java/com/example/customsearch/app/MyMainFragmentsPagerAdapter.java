package com.example.customsearch.app;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import com.example.customsearch.app.fragments.FavoritesImagesFragment;
import com.example.customsearch.app.fragments.ImageListFragment;

/**
 * In this case we can use (and it will be more fast) FragmentStatePagerAdapter,
 * but in that way we lose capability to use refresh() method in 303 line of
 * ImageListFragment
 */
public class MyMainFragmentsPagerAdapter extends FragmentPagerAdapter {
	private String[] fragmentNames;

	public MyMainFragmentsPagerAdapter(FragmentManager fm, Context context) {
		super(fm);
		fragmentNames = context.getResources().getStringArray(R.array.myFragments);
	}

	@Override
	public Fragment getItem(int i) {
		Fragment fragment = new Fragment();
		switch (i) {
			case 0:
				fragment = new ImageListFragment();
				break;
			case 1:
				fragment = new FavoritesImagesFragment();
				break;
		}
		return fragment;
	}

	@Override
	public int getCount() {
		return fragmentNames.length;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return fragmentNames[position];
	}

	public Fragment getFragment(ViewPager container, int position, FragmentManager fm) {
		String name = makeFragmentName(container.getId(), position);
		return fm.findFragmentByTag(name);
	}

	private String makeFragmentName(int viewId, int index) {
		return "android:switcher:" + viewId + ":" + index;
	}
}
