package com.example.pingoapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

public class AddStoryActivity extends AppCompatActivity {


    private Uri imageUri;
    String myUri="";
    private StorageTask storageTask;
    StorageReference storageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_story);

        storageReference= FirebaseStorage.getInstance().getReference("Story");

       CropImage.activity().setAspectRatio(9,16).start(AddStoryActivity.this);

    }

    private String getFileExtesion(Uri uri){
        ContentResolver contentResolver=getContentResolver();
        MimeTypeMap mimeTypeMap= MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void publishStory(){
        final ProgressDialog pd=new ProgressDialog(this);
        pd.setMessage("Story Uploading");
        pd.show();
        String filePathName="Story/"+"story_"+System.currentTimeMillis();

        if (imageUri!=null){
            final StorageReference imageRefrence=storageReference.child(filePathName+""+getFileExtesion(imageUri));
            storageTask =imageRefrence.putFile(imageUri);
            storageTask.continueWithTask(new Continuation() {
                @Override
                public Task<Uri> then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()){
                        throw  task.getException();
                    }
                    return imageRefrence.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUri=task.getResult();
                        myUri=downloadUri.toString();

                        String myuid=FirebaseAuth.getInstance().getCurrentUser().getUid();

                        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Story").child(myuid);

                        String stroyid=ref.push().getKey();
                        long timeend=System.currentTimeMillis()+86400000;//1 day
                        HashMap<String,Object> hashMap=new HashMap<>();
                        hashMap.put("uid",myuid);
                        hashMap.put("sId",stroyid);
                        hashMap.put("sImage",myUri);
                        hashMap.put("sTimeStart", ServerValue.TIMESTAMP);
                        hashMap.put("sTimeEnd",timeend);

                        ref.child(stroyid).setValue(hashMap);
                        pd.dismiss();
                        finish();
                    }
                    else {
                        Toast.makeText(AddStoryActivity.this, "failed", Toast.LENGTH_SHORT).show();
                    }


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddStoryActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else {
            Toast.makeText(this, "no image sleected", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE&&resultCode==RESULT_OK){
            CropImage.ActivityResult result=CropImage.getActivityResult(data);
            imageUri=result.getUri();
            publishStory();
        }
        else {
            Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AddStoryActivity.this,DashBoradActivity.class));
            finish();
        }

    }
}
