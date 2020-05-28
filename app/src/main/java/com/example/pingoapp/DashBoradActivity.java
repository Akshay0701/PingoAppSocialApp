package com.example.pingoapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.example.pingoapp.models.ModelStory;
import com.example.pingoapp.notifications.Token;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import me.ibrahimsn.lib.OnItemSelectedListener;
import me.ibrahimsn.lib.SmoothBottomBar;

public class DashBoradActivity extends AppCompatActivity {

    ActionBar actionBar;
    private static final String ID="some_id";
    private static final String NAME="FirebaseAPP";
    String token;
    FirebaseAuth firebaseAuth;
    ImageView message,darkmode;

    String mUID;
    FirebaseUser user;
    Boolean timepass=false;
    int currentpos=0;

    Toolbar myToolbar;

    SmoothBottomBar smoothBottomBar;

    @SuppressLint({"ResourceAsColor", "WrongConstant", "ClickableViewAccessibility"})
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashborad);
        firebaseAuth=FirebaseAuth.getInstance();
        checkforuserlogin();
        createNotificationChannel();
        final ImageView imagetoolbar=findViewById(R.id.imagetoolbar);
        actionBar=getSupportActionBar();
    actionBar.setTitle("Profile");
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorAccent)));
        actionBar.setTitle(Html.fromHtml("<font color='#43B54A'>ActionBarTitle </font>"));
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        //setting send message btn
        AppBarLayout appBarLayout =findViewById(R.id.bar);
       myToolbar =  findViewById(R.id.toolbar);
     /*   myToolbar.setNavigationIcon(R.drawable.ic_back_light);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

      */
//      setSupportActionBar(myToolbar);
        // setSupportActionBar(myToolbar);
