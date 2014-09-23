package com.example.customsearch.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.example.customsearch.app.animation.DepthPageTransformer;
import com.example.customsearch.app.entitys.MyImage;
import com.example.customsearch.app.fragments.MyCursorAdapter;


public class MainActivity extends SherlockFragmentActivity implements MyCursorAdapter.CallbackSelected {

	private ViewPager mViewPager;
	private MyMainFragmentsPagerAdapter adapter;
	private FragmentManager fm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mViewPager = (ViewPager) findViewById(R.id.myViewPager);
		fm = getSupportFragmentManager();
		adapter = new MyMainFragmentsPagerAdapter(fm, this);

		mViewPager.setAdapter(adapter);
		mViewPager.setPageTransformer(true, new DepthPageTransformer());
	}

	public Fragment getFragmentByPosition(int position) {
		return adapter.getFragment(mViewPager, position, getSupportFragmentManager());
	}


	@Override
	public void onImageSelected(MyImage image, int fragmentId) {
		Log.d("onImageSelected", String.valueOf(fragmentId));
		Intent intent = new Intent(this, ImagePagerActivity.class);
		if (fragmentId == 0) {
			intent.putExtra(ImagePagerActivity.EXTRA_DB_NAME, "mainDB");
		} else {
			intent.putExtra(ImagePagerActivity.EXTRA_DB_NAME, "favDB");
		}
		intent.putExtra(ImagePagerActivity.EXTRA_IMAGE_ID, image.getId());
		startActivityForResult(intent, 1);
	}
}
