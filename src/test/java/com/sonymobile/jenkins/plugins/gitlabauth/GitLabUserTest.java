package com.sonymobile.jenkins.plugins.gitlabauth;

import com.sonymobile.gitlab.model.GitLabSessionInfo;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.apache.commons.io.FileUtils.readFileToString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


/**
 * Tests getting attributes from {@link GitLabUser}.
 *
 * @author Emil Nilsson
 */
public class GitLabUserTest {
    /** The GitLab user. */
    private GitLabUser user;

    /**
     * Creates the GitLab user to test.
     *
     * @throws IOException if the session file couldn't be read
     */
    @Before
    public void setUp() throws IOException {
        String absolutePath = GitLabUserTest.class.getResource("/__files/api/v3/session").getFile();

        user = new GitLabUser(new GitLabSessionInfo(new JSONObject(readFileToString(new File(absolutePath)))));
    }

    @Test
    public void getUsername() {
        assertThat("username", is(user.getUsername()));
    }

    @Test
    public void getPrivateToken() {
        assertThat("0123456789abcdef", is(user.getPrivateToken()));
    }

    @Test
    public void getEmail() {
        assertThat("user@example.com", is(user.getEmail()));
    }

    @Test
    public void getId() {
        assertThat(2, is(user.getId()));
    }

    @Test
    public void isEnabled() {
        assertThat(user.isEnabled(), is(true));
    }
}
