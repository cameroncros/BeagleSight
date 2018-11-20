package com.cross.beaglesightwear;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.cross.beaglesight.R;
import com.cross.beaglesightwear.views.SightGraphWear;
import com.cross.beaglesightlibs.BowConfig;
import com.cross.beaglesightlibs.BowManager;
import com.cross.beaglesightlibs.exceptions.InvalidBowConfigIdException;

import static com.cross.beaglesightwear.ListenerService.REFRESH_DATA;

public class ShowSight extends WearableActivity {
    static final String CONFIG_TAG = "config";

    private String bowId;
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_sight);

        Intent intent = getIntent();
        bowId = (String) intent.getSerializableExtra(CONFIG_TAG);
        drawBowConfig();


        ImageView button = findViewById(R.id.exitButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Enables Always-on
        setAmbientEnabled();

        // Setup broadcast receiver
        IntentFilter filter = new IntentFilter(REFRESH_DATA);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                drawBowConfig();
            }
        };
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    void drawBowConfig()
    {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                BowManager bm = BowManager.getInstance(ShowSight.this);
                final BowConfig bowConfig = bm.bowConfigDao().get(bowId);
                bowConfig.setPositionArray(bm.positionPairDao().getPositionForBow(bowId));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SightGraphWear sightGraphWear = findViewById(R.id.sightGraph);
                        sightGraphWear.setBowConfig(bowConfig);
                    }
                });
            }
        });
    }
}
