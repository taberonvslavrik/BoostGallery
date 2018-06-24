package com.taberonvslavrik.boostgallery.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import com.taberonvslavrik.boostgallery.R;
import com.taberonvslavrik.boostgallery.activity.base.BaseActivity;
import com.taberonvslavrik.boostgallery.adapter.ImageAdapter;

import static com.taberonvslavrik.boostgallery.activity.MainActivity.BundleKeys.BOTTOM;
import static com.taberonvslavrik.boostgallery.activity.MainActivity.BundleKeys.FILES_LIST;
import static com.taberonvslavrik.boostgallery.activity.MainActivity.BundleKeys.INDEX;
import static com.taberonvslavrik.boostgallery.activity.MainActivity.BundleKeys.LEFT;
import static com.taberonvslavrik.boostgallery.activity.MainActivity.BundleKeys.LIST;
import static com.taberonvslavrik.boostgallery.activity.MainActivity.BundleKeys.RIGHT;
import static com.taberonvslavrik.boostgallery.activity.MainActivity.BundleKeys.SCROLL_POSITION;
import static com.taberonvslavrik.boostgallery.activity.MainActivity.BundleKeys.TOP;

/**
 * @author Ivan Lavryshyn.
 */

public class MainActivity extends BaseActivity {

    final static class BundleKeys {
        static final String LEFT = "LEFT";
        static final String TOP = "TOP";
        static final String RIGHT = "RIGHT";
        static final String BOTTOM = "BOTTOM";
        static final String INDEX = "INDEX";
        static final String LIST = "LIST";

        static final String SCROLL_POSITION = "SCROLL_POSITION";
        static final String FILES_LIST = "FILES_LIST";
    }

    private String[] mUrlsFilePath = new String[]{};

    private ProgressDialog mProgressDialog = null;
    private ImageAdapter mImageAdapter = null;
    private GridView gridview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.a_main);

        gridview = findViewById(R.id.gridview);
        mImageAdapter = new ImageAdapter();
        gridview.setAdapter(mImageAdapter);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                startDetailsActivity((ImageView) v, position);
            }
        });

        if (savedInstanceState != null) {
            mImageAdapter.setData(savedInstanceState.getStringArray(FILES_LIST));
            gridview.smoothScrollToPosition(savedInstanceState.getInt(SCROLL_POSITION));
        } else {
            if (isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                loadPhotos();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(SCROLL_POSITION, gridview.getFirstVisiblePosition());
        outState.putStringArray(FILES_LIST, mImageAdapter.getData());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadPhotos();
                } else {
                    finish();
                }
            }
        }
    }

    private void loadPhotos() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mProgressDialog.setMessage(getString(R.string.main_activity_dialog_title));
        mProgressDialog.show();

        final Cursor cursor = this.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            new Thread() {
                public void run() {
                    try {
                        cursor.moveToFirst();
                        mUrlsFilePath = new String[cursor.getCount()];
                        for (int i = 0; i < cursor.getCount(); i++) {
                            cursor.moveToPosition(i);
                            mUrlsFilePath[i] = cursor.getString(1);
                        }
                    } catch (Exception e) {
                        Log.e(this.getClass().getSimpleName(), e.getMessage());
                    } finally {
                        cursor.close();
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mImageAdapter.setData(mUrlsFilePath);
                            mProgressDialog.dismiss();
                        }
                    });
                }
            }.start();
        }
    }

    private void startDetailsActivity(final ImageView imageView, final int imageViewPosition) {
        Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
        int[] screenLocation = new int[2];
        imageView.getLocationOnScreen(screenLocation);

        intent.putExtra(LEFT, screenLocation[0])
                .putExtra(TOP, screenLocation[1])
                .putExtra(RIGHT, imageView.getWidth())
                .putExtra(BOTTOM, imageView.getHeight())
                .putExtra(INDEX, imageViewPosition)
                .putExtra(LIST, mImageAdapter.getData());
        startActivity(intent);
    }
}