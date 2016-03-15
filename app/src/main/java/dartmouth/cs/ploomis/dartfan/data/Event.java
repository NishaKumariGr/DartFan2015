package dartmouth.cs.ploomis.dartfan.data;

import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;

/**
 * Stores information specific to an athletic event. Event objects are either created by the user as
 * reminders, or created by scraping the website for upcoming athletic events.
 */
public class Event implements Comparable<Event>{

    private String mEventName;
    private String mEventNotes;
    private LatLng mLocation; // specific location of event
    private Calendar mDatetime; // time of event

    public Event() {
        mEventName = "";
        mEventNotes = "";
        mDatetime = Calendar.getInstance();
    }

    public String getEventName() {
        return mEventName;
    }

    public void setEventName(String mEventName) {
        this.mEventName = mEventName;
    }

    public String getEventNotes() {
        return mEventNotes;
    }

    public void setEventNotes(String mEventNotes) {
        this.mEventNotes = mEventNotes;
    }

    public LatLng getLocation() {
        return mLocation;
    }

    public void setLocation(LatLng mLocation) {
        this.mLocation = mLocation;
    }

    public Calendar getDatetime() {
        return mDatetime;
    }

    public void setDatetime(Calendar mDatetime) {
        this.mDatetime = mDatetime;
    }

    @Override
    public int compareTo(Event event) {
        return getDatetime().getTime().compareTo(event.getDatetime().getTime());
    }
}
