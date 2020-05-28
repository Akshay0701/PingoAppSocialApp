package com.example.pingoapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class AddPostActivity extends AppCompatActivity {


    private static final int REQUEST_TAKE_GALLERY_VIDEO = 1;
    FirebaseAuth firebaseAuth;
    ActionBar actionBar;



    DatabaseReference userDbRef;

    StorageReference videoStorageReference;

    //view
    EditText titleEt,descriptionEt;
    ImageView imageIv;
    VideoView pVideoVv;
    Button uploadBtn;

    //image picked will be saved in this
    Uri image_rui=null;
    Uri video_rui=null;

    //permission constants
    private static final int CAMERA_REQUEST_CODE =100;
    private static final int STORAGE_REQUEST_CODE =200;


    //permission constants
    private static final int IMAGE_PICK_CAMERA_CODE =300;
    private static final int IMAGE_PICK_GALLERY_CODE=400;

    //permission array
    String[] cameraPermessions;
    String[] storagePermessions;


    //user info
    String name,email,uid,dp;

    //edit post info
    String editTitle,editDescription,editImage;


    //progresses bar

    ProgressDialog pd;


    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        editImage="noImage";
        //init
        titleEt=findViewById(R.id.pTitleEt);
        descriptionEt=findViewById(R.id.pDescription);
        imageIv=findViewById(R.id.pImageIv);
        pVideoVv=findViewById(R.id.pVideoVv);
        uploadBtn=findViewById(R.id.pUploadBtn);
        pd= new ProgressDialog(this,R.style.AlertDialog);



        actionBar=getSupportActionBar();
    actionBar.setTitle("Add New Post");
    actionBar.setDisplayShowHomeEnabled(true);
    actionBar.setDisplayHomeAsUpEnabled(true);


        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorAccent)));
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);

    firebaseAuth=FirebaseAuth.getInstance();
    String uid1= firebaseAuth.getUid();
    //actionBar.setTitle(email);


        //get extra edit info from pervious activity
        Intent intent=getIntent();
        final String isUpdateKey=""+intent.getStringExtra("key");
        final String editPostid=""+intent.getStringExtra("editpostid");

        //validate if user want to update or add
        if(isUpdateKey.equals("editpost")){
            actionBar.setTitle("Update Post");
            uploadBtn.setText("Update");
            loadPostData(editPostid);
        }
        else {
            uploadBtn.setText("Upload");
            actionBar.setTitle("Add  New Post");
        }




    //user details
        userDbRef= FirebaseDatabase.getInstance().getReference("Users");
        Query query=userDbRef.orderByChild("uid").equalTo(uid1);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    name=""+ds.child("name").getValue();
                    email=""+ds.child("email").getValue();
                    dp=""+ds.child("image").getValue();
                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




        //init permissions
        cameraPermessions=new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermessions=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};



        //image
        imageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show image dialog
            //    Toast.makeText(AddPostActivity.this, "hey", Toast.LENGTH_SHORT).show();

                //showImageDialog();
                showDialogtoSelect();
            }
        });

        //upload
        //noinspection deprecation
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title=titleEt.getText().toString().trim();
                String description=descriptionEt.getText().toString().trim();

                if(TextUtils.isEmpty(title)){
                    Toast.makeText(AddPostActivity.this, "enter title", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(description)){
                    Toast.makeText(AddPostActivity.this, "enter Description", Toast.LENGTH_SHORT).show();
                    return;


                }
                if(isUpdateKey.equals("editpost")){
                    beginUpdate(title,description,editPostid);
                }
                else {
                    uploadData(title,description);
                }





            }
        });

    }

    //this function is just for updating post
    private void beginUpdate(String title, String description, String editPostid) {

        pd.setMessage("updating post");
        pd.show();
        if(!editImage.equals("noImage")){
            uploadwithimage(title,description,editPostid);
        }
        else if(imageIv.getDrawable()!=null){

            uploadwithnowimage(title,description,editPostid);
        }
        else{
            //update without image
        updatewithoutImage(title,description,editPostid);
        }




    }

    private void updatewithoutImage(String title, String description, String editPostid) {
        //uri is received upload post to firebase database
        Map<String, Object> hashMap=new HashMap<>();
        //put info
        hashMap.put("uid",uid);
        hashMap.put("uName",name);
        hashMap.put("uEmail",email);
        hashMap.put("uDp",dp);
        hashMap.put("pTitle",title);
        hashMap.put("pDescr",description);
        hashMap.put("pImage","noImage");


        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
        ref.child(editPostid).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                pd.dismiss();
                Toast.makeText(AddPostActivity.this, "post updated", Toast.LENGTH_SHORT).show();

                //reset view
                titleEt.setText("");
                descriptionEt.setText("");
                imageIv.setImageURI(null);
                image_rui=null;
                startActivity(new Intent(AddPostActivity.this,DashBoradActivity.class));

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                pd.dismiss();

            }
        });



    }

    private void uploadwithnowimage(final String title, final String description, String editPostid) {
        pd.setMessage("publishing post...");
        pd.show();
        final String timestamp= String.valueOf(System.currentTimeMillis());
        String filePathName="Posts/"+"post_"+timestamp;


        Bitmap bitmap=((BitmapDrawable)imageIv.getDrawable()).getBitmap();

        ByteArrayOutputStream bout=new ByteArrayOutputStream();
        //image compress
        bitmap.compress(Bitmap.CompressFormat.PNG,100,bout);
        byte[] data=bout.toByteArray();

        StorageReference ref=FirebaseStorage.getInstance().getReference().child(filePathName);


        ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                while(!uriTask.isSuccessful());

                String downloadUri=uriTask.getResult().toString();

                if(uriTask.isSuccessful()){
                    //uri is received upload post to firebase database
                    Map<String, Object> hashMap=new HashMap<>();
                    //put info
                    hashMap.put("uid",uid);
                    hashMap.put("uName",name);
                    hashMap.put("uEmail",email);
                    hashMap.put("uDp",dp);
                  //  hashMap.put("pId",timestamp);
                    hashMap.put("pTitle",title);
                    hashMap.put("pDescr",description);
                    hashMap.put("pImage",downloadUri);
                    hashMap.put("pComments","0");
                    hashMap.put("pLikes","0");
                   // hashMap.put("pTime",timestamp);


                    DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
                    ref.child(timestamp).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            pd.dismiss();
                            Toast.makeText(AddPostActivity.this, "post updated", Toast.LENGTH_SHORT).show();

                            //reset view
                            titleEt.setText("");
                            descriptionEt.setText("");
                            imageIv.setImageURI(null);
                            image_rui=null;

                            startActivity(new Intent(AddPostActivity.this,DashBoradActivity.class));

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            pd.dismiss();

                        }
                    });

                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    private void uploadwithimage(final String title, final String description, final String editPostid) {
        pd.setMessage("Uploading Image");
        pd.show();
        StorageReference mPictureRef=FirebaseStorage.getInstance().getReferenceFromUrl(editImage);
        mPictureRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        pd.setMessage("publishing post...");

                        pd.show();
                        final String timestamp= String.valueOf(System.currentTimeMillis());
                        String filePathName="Posts/"+"post_"+timestamp;

                        Bitmap bitmap=((BitmapDrawable)imageIv.getDrawable()).getBitmap();

                        ByteArrayOutputStream bout=new ByteArrayOutputStream();
                        //image compress
                        bitmap.compress(Bitmap.CompressFormat.PNG,100,bout);
                        byte[] data=bout.toByteArray();

                        StorageReference ref=FirebaseStorage.getInstance().getReference().child(filePathName);


                        ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                                while(!uriTask.isSuccessful());

                                String downloadUri=uriTask.getResult().toString();

                                if(uriTask.isSuccessful()){
                                    //uri is received upload post to firebase database


                                    Map<String, Object> hashMap=new HashMap<>();
                                    //put info
                                    hashMap.put("uid",uid);
                                    hashMap.put("uName",name);
                                    hashMap.put("uEmail",email);
                                    hashMap.put("uDp",dp);
                                 //   hashMap.put("pId",timestamp);
                                    hashMap.put("pTitle",title);
                                    hashMap.put("pDescr",description);
                                    hashMap.put("pImage",downloadUri);
                                    hashMap.put("pComments","0");
                                    hashMap.put("pLikes","0");
                                  //  hashMap.put("pTime",timestamp);


                                    DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
                                    ref.child(editPostid).
                                            updateChildren(hashMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            pd.dismiss();
                                            Toast.makeText(AddPostActivity.this, "post updated", Toast.LENGTH_SHORT).show();

                                            //reset view
                                            titleEt.setText("");
                                            descriptionEt.setText("");
                                            imageIv.setImageURI(null);
                                            image_rui=null;

                                            startActivity(new Intent(AddPostActivity.this,DashBoradActivity.class));
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                            Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            pd.dismiss();

                                        }
                                    });

                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });


    }


    private void loadPostData(String editPostid) {
    DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Posts");
    Query fquery=reference.orderByChild("pId").equalTo(editPostid);
    fquery.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            for(DataSnapshot ds:dataSnapshot.getChildren()){
                editTitle=""+ds.child("pTitle").getValue();
                editDescription=""+ds.child("pDescr").getValue();
                editImage=""+ds.child("pImage").getValue();

                //settting data to actitvbty
                titleEt.setText(editTitle);
                descriptionEt.setText(editDescription);
                try {

                    Picasso.get().load(editImage).into(imageIv);

                }catch (Exception e){

                }




            }

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    });





    }

    //uploading actual post
    private void uploadData(final String title, final String description) {


        pd.setMessage("publishing post...");

        pd.show();


        final String timestamp= String.valueOf(System.currentTimeMillis());
        String filePathName="Posts/"+"post_"+timestamp;
        String filePathVideoName="Posts_Video/"+"post_"+timestamp;



        if(imageIv.getVisibility()== View.VISIBLE&&imageIv.getDrawable()!=null){
            Bitmap bitmap=((BitmapDrawable)imageIv.getDrawable()).getBitmap();

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
                        hashMap.put("uDp",dp);
                        hashMap.put("pId",timestamp);
                        hashMap.put("pTitle",title);
                        hashMap.put("pDescr",description);
                        hashMap.put("pVideo","");
                        hashMap.put("typepost","image");
                        hashMap.put("pImage",downloadUri);
                        hashMap.put("pTime",timestamp);
                        hashMap.put("pComments","0");
                        hashMap.put("pLikes","0");


                        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
                        ref.child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                pd.dismiss();
                                Toast.makeText(AddPostActivity.this, "post published", Toast.LENGTH_SHORT).show();

                                //reset view
                                titleEt.setText("");
                                descriptionEt.setText("");
                                imageIv.setImageURI(null);
                                image_rui=null;

                                startActivity(new Intent(AddPostActivity.this,DashBoradActivity.class));

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                pd.dismiss();

                            }
                        });

                    }



                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    pd.dismiss();
                }
            });


        }

        else if (pVideoVv.getVisibility()== View.VISIBLE){
            //upload post  with video
            StorageReference ref= FirebaseStorage.getInstance().getReference().child(filePathVideoName);
            Uri file = Uri.fromFile(new File(String.valueOf(video_rui)));

            ref.putFile(video_rui)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content

                            Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                            while(!uriTask.isSuccessful());

                            String downloadVidoeUri=uriTask.getResult().toString();

                            if(uriTask.isSuccessful()) {
                            //on sussces
                                //uri is received upload post to firebase database
                                HashMap<Object, String> hashMap=new HashMap<>();
                                //put info
                                hashMap.put("uid",uid);
                                hashMap.put("uName",name);
                                hashMap.put("uEmail",email);
                                hashMap.put("uDp",dp);
                                hashMap.put("pId",timestamp);
                                hashMap.put("pTitle",title);
                                hashMap.put("pDescr",description);
                                hashMap.put("pVideo",downloadVidoeUri);
                                hashMap.put("typepost","video");
                                hashMap.put("pImage","noImage");
                                hashMap.put("pTime",timestamp);
                                hashMap.put("pComments","0");
                                hashMap.put("pLikes","0");


                                DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
                                ref.child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        pd.dismiss();
                                        Toast.makeText(AddPostActivity.this, "post published", Toast.LENGTH_SHORT).show();

                                        //reset view
                                        titleEt.setText("");
                                        descriptionEt.setText("");
                                        pVideoVv.setVideoURI(null);
                                        pVideoVv=null;

                                        startActivity(new Intent(AddPostActivity.this,DashBoradActivity.class));

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        pd.dismiss();

                                    }
                                });

                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            // ...
                        }
                    });





        }
        else {
            //post  without image
            //uri is received upload post to firebase database
            HashMap<Object, String> hashMap=new HashMap<>();
            //put info
            hashMap.put("uid",uid);
            hashMap.put("uName",name);
            hashMap.put("uEmail",email);
            hashMap.put("uDp",dp);
            hashMap.put("pId",timestamp);
            hashMap.put("pTitle",title);
            hashMap.put("pDescr",description);
            hashMap.put("pVideo","noVideo");
            hashMap.put("typepost","video");
            hashMap.put("pImage","noImage");
            hashMap.put("pTime",timestamp);
            hashMap.put("pComments","0");
            hashMap.put("pLikes","0");


            DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
            ref.child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    pd.dismiss();
                    Toast.makeText(AddPostActivity.this, "post published", Toast.LENGTH_SHORT).show();

                   //reset view
                    titleEt.setText("");
                    descriptionEt.setText("");
                    imageIv.setImageURI(null);
                    image_rui=null;

                    startActivity(new Intent(AddPostActivity.this,DashBoradActivity.class));




                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();

                }
            });




        }








    }


    private void showImageDialog() {


        String[] options={"Camera","Gallery"};

        //dialog box
        AlertDialog.Builder builder=new AlertDialog.Builder(AddPostActivity.this);

        builder.setTitle("Choose Action");



        Toast.makeText(this, " reached", Toast.LENGTH_SHORT).show();
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(which==0){
                    //camera clicked

                    if(!checkCameraPermission()){
                        requestCameraPermission();
                    }
                    else {
                        pickFromCamera();
                    }


                }
                if(which==1){
                    //camera clicked

                    if(!checkStoragePermission()){
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


    private void showDialogtoSelect() {


        String[] options={"Upload Image","Upload Video"};

        //dialog box
        AlertDialog.Builder builder=new AlertDialog.Builder(AddPostActivity.this);

        builder.setTitle("Choose Action");



        Toast.makeText(this, " reached", Toast.LENGTH_SHORT).show();
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(which==0){
                    //upload image

                    pVideoVv.setVisibility(View.GONE);
                    imageIv.setVisibility(View.VISIBLE);
                    showImageDialog();


                }
                if(which==1){
                    //get Video to upload from intent
                    pVideoVv.setVisibility(View.VISIBLE);
                    imageIv.setVisibility(View.GONE);
                    Intent intent = new Intent();
                    intent.setType("video/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent,"Select Video"),REQUEST_TAKE_GALLERY_VIDEO);



                }


            }
        });
        builder.create().show();


    }

    private void pickFromCamera() {

        ContentValues cv=new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE,"Temp Pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION,"Temp Descr");
        image_rui=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,cv);


        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,image_rui);
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);
    }

    private void pickFromGallery() {

        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result;
    }


    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermessions,STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);

        return result&&result1;
    }


    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermessions,CAMERA_REQUEST_CODE);
    }


    @Override
    protected void onStart() {


        super.onStart();
        //showDialogtoSelect();
        checkforuserlogin();

    }

    @Override
    protected void onResume() {


        super.onResume();

        checkforuserlogin();

    }

    public void checkforuserlogin() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            email=user.getEmail();
            uid=user.getUid();


        }
        else{
            startActivity(new Intent(this,MainActivity.class));
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


        menu.findItem(R.id.action_add_post).setVisible(false);

        menu.findItem(R.id.action_search).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.action_logout){
            firebaseAuth.signOut();
            checkforuserlogin();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case CAMERA_REQUEST_CODE:{

                if(grantResults.length>0){
                    boolean cameraAccepted=grantResults[0]== PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted=grantResults[1]== PackageManager.PERMISSION_GRANTED;

                    if(cameraAccepted&&storageAccepted){

                        pickFromCamera();
                    }
                    else {
                        Toast.makeText(this, "camera  & gallery both permission needed", Toast.LENGTH_SHORT).show();

                    }
                }
                else{

                }




            }
            break;
            case STORAGE_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean storageAccepted=grantResults[1]== PackageManager.PERMISSION_GRANTED;

                    if(storageAccepted){

                        pickFromGallery();
                    }
                    else {
                        Toast.makeText(this, "gallery both permission needed", Toast.LENGTH_SHORT).show();

                    }
                }
                else{

                }

            }
            break;
        }



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {


        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==IMAGE_PICK_GALLERY_CODE){
                image_rui=data.getData();

                imageIv.setImageURI(image_rui);
            }
            else if(requestCode==IMAGE_PICK_CAMERA_CODE){

                imageIv.setImageURI(image_rui);

            }
            else if (requestCode==REQUEST_TAKE_GALLERY_VIDEO){
                video_rui=data.getData();
                pVideoVv.setVideoURI(video_rui);
                pVideoVv.start();

            }
        }



    }
    @Override
    public void onBackPressed() {

        super.onBackPressed();
        finish();
    }

}
