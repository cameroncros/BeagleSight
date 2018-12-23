package com.cross.beaglesight;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.view.View;

import com.cross.beaglesightlibs.Target;
import com.cross.beaglesightlibs.TargetManager;
import com.cross.beaglesightlibs.XmlParser;

import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.runner.AndroidJUnit4;

import static android.app.Activity.RESULT_OK;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.anyIntent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasType;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.cross.beaglesight.EditTarget.TARGET_KEY;
import static com.cross.beaglesight.TargetMap.FILE_SELECT_CODE;
import static com.cross.beaglesight.TestUtils.TestTarget;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

@RunWith(AndroidJUnit4.class)
public class TestTargetMap {

    @Rule
    public IntentsTestRule<TargetMap> activityTestRule = new IntentsTestRule<>(TargetMap.class);

    @Test
    public void showARMode() {
        Instrumentation.ActivityResult intentResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, new Intent());

        intending(anyIntent()).respondWith(intentResult);

        onView(withId(R.id.arMode)).perform(click());

        intended(hasComponent(TargetAR.class.getName()));
    }

    @Test
    public void addTarget() {
        activityTestRule.getActivity();

        Intent intent = new Intent();
        intent.putExtra(TARGET_KEY, TestTarget());

        Instrumentation.ActivityResult intentResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, intent);

        intending(anyIntent()).respondWith(intentResult);

        onView(withId(R.id.action_add)).perform(click());

        intended(hasComponent(EditTarget.class.getName()));
    }

    @Test
    public void exportTargets() {
        activityTestRule.getActivity();
        intending(anyIntent()).respondWith(new Instrumentation.ActivityResult(RESULT_OK, new Intent()));

        onView(withContentDescription("More options")).perform(click());
        onView(allOf(withId(R.id.title), withText(R.string.action_export))).perform(click());
        // To hard to validate the export intent, just forget about it.
    }

    @Test
    public void importTargets() throws IOException, InterruptedException {
        final TargetMap targetMap = activityTestRule.getActivity();

        List<Target> targetList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            targetList.add(TestTarget());
        }

        File file = File.createTempFile("test", "data");
        FileOutputStream fos = new FileOutputStream(file);
        XmlParser.serialiseTargets(fos, targetList);

        final Intent intent = new Intent();
        intent.setData(Uri.fromFile(file));

        targetMap.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                targetMap.onActivityResult(FILE_SELECT_CODE, RESULT_OK, intent);
            }
        });
        Thread.sleep(1000); // Sleep to allow async task to complete.

        TargetManager tm = TargetManager.getInstance(targetMap);
        for (Target target : targetList) {
            assertNotNull(tm.getTarget(target.getId()));
        }
    }

    @Test
    public void getPublicTargets() {
        activityTestRule.getActivity();

        intending(anyIntent()).respondWith(new Instrumentation.ActivityResult(RESULT_OK, new Intent()));
        onView(withContentDescription("More options")).perform(click());
        onView(allOf(withId(R.id.title), withText(R.string.action_update))).perform(click());
    }
}