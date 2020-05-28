package com.example.pingoapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.pingoapp.adapters.AdapterChat;
import com.example.pingoapp.models.ModelChat;
import com.example.pingoapp.models.ModelChatList;
import com.example.pingoapp.models.ModelUser;
import com.example.pingoapp.notifications.APIService;
import com.example.pingoapp.notifications.Client;
import com.example.pingoapp.notifications.Data;
import com.example.pingoapp.notifications.Response;
import com.example.pingoapp.notifications.Sender;
import com.example.pingoapp.notifications.Token;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;

public class ChatActivity extends AppCompatActivity {



    FirebaseAuth firebaseAuth;

    SharedPreferences.Editor editor;

    String HisID;
    String MyID;
    String Hisimage;

    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profileIv,attachBtn;
    TextView nameTv,userStatusTv;
    EditText messaheEt;
    ImageButton sendBtn;


    FirebaseDatabase database;
    DatabaseReference databaseReference;

    ValueEventListener seenListner;
    DatabaseReference userRefForSeen;

    List<ModelChat> chatList;
    AdapterChat adapterChat;



    APIService  apiService;
    boolean notify=false;


    //permission constants
    private static final int CAMERA_REQUEST_CODE =100;
    private static final int STORAGE_REQUEST_CODE =200;


    //permission constants
    private static final int IMAGE_PICK_CAMERA_CODE =300;
    private static final int IMAGE_PICK_GALLERY_CODE=400;

    //permission array
    String[] cameraPermessions;
    String[] storagePermessions;
    //image picked will be saved in this
    Uri image_rui=null;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //init permissions
        cameraPermessions=new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermessions=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        editor= PreferenceManager.getDefaultSharedPreferences(this).edit();
        //init
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        recyclerView=findViewById(R.id.chat_regular);
        profileIv=findViewById(R.id.profileIv);
        nameTv=findViewById(R.id.namettv);
        userStatusTv=findViewById(R.id.onlinestatus);
        messaheEt=findViewById(R.id.message_Et);
        sendBtn=findViewById(R.id.send_btn);
        attachBtn=(ImageView)findViewById(R.id.attachBtn);

        firebaseAuth=FirebaseAuth.getInstance();

        Intent intent=getIntent();
        HisID=intent.getStringExtra("hisUID");



