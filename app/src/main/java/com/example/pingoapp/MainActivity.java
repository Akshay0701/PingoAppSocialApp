package com.example.pingoapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button login,register;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    register=findViewById(R.id.register_btn);

        //noinspection deprecation
        register.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //start new register
            startActivity(new Intent(MainActivity.this,RegisterActivity.class));

        }
    });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,loginActivity.class));

            }
        });

    }
}
