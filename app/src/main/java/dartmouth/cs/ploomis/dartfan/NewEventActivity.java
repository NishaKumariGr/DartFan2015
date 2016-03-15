package dartmouth.cs.ploomis.dartfan;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Calendar;

import dartmouth.cs.ploomis.dartfan.data.Event;
import dartmouth.cs.ploomis.dartfan.data.EventDatasource;

/**
 * Shows a dialog for creating a new Event that will appear in the EventsFragment.
 */

public class NewEventActivity extends FragmentActivity {

    private static final int MAP_ZOOM = 15;
    private Event mNewEvent;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Location mUserLocation;
    private Marker mMarker;

    /**
     * Displays a dialog letting the user choose a date and time when they wish to be reminded
     * about an event.
     *
     * @param view the "Set Reminder" button
     */
    public void displayDateTimeDialog(View view) {
        DateTimeDialogFragment fragment = DateTimeDialogFragment.
                newInstance(DateTimeDialogFragment.ID_DATE_INPUT);
        fragment.show(getFragmentManager(), "Choose reminder date");
    }

    /**
     * Saves the new event to SQLite database
     *
     * @param view the save button
     */
    public void onSaveClicked(View view) {
        EventDatasource datasource = new EventDatasource(this);

        EditText nameText = (EditText) findViewById(R.id.eventName);
        EditText notesText = (EditText) findViewById(R.id.eventNotes);

        mNewEvent.setEventName(nameText.getText().toString());
        mNewEvent.setEventNotes(notesText.getText().toString());
        datasource.insertEvent(mNewEvent);

        // make the user "follow" this event
        SharedPreferences prefs = getSharedPreferences(HomeFragment.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(mNewEvent.getEventName(), true);
        editor.apply();

        Toast.makeText(this, "Event saved", Toast.LENGTH_SHORT).show();
        finish();
    }

    public void onDateSet(int year, int month, int day) {
        Calendar c = mNewEvent.getDatetime();
        c.set(year, month, day);
        mNewEvent.setDatetime(c);
    }

    public void onTimeSet(int hourOfDay, int minute) {
        Calendar c = mNewEvent.getDatetime();
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        mNewEvent.setDatetime(c);
    }

    private void addMarkerAtPoint(LatLng coordinate) {
        mNewEvent.setLocation(coordinate);
        if (mMarker != null)
            mMarker.remove();
        if (mMap != null) {
            mMarker = mMap.addMarker(new MarkerOptions().position(coordinate)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinate, MAP_ZOOM));
        }
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link com.google.android.gms.maps.SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        addMarkerAtPoint(new LatLng(mUserLocation.getLatitude(), mUserLocation.getLongitude()));
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                addMarkerAtPoint(latLng);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        // Use the user's last location as a focus point on the map
        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        String provider = locationManager.getBestProvider(criteria, true);
        mUserLocation = locationManager.getLastKnownLocation(provider);

        mNewEvent = new Event();
        mNewEvent.setLocation(new LatLng(mUserLocation.getLatitude(), mUserLocation.getLongitude()));

        setUpMapIfNeeded();
    }
}