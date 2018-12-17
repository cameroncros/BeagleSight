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
import static androidx.test.espresso.action.ViewActions.typeTextIntoFocusedView;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.anyIntent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.cross.beaglesight.ShowSight.CONFIG_TAG;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class TestAddSight {
    private BowManager bm = null;

    @Rule
    public IntentsTestRule<AddSight> activityTestRule = new IntentsTestRule<>(AddSight.class, true, false);
    private AddSight addSight;
    private BowConfig bowConfig;

    @Before
    public void setUp() {
        Intent intent = new Intent();
        bowConfig = TestUtils.TestBowConfig();
        addSight = activityTestRule.launchActivity(intent);

        bm = BowManager.getInstance(addSight.getApplicationContext());
    }

    @Test
    public void addPosition() {
        int count = bm.getAllBowConfigsWithPositions().size();
        onView(withId(R.id.name)).perform(click());
        onView(withId(R.id.name)).perform(typeTextIntoFocusedView("Name"));
        onView(withId(R.id.description)).perform(click());
        onView(withId(R.id.description)).perform(typeTextIntoFocusedView("Description"));
        onView(withId(R.id.add_button)).perform(click());

        assertEquals(count + 1, bm.getAllBowConfigsWithPositions().size());
    }
}