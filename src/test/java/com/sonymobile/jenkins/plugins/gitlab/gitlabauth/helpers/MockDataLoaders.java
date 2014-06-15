package com.sonymobile.jenkins.plugins.gitlab.gitlabauth.helpers;

import com.sonymobile.gitlab.model.FullGitLabUserInfo;
import com.sonymobile.gitlab.model.GitLabGroupInfo;
import com.sonymobile.gitlab.model.GitLabGroupMemberInfo;
import com.sonymobile.gitlab.model.GitLabUserInfo;

import java.util.List;

import static com.sonymobile.gitlab.helpers.JsonFileLoader.jsonFile;

/**
 * Helper methods for loading mock data from JSON files for tests.
 *
 * @author Emil Nilsson
 */
public class MockDataLoaders {
    public MockDataLoaders() { /* empty */ }

    /**
     * Loads a JSON file with a user.
     *
     * @param variant the variant name
     * @return a user
     * @throws Exception if loading the file failed
     */
    public static GitLabUserInfo loadUser(String variant) throws Exception {
        return jsonFile("api/v3/users/1")
                .withVariant(variant)
                .withType(FullGitLabUserInfo.class)
                .loadAsObject();
    }

    /**
     * Load a JSON file with a normal user.
     *
     * @see #loadUser(String)
     */
    public static GitLabUserInfo loadUser() throws Exception {
        return loadUser(null);
    }

    /**
     * Load a JSON file with an admin user.
     *
     * @see #loadUser(String)
     */
    public static GitLabUserInfo loadAdminUser() throws Exception {
        return loadUser("admin");
    }

    /**
     * Loads a JSON file with group members.
     *
     * @param variant the variant name
     * @param groupId the group ID
     * @return a list of group members
     * @throws Exception if loading the file failed
     */
    public static List<GitLabGroupMemberInfo> loadGroupMembers(int groupId, String variant) throws Exception {
        return jsonFile("api/v3/groups/1/members")
                .withVariant(variant)
                .withType(GitLabGroupMemberInfo.class)
                .andParameters(groupId)
                .loadAsArray();
    }

    /**
     * Loads a JSON file with group members.
     *
     * @see #loadGroupMembers(int, String)
     */
    public static List<GitLabGroupMemberInfo> loadGroupMembers(int groupId) throws Exception {
        return loadGroupMembers(groupId, null);
    }

    /**
     * Loads a JSON file with all groups.
     *
     * @param variant the variant name
     * @return a list of groups
     * @throws Exception if loading the file failed
     */
    public static List<GitLabGroupInfo> loadGroups(String variant) throws Exception {
        return jsonFile("api/v3/groups")
                .withVariant(variant)
                .withType(GitLabGroupInfo.class)
                .loadAsArray();
    }

    /**
     * Loads the JSON file with all groups.
     *
     * @see #loadGroups(String)
     */
    public static List<GitLabGroupInfo> loadGroups() throws Exception {
        return loadGroups(null);
    }
}
