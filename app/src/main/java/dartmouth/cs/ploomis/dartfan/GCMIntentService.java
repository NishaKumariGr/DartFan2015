package dartmouth.cs.ploomis.dartfan;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;


/**
 * Created by kristenvondrak on 3/1/15.
 */
public class GCMIntentService extends IntentService {
    private static final String TAG = "GCMIntentService";


    public GCMIntentService() {
        super("GCMIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
            // Message received from server
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                //handle send error in here
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                //handle delete message on server in here
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                String team_id = (String) extras.get("message_teamid");
                String team_data = (String) extras.get("message_teamdata");
                Log.d("message", "team id = " + team_id);
                Intent i = new Intent();
                i.setAction("GCM_NOTIFY");
                i.putExtra("message_teamid", team_id);
                i.putExtra("message_teamdata", team_data);
                sendBroadcast(i);
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GCMBroadcastReceiver.completeWakefulIntent(intent);
    }

}
