package com.taberonvslavrik.boostgallery.activity;

import android.os.Bundle;

import com.taberonvslavrik.boostgallery.R;
import com.taberonvslavrik.boostgallery.activity.base.BaseActivity;
import com.taberonvslavrik.boostgallery.view.SwipeImageView;

import static com.taberonvslavrik.boostgallery.activity.MainActivity.BundleKeys.BOTTOM;
import static com.taberonvslavrik.boostgallery.activity.MainActivity.BundleKeys.INDEX;
import static com.taberonvslavrik.boostgallery.activity.MainActivity.BundleKeys.LEFT;
import static com.taberonvslavrik.boostgallery.activity.MainActivity.BundleKeys.LIST;
import static com.taberonvslavrik.boostgallery.activity.MainActivity.BundleKeys.RIGHT;
import static com.taberonvslavrik.boostgallery.activity.MainActivity.BundleKeys.TOP;

/**
 * @author Ivan Lavryshyn.
 */

public class DetailsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.a_details);

        SwipeImageView swipeImageView = findViewById(R.id.swipe_image_view);

        if (savedInstanceState == null) {
            final Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                swipeImageView.setImages(bundle.getStringArray(LIST));
                swipeImageView.showImage(bundle.getInt(INDEX),
                        bundle.getInt(TOP), bundle.getInt(LEFT),
                        bundle.getInt(RIGHT), bundle.getInt(BOTTOM));
            }
        }
    }
}
