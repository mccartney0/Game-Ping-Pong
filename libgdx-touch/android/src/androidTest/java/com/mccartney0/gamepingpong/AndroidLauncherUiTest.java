package com.mccartney0.gamepingpong;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;


import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.ads.AdView;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AndroidLauncherUiTest {

    @Rule
    public ActivityScenarioRule<AndroidLauncher> activityRule =
            new ActivityScenarioRule<>(AndroidLauncher.class);

    @Test
    public void launcherShowsGameSurface() {
        onView(isRoot()).check(matches(isDisplayed()));
    }

    @Test
    public void bannerStartsHiddenDuringGameplay() {
        onView(isAssignableFrom(AdView.class))
                .check(matches(withEffectiveVisibility(
                        androidx.test.espresso.matcher.ViewMatchers.Visibility.GONE)));
    }
}
