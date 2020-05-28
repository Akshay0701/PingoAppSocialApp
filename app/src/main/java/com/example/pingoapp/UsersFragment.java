package com.example.pingoapp;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.baoyz.widget.PullRefreshLayout;
import com.example.pingoapp.adapters.AdapterPostInUser;
import com.example.pingoapp.adapters.AdapterUsers;
import com.example.pingoapp.models.ModelPosts;
import com.example.pingoapp.models.ModelUser;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;


/**
 * A simple {@link Fragment} subclass.
 */
public class UsersFragment extends Fragment {


    FirebaseAuth firebaseAuth;

    SharedPreferences.Editor editor;

    AdapterUsers adapterUsers;
    List<ModelUser> userList;

    RecyclerView recyclerView;
    //this is for a

    RecyclerView recyclerViewpost;
    List<ModelPosts> postsList;
    AdapterPostInUser adapterPosts;

    //for refresh As you already know
    PullRefreshLayout refresh;

    public UsersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_users, container, false);




        editor= PreferenceManager.getDefaultSharedPreferences(getContext()).edit();

        refresh=view.findViewById(R.id.refresh);
        refresh.setClipToPadding(true);
        refresh.setColorSchemeColors(R.color.colorPrimary);
        recyclerViewpost = view.findViewById(R.id.postRecycleView);
        //init post
        //we trying gird for snowing post
        GridLayoutManager manager = new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false);

        //this default we use all over
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //show newest post
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set layout to recycleview
        recyclerViewpost.setLayoutManager(manager);

        //init postlist
        postsList = new ArrayList<>();
        //init
        recyclerView=view.findViewById(R.id.users_regulerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));



        firebaseAuth= FirebaseAuth.getInstance();
        String MyID=firebaseAuth.getUid();
      //  Toast.makeText(getActivity(), ""+MyID, Toast.LENGTH_SHORT).show();

        userList=new ArrayList<>();

      //  loadPosts();
        getalluser();

        refresh.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
              //  loadPosts();
                getalluser();
            }
        });

        return view;
    }
    private void loadPosts() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                //TODO your background code
                //path
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Query ref = FirebaseDatabase.getInstance().getReference("Posts");
                        //get all data from this ref
                        postsList.clear();
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    ModelPosts modelPosts = ds.getValue(ModelPosts.class);
                                    postsList.add(modelPosts);
                                }


                                //adapter
                                adapterPosts = new AdapterPostInUser(getActivity(), postsList);

                                //set adapter to recycle
                                recyclerViewpost.setAdapter(adapterPosts);

                                /*et swipe to delete option

                                ItemTouchHelper.SimpleCallback simpleCallback=  new ItemTouchHelper.SimpleCallback(0,
                                        ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT) {
                                    private float previousDx = 0;
                                    @Override
                                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                                        return false;
                                    }

                                    @Override
                                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                                        //call funtion to delete
                                        previousDx = 0;
                                        //Toast.makeText(getContext(), ""+direction, Toast.LENGTH_SHORT).show();
                                        if(direction == 8) {
                                            // RIGHT swipe do delte post
                                            Boolean result= adapterPosts.deletepostwithswipe(firebaseAuth.getCurrentUser().getUid(),viewHolder.getAdapterPosition());
                                            if (result){
                                                adapterPosts.notifyItemRemoved(viewHolder.getAdapterPosition());

                                            }
                                            else {
                                                adapterPosts.notifyItemChanged(viewHolder.getAdapterPosition());
                                            }
                                        } else if (direction==4){
                                            // LEFT swipe do go to profile
                                            adapterPosts.profileopen(viewHolder.getAdapterPosition());
                                            adapterPosts.notifyItemChanged(viewHolder.getAdapterPosition());
                                        }

                                    }
                                };
                                simpleCallback.getSwipeEscapeVelocity(0.6f);
                                ItemTouchHelper itemTouchHelper= new ItemTouchHelper(simpleCallback);
                                itemTouchHelper.attachToRecyclerView(recyclerView);
                                refresh.setRefreshing(false);
                                //set swipe to delete option
                    /*we don,t requried it


                    ItemTouchHelper.SimpleCallback simpleCallback=  new ItemTouchHelper.SimpleCallback(0,
                            ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT) {
                        private float previousDx = 0;
                        @Override
                        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                            return false;
                        }

                        @Override
                        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                            //call funtion to delete
                            previousDx = 0;
                            //Toast.makeText(getContext(), ""+direction, Toast.LENGTH_SHORT).show();
                            if(direction == 8) {
                                // RIGHT swipe do delte post
                                Boolean result= adapterPosts.deletepostwithswipe(firebaseAuth.getCurrentUser().getUid(),viewHolder.getAdapterPosition());
                                if (result){
                                    adapterPosts.notifyItemRemoved(viewHolder.getAdapterPosition());

                                }
                                else {
                                    adapterPosts.notifyItemChanged(viewHolder.getAdapterPosition());
                                }
                            } else if (direction==4){
                                // LEFT swipe do go to profile
                                adapterPosts.profileopen(viewHolder.getAdapterPosition());
                                adapterPosts.notifyItemChanged(viewHolder.getAdapterPosition());
                            }

                        }
                    };
                    simpleCallback.getSwipeEscapeVelocity(0.6f);
                    ItemTouchHelper itemTouchHelper= new ItemTouchHelper(simpleCallback);
                    itemTouchHelper.attachToRecyclerView(recyclerView);
                 //   refresh.setRefreshing(false);
   */
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                //case of error
                            }
                        });
                    }
                }).start();
            }
        });
    }

    private void getalluser() {

       /* AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                //TODO your background code
                //
        */
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
                        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                userList.clear();
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    ModelUser modelUser = ds.getValue(ModelUser.class);

                                    if (!modelUser.getUid().equals(fUser.getUid())) {
                                        userList.add(modelUser);
                                    }

                                    adapterUsers = new AdapterUsers(getActivity(), userList);


                                   /*  this code use for auto scroll user bas aur kuch nahi hai iska kaaam
                                   final int speedScroll = 1000;
                                    final Handler handler = new Handler();
                                    final Runnable runnable = new Runnable() {
                                        int count = 0;
                                        boolean flag = true;
                                        @Override
                                        public void run() {
                                            if(count < adapterUsers.getItemCount()){
                                                if(count==adapterUsers.getItemCount()-1){
                                                    flag = false;
                                                }else if(count == 0){
                                                    flag = true;
                                                }
                                                if(flag) count++;
                                                else count--;

                                                recyclerView.smoothScrollToPosition(count);
                                                handler.postDelayed(this,speedScroll);
                                            }
                                        }
                                    };

                                    handler.postDelayed(runnable,speedScroll);

                                    */
                                }
                                recyclerView.setAdapter(adapterUsers);


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }).start();
           /* }
        });

            */

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void checkforuserlogin() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {


        }
        else{
            startActivity(new Intent(getActivity(),RegisterActivity.class));
            try {
                Objects.requireNonNull(getActivity()).finish();
            }catch (NullPointerException e){

            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onStart() {
        super.onStart();
        checkforuserlogin();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String isdashboradtutorial=prefs.getString("usertutorial","");
        if(isdashboradtutorial.equals("")) {
            tutorialstart();
        }

    }




    private void tutorialstart() {
        TapTargetView.showFor(getActivity(),              // `this` is an Activity
                TapTarget.forView(getActivity().findViewById(R.id.join_us), "JOin Us", "Muje Bata Nahi Kya Karta Hai Ye..:)")
                        // All options below are optional
                        .dimColor(R.color.colorgray)
                        .outerCircleColor(R.color.colorPrimary)
                        .targetCircleColor(android.R.color.black)
                        .transparentTarget(true)
                        .titleTextDimen(R.dimen.default_bmb_ham_button_height)
                        .descriptionTextColor(R.color.colorgray)
                        .textColor(R.color.white)
                        .id(2),                 // Specify the target radius (in dp)
                new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                    @Override
                    public void onTargetClick(TapTargetView view) {
                        super.onTargetClick(view);      // This call is optional
                        // doSomething();
                        SharedPreferences.Editor editor;
                        editor= PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
                        editor.putString("usertutorial", "1");//1 value will note as user as seen intro
                        editor.apply();
                        //  Toast.makeText(
                    }
                });

    }


}
