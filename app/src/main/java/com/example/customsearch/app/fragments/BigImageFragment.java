package com.example.customsearch.app.fragments;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.actionbarsherlock.app.SherlockFragment;
import com.example.customsearch.app.R;
import com.example.customsearch.app.internet.BigImageTask;
import com.example.customsearch.app.internet.CallbackGetBigImage;

public class BigImageFragment extends SherlockFragment implements CallbackGetBigImage {
	private BigImageTask bigImageTask;
	public static final String EXTRA_IMAGE_LINK = "Link";
	private String imageLink;
	private View view;
	private ImageView imageView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		imageLink = (String) getArguments().getSerializable(EXTRA_IMAGE_LINK);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.big_image_layout, container, false);
		imageView = (ImageView) view.findViewById(R.id.image);
		searchImages(imageLink);
		return view;
	}

	private void searchImages(String query) {
//		ImageLoader imageManager = new ImageLoader(this);
//		imageManager.fetchImage(getSherlockActivity(), 3600, query, imageView);

		if (bigImageTask == null) {
			bigImageTask = new BigImageTask(getSherlockActivity(), this);
			Log.i("FRAGMENT", "Create task: " + bigImageTask.hashCode());
			bigImageTask.execute(query);
		} else {
			Log.i("FRAGMENT", "Set context to task " + bigImageTask.hashCode());
			if (bigImageTask.getStatus() != AsyncTask.Status.RUNNING) {
				bigImageTask = new BigImageTask(getSherlockActivity(), this);
				bigImageTask.setContext(getSherlockActivity());
				bigImageTask.execute(query);
			}
		}
	}

	public static BigImageFragment newInstance(String imageLink) {
		Bundle args = new Bundle();
		args.putSerializable(EXTRA_IMAGE_LINK, imageLink);
		BigImageFragment fragment = new BigImageFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onImageLoad(Bitmap bitmap) {
		if (bitmap != null) {
			imageView.setImageBitmap(bitmap);
			view.invalidate();
		}
	}
}
