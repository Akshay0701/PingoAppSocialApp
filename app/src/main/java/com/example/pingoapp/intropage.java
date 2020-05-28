package com.example.pingoapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.viewpager.widget.ViewPager;

import static android.app.Notification.DEFAULT_SOUND;
import static androidx.core.app.NotificationCompat.DEFAULT_VIBRATE;

public class intropage extends AppCompatActivity {

    private ViewPager viewPager;
    private LinearLayout mDotsLayout;

    private TextView[] mDots;

    private SliderAdapterIntro sliderAdapterIntro;


    Button back,next_finish;
    int currentPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intropage);
        viewPager=findViewById(R.id.viewpager);
        mDotsLayout=findViewById(R.id.dontslinear_layout);
        back=findViewById(R.id.backBtn);
        next_finish=findViewById(R.id.next_finishBtn);

        triggerNotification();

        sliderAdapterIntro=new SliderAdapterIntro(this);

        viewPager.setAdapter(sliderAdapterIntro);
        addDotsIndicator(0);
        viewPager.addOnPageChangeListener(viewListner);
        getSupportActionBar().hide();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        //onclick
        next_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPage==mDots.length-1){
                    //redirect to Main Screen and save in shredphrence that user completed intro view
                    SharedPreferences.Editor editor;
                    editor= PreferenceManager.getDefaultSharedPreferences(intropage.this).edit();
                    editor.putString("isIntroShow", "1");//1 value will note as user as seen intro
                    editor.apply();
                    startActivity(new Intent(intropage.this,DashBoradActivity.class));
                }
                else {
                    viewPager.setCurrentItem(currentPage + 1);
                }
            }
        });
        //onbackclick
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(currentPage-1);
            }
        });

        //permission
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ||ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            //permission is not granted
            new AlertDialog.Builder(this,4)
                    .setTitle("Required Location Permission")
                    .setMessage("You have to give this permission to acess this feature")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(intropage.this,
                                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CAMERA, Manifest.permission.CALL_PHONE},
                                    1);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            ActivityCompat.requestPermissions(intropage.this,
                                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CAMERA, Manifest.permission.CALL_PHONE},
                                    1);
                        }
                    })
                    .create()
                    .show();
            /*to notification
            new AlertDialog.Builder(this,4)
                    .setTitle("Notification Permission")
                    .setMessage("Allow Notification permission To get notify ")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent settingsIntent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName())
                                    .putExtra(Settings.EXTRA_CHANNEL_ID, R.string.NEWS_CHANNEL_ID);
                            startActivity(settingsIntent);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create()
                    .show();

             */
            // return;

        }
     /*
        //request permission
        Dexter.withActivity(this).withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {


                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {

                        Toast.makeText(introPage.this, "acces deined", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();

        //phone permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            new AlertDialog.Builder(this,4)
                    .setTitle("Required Location Permission")
                    .setMessage("You have to give this permission to acess this feature")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(introPage.this,
                                    new String[]{Manifest.permission.CALL_PHONE},
                                    1);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create()
                    .show();
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //return;
        }

*/

    }

    public void addDotsIndicator(int position){
        mDots=new TextView[sliderAdapterIntro.getCount()];
        mDotsLayout.removeAllViews();
        for (int i=0;i<mDots.length;i++){
            mDots[i]=new TextView(this);
            mDots[i].setText(Html.fromHtml("&#8226"));
            mDots[i].setTextSize(25f);
            mDots[i].setTextColor(getResources().getColor(R.color.colorAccent));
            mDotsLayout.addView(mDots[i]);
        }
        if(mDots.length>0){
            mDots[position].setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        }
    }

    ViewPager.OnPageChangeListener viewListner=new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int i) {
            addDotsIndicator(i);
            currentPage=i;

            if(i==0){
                next_finish.setEnabled(true);
                back.setEnabled(false);
                back.setVisibility(View.INVISIBLE);

                next_finish.setText("Next");
                back.setText("");
            }
            else if(i==mDots.length-1){
                next_finish.setEnabled(true);
                back.setEnabled(true);
                back.setVisibility(View.VISIBLE);

                next_finish.setText("Finish");
                back.setText("Back");
            }
            else {
                next_finish.setEnabled(true);
                back.setEnabled(true);
                back.setVisibility(View.VISIBLE);

                next_finish.setText("Next");
                back.setText("Back");

            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);


//        final FirebaseAuth mAuth=FirebaseAuth.getInstance();
        String introShow=prefs.getString("isIntroShow","");

        if(introShow.equals("")) {
            //  Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
        }
        else {
            //will exicute this if user already shown this intro and it will redirect to mainscreen
            startActivity(new Intent(intropage.this,DashBoradActivity.class));

        }


    }


    //its for notification channel creation
    private void createNotificationChannel(){
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        //setting user to particular category
        FirebaseMessaging.getInstance().subscribeToTopic("nonuser")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Done";
                        if (!task.isSuccessful()) {
                            msg = "Error";
                        }
                        Toast.makeText(intropage.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(getString(R.string.NEWS_CHANNEL_ID)
                    ,getString(R.string.CHANNEL_NEWS), NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription(getString(R.string.CHANNEL_DESCRIPTION));
            notificationChannel.setShowBadge(true);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);


            if (defaultSoundUri != null) {
                AudioAttributes att = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();
                notificationChannel.setSound(defaultSoundUri, att);
            }

            notificationManager.createNotificationChannel(notificationChannel);



            Toast.makeText(this, "created", Toast.LENGTH_SHORT).show();
        }
    }


    //its to show actual notification
    private void triggerNotification(){
        Intent intent = new Intent(this, RegisterActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0, intent, 0);

        @SuppressLint("ResourceAsColor") NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.NEWS_CHANNEL_ID))
                .setSmallIcon(R.mipmap.logo)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.logo))
                .setContentTitle("Wellcome To Social App")
                .setContentText("Welcome to our Application,Register As User")
                .setStyle(new NotificationCompat.BigTextStyle().bigText("God calls us to the right sharing of world resources, from the burdens of materialism and poverty into the abundance of God's love to work for equity through partnerships with our sisters and brothers throughout the world."))
                .setContentIntent(pendingIntent)
                .setColor(R.color.colorPrimaryDark)
                .setChannelId(getString(R.string.NEWS_CHANNEL_ID))
                .setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setVibrate(new long[]{Notification.DEFAULT_VIBRATE})
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(0, builder.build());


    }
}
