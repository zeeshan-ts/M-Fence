package com.ts.zeeshanafzal.m_fence.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.ts.zeeshanafzal.m_fence.R;
import com.ts.zeeshanafzal.m_fence.map.MapActivity;
import com.ts.zeeshanafzal.m_fence.utility.Consts;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class GeoFenceTransitionService extends IntentService {

    private String TAG = "GeoFenceTransitionService";

    public GeoFenceTransitionService() {
        super("GeoFenceTransitionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        Log.i(TAG, "onHandleIntent() " + geofencingEvent.getGeofenceTransition());

        showTransitionNotification(geofencingEvent);
    }

    private void showTransitionNotification(GeofencingEvent geofencingEvent) {
        String transition = "";
        if (geofencingEvent.getGeofenceTransition() == Geofence.GEOFENCE_TRANSITION_ENTER) {
            transition = "Entered";
        } else if (geofencingEvent.getGeofenceTransition() == Geofence.GEOFENCE_TRANSITION_EXIT) {
            transition = "Exited";
        }

        Intent i = new Intent(getBaseContext(), MapActivity.class);
        PendingIntent pi = PendingIntent.getActivity(getBaseContext(), 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder notificationBuilder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder = new Notification.Builder(getBaseContext(), Consts.NOTIFICATION_CHANNEL);
        } else {
            notificationBuilder = new Notification.Builder(getBaseContext());
        }

        Notification notification = notificationBuilder.setContentText("You have "+transition+ " the fence")
                .setContentTitle("M-Fence")
                .setSmallIcon(R.drawable.ic_location_on_white_18dp)
//                .setContentIntent(pi)
//                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.ic_location_on_purple_900_24dp))
                .setColor(ContextCompat.getColor(this,R.color.colorPrimary))
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1,notification);
    }
}
