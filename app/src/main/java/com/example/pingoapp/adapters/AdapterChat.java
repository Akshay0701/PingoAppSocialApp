package com.example.pingoapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.pingoapp.R;
import com.example.pingoapp.models.ModelChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterChat  extends RecyclerView.Adapter<AdapterChat.MyHolder> {



    FirebaseUser firebaseUser;

    private static final int MSG_TYPE_LEFT=0;
    private static final int MSG_TYPE_RIGHT=1;
    Context context;
    List<ModelChat> chatList;
    String imageUrl;

    public AdapterChat(Context context, List<ModelChat> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==MSG_TYPE_LEFT){
            View view= LayoutInflater.from(context).inflate(R.layout.row_chat_left,parent,false);
            return new MyHolder(view);
        }
        else {
            View view= LayoutInflater.from(context).inflate(R.layout.row_chat_right,parent,false);
        return new MyHolder(view);
        }


    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int position) {


        String message = chatList.get(position).getMessage();
        String timestamp = chatList.get(position).getTimestamp();
        String type = chatList.get(position).getType();


        Calendar cal = Calendar.getInstance(Locale.ENGLISH);

        cal.setTimeInMillis(Long.parseLong(timestamp));
        String datetime = DateFormat.format("dd//MM/yyyy hh:mm:aa", cal).toString();


        if(type.equals("text")){
            //text message
            holder.messageIv.setVisibility(View.GONE);
            holder.messageTv.setVisibility(View.VISIBLE);
            holder.messageTv.setText(message);
        }
        else {

            //text message
            holder.messageIv.setVisibility(View.VISIBLE);
            holder.messageTv.setVisibility(View.GONE);
         //   Picasso.get().load(message).placeholder(R.drawable.ic_image_black).into(holder.messageIv);
            Glide.with(context).load(message).placeholder(R.drawable.ic_image_black).into(holder.messageIv);
        }
        //set Data
     //   holder.messageTv.setText(message);


        holder.timeTv.setText(datetime);

        try {
           // Picasso.get().load(imageUrl).into(holder.profileIv);
            Glide.with(context).load(imageUrl).placeholder(R.drawable.ic_us_dark).into(holder.profileIv);
        } catch (Exception e) {

        }


        //check for delete message
        holder.messageLAyout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(context,R.style.AlertDialog);
                builder.setTitle("Delete");
                builder.setMessage("Are You Want to Delete This Message?");
                //delete button
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteMEssage(position);
                    }
                });
                //cancel button
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                   dialog.dismiss();
                    }
                });

                builder.create().show();


            }
        });





     //   if (position == chatList.size() - 1) {
   //         Toast.makeText(context, ""+chatList.get(position).getSeen(), Toast.LENGTH_SHORT).show();
        holder.isSeenTv.setVisibility(View.GONE);
        if (chatList.get(chatList.size()-1).getSeen() != null) {
                Boolean isseen = chatList.get(position).getSeen();
              //  Toast.makeText(context, "" + chatList.get(position).getSeen().toString(), Toast.LENGTH_SHORT).show();
            holder.isSeenTv.setVisibility(View.VISIBLE);
                if (isseen) {
                    holder.isSeenTv.setText("Seen");
                } else {
                    holder.isSeenTv.setText("Delevered");
                }
            } else {
                holder.isSeenTv.setVisibility(View.GONE);
            }

       // }
    }

    private void deleteMEssage(int position) {

        //myid
        final String myUID=FirebaseAuth.getInstance().getCurrentUser().getUid();
        String msgTimeStamp = chatList.get(position).getTimestamp();
        DatabaseReference dbRef= FirebaseDatabase.getInstance().getReference("Chats");
        Query query=dbRef.orderByChild("timestamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                for (DataSnapshot ds:dataSnapshot.getChildren()) {



                    if(ds.child("sender").getValue().equals(myUID)){
                        //1 remove the message from chats
                     //   ds.getRef().removeValue();

                        //2 set value message this was deleted
                        HashMap<String, Object> hashMap=new HashMap<>();
                        hashMap.put("message","This message was deleted..");
                        hashMap.put("type","text");
                        ds.getRef().updateChildren(hashMap);
                        Toast.makeText(context, "Message was Deleted...", Toast.LENGTH_SHORT).show();

                    }
                    else {


                        Toast.makeText(context, "you can only delete your message..", Toast.LENGTH_SHORT).show();


                    }




                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }


    @Override
    public int getItemViewType(int position) {
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        if(chatList.get(position).getSender().equals(firebaseUser.getUid())){
            return MSG_TYPE_RIGHT;
        }
        else {
            return MSG_TYPE_LEFT;
        }
    }

    class MyHolder extends RecyclerView.ViewHolder{

        ImageView profileIv,messageIv;
        TextView messageTv,timeTv,isSeenTv;
        LinearLayout messageLAyout;



        public MyHolder(@NonNull View itemView) {


            super(itemView);

        profileIv=itemView.findViewById(R.id.profileIv);
        messageIv=itemView.findViewById(R.id.message_Iv);
        messageTv=itemView.findViewById(R.id.message_Tv);
        timeTv=itemView.findViewById(R.id.timeIv);
        isSeenTv=itemView.findViewById(R.id.isseenTv);
        messageLAyout=itemView.findViewById(R.id.messageLayout);
        }
    }


}
