package com.example.pingoapp.adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.pingoapp.ChatActivity;
import com.example.pingoapp.R;
import com.example.pingoapp.ThereProfileActivity;
import com.example.pingoapp.models.ModelUser;
import com.squareup.picasso.Picasso;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder> {





    Context context;
    List<ModelUser> userList;


    public AdapterUsers(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;

    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(context).inflate(R.layout.row_user,parent,false);
        return new MyHolder(view);


    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, int position) {

        final String hisUID=userList.get(position).getUid();

        final String userimage=userList.get(position).getImage();
        String userName=userList.get(position).getName();
        String phones=userList.get(position).getPhone();
        final String userEmail=userList.get(position).getEmail();
        String backimg=userList.get(position).getBackimg();


        //setdata
        holder.mNameTv.setText(userName);
       // Toast.makeText(context, ""+hisUID, Toast.LENGTH_SHORT).show();

        try {
           // Picasso.get().load(userimage).placeholder(R.drawable.ic_add_dark).into(holder.mavatarIv);
            Glide.with(context).load(userimage).placeholder(R.drawable.ic_us_dark).into(holder.mavatarIv);
          //  Picasso.get().load(backimg).placeholder(R.drawable.ic_us_dark).into(holder.backimg);
            Glide.with(context).load(backimg).placeholder(R.drawable.ic_us_dark).into(holder.backimg);

        }catch (Exception e){

        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show dialog
                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setItems(new String[]{"Posts", "Chat"}, new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which==0){
                            //goto posts pages
                            Intent intent=new Intent(context, ThereProfileActivity.class);
                            intent.putExtra("uid", hisUID);
                            //this code is for transtion between activity
                            Pair[] pairs=new Pair[2];
                            pairs[0]=new Pair<View,String>(holder.mavatarIv,"avatarIv");
                            pairs[1]=new Pair<View,String>(holder.backimg,"backimg");
                            ActivityOptions options=ActivityOptions.makeSceneTransitionAnimation((Activity) context,pairs);
                            context.startActivity(intent,options.toBundle());

                        }
                        if(which==1){
                            //redirect to chat acitvity
                            Intent intent=new Intent(context, ChatActivity.class);
                            intent.putExtra("hisUID", hisUID);

                            //this code is for transtion between activity
                            Pair[] pairs=new Pair[2];
                            pairs[0]=new Pair<View,String>(holder.mavatarIv,"avatarIv");
                            pairs[1]=new Pair<View,String>(holder.backimg,"backimg");
                            ActivityOptions options=ActivityOptions.makeSceneTransitionAnimation((Activity) context,pairs);

                            context.startActivity(intent,options.toBundle());
                        }

                    }
                });
                builder.create().show();

            }
        });

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
    ImageView mavatarIv,backimg;
    TextView mNameTv;
    public MyHolder(@NonNull View itemView) {
        super(itemView);
        mavatarIv=itemView.findViewById(R.id.avatarIv);
        backimg=itemView.findViewById(R.id.backimg);
        mNameTv=itemView.findViewById(R.id.nametv);

    }
}
}
