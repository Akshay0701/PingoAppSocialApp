package com.example.pingoapp;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class SliderAdapterIntro extends PagerAdapter {

    Context context;
    LayoutInflater layoutInflater;

    public SliderAdapterIntro(Context context){
        this.context=context;
    }
    //array of images
    public String[] slide_images={

            "wlecomesocialapp.json",

            "connectingsocialapp.json",

            "spreadlovesocialapp.json",

            "uptodatesocialapp.json",

            "securesocialapp.json"


    };

    //array of images
    public String[] slide_title={
            "Welcome To PinGoo App"
            ,"Connecting People"
            ,"Spread Love"
            ,"Update Posts,Your Timeline"
            ,"Secure Chatting"
    };

    //array of images
    public String[] slide_description={
            "Social media are interactive Web 2.0 Internet-based applications. " +
                    "User-generated content, or user-shared content, such as text posts or comments," +
                    "digital photos or videos, and data generated through all online interactions, is the lifeblood of social media"
            ,"Connect to friends more easly"
            ,"Spread love with this app make new friend"
            ,"Always Up-To-Date Guide to Social Media with Image and have fun..."
            ,"Secured by SSL/TLS and PCI compliant\n There Is Multiple Authentication By Commander ,User ,School"
    };

    @Override
    public int getCount() {
        return slide_title.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view==(RelativeLayout)object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        layoutInflater=(LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view =layoutInflater.inflate(R.layout.slide_layout,container,false);

        //ImageView slideImageView=(ImageView)view.findViewById(R.id.intro_img);
        TextView slideTitle=(TextView) view.findViewById(R.id.intro_title);
        LottieAnimationView slideImageView=(LottieAnimationView)view.findViewById(R.id.intro_img);
        //slideImageView.setSpeed(200f);


        TextView slideDescription=(TextView) view.findViewById(R.id.intro_description);

        slideImageView.setAnimation(slide_images[position]);
        slideTitle.setText(slide_title[position]);
        slideDescription.setText(slide_description[position]);

        container.addView(view);
        return view;

    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((RelativeLayout)object);
    }
}