        //layout
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        //create api service
        apiService= Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);





        userRefForSeen=FirebaseDatabase.getInstance().getReference("Chats");
        database=FirebaseDatabase.getInstance();
        databaseReference=database.getReference("Users");

        //starting loading screen in backgorund from database getting shit data
        ChatActivity.AsyncTaskRunner runner = new ChatActivity.AsyncTaskRunner();
        String sleepTime = "asd";
        runner.execute(sleepTime);


        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                notify=true;
                String message=messaheEt.getText().toString().trim();

                if(TextUtils.isEmpty(message)){
                    Toast.makeText(ChatActivity.this, "please write message", Toast.LENGTH_SHORT).show();
                }else{
                    sendmessage(message);
                }


                messaheEt.setText("");

            }
        });

        //button  handle to send image
        attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show image dialog

                showImageDialog();
            }
        });


        messaheEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().length()==0){
                    checkTypingStatus("noOne");
                }else{
                    checkTypingStatus(HisID);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });




    }


    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        private String resp="asd";
        ProgressDialog progressDialog;

        @Override
        protected String doInBackground(String... params) {

            Query usersQuery=databaseReference.orderByChild("uid").equalTo(HisID);

            usersQuery.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot ds:dataSnapshot.getChildren()){
                        String name=""+ds.child("name").getValue();
                        Hisimage=""+ds.child("image").getValue();

                        String typingStatus=""+ds.child("typingTo").getValue();

                        if(typingStatus.equals(MyID)){
                            userStatusTv.setText("Typing..");
                        }
                        else{

                            String onlineStatus=""+ds.child("onlineStatus").getValue();
                            if(onlineStatus.equals("online")){
                                userStatusTv.setText(onlineStatus);
                            }else
                            {
                                Calendar cal = Calendar.getInstance(Locale.ENGLISH);

                                cal.setTimeInMillis(Long.parseLong(onlineStatus));
                                String datetime = DateFormat.format("dd//MM/yyyy hh:mm:aa", cal).toString();
                                userStatusTv.setText("Last Seen at "+datetime);

                            }
                        }






                        nameTv.setText(name);

                        try {
                            Glide.with(ChatActivity.this).load(Hisimage).placeholder(R.drawable.ic_chatpepl_dark).into(profileIv);
                            Picasso.get().load(Hisimage).placeholder(R.drawable.ic_chatpepl_dark).into(profileIv);

                        }catch (Exception e){
                            Picasso.get().load(R.drawable.ic_chatpepl_dark).into(profileIv);

                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            readMessage();
            seenmessage();




            return resp;
        }


        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
            Log.e("process done ","yeah  hooo");
        }


        @Override
        protected void onPreExecute() {

        }


        @Override
        protected void onProgressUpdate(String... text) {


        }
    }


    private void showImageDialog() {


        String[] options={"Camera","Gallery"};

        //dialog box
        AlertDialog.Builder builder=new AlertDialog.Builder(ChatActivity.this);

        builder.setTitle("Choose Action");



      //  Toast.makeText(this, " reached", Toast.LENGTH_SHORT).show();
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

    private void seenmessage() {
    seenListner=userRefForSeen.addValueEventListener(new ValueEventListener() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            for (DataSnapshot ds:dataSnapshot.getChildren()){
                ModelChat chat=ds.getValue(ModelChat.class);
             if(Objects.requireNonNull(chat).getReciver().equals(MyID)&&chat.getSender().equals(HisID)){
                    HashMap<String, Object> hasSeenHashMap=new HashMap<>();
                    hasSeenHashMap.put("isSeen",true);
                    ds.getRef().updateChildren(hasSeenHashMap);
               }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    });

    }

    @Override
    protected void onPause() {

        String timestamp= String.valueOf(System.currentTimeMillis());

        checkOnlineStatus(timestamp);
        checkTypingStatus("noOne");

        userRefForSeen.removeEventListener(seenListner);


        super.onPause();
    }

    private void readMessage() {
    chatList=new ArrayList<>();
    DatabaseReference dbRef=FirebaseDatabase.getInstance().getReference("Chats");
    dbRef.addValueEventListener(new ValueEventListener() {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            chatList.clear();
            for(DataSnapshot ds: dataSnapshot.getChildren()){
                Toast.makeText(ChatActivity.this, ds.child("isSeen").getValue().toString(), Toast.LENGTH_SHORT).show();

                ModelChat chat= ds.getValue(ModelChat.class);
               if (chat != null && (chat.getReciver().equals(MyID) && chat.getSender().equals(HisID) ||
                       chat.getReciver().equals(HisID) && chat.getSender().equals(MyID))) {
                   chat.setSeen(ds.child("isSeen").getValue(Boolean.class));
                    chatList.add(chat);
                //   Toast.makeText(ChatActivity.this, ""+chat.getType(), Toast.LENGTH_SHORT).show();
            //    Toast.makeText(ChatActivity.this, ""+ ds.getValue(ModelChat.class)+Objects.requireNonNull(chat).getReciver(), Toast.LENGTH_SHORT).show();

               }


                adapterChat=new AdapterChat(ChatActivity.this,chatList,Hisimage);
                adapterChat.notifyDataSetChanged();
                recyclerView.setAdapter(adapterChat);


            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    });


    }

    private void sendmessage(final String message) {


        String timestamp = String.valueOf(System.currentTimeMillis());
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap=new HashMap<>();
        hashMap.put("sender",MyID);
        hashMap.put("reciver",HisID);
        hashMap.put("message",message);
        hashMap.put("timestamp",timestamp);
        hashMap.put("type","text");
        hashMap.put("isSeen",false);

        databaseReference.child("Chats").push().setValue(hashMap);


        final DatabaseReference database=FirebaseDatabase.getInstance().getReference("Users").child(MyID);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ModelUser user=dataSnapshot.getValue(ModelUser.class);
                if(notify){
                    if (user != null) {
                        senNotification(HisID,user.getName(),message);
                    }
                }
                notify=false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //create chat list in firebase
        final DatabaseReference chatRef1=FirebaseDatabase.getInstance().getReference("Chatlist").child(MyID).child(HisID);
        chatRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    chatRef1.child("id").setValue(HisID);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference chatRef2=FirebaseDatabase.getInstance().getReference("Chatlist").child(HisID).child(MyID);
        chatRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    chatRef2.child("id").setValue(MyID);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




    }

    private void sendImageMessage(final Uri image_rui)throws IOException {
        notify=true;
        //progress dialog
        final ProgressDialog pd=new ProgressDialog(this);
        pd.setMessage("sending image..");
        pd.show();

        final String timestamp=""+ System.currentTimeMillis();

        String fileNameAndPath="ChatImages/"+"post_"+timestamp;

        //get bitmap of image
        Bitmap bitmap= MediaStore.Images.Media.getBitmap(this.getContentResolver(),image_rui);
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
        final byte[] data=baos.toByteArray();
        StorageReference reference= FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
        reference.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //success
                pd.dismiss();

                Task<Uri>uriTask=taskSnapshot.getStorage().getDownloadUrl();
                while(!uriTask.isSuccessful());
                String downloadUri=uriTask.getResult().toString();

                if(uriTask.isSuccessful()){
                    //add image
                    DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference();

                    //setup requried data
                    HashMap<String, Object> hashMap=new HashMap<>();
                    hashMap.put("sender",MyID);
                    hashMap.put("reciver",HisID);
                    hashMap.put("message",downloadUri);
                    hashMap.put("timestamp",timestamp);
                    hashMap.put("type","image");
                    hashMap.put("isSeen",false);
                    //put it on firebase
                    databaseReference.child("Chats").push().setValue(hashMap);
                    //send notifications
                    DatabaseReference database=FirebaseDatabase.getInstance().getReference("Users");
                    database.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            ModelUser user=dataSnapshot.getValue(ModelUser.class);
                            if(notify){
                                senNotification(HisID,user.getName(),"Send you Image");
                                Log.e("notification","start");
                                notify=false;
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    //create chat list in firebase
                    final DatabaseReference chatRef1=FirebaseDatabase.getInstance().getReference("Chatlist").child(MyID).child(HisID);
                    chatRef1.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(!dataSnapshot.exists()){
                                chatRef1.child("id").setValue(HisID);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    final DatabaseReference chatRef2=FirebaseDatabase.getInstance().getReference("Chatlist").child(HisID).child(MyID);
                    chatRef2.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(!dataSnapshot.exists()){
                                chatRef2.child("id").setValue(MyID);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

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

    private void senNotification(final String hisID, final String name, final String message) {
        DatabaseReference allTokens=FirebaseDatabase.getInstance().getReference("Tokens");
        Query query=allTokens.orderByKey().equalTo(hisID);
        query.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot  ds:dataSnapshot.getChildren()){
                    Token  token=ds.getValue(Token.class);
                    Data data=new Data(MyID,name+":"+message,"New Message",hisID,"chat",R.drawable.ic_chatpepl_dark);

                    Sender sender=new Sender(data, Objects.requireNonNull(token).getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<Response>() {
                                @SuppressWarnings("NullableProblems")
                                @Override
                                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                                 Toast.makeText(ChatActivity.this, ""+response.message(), Toast.LENGTH_SHORT).show();
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


    private void checkOnlineStatus(String status){
        DatabaseReference dbRef=FirebaseDatabase.getInstance().getReference("Users").child(MyID);
        HashMap<String, Object> hashMap=new HashMap<>();
        hashMap.put("onlineStatus",status);

        dbRef.updateChildren(hashMap);
    }

    private void checkTypingStatus(String Typing){
        DatabaseReference dbRef=FirebaseDatabase.getInstance().getReference("Users").child(MyID);
        HashMap<String, Object> hashMap=new HashMap<>();
        hashMap.put("typingTo",Typing);

        dbRef.updateChildren(hashMap);
    }

    @Override
    protected void onStart() {



        checkforuserlogin();
        checkOnlineStatus("online");
        super.onStart();
    }



    @Override
    protected void onResume() {

        checkOnlineStatus("online");
        super.onResume();
    }

    public void checkforuserlogin() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {

            MyID=user.getUid();

        }
        else{
            startActivity(new Intent(this,RegisterActivity.class));
            finish();
        }
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
            firebaseAuth.signOut();
            checkforuserlogin();
        }


        return true;
    }

    private void setSupportActionBar(Toolbar toolbar) {
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
                //USE this to upload in firebase
                try {
                    sendImageMessage(image_rui);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            else if(requestCode==IMAGE_PICK_CAMERA_CODE){
                //USE this to upload in firebase
                try {
                    sendImageMessage(image_rui);
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }




































    }



}
