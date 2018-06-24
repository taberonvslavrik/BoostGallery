package com.taberonvslavrik.boostgallery.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.taberonvslavrik.boostgallery.util.ImageLoader;

/**
 * @author Ivan Lavryshyn.
 */

public class ImageAdapter extends BaseAdapter {

    private String[] mFilePathArray = new String[]{};

    public void setData(String[] filePathArray) {
        this.mFilePathArray = filePathArray;
        notifyDataSetChanged();
    }

    public String[] getData() {
        return mFilePathArray;
    }

    public int getCount() {
        return mFilePathArray.length;
    }

    public Object getItem(int position) {
        return mFilePathArray[position];
    }

    public long getItemId(int position) {
        return mFilePathArray[position].hashCode();
    }

    public View getView(final int position, final View convertView, ViewGroup parent) {
        ImageView imageView = (ImageView) convertView;
        if (imageView == null) {
            imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(parent.getMeasuredWidth() / 2,
                    parent.getMeasuredHeight() / 2));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setBackgroundResource(android.R.color.darker_gray);
        }

        final ImageView finalImageView = imageView;
        ImageLoader.loadImage(mFilePathArray[position], finalImageView);
        return imageView;
    }
}