//

        //    checkfordeletionofstory();



        darkmode=findViewById(R.id.darkmode);
        darkmode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs;
                prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                new AlertDialog.Builder(DashBoradActivity.this,5)
                        .setTitle("Activate/Deactivate")
                        .setMessage("Are you sure you want to swtich your mode?")

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES){
                                    //come to light mode
                                     AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                                    InitApplication.getInstance().setIsNightModeEnabled(false,getApplicationContext());
                                    Intent intent = getIntent();
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                    finish();
                                    //  imagetoolbar.setImageResource(R.drawable.pingoroundlogodark);
                                    timepass=false;
                                    Toast.makeText(DashBoradActivity.this, "Light Mode Activated", Toast.LENGTH_SHORT).show();
                                    startActivity(intent);

                                } else {
                                       AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                                    //come to dark mode
                                    InitApplication.getInstance().setIsNightModeEnabled(true,getApplicationContext());
                                    Intent intent = getIntent();
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                    finish();
                                    // imagetoolbar.setImageResource(R.drawable.pingoroundlogo);
                                    timepass=true;
                                    Toast.makeText(DashBoradActivity.this, "Dark Mode Activated", Toast.LENGTH_SHORT).show();
                                    startActivity(intent);
                                }
                            }
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .create()
                        .show();


                

            }
        });

       // updateToken(token);

        message=findViewById(R.id.message);

        getSupportActionBar().hide();
        getWindow().getDecorView().setBackgroundColor(R.color.colorPrimary);

        //    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);



        firebaseAuth= FirebaseAuth.getInstance();

        BottomNavigationView bottomNavigationView=findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(selectedListener);

        actionBar.setTitle("Home");
        actionBar.setTitle(Html.fromHtml("<font color='#43B54A'>Home </font>"));
        HomeFragment fragment1=new HomeFragment();
        FragmentTransaction ft1=getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.content1,fragment1,"");
        currentpos=0;
        ft1.commit();


        //checking dark mode
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean true1=false;
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES){
      //      AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

            imagetoolbar.setImageResource(R.drawable.pingoroundlogodark);

        }
        else {
        //    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            imagetoolbar.setImageResource(R.drawable.pingoroundlogo);
        }

        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionBar.setTitle("Chats List");
                actionBar.setTitle(Html.fromHtml("<font color='#43B54A'>Chats List </font>"));
                Chat_ListFragment fragment4=new Chat_ListFragment();
                FragmentTransaction ft4=getSupportFragmentManager().beginTransaction();
                ft4.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
                ft4.replace(R.id.content1,fragment4,"");
                currentpos=3;
                ft4.commit();
            }
        });

     



        //tutorialstart(myToolbar);
    }


    private void tutorialstart(Toolbar myToolbar) {

        // We load a drawable and create a location to show a tap target here
        // We need the display to get the width and height at this point in time
        final Display display = getWindowManager().getDefaultDisplay();
        // Load our little droid guy
        final Drawable droid = ContextCompat.getDrawable(this, R.drawable.pingoroundlogo);
        // Tell our droid buddy where we want him to appear
        final Rect droidTarget = new Rect(0, 0, droid.getIntrinsicWidth() * 2, droid.getIntrinsicHeight() * 2);
        // Using deprecated methods makes you look way cool
        droidTarget.offset(display.getWidth() / 2, display.getHeight() / 2);

        final SpannableString sassyDesc = new SpannableString("It allows you to go back, sometimes");
        sassyDesc.setSpan(new StyleSpan(Typeface.ITALIC), sassyDesc.length() - "sometimes".length(), sassyDesc.length(), 0);

        // We have a sequence of targets, so lets build it!
        final TapTargetSequence sequence = new TapTargetSequence(this)
                .targets(
                        // This tap target will target the back button, we just need to pass its containing toolbar
                        TapTarget.forToolbarMenuItem(myToolbar, R.id.message, "Message Button", "As you can see recent message over here...")
                                .dimColor(R.color.colorgray)
                                .outerCircleColor(R.color.colorPrimary)
                                .targetCircleColor(android.R.color.black)
                                .transparentTarget(true)
                                .titleTextDimen(R.dimen.default_bmb_ham_button_height)
                                .descriptionTextColor(R.color.colorgray)
                                .textColor(R.color.white)
                                .id(1),
                        // Likewise, this tap target will target the search button
                        TapTarget.forToolbarMenuItem(myToolbar, R.id.darkmode, "Dark Mode", "As you can easily swtich Light to Dark mode ..")
                                .dimColor(R.color.colorgray)
                                .outerCircleColor(R.color.colorPrimary)
                                .targetCircleColor(android.R.color.black)
                                .descriptionTextColor(R.color.colorgray)
                                .transparentTarget(true)
                                .titleTextDimen(R.dimen.default_bmb_ham_button_height)
                                .textColor(R.color.white)
                                .id(2),
                        // You can also target the overflow button in your toolbar
                        // This tap target will target our droid buddy at the given target rect
                        TapTarget.forBounds(droidTarget, "Oh look!", "You can point to any part of the screen. You also can't cancel this one!")
                                .cancelable(true)
                                .icon(droid)
                                .id(4)
                )
                .listener(new TapTargetSequence.Listener() {
                    // This listener will tell us when interesting(tm) events happen in regards
                    // to the sequence
                    @Override
                    public void onSequenceFinish() {
                      //  ((TextView) findViewById(R.id.educated)).setText("Congratulations! You're educated now!");
                        SharedPreferences.Editor editor;
                        editor= PreferenceManager.getDefaultSharedPreferences(DashBoradActivity.this).edit();
                        editor.putString("dashboradTutorial", "1");//1 value will note as user as seen intro
                        editor.apply();
                      //  Toast.makeText(DashBoradActivity.this, "finish", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                        Log.d("TapTargetView", "Clicked on " + lastTarget.id());
                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                        final AlertDialog dialog = new AlertDialog.Builder(DashBoradActivity.this)
                                .setTitle("Uh oh")
                                .setMessage("You canceled the sequence")
                                .setPositiveButton("Oops", null).show();
                        TapTargetView.showFor(dialog,
                                TapTarget.forView(dialog.getButton(DialogInterface.BUTTON_POSITIVE), "Uh oh!", "You canceled the sequence at step " + lastTarget.id())
                                        .cancelable(false)
                                        .tintTarget(false), new TapTargetView.Listener() {
                                    @Override
                                    public void onTargetClick(TapTargetView view) {
                                        super.onTargetClick(view);
                                        dialog.dismiss();
                                    }
                                });
                    }
                });

        // You don't always need a sequence, and for that there's a single time tap target
        final SpannableString spannedDesc = new SpannableString("This is small tutorial");
        spannedDesc.setSpan(new UnderlineSpan(), spannedDesc.length() - "TapTargetView".length(), spannedDesc.length(), 0);
        TapTargetView.showFor(this, TapTarget.forView(findViewById(R.id.message), "Hello, world!", spannedDesc)
                .cancelable(false)
                .drawShadow(true)
                .titleTextDimen(R.dimen.default_bmb_text_inside_circle_height)
                .tintTarget(false), new TapTargetView.Listener() {
            @Override
            public void onTargetClick(TapTargetView view) {
                super.onTargetClick(view);
                // .. which evidently starts the sequence we defined earlier
                sequence.start();
            }

            @Override
            public void onOuterCircleClick(TapTargetView view) {
                super.onOuterCircleClick(view);
                Toast.makeText(view.getContext(), "You clicked the outer circle!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTargetDismissed(TapTargetView view, boolean userInitiated) {
                Log.d("TapTargetViewSample", "You dismissed me :(");
            }
        });


    }

    private void createNotificationChannel() {
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
                        Toast.makeText(DashBoradActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            NotificationChannel notificationChannel = new NotificationChannel(ID
                    ,NAME, NotificationManager.IMPORTANCE_HIGH);
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
/*removing stroy after 1 day
    private void checkfordeletionofstory() {
        long cutoff = new Date().getTime() - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS);
        Query ttlRef = FirebaseDatabase.getInstance().getReference("Story");
        Query oldItems = ttlRef.orderByChild("pTime").endAt(cutoff);
        oldItems.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot itemSnapshot: snapshot.getChildren()) {
                    final ModelStory modelStory= itemSnapshot.getValue(ModelStory.class);
                    StorageReference picRef= FirebaseStorage.getInstance().getReferenceFromUrl(modelStory.getsImage());
                    picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            //imag
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                          //  pd.dismiss();
                         //   Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                   // itemSnapshot.getRef().removeValue();
                    itemSnapshot.getRef().removeValue();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw databaseError.toException();
            }
        });

    }

 */

    @Override
    protected void onResume() {
        checkforuserlogin();
        super.onResume();
    }

    public void updateToken(String token){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken=new Token(token);
        ref.child(mUID).setValue(mToken);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener=
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    switch (menuItem.getItemId()){
                        case R.id.nav_home:
                            //home fragmentation

                            actionBar.setTitle("Home");
                           actionBar.setTitle(Html.fromHtml("<font color='#43B54A'>Home </font>"));
                            HomeFragment fragment1=new HomeFragment();
                            FragmentTransaction ft1=getSupportFragmentManager().beginTransaction();
                            if (currentpos>0) {
                                ft1.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
                            }
                            else if (currentpos==0)
                            {
                                break;
                            }
                            ft1.replace(R.id.content1,fragment1,"");
                            ft1.commit();
                            currentpos=0;
                            return true;
                        case R.id.nav_profile:
                            //profile fargment transcatrion

                            actionBar.setTitle("Profile");
                           actionBar.setTitle(Html.fromHtml("<font color='#43B54A'>Profile </font>"));
                            ProfileFragment fragment2=new ProfileFragment();
                            FragmentTransaction ft2=getSupportFragmentManager().beginTransaction();
                            if (currentpos>1) {
                                ft2.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
                            }
                            else if (currentpos==1){
                                break;
                            }
                            else {
                                ft2.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
                            }
                            ft2.replace(R.id.content1,fragment2,"");
                            currentpos=1;
                            ft2.commit();
                            return true;
                        case R.id.nav_user:
                            //user fragmentation

                            actionBar.setTitle("Users");
                            actionBar.setTitle(Html.fromHtml("<font color='#43B54A'>Users </font>"));
                            UsersFragment fragment3=new UsersFragment();
                            FragmentTransaction ft3=getSupportFragmentManager().beginTransaction();
                            if (currentpos>3) {
                                ft3.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
                            }
                            else if (currentpos==3){
                                break;
                            }
                            else {
                                ft3.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
                            }
                            ft3.replace(R.id.content1,fragment3,"");
                            ft3.commit();
                            currentpos=3;
                            return true;
                        case R.id.nav_chat:
                            //user fragmentation
                            actionBar.setTitle("Chats List");
                            actionBar.setTitle(Html.fromHtml("<font color='#43B54A'>Chats List </font>"));
                            Chat_ListFragment fragment4=new Chat_ListFragment();
                            FragmentTransaction ft4=getSupportFragmentManager().beginTransaction();
                            if (currentpos>4) {
                                ft4.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
                            }
                            else if (currentpos==4){
                                break;
                            }
                            else {
                                ft4.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
                            }
                            ft4.replace(R.id.content1,fragment4,"");
                            currentpos=4;
                            ft4.commit();
                            return true;

                        case  R.id.nav_add_post:
                            startActivity(new Intent(DashBoradActivity.this,AddPostActivity.class));
                            if (currentpos>2) {
                                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                            }
                            else if (currentpos==2){
                                break;
                            }
                            else {
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            }
                            currentpos=2;
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            return true;
                    }

                    return false;
                }
            };

    public void checkforuserlogin() {
        firebaseAuth= FirebaseAuth.getInstance();
      user = firebaseAuth.getCurrentUser();
        if (user != null) {
            token=FirebaseInstanceId.getInstance().getToken();
            mUID=user.getUid();

            SharedPreferences sp=getSharedPreferences("SP_User",MODE_PRIVATE);
            SharedPreferences.Editor editor=sp.edit();
            editor.putString("Current_USERID",mUID);
            editor.apply();

            //updatetoken
            //noinspection deprecation
            updateToken(FirebaseInstanceId.getInstance().getToken());
        }
        else{
            startActivity(new Intent(DashBoradActivity.this,RegisterActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        checkforuserlogin();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String isdashboradtutorial=prefs.getString("dashboradTutorial","");
        if(isdashboradtutorial.equals("")) {
          tutorialstart(myToolbar);
        }

        super.onStart();
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_message) {

            actionBar.setTitle("Chats List");
            Chat_ListFragment fragment4=new Chat_ListFragment();
            FragmentTransaction ft4=getSupportFragmentManager().beginTransaction();
            ft4.replace(R.id.content1,fragment4,"");
            ft4.commit();
        }

        return super.onOptionsItemSelected(item);
    }

}
