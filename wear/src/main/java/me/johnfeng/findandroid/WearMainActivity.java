package me.johnfeng.findandroid;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.CircularProgressButton;

public class WearMainActivity extends Activity implements Handler.Callback {

    private Handler mHandler;
    private static final int FIND_TIME = 5000;
    CircularProgressButton findButton;
    boolean isProgressing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wear_round_act_main);
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
            sendNotification();
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

    void sendNotification() {
        int notificationId = 001;

        // Create an intent for the reply action
        Intent actionIntent = new Intent(this, WearMainActivity.class);
        PendingIntent actionPendingIntent =
                PendingIntent.getActivity(this, 0, actionIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        // Create the action
        NotificationCompat.Action action =
                new NotificationCompat.Action.Builder(R.mipmap.ic_launcher,
                        getString(R.string.Found), actionPendingIntent)
                        .build();

        // Build the notification and add the action via WearableExtender
        Notification notification =
                new NotificationCompat.Builder(WearMainActivity.this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentText(getString(R.string.app_name))
                        .extend(new NotificationCompat.WearableExtender().addAction(action))
                        .setVibrate(new long[]{1000, 500, 1000, 500, 1000, 500, 1000, 500})
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                        .build();

        // Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(WearMainActivity.this);

        // Issue the notification with notification manager.
        notificationManager.notify(notificationId, notification);
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

