package me.johnfeng.findandroid;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.NotificationCompat.WearableExtender;

import com.dd.CircularProgressButton;

public class MobileMainActivity extends Activity implements Handler.Callback {

    private Handler mHandler;
    private static final int FIND_TIME = 5000;
    CircularProgressButton findButton;
    boolean isProgressing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mobile_act_main);
        mHandler = new Handler(this);

        findButton = (CircularProgressButton) findViewById(R.id.findButton);
        findButton.setIndeterminateProgressMode(true); // turn on indeterminate progress
        findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFindAndroid();
            }
        });
    }


    void startFindAndroid() {
        if (isProgressing) {
            Toast.makeText(this, "Please Wait a Sec...", Toast.LENGTH_SHORT)
                    .show();
            return;
        } else {
            isProgressing = true;
            setFindButtonState(50);

            Message message = new Message();
            message.what = 100;
            mHandler.sendMessageDelayed(message, FIND_TIME);
        }
    }


    /**
     * @param state normal state [0]
     *              progress state [1-99]
     *              success state [100]
     *              error state [-1]
     */
    void setFindButtonState(int state) {
        if (findButton == null) {
            return;
        }

        if (state < -1 || state > 100) {
            return;
        }

        findButton.setProgress(state);
    }

    void buildNotification() {

        int notificationId = 001;
// Build intent for notification content
        Intent viewIntent = new Intent(this, MobileMainActivity.class);
        viewIntent.putExtra(EXTRA_EVENT_ID, eventId);
        PendingIntent viewPendingIntent =
                PendingIntent.getActivity(this, 0, viewIntent, 0);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable)
                        .setContentTitle(eventTitle)
                        .setContentText(eventLocation)
                        .setContentIntent(viewPendingIntent);

// Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

// Build the notification and issues it with notification manager.
        notificationManager.notify(notificationId, notificationBuilder.build());
    }


    @Override
    public boolean handleMessage(final Message msg) {
        if (msg.what == 0) {
            setFindButtonState(0);
            isProgressing = false;
        } else {
            // 100 && -1
            setFindButtonState(msg.what);
            Message message = new Message();
            message.what = 0;
            mHandler.sendMessageDelayed(message, 1500);
        }
        return true;
    }
}
