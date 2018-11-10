package com.cross.beaglesight;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Intent bowsIntent = new Intent(this, SightList.class);
        Button manageBows = findViewById(R.id.manageBows);
        manageBows.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(bowsIntent);
            }
        });

        final Intent targetsIntent = new Intent(this, TargetMap.class);
        Button manageTargets = findViewById(R.id.manageTargets);
        manageTargets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(targetsIntent);
            }
        });
    }
}
