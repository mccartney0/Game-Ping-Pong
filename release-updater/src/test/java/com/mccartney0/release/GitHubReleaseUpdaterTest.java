package com.mccartney0.release;

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class GitHubReleaseUpdaterTest {
    @Test
    public void comparesVersionsIgnoringVPrefix() {
        Assert.assertTrue(GitHubReleaseUpdater.compareVersions("v1.2.0", "1.1.9") > 0);
        Assert.assertEquals(0, GitHubReleaseUpdater.compareVersions("1.0", "v1.0.0"));
        Assert.assertTrue(GitHubReleaseUpdater.compareVersions("1.0.0", "1.0.1") < 0);
    }

    @Test
    public void verifiesSha256WhenProvided() {
        byte[] content = "foldcut-release".getBytes(StandardCharsets.UTF_8);
        Assert.assertTrue(GitHubReleaseUpdater.sha256Matches(
                content,
                "b6bc90fda583f21275951e8b9b6bbc859f22e1577cb900aeef5cf1328828308c"));
        Assert.assertFalse(GitHubReleaseUpdater.sha256Matches(content, "invalid"));
        Assert.assertTrue(GitHubReleaseUpdater.sha256Matches(content, null));
    }
}
