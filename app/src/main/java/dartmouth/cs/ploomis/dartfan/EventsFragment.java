package dartmouth.cs.ploomis.dartfan;

import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import dartmouth.cs.ploomis.dartfan.data.Event;
import dartmouth.cs.ploomis.dartfan.data.EventDatasource;

/**
 * Displays updates about daily events/games for the teams the user is following. When a list item
 * is clicked, a new activity is started to show the event location, time, and any other information.
 * The user can also create new custom events, to serve as reminders. Location and time can also be
 * added to these custom events.
 */

public class EventsFragment extends ListFragment {

    private static final String CALENDAR_URL = "http://www.dartmouthsports.com/rss.dbml?db_oem_id=11600&media=schedulesxml";
    private static final int CONNECTION_TIMEOUT = 10000;
    private static final String DARTMOUTH_HOME_LOCATION = "Hanover, N.H.";

    // maps team names to the location of the team facility
    private static final Map<String, LatLng> locationMap = new HashMap<String, LatLng>() {
        {
            put("M Basketball", new LatLng(43.702811, -72.283985));
            put("W Basketball", new LatLng(43.702811, -72.283985));
            put("M Squash", new LatLng(43.702811, -72.283985));
            put("W Squash", new LatLng(43.702811, -72.283985));
            put("Baseball", new LatLng(43.726034, -72.142917));
            put("M Soccer", new LatLng(43.700180, -72.287522));
            put("W Soccer", new LatLng(43.700180, -72.287522));
            put("Football", new LatLng(43.701977, -72.285441));
            put("M Lacrosse", new LatLng(43.726034, -72.142917));
            put("W Lacrosse", new LatLng(43.726034, -72.142917));
            put("Men's Hockey", new LatLng(43.699852, -72.281521));
            put("Women's Hockey", new LatLng(43.699852, -72.281521));
            put("M Tennis", new LatLng(43.702811, -72.283985));
            put("W Tennis", new LatLng(43.702811, -72.283985));
            put("M Swimming & Diving", new LatLng(43.702811, -72.283985));
            put("W Swimming & Diving", new LatLng(43.702811, -72.283985));
            put("M Track & Field", new LatLng(43.700938, -72.282282));
            put("W Track & Field", new LatLng(43.700938, -72.282282));
        }
    };

    private EventAdapter mAdapter;
    private ArrayList<Event> mEvents;

    /** Refreshes the Event objects in the list view. Only shows events for the sports the user is following. */
    private void updateEvents() {
        EventDatasource datasource = new EventDatasource(getActivity());
        ArrayList<Event> data = datasource.getEvents();
        ArrayList<Event> newEvents = new ArrayList<>();

        // filter events by team and time
        SharedPreferences prefs = getActivity().getSharedPreferences(HomeFragment.SHARED_PREFERENCES_KEY,
                Context.MODE_PRIVATE);
        for (Event event : data) {
            boolean isFollowed = prefs.getBoolean(event.getEventName(), false);
            if (isFollowed) {
                newEvents.add(event);
            }
        }
        Collections.sort(newEvents);

        mAdapter.clear();
        mAdapter.addAll(newEvents);
        mEvents = newEvents;
        mAdapter.notifyDataSetChanged();
    }

    /** Creates a reminder in the user's calendar for a certain event */
    private void createEventReminder(Event event) {
        Calendar cal = event.getDatetime();
        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setType("vnd.android.cursor.item/event");
        intent.putExtra("beginTime", cal.getTimeInMillis());
        intent.putExtra("allDay", false);
        intent.putExtra("rrule", "FREQ=DAILY");
        intent.putExtra("endTime", cal.getTimeInMillis()+60*60*1000);
        intent.putExtra("title", event.getEventName());
        intent.putExtra("description", event.getEventNotes());
        startActivity(intent);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mEvents = new ArrayList<>();
        mAdapter = new EventAdapter(getActivity(), mEvents);
        setListAdapter(mAdapter);

        // set up the header of the events list view
        View header = getActivity().getLayoutInflater().inflate(R.layout.fragment_events, null);
        TextView headerText = (TextView) header.findViewById(R.id.title);
        String headerString = "Upcoming Events";
        headerText.setText(headerString);
        getListView().addHeaderView(headerText);

        setEmptyText("No events. Reminders you create or updates about teams you follow will show up here");

        new EventCollector().execute(); // background task to collect events from RSS feed
    }

    @Override
    public void onResume() {
        super.onResume();
        updateEvents();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        int index = position-1;
        if (index > -1) {
            Event event = mEvents.get(index);
            Intent intent = new Intent(getActivity(), EventDisplayActivity.class);
            intent.putExtra("eventName", event.getEventName());
            intent.putExtra("lat",event.getLocation().latitude);
            intent.putExtra("long", event.getLocation().longitude);
            startActivity(intent);
        }
    }

    /**
     * Custom ArrayAdapter for displaying Event object in a list view
     */

    private class EventAdapter extends ArrayAdapter<Event> {

        private ArrayList<Event> mEvents;
        private Context mContext;

        public EventAdapter(Context context, ArrayList<Event> events) {
            super(context, R.layout.updates_item, events);
            mEvents = events;
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Event event = mEvents.get(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.updates_item, parent, false);
            }

            TextView nameText = (TextView) convertView.findViewById(R.id.eventName);
            TextView timeText = (TextView) convertView.findViewById(R.id.eventTime);
            TextView notesText = (TextView) convertView.findViewById(R.id.eventNotes);
            Button calBtn = (Button) convertView.findViewById(R.id.eventCalBtn);

            calBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getListView().getPositionForView(v)-1;
                    createEventReminder(mEvents.get(pos));
                }
            });

            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm aa");
            String datetime = formatter.format(event.getDatetime().getTime());

            nameText.setText(event.getEventName());
            timeText.setText(datetime);
            notesText.setText(event.getEventNotes());

            return convertView;
        }
    }

    /**
     * Scrapes the Dartmouth sports calendar and adds Event objects to the Updates list
     */

    private class EventCollector extends AsyncTask<Void, Void, Void> {

        private ProgressDialog mProgess;
        private EventDatasource mDatasource;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mDatasource = new EventDatasource(getActivity());

            mProgess = new ProgressDialog(getActivity());
            mProgess.setMessage("Loading events...");
            mProgess.setIndeterminate(false);
            mProgess.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                Document calendarContent = Jsoup.connect(CALENDAR_URL).timeout(CONNECTION_TIMEOUT).get();

                // iterate through items in the RSS feed, creating and adding Event objects to the DB
                if (calendarContent != null) {
                    for (Element calendarItem : calendarContent.select("item")) {
                        String sport = calendarItem.select("sport").first().text();
                        Log.d("crawler", sport);
                        String opponent = calendarItem.select("opponent").first().text();
                        String location = calendarItem.select("location").first().text();
                        String datetime = calendarItem.select("date").first().text();
                        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm aa");

                        if (location.contains(DARTMOUTH_HOME_LOCATION) &&
                                locationMap.get(sport) != null) {
                            Event event = new Event();
                            event.setEventName(sport);
                            Calendar c = event.getDatetime();
                            try {
                                c.setTime(formatter.parse(datetime));
                                event.setDatetime(c);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            event.setEventNotes("vs. "+opponent);
                            event.setLocation(locationMap.get(sport));
                            mDatasource.insertEvent(event);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            updateEvents();
            mProgess.dismiss();
        }
    }
}
