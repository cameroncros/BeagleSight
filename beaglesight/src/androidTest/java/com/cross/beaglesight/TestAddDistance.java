package com.cross.beaglesight;

import android.app.Instrumentation;
import android.content.Intent;
import android.widget.TextView;

import com.cross.beaglesightlibs.BowConfig;
import com.cross.beaglesightlibs.BowManager;
import com.cross.beaglesightlibs.PositionPair;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.runner.AndroidJUnit4;

import static android.app.Activity.RESULT_OK;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeTextIntoFocusedView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.cross.beaglesight.ShowSight.CONFIG_TAG;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class TestAddDistance {

    @Rule
    public IntentsTestRule<AddDistance> activityTestRule = new IntentsTestRule<>(AddDistance.class, true, false);
    private AddDistance addDistance;
    private BowConfig bowConfig;

    @Before
    public void setUp() {
        Intent intent = new Intent();
        bowConfig = TestUtils.TestBowConfig();
        bowConfig.getPositionArray().add(new PositionPair(10, 10));
        bowConfig.getPositionArray().add(new PositionPair(20, 20));
        bowConfig.getPositionArray().add(new PositionPair(30, 30));
        intent.putExtra(CONFIG_TAG, bowConfig);
        addDistance = activityTestRule.launchActivity(intent);
    }

    @After
    public void testDown() {
        BowManager.getInstance(addDistance).deleteBowConfig(bowConfig);
    }

    @Test
    public void getEstimate() {
        // Fill distance
        onView(withId(R.id.simpleDistance)).perform(click());
        onView(withId(R.id.simpleDistance)).perform(typeTextIntoFocusedView("15"));

        // Check estimated pinSettings are filled
        TextView pinSetting1 = addDistance.findViewById(R.id.pinSetting1);
        TextView pinSetting2 = addDistance.findViewById(R.id.pinSetting2);

        assertNotEquals("NaN", pinSetting1.getText().toString());
        assertNotEquals("NaN", pinSetting2.getText().toString());
        assertNotEquals("", pinSetting1.getText().toString());
        assertNotEquals("", pinSetting2.getText().toString());

        // Add offsets
        onView(withId(R.id.offset1)).perform(click());
        onView(withId(R.id.offset1)).perform(typeTextIntoFocusedView("0"));
        onView(withId(R.id.offset2)).perform(click());
        onView(withId(R.id.offset2)).perform(typeTextIntoFocusedView("1"));

        // Check simplePin is filled
        TextView simplePin = addDistance.findViewById(R.id.simplePin);
        assertEquals(pinSetting1.getText().toString(), simplePin.getText().toString());

        // Save, because why not
        onView(withId(R.id.addDistance)).perform(click());

        validateResult();
    }

    @Test
    public void addDistance() {
        onView(withId(R.id.simpleDistance)).perform(click());
        onView(withId(R.id.simpleDistance)).perform(typeTextIntoFocusedView("15"));

        onView(withId(R.id.simplePin)).perform(click());
        onView(withId(R.id.simplePin)).perform(typeTextIntoFocusedView("15"));

        onView(withId(R.id.addDistance)).perform(click());

        validateResult();
    }

    private void validateResult() {
        Instrumentation.ActivityResult result = activityTestRule.getActivityResult();
        assertEquals(RESULT_OK, result.getResultCode());
        assertTrue(result.getResultData().hasExtra(CONFIG_TAG));
        BowConfig resultBow = result.getResultData().getParcelableExtra(CONFIG_TAG);
        assertEquals(bowConfig.getPositionArray().size() + 1, resultBow.getPositionArray().size());
        assertEquals(15, resultBow.getPositionArray().get(3).getDistance(), 0.1);
        assertEquals(15, resultBow.getPositionArray().get(3).getPosition(), 0.1);
        assertEquals(bowConfig.getId(), resultBow.getPositionArray().get(3).getBowId());
        assertNotNull(resultBow.getPositionArray().get(3).getId());
    }
}