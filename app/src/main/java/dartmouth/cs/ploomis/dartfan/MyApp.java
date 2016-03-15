package dartmouth.cs.ploomis.dartfan;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Created by kristenvondrak on 3/4/15.
 */
public class MyApp extends Application {
    private ImageView img; // the image
    private RelativeLayout bgimg; // layout of the activity
    private Bitmap nav; // the image in the Bitmap format


    public void loadBitmap(int id) {
        nav = BitmapFactory.decodeStream(getResources().openRawResource(id));
        img.setImageBitmap(nav);
    }
    public void unloadBitmap() {
        if (img != null)
            img.setImageBitmap(null);
        if (nav!= null) {
            nav.recycle();
        }
        nav = null;
    }


    public void setImage(ImageView i, int sourceid) {
        unloadBitmap();
        img = i;
        loadBitmap(sourceid);
    }
}