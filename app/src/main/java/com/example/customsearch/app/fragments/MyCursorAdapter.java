package com.example.customsearch.app.fragments;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.customsearch.app.R;
import com.example.customsearch.app.database.DatabaseHandler;
import com.example.customsearch.app.entitys.MyImage;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class MyCursorAdapter extends CursorAdapter {

	private DatabaseHandler.MyCursor myCursor;
	private int fragmentID;
	private CallbackSelected mCallback;

	public interface CallbackSelected {
		void onImageSelected(MyImage image, int fragmentId);
	}

	public MyCursorAdapter(Context context, DatabaseHandler.MyCursor cursor, int fragmentID) {
		super(context, cursor, 0);
		myCursor = cursor;
		mCallback = (CallbackSelected) context;
		this.fragmentID = fragmentID;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		return inflater.inflate(R.layout.image_list_layout, viewGroup, false);
	}

	@Override
	public void bindView(View view, Context context, final Cursor cursor) {
		final MyImage myImage = myCursor.getImage();
		ViewHolder holder = new ViewHolder(view);
		InputStream is = new ByteArrayInputStream(myImage.getThumbnailBLOB());
		Bitmap bmp = BitmapFactory.decodeStream(is);
		holder.img.setImageBitmap(bmp);
		holder.img.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d("Image #: ", String.valueOf(myImage.getId()));
				loadBigImage(myImage, fragmentID);
			}
		});
		holder.name.setText(myImage.getSnippet());
		holder.box.setTag(myCursor.getPosition());
	}

	private void loadBigImage(MyImage myImage, int fragmentID) {
		mCallback.onImageSelected(myImage, fragmentID);
	}

	private class ViewHolder {
		View view;
		ImageView img;
		TextView name;
		CheckBox box;

		private ViewHolder(View view) {
			this.view = view;
			img = (ImageView) view.findViewById(R.id.thumbnail);
			name = (TextView) view.findViewById(R.id.name);
			box = (CheckBox) view.findViewById(R.id.check);
		}
	}
}
