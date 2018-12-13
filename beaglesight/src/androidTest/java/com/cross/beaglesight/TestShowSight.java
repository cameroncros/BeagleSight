package com.cross.beaglesight;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;

import com.cross.beaglesightlibs.BowConfig;
import com.cross.beaglesightlibs.BowManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.runner.AndroidJUnit4;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.anyIntent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.cross.beaglesight.ShowSight.CONFIG_TAG;
import static org.hamcrest.Matchers.allOf;

@RunWith(AndroidJUnit4.class)
public class TestShowSight {
    private BowManager bm = null;

    @Rule
    public IntentsTestRule<ShowSight> activityTestRule = new IntentsTestRule<>(ShowSight.class, true, false);
    private ShowSight showSight;
    private BowConfig bowConfig;

    @Before
    public void setUp() {
        Intent intent = new Intent();
        bowConfig = TestUtils.TestBowConfig();
        intent.putExtra(CONFIG_TAG, bowConfig);
        showSight = activityTestRule.launchActivity(intent);

        bm = BowManager.getInstance(showSight.getApplicationContext());
    }

    @Test
    public void addPosition() {
        Intent intent = new Intent();
        BowConfig returnConfig = TestUtils.TestBowConfig();
        intent.putExtra(CONFIG_TAG, returnConfig);
        Instrumentation.ActivityResult intentResult = new Instrumentation.ActivityResult(Activity.RESULT_OK,intent);

        intending(anyIntent()).respondWith(intentResult);

        onView(withId(R.id.fabAddPosition)).perform(click());

        intended(allOf(hasComponent(AddDistance.class.getName()),
                       hasExtra(CONFIG_TAG, bowConfig)));
    }
}