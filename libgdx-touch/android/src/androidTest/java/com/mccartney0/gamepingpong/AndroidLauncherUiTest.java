package com.mccartney0.gamepingpong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.view.View;
import android.view.ViewGroup;

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
    public void launcherCreatesGameSurface() {
        activityRule.getScenario().onActivity(activity -> {
            assertNotNull(activity.getGame());
            assertNotNull(activity.getWindow().getDecorView());
        });
    }

    @Test
    public void bannerStartsHiddenDuringGameplay() {
        activityRule.getScenario().onActivity(activity -> {
            AdView banner = findAdView(activity.getWindow().getDecorView());
            assertNotNull("banner deve estar anexado ao launcher", banner);
            assertEquals(View.GONE, banner.getVisibility());
        });
    }

    private static AdView findAdView(View view) {
        if (view instanceof AdView) {
            return (AdView) view;
        }
        if (!(view instanceof ViewGroup)) {
            return null;
        }
        ViewGroup group = (ViewGroup) view;
        for (int index = 0; index < group.getChildCount(); index++) {
            AdView result = findAdView(group.getChildAt(index));
            if (result != null) {
                return result;
            }
        }
        return null;
    }
}
