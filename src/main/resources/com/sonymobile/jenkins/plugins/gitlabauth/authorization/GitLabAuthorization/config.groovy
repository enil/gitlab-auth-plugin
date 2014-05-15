package com.sonymobile.jenkins.plugins.gitlabauth.GitLabAuthorization

import com.sonymobile.jenkins.plugins.gitlabauth.acl.GitLabGlobalACL


def f = namespace("/lib/form")
def j = namespace("jelly:core")

// use default permissions
def acl = instance?.rootACL ?: new GitLabGlobalACL()

link(rel: "stylesheet", href: rootURL+"/plugin/gitlab-auth/table.css", type: "text/css")
script(type: "text/javascript", src: rootURL+"/plugin/gitlab-auth/authorization.matrix.js")

f.block() {
    table("class": "center-align global-matrix-authorization-strategy-table", name: "permissionTable", id: "permissionTable") {
        tr {
            td("class": "pane-header", rowspan: "2") {
                text("Permission")
                br()
                text("Identity")
            }

            acl.applicablePermissionGroups.each { permissionGroup ->
                td("class": "pane-header", colspan: permissionGroup.size()) {
                    text(permissionGroup.title)
                }
            }
        }

        tr {
            acl.applicablePermissionGroups*.each { permission ->
                td {
                    script(type: "text/javascript") {
                        text('addItem("' + permission.id + '")')
                    }

                    if (!permission.enabled) {
                        i { text(permission.name) }
                    } else {
                        text(permission.name)
                    }
                }
            }
        }

        descriptor.permissionIdentities.each { identity ->
            tr(name: identity) {
                td {
                    text(identity.displayName)
                }

                acl.applicablePermissionGroups*.each { permission ->
                    td {
                        f.checkbox(name: "[${permission.id}]", checked: acl?.isPermissionSet(identity, permission))
                    }
                }
            }
        }
    }

    f.entry(title: "Add group/user:") {
        input(type: "text", id: "addUserGroupText", style: "width: 200px")
        input(type: "button", id: "addGroupRowButton", onclick: "addGroupRow()", value: "Add group")
        input(type: "button", id: "addUserRowButton", onclick: "addUserRow()", value: "Add user")
    }

    f.entry(title: "Jenkins admins:") {
        f.checkbox(field: "useGitLabAdmins", title: "Make all GitLab admins Jenkins admins.")
    }

    f.entry(title: "Admin usernames:", description: "GitLab usernames who will be granted admin rights. Should be separated by a comma.") {
        f.textbox(field: "adminUsernames")
    }

    f.entry(title: "Admin groups:", description: "GitLab groups who will be granted admin rights. Should be separated by a comma.") {
        f.textbox(field: "adminGroups")
    }
}