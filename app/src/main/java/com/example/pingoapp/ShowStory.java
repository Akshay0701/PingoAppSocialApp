package com.example.pingoapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pingoapp.adapters.AdapterUsers;
import com.example.pingoapp.models.ModelStory;
import com.example.pingoapp.models.ModelUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import jp.shts.android.storiesprogressview.StoriesProgressView;

public class ShowStory extends AppCompatActivity implements StoriesProgressView.StoriesListener {


    //stroy component
    int counter=0;
    long presstime=0L;
    long limit=500L;

    AdapterUsers adapterUsers ;
    StoriesProgressView storiesProgressView;
    List<ModelUser> userList;

     RecyclerView recyclerView;
    LinearLayout r_seen;
    ImageView story_delete;
    TextView seen_number;

    ImageView dpimg,storyimg;
    Button back;
    TextView uNameTv;
    //uid  of story user
    String uid,uName,uDp,sImage;
    Boolean stopit=false;
    ProgressBar progress_bar;

    List<String> userIds;
    List<String> images;
    List<String> storyids;
    String  userid;


    private View.OnTouchListener onTouchListener=new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    presstime = System.currentTimeMillis();
                    storiesProgressView.pause();
                    return false;
                case MotionEvent.ACTION_UP:
                    long now = System.currentTimeMillis();
                    storiesProgressView.resume();
                    return limit < now - presstime;

            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_story);

        getSupportActionBar().hide();

        r_seen=findViewById(R.id.r_seen);
        story_delete=findViewById(R.id.story_delete);
        seen_number=findViewById(R.id.seen_number);

        uNameTv=findViewById(R.id.uNameTv);
        progress_bar=findViewById(R.id.progress_bar);
        dpimg=findViewById(R.id.dpimg);
        storyimg=findViewById(R.id.storyimg);
        back=findViewById(R.id.back);
        storiesProgressView=findViewById(R.id.stories);


        r_seen.setVisibility(View.GONE);
        story_delete.setVisibility(View.GONE);
        //get  uid of clicked posts
        Intent intent=getIntent();
        uid=intent.getStringExtra("userid");
        if (uid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            r_seen.setVisibility(View.VISIBLE);
            story_delete.setVisibility(View.VISIBLE);
        }

        //setting story
        getStories(uid);
        userinfo(uid);
       // uName=intent.getStringExtra("uName");
       // uDp=intent.getStringExtra("uDp");
     //   sImage=intent.getStringExtra("sImage");

        //loading data in story activity

        //reverse
        View reverse=findViewById(R.id.reverse);
        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storiesProgressView.reverse();
            }
        });
        reverse.setOnTouchListener(onTouchListener);

        //skip
        View skip=findViewById(R.id.skip);
        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storiesProgressView.skip();
            }
        });
        skip.setOnTouchListener(onTouchListener);


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //go back to home
                progress_bar.setVisibility(View.GONE);
                stopit=true;
                onBackPressed();
            }
        });

        r_seen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show button layout

                final BottomSheetDialog bottomSheetDialog=new BottomSheetDialog(ShowStory.this,R.style.BottomSheetDialogTheme);
                View bottonView=  LayoutInflater.from(ShowStory.this).inflate(R.layout.bottom_sheet_storyview,
                        (CardView)findViewById(R.id.bottomSheetStoryView));
                //call this function when share btn is click
                //all down is user list of view

                //init postlist
                userList=new ArrayList<>();
                //init
                recyclerView=bottonView.findViewById(R.id.users_regulerview);
               recyclerView.setLayoutManager(new LinearLayoutManager(ShowStory.this, LinearLayoutManager.HORIZONTAL, false));
              //  GridLayoutManager manager = new GridLayoutManager(ShowStory.this, 2, GridLayoutManager.VERTICAL, false);
               // recyclerView.setLayoutManager(manager);
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        //TODO your background code
                        //path
                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                DatabaseReference databaseReference=FirebaseDatabase.getInstance().
                                        getReference("Story").child(uid).child(storyids.get(counter)).child("views");
                                userIds=new ArrayList<>();
                                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        userIds.clear();
                                        for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                                            userIds.add(snapshot.getKey());
                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
                                DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        userList.clear();
                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                            ModelUser modelUser = ds.getValue(ModelUser.class);

                                           for(String uid:userIds){
                                               if (modelUser.getUid().equals(uid))
                                               {
                                                   userList.add(modelUser);
                                               }
                                           }



                                            adapterUsers = new AdapterUsers(ShowStory.this, userList);

                                            recyclerView.setAdapter(adapterUsers);
                                   /*  this code use for auto scroll user bas aur kuch nahi hai iska kaaam
                                   final int speedScroll = 1000;
                                    final Handler handler = new Handler();
                                    final Runnable runnable = new Runnable() {
                                        int count = 0;
                                        boolean flag = true;
                                        @Override
                                        public void run() {
                                            if(count < adapterUsers.getItemCount()){
                                                if(count==adapterUsers.getItemCount()-1){
                                                    flag = false;
                                                }else if(count == 0){
                                                    flag = true;
                                                }
                                                if(flag) count++;
                                                else count--;

                                                recyclerView.smoothScrollToPosition(count);
                                                handler.postDelayed(this,speedScroll);
                                            }
                                        }
                                    };

                                    handler.postDelayed(runnable,speedScroll);

                                    */
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }
                        }).start();
                    }
                });
                bottomSheetDialog.setContentView(bottonView);
                bottomSheetDialog.show();

                if (bottomSheetDialog.isShowing())
                {
                    storiesProgressView.pause();
                }
                else {
                    storiesProgressView.resume();
                }
            }
        });

        story_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Story").child(uid).child(storyids.get(counter));

                StorageReference picRef= FirebaseStorage.getInstance().getReferenceFromUrl(images.get(counter));
                picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        reference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(ShowStory.this, "Deleted", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }
                        });
                    }
                });

            }
        });




    }






    @Override
    public void onBackPressed() {

        stopit=true;
        super.onBackPressed();
        // finish();
    }

    @Override
    public void onNext() {
        Picasso.get().load(images.get(++counter)).placeholder(R.drawable.ic_us_dark).into(storyimg);
        addView(storyids.get(counter));
        seenNumber(storyids.get(counter));
    }

    @Override
    public void onPrev() {
        if ((counter-1)<0) return;
        Picasso.get().load(images.get(--counter)).placeholder(R.drawable.ic_us_dark).into(storyimg);
        seenNumber(storyids.get(counter));
    }

    @Override
    public void onComplete() {
        finish();
    }

    @Override
    protected void onDestroy() {
        storiesProgressView.destroy();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        storiesProgressView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        storiesProgressView.resume();
    }

    private void getStories(String userid){
        images=new ArrayList<>();
        storyids=new ArrayList<>();
        DatabaseReference databaseReference=  FirebaseDatabase.getInstance().getReference("Story").child(userid);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                images.clear();
                storyids.clear();
                for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                    ModelStory story=snapshot.getValue(ModelStory.class);
                    long timecurrent=System.currentTimeMillis();
                    if (timecurrent>story.getsTimeStart()&&timecurrent<story.getsTimeEnd()){
                        images.add(story.getsImage());
                        storyids.add(story.getsId());
                    }
                }

                storiesProgressView.setStoriesCount(images.size());
                storiesProgressView.setStoryDuration(5000L);
                storiesProgressView.setStoriesListener(ShowStory.this);
                storiesProgressView.startStories(counter);


                Picasso.get().load(images.get(counter)).placeholder(R.drawable.ic_us_dark).into(storyimg);
                addView(storyids.get(counter));
                seenNumber(storyids.get(counter));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void userinfo(String userid){
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ModelUser user=dataSnapshot.getValue(ModelUser.class);
                Picasso.get().load(user.getImage()).placeholder(R.drawable.ic_us_dark).into(dpimg);
                uNameTv.setText(user.getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addView(String storyid){
        FirebaseDatabase.getInstance().getReference("Story").
                child(uid).child(storyid).child("views").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
    }

    private void seenNumber(String storyid){
        DatabaseReference databaseReference=FirebaseDatabase.getInstance()
                .getReference("Story").child(uid).child(storyid).child("views");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                seen_number.setText(""+dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


}
