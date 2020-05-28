package com.example.pingoapp;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.Toast;


import com.baoyz.widget.PullRefreshLayout;
import com.example.pingoapp.adapters.AdapterPosts;
import com.example.pingoapp.adapters.AdapterStory;
import com.example.pingoapp.models.ModelPosts;
import com.example.pingoapp.models.ModelStory;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.transition.Fade;
import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {


    SharedPreferences.Editor editor;

    ScrollView scrollView;

    FirebaseAuth firebaseAuth;
    RecyclerView recyclerView,recyclerViewSory;
    List<ModelPosts> postsList;
    List<ModelStory> storyList;
    AdapterPosts adapterPosts;
    AdapterStory adapterStory;


    float size;

    private List<String> followingList;
    PullRefreshLayout refresh;

    public HomeFragment() {
        // Required empty public constructor
    }


    @SuppressLint("ResourceAsColor")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_home, container, false);


        firebaseAuth = FirebaseAuth.getInstance();
        checkfollowing();


        editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();

        //init recycler
        recyclerView = view.findViewById(R.id.postRecycleView);
        recyclerViewSory = view.findViewById(R.id.storyRecycleView);

        refresh=view.findViewById(R.id.refresh);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //show newest post
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set layout to recycleview
        recyclerView.setLayoutManager(layoutManager);

        //init postlist
        postsList = new ArrayList<>();
        storyList = new ArrayList<>();


        recyclerView.addOnScrollListener(new HidingScrollListener() {
            @Override
            public void onHide() {
                hideViews();
                Toast.makeText(getContext(), "asdas", Toast.LENGTH_SHORT).show();
                //     ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
            }

            @Override
            public void onShow() {
                showViews();
            }
        });


        //load post
        loadPosts();
        loadStory();
        showViews();

        setRefreshdialog();

        refresh.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadPosts();
                loadStory();
                showViews();
            }
        });

        scrollView=view.findViewById(R.id.scrollView);

        return view;
    }

    private void setRefreshdialog() {
        int min = 0;
        int max = 4;
        Random r = new Random();
        int i1 = r.nextInt(max - min + 1) + min;
       switch (i1){
           case 0:
               refresh.setRefreshStyle(PullRefreshLayout.STYLE_MATERIAL);
               break;

           case 1:
               refresh.setRefreshStyle(PullRefreshLayout.STYLE_CIRCLES);
               break;

           case 2:
               refresh.setRefreshStyle(PullRefreshLayout.STYLE_WATER_DROP);
               break;
           case 3:
               refresh.setRefreshStyle(PullRefreshLayout.STYLE_RING);
               break;
           case 4:
               refresh.setRefreshStyle(PullRefreshLayout.STYLE_SMARTISAN);
               break;

       }
    }


    private void loadStory() {

        //path
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Story");
        //get all data from this ref
        storyList.clear();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long timestamp=System.currentTimeMillis();

                storyList.add(new ModelStory("","",0,0,FirebaseAuth.getInstance().getCurrentUser().getUid()));

                for (String id:followingList){
                    int countStory=0;
                    ModelStory story=null;
                    for (DataSnapshot snapshot:dataSnapshot.child(id).getChildren()){
                        story=snapshot.getValue(ModelStory.class);
                        if (timestamp>story.getsTimeStart()&&timestamp<story.getsTimeEnd()){
                            countStory++;
                        }
                        else {
                            //deletion of post if time is out of day
                            final DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Story").
                                    child(id).child(story.getsId());

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
//                    Toast.makeText(getContext(), ""+countStory, Toast.LENGTH_SHORT).show();
                    if (countStory>0){
                        storyList.add(story);
                    }
                }


                    //adapter
                    adapterStory = new AdapterStory(getActivity(), storyList);

                    recyclerViewSory.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

                    //set adapter to recycle
                    recyclerViewSory.setAdapter(adapterStory);

                    refresh.setRefreshing(false);

                }



            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                //case of error
            }
        });


    }

    private void checkfollowing(){
        followingList =new ArrayList<>();
        DatabaseReference isFollowing= FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseAuth.getCurrentUser().getUid()).child("following");
        isFollowing.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingList.clear();
                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    followingList.add(dataSnapshot1.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadPosts() {


        //path
        Query ref = FirebaseDatabase.getInstance().getReference("Posts");
        //get all data from this ref
        postsList.clear();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelPosts modelPosts = ds.getValue(ModelPosts.class);
                    for(String id:followingList){
                        if (modelPosts.getUid().equals(id)){
                            postsList.add(modelPosts);
                        }
                    }

                    //adapter

                    adapterPosts = new AdapterPosts(getActivity(), postsList);

                    //set adapter to recycle
                    recyclerView.setAdapter(adapterPosts);

                    //set swipe to delete option
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
                      public void onChildDraw (Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,float dX, float dY,int actionState, boolean isCurrentlyActive){

                          new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                                  .addSwipeLeftActionIcon(R.drawable.ic_profile_green)
                                  .addSwipeLeftBackgroundColor(R.color.colorPrimaryDark)
                                  .addSwipeRightActionIcon(R.drawable.ic_delete_green)
                                  .addSwipeRightBackgroundColor(R.color.colorPrimaryDark)
                                  .create()
                                  .decorate();

                          super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                      }

                  };
                  simpleCallback.getSwipeEscapeVelocity(0.6f);
                   ItemTouchHelper itemTouchHelper= new ItemTouchHelper(simpleCallback);
                   itemTouchHelper.attachToRecyclerView(recyclerView);
                    refresh.setRefreshing(false);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                //case of error
            }
        });



    }


    private void overridePendingTransition(int slide_in_left, int slide_out_right) {
       // overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void searchPost(final String searchQuery) {
        //path
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //get all data from this ref
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelPosts modelPosts = ds.getValue(ModelPosts.class);

                    if (modelPosts.getpTitle().toLowerCase().contains(searchQuery.toLowerCase())
                            || modelPosts.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())) {


                        postsList.add(modelPosts);
                    }

                    //adapter
                    adapterPosts = new AdapterPosts(getActivity(), postsList);

                    //set adapter to recycle
                    recyclerView.setAdapter(adapterPosts);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                //case of error
            }
        });

    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void checkforuserlogin() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {


        } else {
            startActivity(new Intent(getActivity(), RegisterActivity.class));
            try {
                Objects.requireNonNull(getActivity()).finish();
            } catch (NullPointerException e) {

            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);

        //search menu
        MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);


        //serach listner
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //called when user pressed search button
                if (!TextUtils.isEmpty(query)) {
                    searchPost(query);
                } else {
                   // loadPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //called when user pressed any letter
                if (!TextUtils.isEmpty(newText)) {
                    searchPost(newText);
                } else {
                   // loadPosts();
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }


    private void hideViews() {
     //   toolbar.animate().translationY(-toolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
      //  AppBarLayout.animate().translationY(-toolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
       /* recyclerViewSory.animate().translationY(-recyclerViewSory.getHeight()).setInterpolator(new AccelerateInterpolator(2));
        recyclerViewSory.animate()
                .setDuration(200)
                .alpha(10.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        Transition transition = new Slide(Gravity.BOTTOM);
                        transition.setDuration(200);
                        transition.addTarget(recyclerViewSory);
                        TransitionManager.beginDelayedTransition(recyclerViewSory, transition);
                        recyclerViewSory.setVisibility(View.GONE);
                    }
                });

        */



       // AppBarLayout.animate().scaleY(AppBarLayout.getY());
     //   FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mFabButton.getLayoutParams();
   //     int fabBottomMargin = lp.bottomMargin;
      //  mFabButton.animate().translationY(mFabButton.getHeight()+fabBottomMargin).setInterpolator(new AccelerateInterpolator(2)).start();
    }

    private void showViews() {
     /*   recyclerViewSory.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
        recyclerViewSory.animate()
                .setDuration(200)
                .alpha(10.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        //Transition transition = new Fade();
                        Transition transition = new Slide(Gravity.TOP);
                        transition.setDuration(100);
                        transition.addTarget(recyclerViewSory);
                        TransitionManager.beginDelayedTransition(recyclerViewSory, transition);
                        recyclerViewSory.setVisibility(View.VISIBLE);
                    }
                });


      */

      //  toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
     //   AppBarLayout.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
      //  AppBarLayout.animate().scaleY(size);
       // mFabButton.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            editor.remove("username");
            editor.remove("password");
            editor.apply();
            firebaseAuth.signOut();
            checkforuserlogin();
        }
        if (id == R.id.action_add_post) {
            startActivity(new Intent(getActivity(), AddPostActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

}