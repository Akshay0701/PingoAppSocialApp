package com.example.pingoapp;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.widget.PullRefreshLayout;
import com.bumptech.glide.Glide;
import com.example.pingoapp.adapters.AdapterPosts;
import com.example.pingoapp.models.ModelPosts;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import com.google.firebase.storage.UploadTask;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomButtons.SimpleCircleButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.storage.FirebaseStorage.getInstance;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {


    Menu menuu;
    //database components
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FirebaseUser user;
    FirebaseAuth firebaseAuth;

    FloatingActionButton floatingActionButton;
    //addd story
    FloatingActionButton fab_add_Story;


    //saved
    SharedPreferences.Editor editor;

    ProgressDialog pd;

    //firebase storage
    StorageReference storageReference;
    String storagePath="Users_Profile_Cover_Imgs";


    //recycle view
    RecyclerView postsRecycleView;


    //refresh button
    PullRefreshLayout refresh;


    //permission
    private static final int CAMERA_REQUEST_CODE=100;
    private static final int STORAGE_REQUEST_CODE=200;
    private static final int IMAGE_PICK_GALLERY_CODE=300;
    private static final int IMAGE_PICK_CAMERA_CODE=400;
    private static final int IMAGE_PICK_GALLERY_CODE_FOR_STORY=500;

    //SET OF PERMISSION
    String camerapermission[];
    String storagepermission[];


    List<ModelPosts> postsList;
    AdapterPosts adapterPosts;
    String uid;


    //uri of pick image
    Uri uri_image;

    //logout
    Button logoutBtn;

    //profile
    ImageView avatarIv,backimgaa,storyimg;
    TextView namet,emailt,phonet,photot,followert,followingt;



    String profileORcoverphoto;

    String bimg;
    String email;
    String image;
    String phone;
    String name;
    String noofphotos;
    String nooffollower;
    String nooffollowing;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_profile, container, false);
        firebaseAuth=FirebaseAuth.getInstance();
        user=firebaseAuth.getCurrentUser();
        firebaseDatabase=FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference("Users");
        storageReference = getInstance().getReference();
        //setting circuler menu
        final BoomMenuButton bmb=view.findViewById(R.id.bmb);
        setMenucirculer(bmb);


        editor= PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
        avatarIv=view.findViewById(R.id.avatarIV);
     //   namet=view.findViewById(R.id.nameTv);
        floatingActionButton=view.findViewById(R.id.fab_edit);
        fab_add_Story=view.findViewById(R.id.fab_add_Story);
      //  emailt=view.findViewById(R.id.emailTv);
     //   phonet=view.findViewById(R.id.phoneTv);
        photot=view.findViewById(R.id.no_photo);
        followert=view.findViewById(R.id.no_followers);
        followingt=view.findViewById(R.id.no_followings);
        backimgaa=view.findViewById(R.id.backimg);
        storyimg=view.findViewById(R.id.storyimg);
        logoutBtn=view.findViewById(R.id.logout);
        //refresh
        refresh=view.findViewById(R.id.refresh);


        //recycle view
        postsRecycleView=view.findViewById(R.id.recyclerview_posts);
        postsList=new ArrayList<>();

        //init progress dialog
        pd=new ProgressDialog(getActivity(),R.style.AlertDialog);


        //init permission array
        camerapermission=new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagepermission=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        //getting follower data
        getFollower();


        //data base
        Query query=databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){

                     bimg=""+ds.child("backimg").getValue();
                     email=""+ds.child("email").getValue();
                     image=""+ds.child("image").getValue();
                     phone=""+ds.child("phone").getValue();
                     name=""+ds.child("name").getValue();


               //     namet.setText(name);
                //    phonet.setText(phone);
             //       emailt.setText(email);

                    try{
                        Glide.with(getContext()).load(image).into(avatarIv);
                        //Picasso.get().load(image).into(avatarIv);
                        Picasso.get().load(bimg).into(backimgaa);
                        Glide.with(getContext()).load(bimg).into(backimgaa);

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
                Toast.makeText(getContext(), ""+name, Toast.LENGTH_SHORT).show();
            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(getContext(),EditProfile.class));
                // showeditDialog();
            }
        });
        fab_add_Story.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show  option
              //  showAddStoryDialog(); this button first use for adding story but now its  take use to upload
                startActivity(new Intent(getContext(),AddPostActivity.class));
            }
        });


        //loutout listner
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });

        new Handler().postDelayed(new Runnable() {
            public void run() {
                // do something...
            }
        }, 100);
        checkforuserlogin();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                loadMyPosts();
            }
        });


        refresh.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadMyPosts();

            }
        });

       // tutorialstart((Toolbar) menuu);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String isdashboradtutorial=prefs.getString("profiletutorial","");
        if(isdashboradtutorial.equals("")) {
            tutorialstart();
        }
    }

    private void setMenucirculer(BoomMenuButton bmb) {
        for (int i = 0; i < bmb.getButtonPlaceEnum().buttonNumber(); i++) {
            switch (i){
                case 0:
                    bmb.addBuilder(new SimpleCircleButton.Builder().listener(new OnBMClickListener() {
                        @Override
                        public void onBoomButtonClick(int index) {
                            startActivity(new Intent(getContext(),AddPostActivity.class));
                        }
                    }).normalColor(R.color.colorAccent).pieceColor(R.color.daynight_textColor2).normalImageRes(R.drawable.ic_addpost_green));
                    break;
                case 1:
                    bmb.addBuilder(new SimpleCircleButton.Builder().listener(new OnBMClickListener() {
                        @Override
                        public void onBoomButtonClick(int index) {
                            startActivity(new Intent(getContext(),EditProfile.class));
                        }
                    }).normalColor(R.color.colorAccent).pieceColor(R.color.daynight_textColor2).normalImageRes(R.drawable.ic_edit_green));
                    break;
                case 2:
                    bmb.addBuilder(new SimpleCircleButton.Builder().listener(new OnBMClickListener() {
                        @Override
                        public void onBoomButtonClick(int index) {
                            logout();
                        }
                    }).normalColor(R.color.colorAccent).pieceColor(R.color.daynight_textColor2).normalImageRes(R.drawable.ic_logout_green1));
                    break;

            }

        }
    }

    private void getFollower() {
        DatabaseReference follower= FirebaseDatabase.getInstance().getReference().child("Follow").
                child(firebaseAuth.getCurrentUser().getUid()).child("followers");
        follower.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followert.setText(""+dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //to get no of following
        DatabaseReference following= FirebaseDatabase.getInstance().getReference().child("Follow").
                child(firebaseAuth.getCurrentUser().getUid()).child("following");
        following.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingt.setText(""+dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //to get no of posts
        DatabaseReference nopost= FirebaseDatabase.getInstance().getReference().child("Posts");
        nopost.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i=0;
                for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                    ModelPosts posts=snapshot.getValue(ModelPosts.class);
                    if(posts.getUid().equals(user.getUid())){
                        i++;
                    }
                }
                photot.setText(""+i);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void logout() {
        editor.remove("username");
        editor.remove("password");
        editor.apply();
        firebaseAuth.signOut();

        startActivity(new Intent(getActivity(),loginActivity.class));
       getActivity().finish();
    }

    private void loadMyPosts() {
        //linear layout for recycle view
        LinearLayoutManager layoutManager =new LinearLayoutManager(getActivity());
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
                     adapterPosts=new AdapterPosts(getActivity(),postsList);
                     //list this adapter
                     postsRecycleView.setAdapter(adapterPosts);
                     refresh.setRefreshing(false);


                 }

             }

             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {
                 Toast.makeText(getActivity(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();

             }
         });





    }







  /*  //update info
    private void showeditDialog() {
        String options[]={"Edit Profile Picture","Edit Cover Image","Edit Name","Edit Phone"};
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());

        builder.setTitle("Choose Action");


        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which==0){
                    pd.setMessage("updating picture");
                    profileORcoverphoto="image";
                    showimagediaplog();
                }else if(which==1){
                    profileORcoverphoto="backimg";
                    showimagediaplog();
                    pd.setMessage("updating Background Image");
                }else if(which==2){
                    showNamePhone_Updated("name");
                    pd.setMessage("updating Name");
                }else if(which==3){
                    showNamePhone_Updated("phone");
                    pd.setMessage("updating Phone");
                }
            }
        });


        builder.create().show();
    }

    private void showNamePhone_Updated(final String key) {
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle("Update"+key);
        LinearLayout linearLayout=new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);

        final EditText editText =new EditText(getActivity());
        editText.setHint("enter "+key);
        linearLayout.addView(editText);
        builder.setView(linearLayout);
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


                final String value=editText.getText().toString().trim();

                if(!TextUtils.isEmpty(value)){
                    pd.show();
                    HashMap<String, Object> result=new HashMap<>();
                    result.put(key,value);


                    databaseReference.child(user.getUid()).updateChildren(result).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                       pd.dismiss();
                            Toast.makeText(getActivity(), "updated", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

                    //if user edit his name ,also change  it from hist post
                    if(key.equals("name")){
                        DatabaseReference ref =FirebaseDatabase.getInstance().getReference("Posts");
                        Query query=ref.orderByChild("uid").equalTo(uid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot ds:dataSnapshot.getChildren()){
                                    String child=ds.getKey();
                                    dataSnapshot.getRef().child(child).child("uName").setValue(value);
                                }


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        //update name  in current on posts
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot ds:dataSnapshot.getChildren()){
                                    String child=ds.getKey();
                                    if(dataSnapshot.child(child).hasChild("Comments")) {
                                    String child1 =""+dataSnapshot.child(child).getKey();
                                    Query child2=FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                    child2.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for(DataSnapshot ds:dataSnapshot.getChildren()){
                                                String child=ds.getKey();
                                                dataSnapshot.getRef().child(child).child("uName").setValue(value);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                    }

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }


                }
                else {
                    Toast.makeText(getActivity(), "please enter", Toast.LENGTH_SHORT).show();
                }
            }
        });


        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });
        builder.create().show();

    }

    private void showimagediaplog() {
        String options[]={"Camera","Gallery"};
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());

        builder.setTitle("Choose Action");


        builder.setItems(options, new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which==0){
                    if(!checkCameraPermission()){
                        requestCameraPermission();
                    }
                    else {
                        pickFromCamera();
                    }

                }else if(which==1) {
                    if (!checkStoragePermission()){
                        requestStoragePermission();
                    }
                    else {
                        pickFromGallery();
                    }
                }
            }
        });


        builder.create().show();

    }

   */

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean cameraAccepted= grantResults[0]== PackageManager.PERMISSION_GRANTED;
                    boolean wirtewstorageaccepted= grantResults[1]== PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted&&wirtewstorageaccepted){
                        pickFromCamera();
                    }
                    else {
                        Toast.makeText(getActivity(), "please allow permission", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
         case STORAGE_REQUEST_CODE:{
             if(grantResults.length>0){
                 boolean wirtewstorageaccepted= grantResults[0]== PackageManager.PERMISSION_GRANTED;
                 if(wirtewstorageaccepted){
                     pickFromGallery();
                 }
                 else {
                     Toast.makeText(getActivity(), "please allow permission", Toast.LENGTH_SHORT).show();
                 }
             }

         }
         break;

        }

    //    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode==RESULT_OK
                &&data!=null&&data.getData()!=null){
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {


                uri_image =  data.getData();
                uploadprofilecoverphoto(uri_image);
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {

                uri_image =  data.getData();
                uploadprofilecoverphoto(uri_image);
            }
            if (requestCode==IMAGE_PICK_GALLERY_CODE_FOR_STORY){
                uri_image =  data.getData();
               //upload story
                uploadStory(uri_image);
            }
        }


    }

    private void uploadStory(Uri uri_image) {
        pd.setMessage("publishing post...");

        pd.show();
        //time when story uploaded
        final String timestamp= String.valueOf(System.currentTimeMillis());
        String filePathName="Story/"+"story_"+timestamp;

        //upload story
        storyimg.setImageURI(uri_image);
        Bitmap bitmap=((BitmapDrawable)storyimg.getDrawable()).getBitmap();

        ByteArrayOutputStream bout=new ByteArrayOutputStream();
        //image compress
        bitmap.compress(Bitmap.CompressFormat.PNG,100,bout);
        byte[] data=bout.toByteArray();

        StorageReference ref= FirebaseStorage.getInstance().getReference().child(filePathName);
        ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                while(!uriTask.isSuccessful());

                String downloadUri=uriTask.getResult().toString();

                if(uriTask.isSuccessful()){
                    //uri is received upload post to firebase database
                    HashMap<Object, String> hashMap=new HashMap<>();
                    //put info
                    hashMap.put("uid",uid);
                    hashMap.put("uName",name);
                    hashMap.put("uEmail",email);
                    hashMap.put("uDp",image);
                    hashMap.put("sId",timestamp);
                    hashMap.put("Type","Story");
                    hashMap.put("sImage",downloadUri);
                    hashMap.put("pTime",timestamp);


                    DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Story");
                    ref.child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            pd.dismiss();
                            Toast.makeText(getContext(), "Story Uploaded", Toast.LENGTH_SHORT).show();




                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            pd.dismiss();

                        }
                    });

                }



            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                pd.dismiss();
            }
        });




    }

    private void uploadprofilecoverphoto(Uri uri_image) {
          pd.show();

          String filePathAndName = storagePath+"_"+profileORcoverphoto+"_"+user.getUid();

          StorageReference storageReference2nd =storageReference.child(filePathAndName);
          storageReference2nd.putFile(uri_image).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
              @RequiresApi(api = Build.VERSION_CODES.KITKAT)
              @Override
              public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                   // image uploading
                  Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();


                  while(!uriTask.isSuccessful());

                      if(uriTask.isSuccessful()){
                          final String downloadUri=uriTask.getResult().toString();

                       //   @SuppressWarnings("MismatchedQueryAndUpdateOfCollection") HashMap<String,Object>results=new HashMap<>();
                         // results.put(profileORcoverphoto, Objects.requireNonNull(downloadUri).toString());


                       //   Toast.makeText(getActivity(), "image"+downloadUri.toString(), Toast.LENGTH_SHORT).show();


                          databaseReference.child(user.getUid()).child(profileORcoverphoto).setValue(downloadUri)
                                  .addOnSuccessListener(new OnSuccessListener<Void>() {
                                      @Override
                                      public void onSuccess(Void aVoid) {
                                          pd.dismiss();
                                          Toast.makeText(getActivity(), "Image uploaded", Toast.LENGTH_SHORT).show();
                                      }
                                  }).addOnFailureListener(new OnFailureListener() {
                              @Override
                              public void onFailure(@NonNull Exception e) {
                                  pd.dismiss();
                                  Toast.makeText(getActivity(), "error occured", Toast.LENGTH_SHORT).show();
                              }
                          });


                          //if user edit his name ,also change  it from hist post
                          if(profileORcoverphoto.equals("image")){
                              DatabaseReference ref =FirebaseDatabase.getInstance().getReference("Posts");
                              Query query=ref.orderByChild("uid").equalTo(uid);
                              query.addValueEventListener(new ValueEventListener() {
                                  @Override
                                  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                      for(DataSnapshot ds:dataSnapshot.getChildren()){
                                          String child=ds.getKey();
                                          dataSnapshot.getRef().child(child).child("uDp").setValue(downloadUri);
                                      }


                                  }

                                  @Override
                                  public void onCancelled(@NonNull DatabaseError databaseError) {

                                  }
                              });
                              //update user Image in current userr comment on psots
                              ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                  @Override
                                  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                      for(DataSnapshot ds:dataSnapshot.getChildren()){
                                          String child=ds.getKey();
                                          if(dataSnapshot.child(child).hasChild("Comments")) {
                                              String child1 =""+dataSnapshot.child(child).getKey();
                                              Query child2=FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                              child2.addValueEventListener(new ValueEventListener() {
                                                  @Override
                                                  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                      for(DataSnapshot ds:dataSnapshot.getChildren()){
                                                          String child=ds.getKey();
                                                          dataSnapshot.getRef().child(child).child("uDp").setValue(downloadUri);
                                                      }
                                                  }

                                                  @Override
                                                  public void onCancelled(@NonNull DatabaseError databaseError) {

                                                  }
                                              });
                                          }

                                      }
                                  }

                                  @Override
                                  public void onCancelled(@NonNull DatabaseError databaseError) {

                                  }
                              });
                          }

                      }
                      else {
                         // pd.dismiss();
                        //  Toast.makeText(getActivity(), "some error occured", Toast.LENGTH_SHORT).show();
                      }
              }
          }).addOnFailureListener(new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                  pd.dismiss();
                  Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
              }
          });




    }

    private void pickFromGallery() {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Image"),IMAGE_PICK_GALLERY_CODE);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void pickFromCamera() {
        ContentValues values=new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Temp Description");
        //put image uri
        uri_image= Objects.requireNonNull(getActivity()).getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent cameraIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,uri_image);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_CODE);

    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void checkforuserlogin() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {

            uid=user.getUid();


        }
        else{
            startActivity(new Intent(getActivity(),RegisterActivity.class));

            try {
                Objects.requireNonNull(getActivity()).finish();
            }catch (NullPointerException ignored){

            }
        }
    }





    private void tutorialstart() {
        TapTargetView.showFor(getActivity(),              // `this` is an Activity
                TapTarget.forView(getActivity().findViewById(R.id.bmb), "Settings", "you can access setting option over here")
                        // All options below are optional
                        .dimColor(R.color.colorgray)
                        .titleTextDimen(R.dimen.default_bmb_ham_button_height)
                        .descriptionTextColor(R.color.colorgray)
                        .outerCircleColor(R.color.colorPrimary)
                        .targetCircleColor(android.R.color.black)
                        .transparentTarget(true)
                        .textColor(R.color.white)
                        .id(2),              // Specify the target radius (in dp)
                new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);      // This call is optional
                       // doSomething();
                        SharedPreferences.Editor editor;
                        editor= PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
                        editor.putString("profiletutorial", "1");//1 value will note as user as seen intro
                        editor.apply();
                        //  Toast.makeText(
                    }
                });

    }









}
