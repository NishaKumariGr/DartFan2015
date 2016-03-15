package dartmouth.cs.ploomis.dartfan;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


public class ForumActivity extends Activity {

    // -------------------------------------------------------------------- View Layout
    EditText mEnterPostEditText;
    ForumTeam mForumTeam;
    int mCurrentTeam;
    String mCurrentName;
    public static ForumTeamDataSource datasource;
    public static ForumPostsAdapter<ForumPost> adapter;
    public static int mPostId;
    public static final String DATETIME_FORMAT = "M/d/yy";

    // -------------------------------------------------------------------- Google Cloud Messaging
    public static final String PARAMETER_REG_ID = "param_regid";
    public static final String PARAMETER_POSTDATA_ID = "param_dataid";
    public static final String PARAMETER_TEAM_ID = "param_teamid";
    public static final String PARAMETER_REFRESH = "refresh_teamid";
    public static final boolean RESET_LIST_ADAPTER = true;


    private String SENDER_ID = "536948074679";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    GoogleCloudMessaging gcm;
    SharedPreferences prefs;
    AtomicInteger msgId = new AtomicInteger();
    Context context;
    String regid;

    private IntentFilter mMessageIntentFilter;
    private BroadcastReceiver mMessageUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String team_id = intent.getStringExtra("message_teamid");
            String team_data = intent.getStringExtra("message_teamdata");
            Log.d("************************ MESSAGE RECIEVED", "id : " + team_id);

            // If an update is from the current team we are displaying, update the datasource
            if (Integer.valueOf(team_id).intValue() == mCurrentTeam) {
                if (team_data != null) {
                    Log.d("^^^^^^^^^^^^^^^^^^^^^ MESSAGE FOR MY TEAM", team_data);
                    ForumTeam updatedTeam = ForumTeamDataSource.stringToForumTeam(team_data);
                    updateDatabase(updatedTeam, RESET_LIST_ADAPTER);
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forum_layout);

        // Find views
        mEnterPostEditText = (EditText) findViewById(R.id.forum_new_post_edittext);

        // Set the user name
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        if (SP.getBoolean("anony_preference", true)) {
            mCurrentName = "Anonymous";
        }
        else {
            mCurrentName = SP.getString("name_preference","Anonymous");
        }

        // Get the team to display
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mCurrentTeam = extras.getInt(ForumFragment.TEAM_ID_KEY);
        }

        // Retrieve the ForumTeam from database
        datasource = new ForumTeamDataSource(this);
        datasource.open();
        mForumTeam = datasource.fetchEntryById(mCurrentTeam);

        // Set the team name
        TextView textViewTeam = (TextView) findViewById(R.id.forum_team_name);
        Resources res = getResources();
        String[] array = res.getStringArray(R.array.forum_team_names);
        textViewTeam.setText(array[mForumTeam.getTeamId()]);

        // Set the team icon
        ImageView imageView = (ImageView) findViewById(R.id.forum_team_icon);
        TypedArray imgs = getResources().obtainTypedArray(R.array.forum_team_icons);
        imageView.setImageResource(imgs.getResourceId(mForumTeam.getTeamId(), -1));

        // Populate the list view with the posts
        AbsListView listView= (AbsListView) findViewById(R.id.forum_list_view);
        adapter = new ForumPostsAdapter(this, mForumTeam.getPostList());
        listView.setAdapter(adapter);
        datasource.close();

        // Register GCM
        mMessageIntentFilter = new IntentFilter();
        mMessageIntentFilter.addAction("GCM_NOTIFY");

