package com.example.pingoapp;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.pingoapp.adapters.AdapterChatList;
import com.example.pingoapp.models.ModelChat;
import com.example.pingoapp.models.ModelChatList;
import com.example.pingoapp.models.ModelUser;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;


/**
 * A simple {@link Fragment} subclass.
 */
public class Chat_ListFragment extends Fragment {


    FirebaseAuth firebaseAuth;
    RecyclerView recyclerView;
    List<ModelChatList> chatlistList;
    List<ModelUser> userList;
    AdapterChatList adapterChatList;
    DatabaseReference reference;
    FirebaseUser currentUser;



    public Chat_ListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_chat__list, container, false);

        //checking user login or not
        firebaseAuth= FirebaseAuth.getInstance();
        checkforuserlogin();
        currentUser=FirebaseAuth.getInstance().getCurrentUser();

        recyclerView=view.findViewById(R.id.recyclerView);

        chatlistList=new ArrayList<>();

        //starting loading screen in backgorund from database getting shit data
        Toast.makeText(getContext(), "loading", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            public void run() {
                // do something...
                startloading();
            }
        }, 100);

        return view;
    }

    private void startloading() {
                reference= FirebaseDatabase.getInstance().getReference("Chatlist").child(currentUser.getUid());
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds:dataSnapshot.getChildren()){
                            ModelChatList chatList=ds.getValue(ModelChatList.class);
                            chatlistList.add(chatList);
                        }
                        Toast.makeText(getContext(), "loading Last Message", Toast.LENGTH_SHORT).show();
                        loadChatList();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }


    private void loadChatList() {
                userList=new ArrayList<>();
                reference=FirebaseDatabase.getInstance().getReference("Users");
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        userList.clear();
                        for (DataSnapshot ds:dataSnapshot.getChildren()){
                            ModelUser user=ds.getValue(ModelUser.class);
                            for(ModelChatList chatList:chatlistList){
                                if(user.getUid()!=null&&user.getUid().equals(chatList.getId())){
                                    userList.add(user);
                                    break;
                                }
                                //adapter
                                adapterChatList=new AdapterChatList(getContext(),userList);
                                recyclerView.setAdapter(adapterChatList);
                                for(int i=0;i<userList.size();i++){
                                    lastMessage(userList.get(i).getUid());
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }


    private void lastMessage(final String uid) {
                DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Chats");
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String theLastMessge="New Message";
                        adapterChatList.setLastMessageMap(uid,theLastMessge);
                        adapterChatList.notifyDataSetChanged();
                        for(DataSnapshot ds:dataSnapshot.getChildren()){
                            ModelChat chat=ds.getValue(ModelChat.class);
                            if(chat==null){
                                continue;
                            }
                            String sender=chat.getSender();
                            String reciver=chat.getReciver();
                            if(sender==null||reciver==null){
                                continue;
                            }
                            if(chat.getReciver().equals(currentUser.getUid())&&chat.getSender().equals(uid)||chat.getReciver()
                                    .equals(uid)&&chat.getSender().equals(currentUser.getUid())){
                                //instead of showing uri in
                                if(chat.getType().equals("image")){
                                    theLastMessge=chat.getType();
                                }else {
                                    theLastMessge=chat.getMessage();
                                }

                            }

                        }
                        adapterChatList.setLastMessageMap(uid,theLastMessge);
                        adapterChatList.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }


    public void checkforuserlogin() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {


        }
        else{
            startActivity(new Intent(getActivity(),RegisterActivity.class));
            getActivity().finish();
        }
    }





}
