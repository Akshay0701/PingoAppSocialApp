package com.example.pingoapp.adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Build;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.pingoapp.AddStoryActivity;
import com.example.pingoapp.R;
import com.example.pingoapp.ShowStory;
import com.example.pingoapp.models.ModelStory;
import com.example.pingoapp.models.ModelUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.sql.Timestamp;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterStory extends RecyclerView.Adapter<AdapterStory.MyHolder> {




    Context context;
    List<ModelStory> storyList;

    String myUid;



    public AdapterStory(Context context, List<ModelStory> storyList) {
        this.context = context;
        this.storyList = storyList;
        myUid= FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType==0){
            View view= LayoutInflater.from(context).inflate(R.layout.row_addstory,parent,false);
            return new AdapterStory.MyHolder(view);
        }
        else {
            View view= LayoutInflater.from(context).inflate(R.layout.row_story,parent,false);
            return new AdapterStory.MyHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int i) {
        final ModelStory story=storyList.get(i);
        userinfo(holder,story.getUid(),i);

        if (holder.getAdapterPosition()!=0){
            seenStory(holder,story.getUid());
        }
        if (holder.getAdapterPosition()==0){
            myStory(holder.addstroy_text,holder.stroy_plus,false);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                if (holder.getAdapterPosition()==0){
                    myStory(holder.addstroy_text,holder.stroy_plus,true);
                    Toast.makeText(context, "clicked", Toast.LENGTH_SHORT).show();
                }
                else {
                    //TODO:go to story
                    Intent intent=new Intent(context,ShowStory.class);
                    intent.putExtra("userid",story.getUid());
                    //this code is for transtion between activity
                    Pair[] pairs=new Pair[1];
                    pairs[0]=new Pair<View,String>(holder.itemView.findViewById(R.id.linear),"avatarIv");
                    ActivityOptions options=ActivityOptions.makeSceneTransitionAnimation((Activity) context,pairs);
                   // holder.storydpimg.setVisibility(View.GONE);
                   // holder.storyseen.setVisibility(View.VISIBLE);
                    context.startActivity(intent,options.toBundle());
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return storyList.size();
    }


    @Override
    public int getItemViewType(int position) {
        if (position ==0){
            return 0;
        }
        return 1;

    }

    class MyHolder extends RecyclerView.ViewHolder{

        //view  from row post
        ImageView storydpimg,stroy_plus,storyseen;
        TextView addstroy_text,stroyusername;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            storydpimg=itemView.findViewById(R.id.dpimg);
            stroy_plus=itemView.findViewById(R.id.stroy_plus);
            storyseen=itemView.findViewById(R.id.storyseen);
            addstroy_text=itemView.findViewById(R.id.addstroy_text);
            stroyusername=itemView.findViewById(R.id.stroyusername);


        }
    }


    private void userinfo(final MyHolder viewHolder, String  userid, final int pos){
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Users").child(userid);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ModelUser user=dataSnapshot.getValue(ModelUser.class);
                Log.e("asdasd"," name"+user.getName()+"image"+user.getImage());
              // Picasso.get().load(user.getImage()).placeholder(R.drawable.ic_us_dark).into(viewHolder.storydpimg);
                Glide.with(context).load(user.getImage()).placeholder(R.drawable.ic_us_dark).into(viewHolder.storydpimg);
            /*    try {
                    Picasso.get().load(user.getImage()).into(viewHolder.storyseen, new Callback() {
                        @Override
                        public void onSuccess() {
                            //set loading progress bar gone

                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });


                }catch (Exception e) {

                }
             */
                if (pos!=0) {
                   // Picasso.get().load(user.getImage()).placeholder(R.drawable.ic_us_dark).into(viewHolder.storyseen);
                    Glide.with(context).load(user.getImage()).placeholder(R.drawable.ic_us_dark).into(viewHolder.storyseen);
                    viewHolder.stroyusername.setText(user.getName());

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void myStory(final  TextView textView, final ImageView imageView, final boolean click){
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Story").
                child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count=0;
                long timestamp=System.currentTimeMillis();
                for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                    ModelStory story=snapshot.getValue(ModelStory.class);
                    if(timestamp > story.getsTimeStart() && timestamp < story.getsTimeEnd()){
                        count++;
                    }else {
                        //deletion of post if time is out of day
                        final DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Story").
                                child(FirebaseAuth.getInstance().getCurrentUser().
                                        getUid()).child(story.getsId());

                        StorageReference picRef= FirebaseStorage.getInstance().getReferenceFromUrl(story.getsImage());
                        picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                reference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                          //  Toast.makeText(ShowStory.this, "Deleted", Toast.LENGTH_SHORT).show();

                                        }
                                    }
                                });
                            }
                        });
                    }
                }

                if (click){
                    //TODO:show alert dialog
                    if (count>0){
                        final AlertDialog alertDialog=new AlertDialog.Builder(context,4).create();
                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "View Story", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //TODO:goto story
                                Intent intent=new Intent(context,ShowStory.class);
                                intent.putExtra("userid",FirebaseAuth.getInstance().getCurrentUser().getUid());
                                context.startActivity(intent);
                                dialogInterface.dismiss();
                            }
                        });
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add Story", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent=new Intent(context, AddStoryActivity.class);
                            context.startActivity(intent);
                            dialogInterface.dismiss();
                        }
                    });
                    alertDialog.show();
                    }
                    else {
                        Intent intent=new Intent(context, AddStoryActivity.class);
                        context.startActivity(intent);
                    }

                }
                else {
                    if (count>0){
                        textView.setText("My Story");
                        imageView.setVisibility(View.GONE);
                    }
                    else {
                        textView.setText("Add Story");
                        imageView.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void seenStory(final MyHolder viewHolder,String userid){
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Story")
                .child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i=0;
                for (DataSnapshot snapshot:dataSnapshot.getChildren()){
                    if (!snapshot.child("views")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).exists()
                            && System.currentTimeMillis()<snapshot.getValue(ModelStory.class).getsTimeEnd()){
                        i++;
                    }
                }
                if (i>0){
                    viewHolder.storydpimg.setVisibility(View.VISIBLE);
                    viewHolder.storyseen.setVisibility(View.GONE);
                }
                else {
                    viewHolder.storydpimg.setVisibility(View.GONE);
                    viewHolder.storyseen.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
