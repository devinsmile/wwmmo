package au.com.codeka.warworlds;

import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.view.Display;

import android.view.View;
import android.view.WindowManager;
import au.com.codeka.warworlds.game.StarfieldBackgroundRenderer;

/**
 * Generates an image that can be used at the background for the various non-game activities
 * (such as the startup activity, account activities, etc).
 */
public class ActivityBackgroundGenerator {
    /**
     * Sets the background of the given \c View to our custom bitmap.
     */
    public static void setBackground(View view) {
        WindowManager wm = (WindowManager) view.getContext().getSystemService(
                Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        android.util.DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);

        StarfieldBackgroundRenderer renderer = new StarfieldBackgroundRenderer(view.getContext());
        renderer.drawBackground(canvas, 0.0f, 0.0f, width, height, new Random().nextLong());

        view.setBackgroundDrawable(new BitmapDrawable(bmp));
    }
}