package dartmouth.cs.ploomis.dartfan;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by kristenvondrak on 3/1/15.
 */
public class ForumTeamDbHelper extends SQLiteOpenHelper {

    private static final String TAG = "DB Helper";
    private static final String DATABASE_NAME = "DartFan.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_FORUM_TEAM = "entries";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TEAM = "team";
    public static final String COLUMN_POSTS = "posts";
    public static final String COLUMN_POST_INDEX = "post_index";


    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_FORUM_TEAM + "(" +
            COLUMN_ID + " integer primary key autoincrement, " +
            COLUMN_TEAM + " integer not null, " +
            COLUMN_POSTS + " text, " +
            COLUMN_POST_INDEX + " integer not null );";



    // Constructor
    public ForumTeamDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Create table schema if not exists
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate()");
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.w(ForumTeamDbHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FORUM_TEAM);
        onCreate(db);
    }

}
