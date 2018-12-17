package com.cross.beaglesight;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.core.app.NavUtils;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.cross.beaglesightlibs.BowConfig;
import com.cross.beaglesightlibs.BowManager;
import com.cross.beaglesightlibs.PositionPair;

import java.util.UUID;

import static com.cross.beaglesight.ShowSight.CONFIG_TAG;

public class AddSight extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sight);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        Button button = findViewById(R.id.add_button);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        EditText nameEntry = findViewById(R.id.name);
        EditText descriptionEntry = findViewById(R.id.description);
        final BowManager bowManager = BowManager.getInstance(this);

        String name = nameEntry.getText().toString();
        String description = descriptionEntry.getText().toString();

        final BowConfig bowConfig = new BowConfig();

        bowConfig.setId(UUID.randomUUID().toString());
        bowConfig.setName(name);
        bowConfig.setDescription(description);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                bowManager.addBowConfig(bowConfig);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent();
                        intent.putExtra(CONFIG_TAG, bowConfig);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                });
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (getParentActivityIntent() == null) {
                    Log.i("BeagleSight", "You have forgotten to specify the parentActivityName in the AndroidManifest!");
                    onBackPressed();
                } else {
                    NavUtils.navigateUpFromSameTask(this);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
