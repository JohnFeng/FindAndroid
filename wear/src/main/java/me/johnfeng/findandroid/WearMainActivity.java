package me.johnfeng.findandroid;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.wearable.view.WatchViewStub;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.CircularProgressButton;

public class WearMainActivity extends Activity implements Handler.Callback {

    private Handler mHandler;
    private static final int FIND_TIME = 5000;
    CircularProgressButton findButton;
    boolean isProgressing = false;

    private static final int FIND_PHONE_NOTIFICATION_ID = 2;
    private static Notification.Builder notification;

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
            return;
        } else {
            isProgressing = true;
            setFindButtonState(50);
            sendNotification();
            Message message = new Message();
            message.what = 100;
            mHandler.sendMessageDelayed(message, FIND_TIME);
            finish();
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
//        int notificationId = 001;
//
//        // Create an intent for the reply action
//        Intent actionIntent = new Intent(this, WearMainActivity.class);
//        PendingIntent actionPendingIntent =
//                PendingIntent.getActivity(this, 0, actionIntent,
//                        PendingIntent.FLAG_UPDATE_CURRENT);
//
//        // Create the action
//        NotificationCompat.Action action =
//                new NotificationCompat.Action.Builder(R.mipmap.ic_launcher,
//                        getString(R.string.Found), actionPendingIntent)
//                        .build();
//
//        // Build the notification and add the action via WearableExtender
//        Notification notification =
//                new NotificationCompat.Builder(WearMainActivity.this)
//                        .setSmallIcon(R.mipmap.ic_launcher)
//                        .setContentText(getString(R.string.app_name))
//                        .extend(new NotificationCompat.WearableExtender().addAction(action))
//                        .setVibrate(new long[]{1000, 500, 1000, 500, 1000, 500, 1000, 500})
//                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
//                        .build();
//
//        // Get an instance of the NotificationManager service
//        NotificationManagerCompat notificationManager =
//                NotificationManagerCompat.from(WearMainActivity.this);
//
//        // Issue the notification with notification manager.
//        notificationManager.notify(notificationId, notification);


        // Create a notification with an action to toggle an alarm on the phone.
        Intent toggleAlarmOperation = new Intent(this, FindPhoneService.class);
        toggleAlarmOperation.setAction(FindPhoneService.ACTION_TOGGLE_ALARM);
        PendingIntent toggleAlarmIntent = PendingIntent.getService(this, 0, toggleAlarmOperation,
                PendingIntent.FLAG_CANCEL_CURRENT);
        Notification.Action alarmAction = new Notification.Action(R.mipmap.ic_launcher, "", toggleAlarmIntent);
        // This intent turns off the alarm if the user dismisses the card from the wearable.
        Intent cancelAlarmOperation = new Intent(this, FindPhoneService.class);
        cancelAlarmOperation.setAction(FindPhoneService.ACTION_CANCEL_ALARM);
        PendingIntent cancelAlarmIntent = PendingIntent.getService(this, 0, cancelAlarmOperation,
                PendingIntent.FLAG_CANCEL_CURRENT);
        // Use a spannable string for the notification title to resize it.
        SpannableString title = new SpannableString(getString(R.string.app_name));
        title.setSpan(new RelativeSizeSpan(0.85f), 0, title.length(), Spannable.SPAN_POINT_MARK);
        notification = new Notification.Builder(this)
                .setContentTitle(title)
                .setContentText(getString(R.string.turn_alarm_on))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setVibrate(new long[] {0, 50})  // Vibrate to bring card to top of stream.
                .setDeleteIntent(cancelAlarmIntent)
                .extend(new Notification.WearableExtender()
                        .addAction(alarmAction)
                        .setContentAction(0)
                        .setHintHideIcon(true))
                .setLocalOnly(true)
                .setPriority(Notification.PRIORITY_MAX);
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                .notify(FIND_PHONE_NOTIFICATION_ID, notification.build());
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

    /**
     * Updates the text on the wearable notification. This is used so the notification reflects the
     * current state of the alarm on the phone. For instance, if the alarm is turned on, the
     * notification text indicates that the user can tap it to turn it off, and vice-versa.
     *
     * @param context
     * @param notificationText The new text to display on the wearable notification.
     */
    public static void updateNotification(Context context, String notificationText) {
        notification.setContentText(notificationText);
        ((NotificationManager) context.getSystemService(NOTIFICATION_SERVICE))
                .notify(FIND_PHONE_NOTIFICATION_ID, notification.build());
    }
}

