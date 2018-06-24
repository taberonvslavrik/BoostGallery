package com.taberonvslavrik.boostgallery.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.widget.ImageView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Ivan Lavryshyn
 */

public class ImageLoader {

    private static final int CACHE_SIZE = (int) (Runtime.getRuntime().maxMemory());
    private static final int POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(POOL_SIZE);
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    private static final LruCache<String, Bitmap> CACHE = new LruCache<String, Bitmap>(CACHE_SIZE) {
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount() / 1024;
        }
    };

    private ImageLoader() {
    }

    public static void loadImage(final String filePath, final ImageView destinationView) {
        markDestinationView(filePath, destinationView);
        loadBitmap(filePath, destinationView);
    }

    private static void markDestinationView(final String filePath, final ImageView destinationView) {
        destinationView.setTag(filePath);
    }

    private static synchronized void loadBitmap(final String filePath, final ImageView destinationView) {
        final Bitmap bitmap = CACHE.get(filePath);
        if (bitmap != null) {
            setBitmapIntoView(bitmap, destinationView, filePath);
            return;
        }
        destinationView.setImageDrawable(null);
        loadBitmapFromFile(filePath, destinationView);
    }

    private static void loadBitmapFromFile(final String filePath, final ImageView destinationView) {
        EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                final Bitmap bitmap = decodeBitmap(filePath);
                synchronized (ImageLoader.class) {
                    CACHE.put(filePath, bitmap);
                }
                setBitmapIntoView(bitmap, destinationView, filePath);
            }
        });
    }

    private static void setBitmapIntoView(final Bitmap bitmap, final ImageView destinationView,
                                          final String filePath) {
        if (destinationView != null && destinationView.getTag().equals(filePath)) {
            MAIN_HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    destinationView.setImageBitmap(bitmap);
                }
            });
        }
    }

    private static Bitmap decodeBitmap(String filePath) {
        // https://media1.tenor.com/images/5fb8e3d4c56cdf53fb15356f8fd4987e/tenor.gif?itemid=4822389
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        Boolean scaleByHeight = Math.abs(options.outHeight - 100)
                >= Math.abs(options.outWidth - 100);
        if (options.outHeight * options.outWidth * 2 >= 16384) {
            double sampleSize = scaleByHeight
                    ? options.outHeight / 600
                    : options.outWidth / 600;
            options.inSampleSize = (int) Math.pow(2d,
                    Math.floor(Math.log(sampleSize) / Math.log(2d)));
        }

        options.inJustDecodeBounds = false;
        options.inTempStorage = new byte[512];

        return BitmapFactory.decodeFile(filePath, options);
    }
}
