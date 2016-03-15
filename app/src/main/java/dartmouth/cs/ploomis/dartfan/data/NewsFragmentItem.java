package dartmouth.cs.ploomis.dartfan.data;

import android.graphics.Bitmap;

/**
 * Created by nisha on 2/28/15.
 */
public class NewsFragmentItem {

        public String title; // the text for the ListView item title
        public String pubDate;// the date which is in the form of a String
        public String category; // the category of sports
        public Bitmap image;
        public NewsFragmentItem(String title, String pubDate, String category,Bitmap image) {
            this.title = title;
            this.pubDate=pubDate;
            this.category=category;
            this.image=image;
        }
    }
