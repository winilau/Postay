package com.example.postay;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class main extends AppCompatActivity {
    private Button mVolunteer = findViewById(R.id.volunteer);
    private Button mUser = findViewById(R.id.user);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mVolunteer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(main.this, VolunteerLogInActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}