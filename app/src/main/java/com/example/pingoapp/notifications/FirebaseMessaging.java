package com.example.pingoapp.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Switch;

import com.example.pingoapp.ChatActivity;
import com.example.pingoapp.DashBoradActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import static android.app.PendingIntent.FLAG_ONE_SHOT;

public class FirebaseMessaging extends FirebaseMessagingService {


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
    //getcurrent user from sharedphrefrenced
        SharedPreferences sp=getSharedPreferences("SP_User",MODE_PRIVATE);
        String savedCurrentUser =sp.getString("Current_USERID","None");

        String sent=remoteMessage.getData().get("sent");
        String user=remoteMessage.getData().get("user");

        FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();


        if(fUser!=null&& sent.equals(fUser.getUid())){
            if(!savedCurrentUser.equals(user)){
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
                sendAndAboveNotification(remoteMessage);
            }
            else {
                sendNormalNotification(remoteMessage);
            }

            }
        }



    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void sendNormalNotification(RemoteMessage remoteMessage) {
        String user = remoteMessage.getData().get("users");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");


        RemoteMessage.Notification notification = remoteMessage.getNotification();


int i=0;
         //    int i= Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, ChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("hisUID", user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(this, i, intent, FLAG_ONE_SHOT);

        Uri DefSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(Objects.requireNonNull(icon)))
                .setContentText(body)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setSound(DefSoundUri)
                .setContentIntent(pIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int j = 0;
        if (i > 0) {
            j = i;
        }
        notificationManager.notify(j, builder.build());


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendAndAboveNotification(RemoteMessage remoteMessage) {

        String user =remoteMessage.getData().get("user");
        String icon =remoteMessage.getData().get("icon");
        String title =remoteMessage.getData().get("title");
        String body =remoteMessage.getData().get("body");
        String type =remoteMessage.getData().get("type");
        RemoteMessage.Notification notification;
        int i;
        Intent intent;
        Bundle bundle;
        PendingIntent pIntent;
        Uri DefSoundUri;
        OreoAndAboveNotification notification1;
        Notification.Builder builder;
        int j;
        switch (type){
            case "chat":
                notification=remoteMessage.getNotification();
                 i= Integer.parseInt(user.replaceAll("[\\D]",""));
                 intent=new Intent(this, ChatActivity.class);
                bundle=new Bundle();
                bundle.putString("hisUID",user);
                intent.putExtras(bundle);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
              pIntent= PendingIntent.getActivity(this,i,intent, FLAG_ONE_SHOT);

                DefSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

               notification1=new OreoAndAboveNotification(this);
                 builder=notification1.getONotifications(title,body,pIntent,DefSoundUri,icon);

                 j=0;
                if(i>0){
                    j=i;
                }
                notification1.getManager().notify(j,builder.build());
                break;
            default:
                notification=remoteMessage.getNotification();
                i= Integer.parseInt(user.replaceAll("[\\D]",""));
                 intent=new Intent(this, DashBoradActivity.class);
                 bundle=new Bundle();
                bundle.putString("hisUID",user);
                intent.putExtras(bundle);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 pIntent= PendingIntent.getActivity(this,i,intent, FLAG_ONE_SHOT);

                DefSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                 notification1=new OreoAndAboveNotification(this);
                 builder=notification1.getONotifications(title,body,pIntent,DefSoundUri,icon);

                 j=0;
                if(i>0){
                    j=i;
                }
                notification1.getManager().notify(j,builder.build());
                break;
        }



    }
}
