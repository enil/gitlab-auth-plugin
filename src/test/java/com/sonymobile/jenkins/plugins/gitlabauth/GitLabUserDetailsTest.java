package com.sonymobile.jenkins.plugins.gitlabauth;

import com.sonymobile.gitlab.model.GitLabSessionInfo;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.sonymobile.jenkins.plugins.gitlabauth.helpers.FileHelpers.loadJsonObject;
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
     *
     * @throws IOException if the session file couldn't be read
     */
    @Before
    public void setUp() throws IOException {
        normalUser = new GitLabUserDetails(new GitLabSessionInfo(loadJsonObject("/api/v3/session")));
        adminUser = new GitLabUserDetails(new GitLabSessionInfo(loadJsonObject("/api/v3/session", "admin")));
        blockedUser = new GitLabUserDetails(new GitLabSessionInfo(loadJsonObject("/api/v3/session", "blocked")));
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
