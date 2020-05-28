package com.example.pingoapp.adapters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.example.pingoapp.AddPostActivity;
import com.example.pingoapp.models.ModelPosts;
import com.example.pingoapp.PostsDetail;
import com.example.pingoapp.R;
import com.example.pingoapp.ThereProfileActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.bumptech.glide.*;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterPostInUser  extends RecyclerView.Adapter<AdapterPostInUser.MyHolder>  {


    Context context;
    List<ModelPosts> postsList;


    String myUid;

    String typepost;

    //get data
    String uid;
    String uEmail;
    String uName;
    String uDp;
    String pid;
    String pTitle;
    String pDescription;
    String pImage;
    String pTimeStamp;
    String pLikes;
    String pComments;
    String pvideo;

    boolean done;
    boolean mProcressLike=false;
    private DatabaseReference likeRef,postRef;



    //view  from row post
    ImageView uPictureIv1;
    ImageView pImageIv1;
    VideoView pVideoVv1;
    TextView uNameIv1,pTimeTv1,pTitleTv2,pDescriptionTv1,pLikeTv1,pComments1;
    ImageButton moreBtn1;
    Button likeBtn,commentBtn,shareBtn;
    LinearLayout profileLayout1;
    ProgressBar progress_bar1;
    //like animation
    LottieAnimationView likeani1;


    public AdapterPostInUser(Context context, List<ModelPosts> postsList) {
        this.context = context;
        this.postsList = postsList;
        myUid= FirebaseAuth.getInstance().getCurrentUser().getUid();
        likeRef=FirebaseDatabase.getInstance().getReference().child("Likes");
        postRef=FirebaseDatabase.getInstance().getReference().child("Posts");
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(context).inflate(R.layout.row_post_user_panel,parent,false);


        return new MyHolder(view);
    }

    public void profileopen(int adapterPosition){
        // go to that user profile with specified user
        Intent intent=new Intent(context, ThereProfileActivity.class);
        String postpublisher_Uid =postsList.get(adapterPosition).getUid();
        intent.putExtra("uid",postpublisher_Uid);
        context.startActivity(intent);

    }
    public boolean deletepostwithswipe(String myid, int adapterPosition){
        //   Toast.makeText(context, ""+uid+" "+myid, Toast.LENGTH_LONG).show();
        String postpublisher_Uid =postsList.get(adapterPosition).getUid();
        if(myid.equals(postpublisher_Uid)){

            new android.app.AlertDialog.Builder(context,5)
                    .setTitle("Delete Post")
                    .setMessage("Are you sure you want to remove this Post your Timeline?")
                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Continue with delete operation
                            beginDelete(pid,pImage,pvideo,typepost);
                            done=true;
                        }
                    })

                    // A null listener allows the button to dismiss the dialog and take no further action.
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            done=false;
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .create()
                    .show();

            if(done) {
                return true;
            }
            else{
                return false;
            }
        }
        else {
            Toast.makeText(context, "you can delete only your post", Toast.LENGTH_SHORT).show();
            return false;
        }


    }


    @SuppressWarnings("deprecation")
    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int i) {

        //check is post having video
        typepost=postsList.get(i).getTypepost();


        //get data
        uid=postsList.get(i).getUid();
        uEmail=postsList.get(i).getuEmail();
        uName=postsList.get(i).getuName();
        uDp=postsList.get(i).getuDp();
        pid=postsList.get(i).getpId();
        pTitle=postsList.get(i).getpTitle();
        pDescription=postsList.get(i).getpDescr();
        pImage=postsList.get(i).getpImage();
        pTimeStamp=postsList.get(i).getpTime();
        pLikes=postsList.get(i).getpLikes();
        pComments=postsList.get(i).getpComments();
        pvideo=postsList.get(i).getpVideo();


        //convert timestamp
        final Calendar calendar= Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        final String pTime= DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();

        //holder.pImageIv.setVisibility(View.GONE);
        holder.pVideoVv.setVisibility(View.GONE);

        //set user data
        //Toast.makeText(context, "name"+uName, Toast.LENGTH_SHORT).show();
        holder.uNameIv.setText(uName);
        holder.pTimeTv.setText(pTime);
        holder.pTitleTv.setText(pTitle);
        holder.pDescriptionTv.setText(pDescription);
        holder.pLikeTv.setText(pLikes+" Likes");
        holder.pComments.setText(pComments+" Comments");
        setLikes(holder,pid);
        holder.progress_bar.setVisibility(View.GONE);


        //image set user dp
        try {
            Picasso.get().load(uDp).placeholder(R.drawable.ic_us_dark).into(holder.uPictureIv);

        }catch (Exception e) {

        }


        if(pImage.equals("noImage")){

            holder.pImageIv.setVisibility(View.GONE);
            holder.pVideoVv.setVisibility(View.GONE);
        }
        else {
            //shwww image
            holder.pImageIv.setVisibility(View.GONE);
            //post image get
            try {
               /* Picasso.get().load(pImage).into(holder.pImageIv, new Callback() {
                    @Override
                    public void onSuccess() {
                        //set loading progress bar gone

                        //   Matrix matrix = holder.pImageIv.getDisplayMatrix(DisplayType.FIT_TO_SCREEN);
                        // holder.pImageIv.setDisplayMatrix(matrix);
                        holder.pImageIv.setVisibility(View.VISIBLE);
                        //      PhotoViewAttacher photoViewAttacher=new PhotoViewAttacher(holder.pImageIv);
                        //    photoViewAttacher.update();
                        //  holder.imageZoomHelper.setViewZoomable( holder.pImageIv);
                        holder.progress_bar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });

                */
                Glide.with(context).load(pImage).into(holder.pImageIv);
                holder.pImageIv.setVisibility(View.VISIBLE);
                //      PhotoViewAttacher photoViewAttacher=new PhotoViewAttacher(holder.pImageIv);
                //    photoViewAttacher.update();
                //  holder.imageZoomHelper.setViewZoomable( holder.pImageIv);
                holder.progress_bar.setVisibility(View.GONE);


            }catch (Exception e) {

            }
        }


        if (typepost.equals("video")){
            //we are inti video to cideo view

            holder.pImageIv.setVisibility(View.GONE);
            holder.pVideoVv.setVisibility(View.VISIBLE);
            Uri uri= Uri.parse(pvideo);
            holder.pVideoVv.setVideoURI(uri);

            holder.pVideoVv.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                        @Override
                        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                            MediaController mediaController = new MediaController(context);
                            mediaController.setAnchorView(holder.pVideoVv);
                            holder.pVideoVv.setMediaController(mediaController);
                        }
                    });
                }
            });
            holder.pVideoVv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.pVideoVv.start();
                }
            });
            holder.cardview.setVisibility(View.GONE);
           // holder.itemView.setVisibility(View.GONE);
        //    holder.pVideoVv.start();
            //   holder.pVideoVv.pause();
            holder.progress_bar.setVisibility(View.GONE);

        }




        //handle more btn
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                //will implement later
                showMoreOptions(holder.moreBtn,uid,myUid,pid,pImage,pvideo,typepost);
            }
        });
        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add likes
                final int pLikes= Integer.parseInt(postsList.get(i).getpLikes());
                mProcressLike=true;
                //get id  of the post clicked
                final String postIde= postsList.get(i).getpId();
                likeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(mProcressLike){
                            if(dataSnapshot.child(postIde).hasChild(myUid)){
                                //already liked
                                int current= pLikes-1;
                                postRef.child(postIde).child("pLikes").setValue(""+(current));
                                likeRef.child(postIde).child(myUid).removeValue();
                                mProcressLike=false;
                                holder.pLikeTv.setText(current+" Likes");
                                postsList.get(i).setpLikes(String.valueOf(current));

                                //      Toast.makeText(context, ""+pLikes, Toast.LENGTH_SHORT).show();
                                // postsList.clear();

                            }
                            else{
                                //Not already liked
                                int current= pLikes+1;
                                postRef.child(postIde).child("pLikes").setValue(""+(current));
                                likeRef.child(postIde).child(myUid).setValue("Liked");
                                mProcressLike=false;
                                postsList.get(i).setpLikes(String.valueOf(current));
                                holder.pLikeTv.setText(current+" Likes");
                                holder.likeani.setVisibility(View.VISIBLE);
                                holder.likeani.playAnimation();
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        // yourMethod();
                                        holder.likeani.setVisibility(View.GONE);
                                    }
                                }, 1000);
                                //     Toast.makeText(context, ""+pLikes, Toast.LENGTH_SHORT).show();
                                // postsList.clear();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });
        //when user duoble click on image like image
        holder.pImageIv.setOnClickListener(new DoubleTapListener(new DoubleTapCallback() {
            @Override
            public void onDoubleClick(View v) {
                //add likes
                final int pLikes= Integer.parseInt(postsList.get(i).getpLikes());
                mProcressLike=true;
                //get id  of the post clicked
                final String postIde= postsList.get(i).getpId();
                likeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(mProcressLike){
                            if(dataSnapshot.child(postIde).hasChild(myUid)){
                                //already liked
                                int current= pLikes-1;
                                postRef.child(postIde).child("pLikes").setValue(""+(current));
                                likeRef.child(postIde).child(myUid).removeValue();
                                mProcressLike=false;
                                holder.pLikeTv.setText(current+" Likes");
                                postsList.get(i).setpLikes(String.valueOf(current));
                                //      Toast.makeText(context, ""+pLikes, Toast.LENGTH_SHORT).show();
                                // postsList.clear();

                            }
                            else{
                                //Not already liked
                                int current= pLikes+1;
                                postRef.child(postIde).child("pLikes").setValue(""+(current));
                                likeRef.child(postIde).child(myUid).setValue("Liked");
                                mProcressLike=false;
                                postsList.get(i).setpLikes(String.valueOf(current));
                                holder.pLikeTv.setText(current+" Likes");
                                holder.likeani.setVisibility(View.VISIBLE);
                                holder.likeani.playAnimation();
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    public void run() {
                                        // yourMethod();
                                        holder.likeani.setVisibility(View.GONE);
                                    }
                                }, 1000);
                                //     Toast.makeText(context, ""+pLikes, Toast.LENGTH_SHORT).show();
                                // postsList.clear();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }) {
            @Override
            public void onDoubleClick(View v) {

            }
        });
        //user duoble  click  on  video it will like
        // or unlike the video and update or decrase
        // valkue as it code for so don't read to much tis
        // so  simple
     /*   holder.pVideoVv.setOnClickListener(new DoubleTapListener(new DoubleTapCallback() {
            @Override
            public void onDoubleClick(View v) {
                //add likes
                final int pLikes= Integer.parseInt(postsList.get(i).getpLikes());
                mProcressLike=true;
                //get id  of the post clicked
                final String postIde= postsList.get(i).getpId();
                likeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(mProcressLike){
                            if(dataSnapshot.child(postIde).hasChild(myUid)){
                                //already liked
                                int current= pLikes-1;
                                postRef.child(postIde).child("pLikes").setValue(""+(current));
                                likeRef.child(postIde).child(myUid).removeValue();
                                mProcressLike=false;
                                holder.pLikeTv.setText(current+" Likes");
                                postsList.get(i).setpLikes(String.valueOf(current));
                                //      Toast.makeText(context, ""+pLikes, Toast.LENGTH_SHORT).show();
                                // postsList.clear();
                            }
                            else{
                                //Not already liked
                                int current= pLikes+1;
                                postRef.child(postIde).child("pLikes").setValue(""+(current));
                                likeRef.child(postIde).child(myUid).setValue("Liked");
                                mProcressLike=false;
                                postsList.get(i).setpLikes(String.valueOf(current));
                                holder.pLikeTv.setText(current+" Likes");
                                //     Toast.makeText(context, ""+pLikes, Toast.LENGTH_SHORT).show();
                                // postsList.clear();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }) {
            @Override
            public void onDoubleClick(View v) {

            }
        });
        i commented this code just bcoz this shit gving headeacah to my fucking head fuck e shit off its a big masive bugggggg
      */


        holder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //star post Detail  activyty
                Intent intent = new Intent(context, PostsDetail.class);
                intent.putExtra("postId", pid);
                context.startActivity(intent);

            }
        });
        holder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //will implement later
                //   Toast.makeText(context, "share", Toast.LENGTH_SHORT).show();

                BitmapDrawable bitmapDrawable=(BitmapDrawable)holder.pImageIv.getDrawable();
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
        holder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // go to that user profile with specified user
                Intent intent=new Intent(context, ThereProfileActivity.class);
                intent.putExtra("uid",uid);
                context.startActivity(intent);
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
        context.startActivity(Intent.createChooser(sIntent,"Share Via"));

    }

    private Uri saveImageToShare(Bitmap bitmap) {

        File imageFolder=new File(context.getCacheDir(),"images");
        Uri uri=null;
        try {
            imageFolder.mkdir();
            File file=new File(imageFolder,"shared_image.png");
            FileOutputStream stream=new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG,90,stream);
            stream.flush();
            stream.close();
            uri= FileProvider.getUriForFile(context,"com.example.socialapp.fileprovider",file);

        }catch (Exception e) {
            Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

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
        context.startActivity(Intent.createChooser(sIntent,"Share Via"));

    }

    private void setLikes(final MyHolder holder, final String postkey) {
        likeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(postkey).hasChild(myUid)){

                    //user has like this post
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked_black,0,0,0);
                    holder.likeBtn.setText("Liked");
                }
                else {
                    //user has not like this post
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_black,0,0,0);
                    holder.likeBtn.setText("Like");

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void showMoreOptions(ImageButton moreBtn, String uid, String myUid, final String pid, final String pImage, final String pvideo, final String typepost) {
        PopupMenu popupMenu=new PopupMenu(context,moreBtn, Gravity.END);


        if(uid.equals(myUid)){

            popupMenu.getMenu().add(Menu.NONE,0,0,"Delete");
            popupMenu.getMenu().add(Menu.NONE,1,0,"Edit");

        }

        popupMenu.getMenu().add(Menu.NONE,2,0,"View Details");

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                int id=item.getItemId();
                if(id == 0){

                    //clicked actiobns
                    beginDelete(pid,pImage,pvideo,typepost);


                }
                else if(id==1){
                    //start activity editing
                    Intent intent=new Intent(context, AddPostActivity.class);
                    intent.putExtra("key","editpost");
                    intent.putExtra("editpostid",pid);

                    context.startActivity(intent);

                }
                else if(id==2) {
                    //star post Detail  activyty
                    Intent intent =new Intent(context, PostsDetail.class);
                    intent.putExtra("postId",pid);
                    context.startActivity(intent);
                }
                return false;
            }
        });


        popupMenu.show();

    }

    private void beginDelete(String pid, String pImage, String pvideo, String typepost){

        if(pImage.equals("noImage")){
            deleteWithoutImage(pid);
        }
        else if (typepost.equals("video"))
        {
            //delete post with video
            deleteWithVideo(pid,pvideo);
        }
        else{
            deleteWithImage(pid,pImage);
        }


    }

    private void deleteWithVideo(final String pid, String pvideo) {
        final ProgressDialog pd=new ProgressDialog(context);
        pd.setMessage("Deleting..");

        StorageReference picRef= FirebaseStorage.getInstance().getReferenceFromUrl(pvideo);
        picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                //image
                Query fquery= FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pid);
                fquery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds:dataSnapshot.getChildren()){
                            ds.getRef().removeValue();



                        }
                        Toast.makeText(context, "Deleted Message", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void deleteWithImage(final String pid, String pImage) {

        final ProgressDialog pd=new ProgressDialog(context);
        pd.setMessage("Deleting..");

        StorageReference picRef= FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                //image
                Query fquery= FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pid);
                fquery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds:dataSnapshot.getChildren()){
                            ds.getRef().removeValue();



                        }
                        Toast.makeText(context, "Deleted Message", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });




    }

    private void deleteWithoutImage(String pid) {

        final ProgressDialog pd=new ProgressDialog(context);
        pd.setMessage("Deleting..");

        //image
        Query fquery= FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pid);
        fquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    ds.getRef().removeValue();



                }
                Toast.makeText(context, "Deleted Message", Toast.LENGTH_SHORT).show();
                pd.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return postsList.size();
    }


    class MyHolder extends RecyclerView.ViewHolder{

        //view  from row post
        ImageView uPictureIv;
        ImageView pImageIv;
        VideoView pVideoVv;
        TextView uNameIv,pTimeTv,pTitleTv,pDescriptionTv,pLikeTv,pComments;
        ImageButton moreBtn;
        Button likeBtn,commentBtn,shareBtn;
        LinearLayout profileLayout;
        CardView cardview;
        ProgressBar progress_bar;
        //like animation
        LottieAnimationView likeani;


        public MyHolder(@NonNull View itemView) {
            super(itemView);
            uPictureIv=itemView.findViewById(R.id.uPictureIv);
            likeani=itemView.findViewById(R.id.likeani);
            cardview=itemView.findViewById(R.id.cardview);
            likeani.setVisibility(View.GONE);
            progress_bar=itemView.findViewById(R.id.progress_bar);
            pImageIv=itemView.findViewById(R.id.pImageTv);
            pVideoVv=itemView.findViewById(R.id.pVideoVv);
            uNameIv=itemView.findViewById(R.id.uNameTv);
            pTimeTv=itemView.findViewById(R.id.pTimeTv);
            pDescriptionTv=itemView.findViewById(R.id.pDescriptionTv);
            pLikeTv=itemView.findViewById(R.id.pLikesTv);
            pComments=itemView.findViewById(R.id.pCommentTv);
            moreBtn=itemView.findViewById(R.id.moreBtn);
            likeBtn=itemView.findViewById(R.id.likeBtn);
            commentBtn=itemView.findViewById(R.id.commentBtn);
            shareBtn=itemView.findViewById(R.id.shareBtn);
            pTitleTv=itemView.findViewById(R.id.pTitleTv);
            profileLayout=itemView.findViewById(R.id.profileLayout);




        }


    }



}
