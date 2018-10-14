package io.github.yhdesai.makertoolbox.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

import io.github.yhdesai.makertoolbox.MT_Activity;
import io.github.yhdesai.makertoolbox.R;
import io.github.yhdesai.makertoolbox.model.DeveloperMessage;

public class NotificationService extends Service {

    public static final String service_broadcast = "makertoolbox.intent.action.RestartService";
    public static boolean state = false;

    private DatabaseReference databaseReference;
    private int channels = 0;
    private ArrayList<NotificationChannel> notificationChannels;
    private Thread runner;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("Debug tag", "service oncreate");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("chat");
        state = true;
        channels = 0;
        notificationChannels = new ArrayList<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot channel : dataSnapshot.getChildren()){
                    String channel_Id = channel.getKey();
                    Log.e("Debug tag", "channel create");
                    NotificationChannel notificationChannel = new NotificationChannel(NotificationService.this, databaseReference, channel_Id, channels++);
                    notificationChannel.startListner();
                    notificationChannels.add(notificationChannel);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        runner = new Thread(){
            @Override
            public void run() {
                while(true);
            }
        };
        runner.start();
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        runner = null;
        for(NotificationChannel channel : notificationChannels){
            channel.stopListner();
        }
        notificationChannels.clear();
        channels = 0;
        Log.e("Debug tag", "service stopped");
        super.onDestroy();
        Log.e("Debug tag", "service after stopped");
        if(state) {
            Log.e("Debug tag", "service restart attempt");
            Intent intent = new Intent();
            intent.setAction(service_broadcast);
            getBaseContext().sendBroadcast(intent);
        }
    }
}