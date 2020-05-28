package com.example.pingoapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pingoapp.adapters.AdapterPosts;
import com.example.pingoapp.models.ModelPosts;
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
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static com.google.firebase.storage.FirebaseStorage.getInstance;

public class EditProfile extends AppCompatActivity {


    //database components
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FirebaseUser user;
    FirebaseAuth firebaseAuth;

    Button fab_edit;
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
    SwipeRefreshLayout refresh;


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


    //profile
    ImageView avatarIv,backimgaa,storyimg;
    EditText namet,emailt,phonet;


    String profileORcoverphoto;

    String bimg;
    String email;
    String image;
    String phone;
    String name;

    Button updateBtn;
    Switch switchCompat;
    //for setting dark mode
    Switch dark_light_mode;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        switchCompat = findViewById(R.id.switchCompat);
        //color switch
        if (InitApplication.getInstance().isNightModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean true1=false;
        if (prefs.getBoolean("NIGHT_MODE",true1)){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            switchCompat.setChecked(true);
        }
        else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
            switchCompat.setChecked(true);

        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    InitApplication.getInstance().setIsNightModeEnabled(true,getApplicationContext());
                    Intent intent = getIntent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    finish();
                    startActivity(intent);

                } else {
                    InitApplication.getInstance().setIsNightModeEnabled(false,getApplicationContext());
                    Intent intent = getIntent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    finish();
                    startActivity(intent);
                }


            }
        });


        //from here actual code start


        firebaseAuth=FirebaseAuth.getInstance();
        user=firebaseAuth.getCurrentUser();
        firebaseDatabase=FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference("Users");
        storageReference = getInstance().getReference();
          checkforuserlogin();
        //init progress dialog
        pd=new ProgressDialog(EditProfile.this,R.style.AlertDialog);

        editor= PreferenceManager.getDefaultSharedPreferences(this).edit();
        avatarIv=findViewById(R.id.profileIv);
        namet=findViewById(R.id.nameEt);
        fab_edit=findViewById(R.id.fab_edit);
    updateBtn=findViewById(R.id.updateBtn);
        emailt=findViewById(R.id.emailEt);
        phonet=findViewById(R.id.phonesEt);
        backimgaa=findViewById(R.id.backimg);
//        dark_light_mode=findViewById(R.id.siwtchh);




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


                    namet.setText(name);
                    phonet.setText(phone);
                    emailt.setText(email);

                    try{
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

        fab_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showeditDialog();
            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //update values with edit text

                String name,email,phone;
            name=namet.getText().toString();
            email=emailt.getText().toString();
            phone=phonet.getText().toString();

                updateProfileinfo(name,email,phone);

            }
        });

    }

    private void restratapp() {
        Intent intent=new Intent(this,EditProfile.class);
        startActivity(intent);
        finish();

    }

    private void updateProfileinfo(String name, String email, String phone) {

        HashMap<String, Object> result=new HashMap<>();
        result.put("name",name);
        result.put("email",email);
        result.put("phone",phone);

        databaseReference.child(user.getUid()).updateChildren(result).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
              //  pd.dismiss();
                startActivity(new Intent(EditProfile.this,DashBoradActivity.class));
                Toast.makeText(EditProfile.this, "updated", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
              //  pd.dismiss();
                Toast.makeText(EditProfile.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }


    private boolean checkStoragePermission(){
        boolean result= ContextCompat.checkSelfPermission(EditProfile.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);

        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestStoragePermission(){
        requestPermissions(storagepermission,STORAGE_REQUEST_CODE);
    }


    private boolean checkCameraPermission(){
        boolean result= ContextCompat.checkSelfPermission(EditProfile.this, Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);

        boolean result1= ContextCompat.checkSelfPermission(EditProfile.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);

        return result&&result1;
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestCameraPermission(){
        requestPermissions(camerapermission,CAMERA_REQUEST_CODE);

    }



    //update info
    private void showeditDialog() {
        String options[]={"Edit Profile Picture","Edit Cover Image"};
        AlertDialog.Builder builder=new AlertDialog.Builder(EditProfile.this);

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
           }
            }
        });


        builder.create().show();
    }


    private void showimagediaplog() {
        String options[]={"Camera","Gallery"};
        AlertDialog.Builder builder=new AlertDialog.Builder(EditProfile.this);

        builder.setTitle("Choose Action");


        builder.setItems(options, new DialogInterface.OnClickListener() {
            @SuppressLint("NewApi")
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
                        Toast.makeText(EditProfile.this, "please allow permission", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(EditProfile.this, "please allow permission", Toast.LENGTH_SHORT).show();
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
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {


                uri_image = data.getData();
                uploadprofilecoverphoto(uri_image);
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {

                uri_image = data.getData();
                uploadprofilecoverphoto(uri_image);
            }
        }
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
                                    Toast.makeText(EditProfile.this, "Image uploaded", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(EditProfile.this, "error occured", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(EditProfile.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
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
        uri_image= Objects.requireNonNull(this).getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

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
            startActivity(new Intent(this,RegisterActivity.class));

            try {
                Objects.requireNonNull(this).finish();
            }catch (NullPointerException ignored){

            }
        }
    }












}

