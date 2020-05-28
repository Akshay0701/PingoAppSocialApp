package com.example.pingoapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.pingoapp.adapters.AdapterPosts;
import com.example.pingoapp.models.ModelPosts;
import com.example.pingoapp.models.ModelUser;
import com.example.pingoapp.notifications.APIService;
import com.example.pingoapp.notifications.Client;
import com.example.pingoapp.notifications.Data;
import com.example.pingoapp.notifications.Response;
import com.example.pingoapp.notifications.Sender;
import com.example.pingoapp.notifications.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;

public class ThereProfileActivity extends AppCompatActivity {



    //recycle view
    RecyclerView postsRecycleView;

    FirebaseAuth firebaseAuth;

    SharedPreferences.Editor editor;


    APIService apiService;
    boolean notify=false;
    //profile
    ImageView avatarIv,backimgaa;
    TextView namet,emailt,phonet;

    List<ModelPosts> postsList;
    AdapterPosts adapterPosts;
    String uid,myid;
    FirebaseUser user;

    //follow btn
    Button follow_unfollowBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_there_profile);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Profile");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);


        //create api service
        apiService= Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);

        editor= PreferenceManager.getDefaultSharedPreferences(this).edit();

        avatarIv=findViewById(R.id.avatarIV);
        namet=findViewById(R.id.nameTv);
        emailt=findViewById(R.id.emailTv);
        phonet=findViewById(R.id.phoneTv);
        backimgaa=findViewById(R.id.backimg);

        follow_unfollowBtn=findViewById(R.id.follow_unfollowBtn);
        postsRecycleView=findViewById(R.id.recyclerview_posts);

        firebaseAuth=FirebaseAuth.getInstance();

        checkforuserlogin();


        //get  uid of clicked posts
        Intent intent=getIntent();
        uid=intent.getStringExtra("uid");
        postsList=new ArrayList<>();

        isfollowing(uid,follow_unfollowBtn);

        //data base
        Query query=FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){

                    String bimg=""+ds.child("backimg").getValue();
                    String email=""+ds.child("email").getValue();
                    String image=""+ds.child("image").getValue();
                    String phone=""+ds.child("phone").getValue();
                    String name=""+ds.child("name").getValue();


                    namet.setText(name);
                    phonet.setText(phone);
                    emailt.setText(email);

                    try{
                        Glide.with(ThereProfileActivity.this).load(image).into(avatarIv);
                        Glide.with(ThereProfileActivity.this).load(bimg).into(backimgaa);
                        Picasso.get().load(image).into(avatarIv);
                        Picasso.get().load(bimg).into(backimgaa);

                    }catch (Exception e){
                        Picasso.get().load(R.drawable.ic_add_dark).into(avatarIv);
                        Picasso.get().load(R.drawable.ic_back_dark).into(backimgaa);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        avatarIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), ""+namet.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });
        Query follow=FirebaseDatabase.getInstance().getReference("Follow").orderByChild("followers").equalTo(uid);
        follow.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //follow unfollow code
        follow_unfollowBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View view) {
                //check whether you already follow or not
                notify=true;
                if (follow_unfollowBtn.getText().toString().equals("Follow"))
                {
                FirebaseDatabase.getInstance().getReference().child("Follow").child(myid).child("following").child(uid).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(uid).child("followers").child(myid).setValue(true);
                    //send notification
                    final DatabaseReference database=FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    database.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            ModelUser user=dataSnapshot.getValue(ModelUser.class);

                            if(notify){
                                if (user != null) {
                                    senNotification(uid,user.getName(),"Followed You");
                                }
                            }
                            notify=false;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
                else {
                    new android.app.AlertDialog.Builder(ThereProfileActivity.this,5)
                            .setTitle("Un follow")
                            .setMessage("Are you sure you want to remove this from your following list?")

                            // Specifying a listener allows you to take an action before dismissing the dialog.
                            // The dialog is automatically dismissed when a dialog button is clicked.
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Continue with delete operation
                                    FirebaseDatabase.getInstance().getReference().child("Follow").child(myid).child("following").child(uid).removeValue();
                                    FirebaseDatabase.getInstance().getReference().child("Follow").child(uid).child("followers").child(myid).removeValue();
                                    final DatabaseReference database=FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                    database.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            ModelUser user=dataSnapshot.getValue(ModelUser.class);
                                            if(notify){
                                                if (user != null) {
                                                    senNotification(uid,user.getName(),"UnFollowed You");
                                                }
                                            }
                                            notify=false;
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                                }
                            })

                            // A null listener allows the button to dismiss the dialog and take no further action.
                            .setNegativeButton(android.R.string.no, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .create()
                            .show();
               }
            }
        });


        loadHisPosts();


    }

    private void isfollowing(final String uid, final Button button) {
        DatabaseReference isFollowing= FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseAuth.getCurrentUser().getUid()).child("following");
    isFollowing.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(dataSnapshot.child(uid).exists()){
                button.setText("Following");
            }
            else{
                button.setText("Follow");
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    });

    }

    private void loadHisPosts() {
        //linear layout for recycle view
        LinearLayoutManager layoutManager =new LinearLayoutManager(this);
        //show newest
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);


        //set this layout to recycleview
        postsRecycleView.setLayoutManager(layoutManager);


        //init  post intizalitions
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");

        Query query=ref.orderByChild("uid").equalTo(uid);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                postsList.clear();
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    ModelPosts myPosts=ds.getValue(ModelPosts.class);

                    //add list
                    postsList.add(myPosts);

                    //adapters
                    adapterPosts=new AdapterPosts(ThereProfileActivity.this,postsList);
                    //list this adapter
                    postsRecycleView.setAdapter(adapterPosts);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ThereProfileActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }


    private void searchHisPosts(final String searchQuery) {
        //linear layout for recycle view
        LinearLayoutManager layoutManager =new LinearLayoutManager(this);
        //show newest
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);


        //set this layout to recycleview
        postsRecycleView.setLayoutManager(layoutManager);


        //init  post intizalitions
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");

        Query query=ref.orderByChild("uid").equalTo(uid);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                postsList.clear();
                for(DataSnapshot ds:dataSnapshot.getChildren()){


                    ModelPosts myPosts=ds.getValue(ModelPosts.class);

                    if(myPosts.getpTitle().toLowerCase().contains(searchQuery.toLowerCase())||
                            myPosts.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())) {


                        //add list
                        postsList.add(myPosts);
                    }
                    //adapters
                    adapterPosts=new AdapterPosts(ThereProfileActivity.this,postsList);
                    //list this adapter
                    postsRecycleView.setAdapter(adapterPosts);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ThereProfileActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });


    }

    public void checkforuserlogin() {
      user = firebaseAuth.getCurrentUser();
        if (user != null) {

            myid=user.getUid();


        }
        else{
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        menu.findItem(R.id.action_add_post).setVisible(false);

        MenuItem item=menu.findItem(R.id.action_search);

        SearchView searchView=(SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                if(!TextUtils.isEmpty(query)){
                    //search
                    searchHisPosts(query);
                }
                else {
                    loadHisPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if(!TextUtils.isEmpty(query)){
                    //search
                    searchHisPosts(query);
                }
                else {
                    loadHisPosts();
                }
                return false;
            }
        });



        return super.onCreateOptionsMenu(menu);


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }


    private void senNotification(final String hisID, final String name, final String message) {
        DatabaseReference allTokens=FirebaseDatabase.getInstance().getReference("Tokens");
        Query query=allTokens.orderByKey().equalTo(hisID);
        query.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot  ds:dataSnapshot.getChildren()){
                    Token token=ds.getValue(Token.class);
                    Data data=new Data(FirebaseAuth.getInstance().getUid(),name+" "+message,"Follow Notification",hisID,"like",R.drawable.ic_chatpepl_dark);

                    Sender sender=new Sender(data, Objects.requireNonNull(token).getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new retrofit2.Callback<Response>() {
                                @SuppressWarnings("NullableProblems")
                                @Override
                                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                                    Toast.makeText(ThereProfileActivity.this, ""+response.message(), Toast.LENGTH_SHORT).show();
                                    Log.e("notification","ended"+response.message());
                                }

                                @SuppressWarnings("NullableProblems")
                                @Override
                                public void onFailure(Call<Response> call, Throwable t) {

                                }
                            });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id =item.getItemId();
        if(id==R.id.action_logout){
            editor.remove("username");
            editor.remove("password");
            editor.apply();
            firebaseAuth.signOut();

            checkforuserlogin();
        }

        return super.onOptionsItemSelected(item);

    }
}
