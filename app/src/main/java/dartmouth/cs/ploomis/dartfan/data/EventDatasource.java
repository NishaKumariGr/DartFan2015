package dartmouth.cs.ploomis.dartfan.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Used for storing and retrieving Event objects from a SQLite database.
 */

public class EventDatasource extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "events.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_EVENTS = "events";
    private static final String COLUMN_EVENT_NAME = "event_name";
    private static final String COLUMN_EVENT_TIME = "event_time";
    private static final String COLUMN_EVENT_NOTES = "event_notes";
    private static final String COLUMN_EVENT_LATITUDE = "event_latitude";
    private static final String COLUMN_EVENT_LONGITUDE = "event_longitude";

    private static final String DATABASE_CREATE = "create table if not exists " + TABLE_EVENTS + " (" +
            COLUMN_EVENT_NAME + " text, " +
            COLUMN_EVENT_TIME + " float, " +
            COLUMN_EVENT_NOTES + " text, " +
            COLUMN_EVENT_LATITUDE + " float, " +
            COLUMN_EVENT_LONGITUDE + " float);";

    public EventDatasource(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * Retrieves all the rows from the database and converts each row to an Event object.
     *
     * @return a list of Event objects based on the items stored in the database
     */
    public ArrayList<Event> getEvents() {
        ArrayList<Event> events = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_EVENTS, null, null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                Event event = cursorToEvent(cursor);
                events.add(event);
                cursor.moveToNext();
            }
            cursor.close();
        }
        db.close();
        return events;
    }

    /**
     * Inserts an Event object as a row in the database
     *
     * @param event Event object to be inserted - will not be inserted if its information is already
     *              in the database.
     */
    public void insertEvent(Event event) {
        SQLiteDatabase db = getWritableDatabase();

        // insert the event if it is not already in the database
        if (!eventIsInDatabase(event, db)) {
            Log.d("insertEvent", event.getEventName());
            ContentValues values = new ContentValues();
            values.put(COLUMN_EVENT_NAME, event.getEventName());
            values.put(COLUMN_EVENT_TIME, event.getDatetime().getTimeInMillis());
            values.put(COLUMN_EVENT_NOTES, event.getEventNotes());
            values.put(COLUMN_EVENT_LATITUDE, event.getLocation().latitude);
            values.put(COLUMN_EVENT_LONGITUDE, event.getLocation().longitude);

            db.insert(TABLE_EVENTS, null, values);
        }
        db.close();
    }

    /**
     * Queries the database to determine if it contains a certain event.
     *
     * @param db the database to query
     * @param event the Event to compare with other entries to determine if it exists in the db
     * @return true if the event is contained in the db, false otherwise
     */
    private boolean eventIsInDatabase(Event event, SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("select 1 from " + TABLE_EVENTS + " where " + COLUMN_EVENT_NAME
        + "=?" + " AND " + COLUMN_EVENT_TIME + "=?", new String[] {event.getEventName(),
                event.getDatetime().getTimeInMillis()+""});

        return cursor != null && cursor.moveToFirst();
    }

    /** Converts a SQLite cursor to an Event object */
    private Event cursorToEvent(Cursor cursor) {
        Event event = new Event();
        event.setEventName(cursor.getString(0));
        Date date = new Date(cursor.getLong(1));
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        event.setDatetime(c);
        event.setEventNotes(cursor.getString(2));
        double latitude = cursor.getDouble(3);
        double longitude = cursor.getDouble(4);
        event.setLocation(new LatLng(latitude, longitude));
        return event;
    }
}
