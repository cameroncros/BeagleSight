package com.cross.beaglesight;

import android.view.View;

import com.cross.beaglesightlibs.BowConfig;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.UUID;

public class TestUtils {
    static BowConfig TestBowConfig() {
        BowConfig bc = new BowConfig();
        bc.setId(UUID.randomUUID().toString());
        bc.setName(UUID.randomUUID().toString());
        bc.setDescription(UUID.randomUUID().toString());
        return bc;
    }

    static Matcher<View> withIndex(final Matcher<View> matcher, final int index) {
        return new TypeSafeMatcher<View>() {
            int currentIndex = 0;

            @Override
            public void describeTo(Description description) {
                description.appendText("with index:");
                description.appendValue(index);
                description.appendText(" ");
                matcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                return matcher.matches(view) && currentIndex++ == index;
            }
        };
    }
}
