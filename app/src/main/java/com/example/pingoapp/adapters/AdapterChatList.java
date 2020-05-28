package com.example.pingoapp.adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.pingoapp.ChatActivity;
import com.example.pingoapp.R;
import com.example.pingoapp.models.ModelUser;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterChatList extends RecyclerView.Adapter<AdapterChatList.MyHolder> {

    Context context;
    List<ModelUser> userList;
    private HashMap<String, String> lastMessageMap;

    public AdapterChatList(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
        this.lastMessageMap = new HashMap<>();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(context).inflate(R.layout.row_chatlist,parent,false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int i) {
        //get data
        final String hisUid=userList.get(i).getUid();
        String userImage=userList.get(i).getImage();
        String userName=userList.get(i).getName();
        String lastMessage=lastMessageMap.get(hisUid);

        //set data
        holder.nameTv.setText(userName);
        if(lastMessage==null||lastMessage.equals("default")){
            holder.lastMessageTv.setVisibility(View.GONE);
        }
        else {
            holder.lastMessageTv.setVisibility(View.VISIBLE);
            holder.lastMessageTv.setText(lastMessage);
        }
        try {
           // Picasso.get().load(userImage).placeholder(R.drawable.ic_us_dark).into(holder.profileivIv);
            Glide.with(context).load(userImage).placeholder(R.drawable.ic_us_dark).into(holder.profileivIv);
        }catch (Exception e){
            //Picasso.get().load(R.drawable.ic_us_dark).into(holder.profileivIv);
            Glide.with(context).load(R.drawable.ic_us_dark).into(holder.profileivIv);

        }

        holder.lastMessageTv.setText(lastMessage);
        //set online status
        if(userList.get(i).getOnlineStatus().equals("online")){
            //online
            holder.onlineStatusIv.setImageResource(R.drawable.circle_online);
        }
        else {

            //offline
             holder.onlineStatusIv.setImageResource(R.drawable.circle_offline);
        }
        //handle click on user
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                //start chat with that user
                Intent intent=new Intent(context, ChatActivity.class);
                intent.putExtra("hisUID",hisUid);

                //this code is for transtion between activity
               Pair[] pairs=new Pair[2];
               pairs[0]=new Pair<View,String>(holder.profileivIv,"avatarIv");
               pairs[1]=new Pair<View,String>(holder.nameTv,"backimg");
               ActivityOptions options=ActivityOptions.makeSceneTransitionAnimation((Activity) context,pairs);
                //context.startActivity(intent);
                context.startActivity(intent,options.toBundle());


            }
        });


    }

    public void setLastMessageMap(String userId, String lastMessage){
        lastMessageMap.put(userId,lastMessage);
    }


    @Override
    public int getItemCount() {
        return userList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        //view of chatlist
        ImageView profileivIv,onlineStatusIv;
        TextView nameTv,lastMessageTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            //init view
            profileivIv=itemView.findViewById(R.id.profileivIv);
            onlineStatusIv=itemView.findViewById(R.id.onlineStatusIv);
            nameTv=itemView.findViewById(R.id.nameTv);
            lastMessageTv=itemView.findViewById(R.id.lastMessageTv);
        }
    }
}