        // Check device for Play Services APK.
        context = getApplicationContext();
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);
            if (regid.isEmpty()) {
                registerInBackground();
            } else {
                Log.i("GCM", "No valid Google Play Services APK found.");
            }
        }

        // On open, retrieve from server
        refreshFromCloud(mForumTeam.getTeamId());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_forum, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    // --------------------------------------------------------------------------------- Database

    public void updateDatabase(ForumTeam team, boolean adapter_reset) {
        Log.d("UPDATE DATABASE", "team");
        datasource.open();
        datasource.removeEntryByTeamId(team.getTeamId());
        datasource.insertTeam(team);
        datasource.close();
        mForumTeam = team;
        if (adapter_reset) {
            AbsListView listView= (AbsListView) findViewById(R.id.forum_list_view);
            adapter = new ForumPostsAdapter(this, mForumTeam.getPostList());
            listView.setAdapter(adapter);
        }
        notifyChange();
    }

    public static void notifyChange() {
        adapter.notifyDataSetChanged();
    }


    // --------------------------------------------------------------------------- Button handlers

    public void onForumRefreshClicked(View v) {
        // Send request to server
        refreshFromCloud(mForumTeam.getTeamId());
    }

    public void onForumReplyClicked(View v) {
        // Check which post's reply button was clicked
        Button btn_reply = (Button) v.findViewById(R.id.button_reply);
        mPostId = (int) btn_reply.getTag();
        Log.d("onForumReplyClicked", "post_id = " + Integer.toString(mPostId));
        showStudentInfoAlert();
    }

    public void showStudentInfoAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(ForumActivity.this);
        LayoutInflater in = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v = in.inflate(R.layout.reply_dialog, null);

        final EditText comment = (EditText) v.findViewById(R.id.reply_dialog_edittext);
        builder.setTitle("Enter a reply");
        builder.setView(v);

        builder.setPositiveButton(
                "Reply",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        if (isNotEmpty(comment)) {
                            String reply = comment.getText().toString();
                            ForumReply fr = new ForumReply(mCurrentName, reply);
                            mForumTeam.addReplyToPost(mPostId, fr);
                            updateDatabase(mForumTeam, false);
                            sendToCloud(mForumTeam.getTeamId());
                        }
                    }
                }
        );
        builder.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        // cancel dialog view
                    }
                }
        );
        builder.create().show();
    }

    public void onForumPostClicked(View v) {
        Log.d("onForumPostClicked", "");
        String post = mEnterPostEditText.getText().toString();
        if (!post.equals(ForumTeamDataSource.EMPTY_STRING)) {

            // Clear field
            mEnterPostEditText.setText(ForumTeamDataSource.EMPTY_STRING);

            // Create new forum post
            SimpleDateFormat datetime = new SimpleDateFormat(DATETIME_FORMAT);
            Date d = new Date();
            ForumPost fp = new ForumPost(mCurrentTeam, post, mCurrentName, datetime.format(d));

            // Add to forum team
            mForumTeam.addPost(fp);

            // Add to database
            updateDatabase(mForumTeam, false);

            // Send to cloud
            sendToCloud(mForumTeam.getTeamId());
        }
    }

    // --------------------------------------------------------------------- GCM Methods


    @Override
    protected void onResume() {
        registerReceiver(mMessageUpdateReceiver, mMessageIntentFilter);
        datasource.open();
        super.onResume();
        checkPlayServices();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mMessageUpdateReceiver);
        datasource.close();
        super.onPause();
    }

    /*
    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("GCM", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i("GCM", "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing registration ID is not guaranteed to work with
        // the new app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i("GCM", "App version changed.");
            return "";
        }
        return registrationId;
    }
    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the registration ID in your app is up to you.
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private void refreshFromCloud(int team) {
        Log.d(">>>>>>>>>>>>>>>>>>>>>REFRESH FROM CLOUD", "");
        new AsyncTask<String, Void, String>() {

            @Override
            protected String doInBackground(String... arg0) {
                String url = getString(R.string.server_addr) + "/refresh.do";
                String res = "";

                // Send team id to server
                Map<String, String> params = new HashMap<String, String>();
                params.put(PARAMETER_REFRESH, Integer.toString(mCurrentTeam));

                try {
                    res = ServerUtilities.post(url, params);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                return res;
            }

            @Override
            protected void onPostExecute(String res) {
            }

        }.execute();
    }

    private void sendToCloud(int team) {
        Log.d(">>>>>>>>>>>>>>>>>>>>>SEND TO CLOUD", "");
        new AsyncTask<String, Void, String>() {

            @Override
            protected String doInBackground(String... arg0) {
                String url = getString(R.string.server_addr) + "/post.do";
                String res = "";

                // Create JSON obj for team
                Map<String, String> params = new HashMap<String, String>();
                params.put(PARAMETER_POSTDATA_ID, ForumTeamDataSource.forumTeamToString(mForumTeam));
                params.put(PARAMETER_TEAM_ID, Integer.toString(mCurrentTeam));


                try {
                    Log.d("AsyncTask postEntries", "JSONArray: " +
                            ForumTeamDataSource.forumTeamToString(mForumTeam) );
                    res = ServerUtilities.post(url, params);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                return res;
            }

            @Override
            protected void onPostExecute(String res) {
            }

        }.execute();
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                Log.d("registerInBackground()", "doInBackground");
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    ServerUtilities.sendRegistrationIdToBackend(context, regid);

                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the registration ID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }
            @Override
            protected void onPostExecute(String msg) {
                Log.i("GCM", "gcm register msg: " + msg);
            }
        }.execute(null, null, null);
    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app. Not needed for this demo since the
     * device sends upstream messages to a server that echoes back the message
     * using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend() {
        // Your implementation here.
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i("GCM", "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    private boolean isNotEmpty(EditText id) {
        // Check if there is text
        return id.getText().toString().trim().length() != 0;
    }

}