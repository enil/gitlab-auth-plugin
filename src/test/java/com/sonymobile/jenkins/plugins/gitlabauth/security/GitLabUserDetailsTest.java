package com.sonymobile.jenkins.plugins.gitlabauth.security;

import com.sonymobile.gitlab.helpers.JsonFileLoader;
import com.sonymobile.gitlab.model.GitLabSessionInfo;
import com.sonymobile.jenkins.plugins.gitlabauth.security.GitLabUserDetails;

import org.junit.Before;
import org.junit.Test;

import static com.sonymobile.gitlab.helpers.JsonFileLoader.jsonFile;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests getting attributes from {@link GitLabUserDetails}.
 *
 * @author Emil Nilsson
 */
public class GitLabUserDetailsTest {
    /** A normal GitLab user. */
    private GitLabUserDetails normalUser;

    /** An administrator user. */
    private GitLabUserDetails adminUser;

    /** A blocked user. */
    private GitLabUserDetails blockedUser;

    /**
     * Creates the GitLab normalUser to test.
     */
    @Before
    public void setUp() throws Exception {
        JsonFileLoader.ObjectLoader<GitLabSessionInfo> sessionFile = jsonFile("api/v3/session")
                .withType(GitLabSessionInfo.class);

        normalUser = new GitLabUserDetails(sessionFile.loadAsObject());
        adminUser = new GitLabUserDetails((sessionFile.withVariant("admin").loadAsObject()));
        blockedUser = new GitLabUserDetails((sessionFile.withVariant("blocked").loadAsObject()));
    }

    @Test
    public void getUsername() {
        assertThat("username", is(normalUser.getUsername()));
    }

    @Test
    public void getPrivateToken() {
        assertThat("0123456789abcdef", is(normalUser.getPrivateToken()));
    }

    @Test
    public void getEmail() {
        assertThat("user@example.com", is(normalUser.getEmail()));
    }

    @Test
    public void getId() {
        assertThat(2, is(normalUser.getId()));
    }

    @Test
    public void isEnabled() {
        assertThat(normalUser.isEnabled(), is(true));
        assertThat(blockedUser.isEnabled(), is(false));
    }
}
