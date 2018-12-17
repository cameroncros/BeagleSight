package com.cross.beaglesight;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.cross.beaglesightlibs.BowConfig;
import com.cross.beaglesightlibs.BowManager;
import com.cross.beaglesightlibs.PositionCalculator;
import com.cross.beaglesightlibs.PositionPair;
import com.cross.beaglesightlibs.exceptions.InvalidNumberFormatException;

import static com.cross.beaglesight.ShowSight.CONFIG_TAG;

public class AddDistance extends AppCompatActivity {

    private Button add = null;

    private EditText simpleDistance = null;
    private EditText simplePin = null;

    private EditText pinSetting1 = null;
    private EditText offset1 = null;

    private EditText pinSetting2 = null;
    private EditText offset2 = null;

    private BowConfig bowConfig = null;


    private void updateAddStatus() {
        String distance = simpleDistance.getText().toString();
        String pinSetting = simplePin.getText().toString();

        try {
            //noinspection ResultOfMethodCallIgnored
            Double.parseDouble(distance);
            //noinspection ResultOfMethodCallIgnored
            Double.parseDouble(pinSetting);
            add.setEnabled(true);
            add.invalidate();
        } catch (NumberFormatException nfe) {
            add.setEnabled(false);
            add.invalidate();
        }
    }

    private void calculateResultPin() {
        try {
            float y1 = Float.parseFloat(pinSetting1.getText().toString());
            float y2 = Float.parseFloat(pinSetting2.getText().toString());
            float x1 = Float.parseFloat(offset1.getText().toString());
            float x2 = Float.parseFloat(offset2.getText().toString());

            // y(0) == Correct pin setting
            //y(x) = (y1-y2)/(x1-x2) * x + c
            // y(0) = c
            // c = y1 - (y1-y2)/(x1-x2) * x1
            float c = y1 - ((y1 - y2) / (x1 - x2) * x1);
            if (c == Float.NaN)
            {
                return;
            }
            String cstring = PositionCalculator.getDisplayValue(c, 2);

            if (simplePin.getText().toString().equals("")) {
                simplePin.setText(cstring);
                simplePin.invalidate();
            }
        } catch (NumberFormatException nfe) {
            // Do nothing
        }
    }

    private void updateEstimates() {
        String distance = simpleDistance.getText().toString();
        try {
            float dist = Float.parseFloat(distance);
            // Guess first pin setting.
            {
                float positionGuess = bowConfig.getPositionCalculator().calcPosition(dist);
                if (positionGuess == Float.NaN) {
                    return;
                }
                String guessString = PositionCalculator.getDisplayValue(positionGuess, 0);
                if (offset1.getText().toString().equals("")) {
                    pinSetting1.setText(guessString);
                }
            }
            // Guess slightly offsetted pin setting.
            {
                float positionGuess = bowConfig.getPositionCalculator().calcPosition(dist - 1);
                if (positionGuess == Float.NaN) {
                    return;
                }
                String guessString = PositionCalculator.getDisplayValue(positionGuess, 0);
                if (offset2.getText().toString().equals("")) {
                    pinSetting2.setText(guessString);
                }
            }

        } catch (NumberFormatException nfe) {
            // Do nothing.
        }
    }

    private final TextWatcher simpleListener = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            updateAddStatus();
            updateEstimates();

        }
    };

    private final TextWatcher calcPinListener = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            calculateResultPin();
        }
    };

    private final View.OnClickListener addPinSetting = new View.OnClickListener() {

        @SuppressWarnings("ResultOfMethodCallIgnored")
        @Override
        public void onClick(View v) {
            try {
                Float distance = Float.parseFloat(simpleDistance.getText().toString());
                Float pinSetting = Float.parseFloat(simplePin.getText().toString());
                final PositionPair pair = new PositionPair(distance, pinSetting);
                pair.setBowId(bowConfig.getId());
                bowConfig.getPositionArray().add(pair);
                Intent intent = new Intent();
                intent.putExtra(CONFIG_TAG, bowConfig);
                setResult(RESULT_OK, intent);
                finish();
            } catch (NumberFormatException nfe) {
                add.setEnabled(false);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_distance);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        Intent intent = getIntent();
        bowConfig = intent.getParcelableExtra(CONFIG_TAG);
        BowManager bm = BowManager.getInstance(this);

        add = findViewById(R.id.addDistance);
        add.setOnClickListener(addPinSetting);
        add.setEnabled(false);

        simpleDistance = findViewById(R.id.simpleDistance);
        simplePin = findViewById(R.id.simplePin);

        simpleDistance.addTextChangedListener(simpleListener);
        simplePin.addTextChangedListener(simpleListener);

        pinSetting1 = findViewById(R.id.pinSetting1);
        pinSetting2 = findViewById(R.id.pinSetting2);
        offset1 = findViewById(R.id.offset1);
        offset2 = findViewById(R.id.offset2);

        pinSetting1.addTextChangedListener(calcPinListener);
        pinSetting2.addTextChangedListener(calcPinListener);
        offset1.addTextChangedListener(calcPinListener);
        offset2.addTextChangedListener(calcPinListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
