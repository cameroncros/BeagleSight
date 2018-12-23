package com.cross.beaglesight;

import android.view.View;

import com.cross.beaglesightlibs.BowConfig;
import com.cross.beaglesightlibs.LocationDescription;
import com.cross.beaglesightlibs.Target;

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

    static Target TestTarget() {
        Target target = new Target();
        target.setId(UUID.randomUUID().toString());
        target.setName(UUID.randomUUID().toString());
        target.setTargetLocation(TestLocation(target.getId()));
        for (int i = 0; i < 5; i++)
        {
            target.getShootLocations().add(TestLocation(target.getId()));
        }
        return target;
    }

    private static LocationDescription TestLocation(String targetId) {
        LocationDescription locationDescription = new LocationDescription();
        locationDescription.setTargetId(targetId);
        locationDescription.setLocationId(UUID.randomUUID().toString());
        locationDescription.setAltitude(1000*Math.random());
        locationDescription.setLatitude(1000*Math.random());
        locationDescription.setLongitude(1000*Math.random());
        locationDescription.setLatlng_accuracy((float) (10*Math.random()));
        locationDescription.setAltitude_accuracy((float) (10*Math.random()));
        return locationDescription;
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
