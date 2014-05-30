/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Andreas Alanko, Emil Nilsson, Sony Mobile Communications AB.
 * All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.sonymobile.jenkins.plugins.gitlabauth.authorization.GitLabAuthorization

import com.sonymobile.jenkins.plugins.gitlabauth.acl.GitLabGlobalACL


def f = namespace("/lib/form")

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