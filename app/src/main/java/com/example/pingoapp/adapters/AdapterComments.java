package com.example.pingoapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.pingoapp.R;
import com.example.pingoapp.models.ModelComment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterComments extends RecyclerView.Adapter<AdapterComments.MyHolder> {


    public AdapterComments(Context context, List<ModelComment> commentList, String myUid, String postId) {
        this.context = context;
        this.commentList = commentList;
        this.myUid = myUid;
        this.postId = postId;
    }

    Context context;
    List<ModelComment> commentList;
    String myUid,postId;

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.row_comments,parent,false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int i) {

        //get values
        final String uid=commentList.get(i).getUid();
        String name=commentList.get(i).getuName();
        String email=commentList.get(i).getuEmail();
        String image=commentList.get(i).getuDp();
        final String cid=commentList.get(i).getcId();
        String comment=commentList.get(i).getComment();
        String timestamp=commentList.get(i).getTimestamp();

        //convert timestamp
        Calendar calendar= Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String pTime= DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();

        //set data
        holder.nameTv.setText(name);
        holder.commentTv.setText(comment);
        holder.itmeTv.setText(pTime);

        try{
            Picasso.get().load(image).placeholder(R.drawable.ic_us_dark).into(holder.avatarIv);
        }catch (Exception e){ }

        // comment click listner
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if myuid and post id mactch
                if(myUid.equals(uid)){
                    //show delete options
                    AlertDialog.Builder builder=new AlertDialog.Builder(v.getRootView().getContext(),R.style.AlertDialog);
                    builder.setTitle("Delete");
                    builder.setMessage("Are You Want To Delete");

                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteComment(cid);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    //show dialog
                    builder.create().show();
                }
            }
        });


    }

    private void deleteComment(String cid) {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.child("Comments").child(cid).removeValue();//it will remove comment

        //now update that comment
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String comments=""+dataSnapshot.child("pComments").getValue();
                String newCommentVal= String.valueOf(Integer.parseInt(comments)-1);
               ref.child("pComments").setValue(""+newCommentVal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        //declare view components
        ImageView avatarIv;
        TextView nameTv,commentTv,itmeTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            avatarIv=itemView.findViewById(R.id.avatarIv);
            nameTv=itemView.findViewById(R.id.nameTv);
            commentTv=itemView.findViewById(R.id.commentTv);
            itmeTv=itemView.findViewById(R.id.timeTv);


        }
    }
}
