package com.cross.beaglesight;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;

import com.cross.beaglesightlibs.BowConfig;
import com.cross.beaglesightlibs.BowManager;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.List;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.runner.AndroidJUnit4;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.anyIntent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.cross.beaglesight.ShowSight.CONFIG_TAG;
import static com.cross.beaglesight.TestUtils.withIndex;
import static org.hamcrest.Matchers.allOf;

@RunWith(AndroidJUnit4.class)
public class TestSightList {
    private BowManager bm = null;

    @Rule
    public IntentsTestRule<SightList> activityTestRule = new IntentsTestRule<>(SightList.class);
    private SightList sightList;

    @Before
    public void setUp() {
        sightList = activityTestRule.getActivity();
        bm = BowManager.getInstance(sightList);

        while(bm.getAllBowConfigsWithPositions().size() != 0)
        {
            bm.deleteBowConfig(bm.getAllBowConfigsWithPositions().get(0));
        }

        for (int i = 0; i < 5; i++) {
            BowConfig bc = new BowConfig();
            bc.setId("Fake bow" + i);
            bc.setName("Fake bow" + i);
            bc.setDescription("This is a testing bow, if you see this in a live app, i fucked up.");
            bm.addBowConfig(bc);
        }
    }

    @Test
    public void multiSelectTest() {
        sightList = activityTestRule.getActivity();

        // Store first 3 entries
        List<BowConfig> configs = bm.getAllBowConfigsWithPositions();
        BowConfig bc0 = configs.get(0);
        BowConfig bc1 = configs.get(1);
        BowConfig bc2 = configs.get(2);

        // Enter multiselect mode
        onView(withIndex(withId(R.id.itemName), 0)).perform(longClick());

        // Select next 2 entries
        onView(withIndex(withId(R.id.itemName), 1)).perform(longClick());
        onView(withIndex(withId(R.id.itemName), 2)).perform(click());

        // Check selected configs.
        Assert.assertTrue(sightList.selectedBowConfigs.contains(bc0));
        Assert.assertTrue(sightList.selectedBowConfigs.contains(bc1));
        Assert.assertTrue(sightList.selectedBowConfigs.contains(bc2));

        // Delete
        onView(withId(R.id.action_delete)).perform(click());

        // Ensure first 3 were deleted.
        configs = bm.getAllBowConfigsWithPositions();
        Assert.assertFalse(configs.contains(bc0));
        Assert.assertFalse(configs.contains(bc1));
        Assert.assertFalse(configs.contains(bc2));
    }

    @Test
    public void selectBow() {
        sightList = activityTestRule.getActivity();
        Intent intent = new Intent();
        BowConfig bc = TestUtils.TestBowConfig();
        intent.putExtra(CONFIG_TAG, bc);
        Instrumentation.ActivityResult intentResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, intent);

        intending(anyIntent()).respondWith(intentResult);

        List<BowConfig> configs = bm.getAllBowConfigsWithPositions();
        BowConfig firstConfig = configs.get(0);

        onView(withIndex(withId(R.id.itemName), 0)).perform(click());

        intended(allOf(hasComponent(ShowSight.class.getName()), hasExtra(CONFIG_TAG, firstConfig)));
    }

    @Test
    public void addBow() {
        sightList = activityTestRule.getActivity();
        Intent intent = new Intent();
        BowConfig bc = TestUtils.TestBowConfig();
        intent.putExtra(CONFIG_TAG, bc);
        Instrumentation.ActivityResult intentResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, intent);

        intending(anyIntent()).respondWith(intentResult);

        onView(withIndex(withId(R.id.fabAddSight), 0)).perform(click());

        intended(hasComponent(ShowSight.class.getName()));
    }
}