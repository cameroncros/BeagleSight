package com.cross.beaglesight;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.cross.beaglesightlibs.BowConfig;
import com.cross.beaglesightlibs.BowManager;
import com.cross.beaglesightlibs.XmlParser;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.List;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.runner.AndroidJUnit4;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
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
import static com.cross.beaglesight.SightList.FILE_SELECT_CODE;
import static com.cross.beaglesight.TestUtils.withIndex;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.isOneOf;
import static org.junit.Assert.assertEquals;

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
    public void bowListRecyclerViewTest() {
        sightList = activityTestRule.getActivity();

        // Store first 3 entries
        List<BowConfig> configs = bm.getAllBowConfigsWithPositions();
        BowConfig bc0 = configs.get(0);
        BowConfig bc1 = configs.get(1);
        BowConfig bc2 = configs.get(2);

        // Test single select.
        Intent intent = new Intent();
        BowConfig bc = TestUtils.TestBowConfig();
        intent.putExtra(CONFIG_TAG, bc);
        Instrumentation.ActivityResult intentResult = new Instrumentation.ActivityResult(RESULT_OK, intent);

        intending(anyIntent()).respondWith(intentResult);

        onView(withIndex(withId(R.id.itemName), 0)).perform(click());

        intended(allOf(hasComponent(ShowSight.class.getName()), hasExtra(CONFIG_TAG, bc0)));

        // Test multi-select
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
    public void exportBow()
    {
        intending(anyIntent()).respondWith(new Instrumentation.ActivityResult(RESULT_CANCELED, new Intent()));
        onView(withIndex(withId(R.id.itemName), 0)).perform(longClick());
        onView(withId(R.id.action_export)).perform(click());
        // To hard to validate the export intent, just forget about it.
    }

    @Test
    public void importBows() throws IOException, InterruptedException {
        sightList = activityTestRule.getActivity();

        Intent intent = new Intent();
        BowConfig bc = TestUtils.TestBowConfig();
        File file = File.createTempFile("test", "data");
        FileOutputStream fos = new FileOutputStream(file);
        XmlParser.serialiseSingleBowConfig(fos, bc);
        intent.setData(Uri.fromFile(file));

        sightList.onActivityResult(FILE_SELECT_CODE, RESULT_OK, intent);
        Thread.sleep(1000); // Sleep to allow async task to complete.

        BowConfig resultBowConfig = bm.getBowConfig(bc.getId());
        assertEquals(bc, resultBowConfig);
    }

    @Test
    public void addBow() {
        sightList = activityTestRule.getActivity();
        Intent intent = new Intent();
        BowConfig bc = TestUtils.TestBowConfig();
        intent.putExtra(CONFIG_TAG, bc);
        Instrumentation.ActivityResult intentResult = new Instrumentation.ActivityResult(RESULT_OK, intent);

        intending(anyIntent()).respondWith(intentResult);

        onView(withIndex(withId(R.id.fabAddSight), 0)).perform(click());

        intended(hasComponent(ShowSight.class.getName()));
    }
}