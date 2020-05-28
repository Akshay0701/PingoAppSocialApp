package com.example.pingoapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.pingoapp.adapters.AdapterComments;
import com.example.pingoapp.models.ModelComment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PostsDetail extends AppCompatActivity {


    ProgressDialog pd;

    boolean mProcessComment =false;
    boolean mProcressLike =false;


    SharedPreferences.Editor editor;
    //get to details of user
    String hisUid,myUid,myEmail,myName,myDp
            ,postId,pLikes,hisDp,hisName,pImage;

    ImageView uPictureIv,pImageIv;
    VideoView pVideoVv;
    TextView uNameTv,pTimeTiv,pTitleTv,pDescriptionTv,pLikesTv,pCommentTv;
    ImageButton moreBtn;
    Button likeBtn,shareBtn;
    LinearLayout profileLayout;

    //add comment components
    EditText commentEt;
    ImageButton sendBtn;
    ImageView cAvatarIv;

    ActionBar actionBar;

    RecyclerView recyclerView;

    List<ModelComment> commentList;
    AdapterComments adapterComments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts_detail);


        actionBar=getSupportActionBar();
        actionBar.setTitle("Post Details");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorAccent)));
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);

        editor= PreferenceManager.getDefaultSharedPreferences(this).edit();

        //init view
        uPictureIv=findViewById(R.id.uPictureIv);
        pImageIv=findViewById(R.id.pImageIv);
        pVideoVv=findViewById(R.id.pVideoVv);
        uNameTv=findViewById(R.id.uNameTv);
        pTimeTiv=findViewById(R.id.pTimeTv);
        pTitleTv=findViewById(R.id.pTitleTv);
        pDescriptionTv=findViewById(R.id.pDescriptionTv);
        pLikesTv=findViewById(R.id.pLikesTv);
        pCommentTv=findViewById(R.id.pCommentTv);
        moreBtn=findViewById(R.id.moreBtn);
        likeBtn=findViewById(R.id.likeBtn);
        shareBtn=findViewById(R.id.shareBtn);
        profileLayout=findViewById(R.id.profileLayout);
        recyclerView=findViewById(R.id.recyclerView);

        //get post id by intent
        Intent intent=getIntent();
        postId=intent.getStringExtra("postId");


        commentEt=findViewById(R.id.commentEt);
        sendBtn=findViewById(R.id.sendBtn);
        cAvatarIv=findViewById(R.id.cAvatarIv);

        loadPostInfo();

        checkforuserlogin();

        loadUserInfo();

        setLikes();

        //set SubTitle of Action Bar
        actionBar.setSubtitle("SignedIn As: "+myEmail);
        loadComments();

        //send comment buuton
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
            }
        });

        //like btn handle
        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likePost();
            }
        });

        //more button handle
        moreBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                showMoreOptions();
            }
        });

        //share handle
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pTitle=pTitleTv.getText().toString();
                String pDescription=pDescriptionTv.getText().toString();

                BitmapDrawable bitmapDrawable=(BitmapDrawable)pImageIv.getDrawable();
                if(bitmapDrawable==null){
                    //post without image
                    shareTextOnly(pTitle,pDescription);

                }else {
                    //post with image
                    Bitmap bitmap=bitmapDrawable.getBitmap();
                    shareImageAndText(pTitle,pDescription,bitmap);


                }

            }
        });
    }

    private void shareImageAndText(String pTitle, String pDescription, Bitmap bitmap) {
        String shareBody=pTitle+"\n"+pDescription;

        Uri uri=saveImageToShare(bitmap);

        //share intent
        Intent sIntent=new Intent(Intent.ACTION_SEND);
        sIntent.putExtra(Intent.EXTRA_STREAM,uri);

        sIntent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
        sIntent.putExtra(Intent.EXTRA_TEXT,shareBody);
        sIntent.setType("image/png");
        startActivity(Intent.createChooser(sIntent,"Share Via"));

    }

    private Uri saveImageToShare(Bitmap bitmap) {

        File imageFolder=new File(this.getCacheDir(),"images");
        Uri uri=null;
        try {
            imageFolder.mkdir();
            File file=new File(imageFolder,"shared_image.png");
            FileOutputStream stream=new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG,90,stream);
            stream.flush();
            stream.close();
            uri= FileProvider.getUriForFile(this,"com.example.socialapp.fileprovider",file);

        }catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

        }
        return uri;
    }

    private void shareTextOnly(String pTitle, String pDescription) {

        String shareBody=pTitle+"\n"+pDescription;

        //share intent
        Intent sIntent=new Intent(Intent.ACTION_SEND);
        sIntent.setType("text/plain");
        sIntent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
        sIntent.putExtra(Intent.EXTRA_TEXT,shareBody);
        startActivity(Intent.createChooser(sIntent,"Share Via"));

    }
    private void loadComments() {
        //linear layout
        LinearLayoutManager layoutManager=new LinearLayoutManager(getApplicationContext());
        //set layout to recycler
        recyclerView.setLayoutManager(layoutManager);

        //init
        commentList=new ArrayList<>();

        //path  of post ,to get its comment
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                commentList.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    ModelComment modelComment=ds.getValue(ModelComment.class);
                    commentList.add(modelComment);

                    //pass myuid and postid as parameter of contructor of comment Adapter


                    //setup adapter
                    adapterComments=new AdapterComments(getApplicationContext(),commentList,myUid,postId);

                    recyclerView.setAdapter(adapterComments);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void showMoreOptions() {
        PopupMenu popupMenu=new PopupMenu(this,moreBtn, Gravity.END);


        if(hisUid.equals(myUid)){

            popupMenu.getMenu().add(Menu.NONE,0,0,"Delete");
            popupMenu.getMenu().add(Menu.NONE,1,0,"Edit");

        }


        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                int id=item.getItemId();
                if(id == 0){

                    //clicked actiobns
                    beginDelete();


                }
                else if(id==1){
                    //start activity editing
                    Intent intent=new Intent(PostsDetail.this, AddPostActivity.class);
                    intent.putExtra("key","editpost");
                    intent.putExtra("editpostid",postId);

                    startActivity(intent);

                }
                return false;
            }
        });


        popupMenu.show();

    }

    private void beginDelete() {

        if(pImage.equals("noImage")){
            deleteWithoutImage();
        }
        else{
            deleteWithImage();
        }

    }

    private void deleteWithImage() {

        final ProgressDialog pd=new ProgressDialog(this,R.style.AlertDialog);
        pd.setMessage("Deleting..");

        StorageReference picRef= FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                //image
                Query fquery= FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
                fquery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds:dataSnapshot.getChildren()){
                            ds.getRef().removeValue();



                        }
                        Toast.makeText(PostsDetail.this, "Deleted Message", Toast.LENGTH_SHORT).show();
                        pd.dismiss();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(PostsDetail.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void deleteWithoutImage() {
        final ProgressDialog pd=new ProgressDialog(this,R.style.AlertDialog);
        pd.setMessage("Deleting..");

        //image
        Query fquery= FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
        fquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    ds.getRef().removeValue();



                }
                Toast.makeText(PostsDetail.this, "Deleted Message", Toast.LENGTH_SHORT).show();
                pd.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setLikes() {
        final DatabaseReference likeRef=FirebaseDatabase.getInstance().getReference().child("Likes");
        likeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(postId).hasChild(myUid)){

                    //user has like this post
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked_black,0,0,0);
                    likeBtn.setText("Liked");
                }
                else {
                    //user has not like this post
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_black,0,0,0);
                    likeBtn.setText("Like");

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void likePost() {
        //add likes
        mProcressLike=true;
        //get id  of the post clicked


        final DatabaseReference likeRef=FirebaseDatabase.getInstance().getReference().child("Likes");
        final DatabaseReference postRef=FirebaseDatabase.getInstance().getReference().child("Posts");
        likeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(mProcressLike){
                    if(dataSnapshot.child(postId).hasChild(myUid)){
                        //already liked
                        postRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)-1));
                        likeRef.child(postId).child(myUid).removeValue();
                        mProcressLike=false;



                    }
                    else{
                        //Not already liked
                        postRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)+1));
                        likeRef.child(postId).child(myUid).setValue("Liked");
                        mProcressLike=false;


                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void postComment() {

        pd=new ProgressDialog(this,R.style.AlertDialog);
        pd.setMessage("Adding Comment");

        //get data from comment edit text
        String comment=commentEt.getText().toString().trim();
        //vadalite
        if(TextUtils.isEmpty(comment)){
            Toast.makeText(this, "empty comment", Toast.LENGTH_SHORT).show();
        return;
        }

        String timeStamp= String.valueOf(System.currentTimeMillis());

        //each post have child comment value which store comment of that posts
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");

        //hash map
        HashMap<String, Object> hashMap=new HashMap<>();
        //put info hashmap
        hashMap.put("cId",timeStamp);
        hashMap.put("comment",comment);
        hashMap.put("timestamp",timeStamp);
        hashMap.put("uid",myUid);
        hashMap.put("uEmail",myEmail);
        hashMap.put("uDp",myDp);
        hashMap.put("uName",myName);


        //put
        ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                pd.dismiss();
                Toast.makeText(PostsDetail.this, "comment Added", Toast.LENGTH_SHORT).show();
            commentEt.setText("");
            updateCommentCount();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                pd.dismiss();
                Toast.makeText(PostsDetail.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void updateCommentCount() {
        //whenever user enter comment  add comment it incerase count as Likes
        mProcessComment=true;
        final DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(mProcessComment){
                    String comments=""+dataSnapshot.child("pComments").getValue();
                    int newCommentVal= Integer.parseInt(comments)+1;
                    ref.child("pComments").setValue(""+newCommentVal);
                    mProcessComment=false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadUserInfo() {
    Query myRef= FirebaseDatabase.getInstance().getReference("Users");
    myRef.orderByChild("uid").equalTo(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            for(DataSnapshot ds:dataSnapshot.getChildren()){
                myName=""+ds.child("name").getValue();
                myDp=""+ds.child("image").getValue();

                //set image
                try{
                    Picasso.get().load(myDp).placeholder(R.drawable.ic_us_dark).into(cAvatarIv);

                }catch (Exception e){


                    Picasso.get().load(R.drawable.ic_us_dark).into(cAvatarIv);

                }


            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    });

    }

    private void loadPostInfo() {

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Posts");
        Query query=ref.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //keep checking psot
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    //get data
                    String pTitle=""+ds.child("pTitle").getValue();
                    String pDescr=""+ds.child("pDescr").getValue();
                    pLikes=""+ds.child("pLikes").getValue();
                    String pTimeStamp=""+ds.child("pTime").getValue();
                     pImage=""+ds.child("pImage").getValue();
                    hisDp=""+ds.child("uDp").getValue();
                    hisUid=""+ds.child("uid").getValue();
                    String uEmail=""+ds.child("uEmail").getValue();
                    String commentCount=""+ds.child("pComments").getValue();
                    hisName=""+ds.child("uName").getValue();
                    //check is post having video
                    String typepost=""+ds.child("typepost").getValue();
                    String pvideo=""+ds.child("pVideo").getValue();

                    //convert timestamp
                    Calendar calendar= Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
                    String pTime= DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();

                    //set Data
                    pTitleTv.setText(pTitle);
                    pDescriptionTv.setText(pDescr);
                    pLikesTv.setText(pLikes+"Likes");
                    pTimeTiv.setText(pTime);
                    pCommentTv.setText(commentCount+"Comments");

                    uNameTv.setText(hisName);

                    //set image
                    if(pImage.equals("noImage")){

                        pImageIv.setVisibility(View.GONE);
                    }else {
                        //shwww image
                        pImageIv.setVisibility(View.VISIBLE);
                        //post image get
                        try {
                            Picasso.get().load(pImage).into(pImageIv);

                        }catch (Exception e) {

                        }
                    }
                    if (typepost.equals("video")){
                        pImageIv.setVisibility(View.GONE);
                        pVideoVv.setVisibility(View.VISIBLE);
                        Uri uri= Uri.parse(pvideo);
                        pVideoVv.setVideoURI(uri);

                        pVideoVv.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                                    @Override
                                    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                                        MediaController mediaController = new MediaController(getApplicationContext());
                                        mediaController.setAnchorView(pVideoVv);
                                        pVideoVv.setMediaController(mediaController);
                                    }
                                });
                            }
                        });
                        pVideoVv.start();
                    }

                    //set image who comment
                    try {

                        Picasso.get().load(hisDp).placeholder(R.drawable.ic_us_dark).into(uPictureIv);
                    }catch (Exception e){


                        Picasso.get().load(R.drawable.ic_us_dark).into(uPictureIv);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    public void checkforuserlogin() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {

            myEmail=user.getEmail();
            myUid=user.getUid();

        }
        else{
            startActivity(new Intent(this,RegisterActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);

        //hide sreach bar and post  options
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.action_logout){
            editor.remove("username");
            editor.remove("password");
            editor.apply();
            FirebaseAuth.getInstance().signOut();
            checkforuserlogin();
        }
        return super.onOptionsItemSelected(item);
    }
}
