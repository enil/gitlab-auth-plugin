package com.sonymobile.jenkins.plugins.gitlabauth.authorization.GitLabFolderAuthorization

def f = namespace("/lib/form")
def j = namespace("jelly:core")
def st = namespace("jelly:stapler")

def acl = instance?.ACL

link(rel: "stylesheet", href: rootURL+"/plugin/gitlab-auth/table.css", type: "text/css")
script(type: "text/javascript", src: rootURL+"/plugin/gitlab-auth/authorization.matrix.js")

if (instance?.groupId) {
    f.section(title: "GitLab Group") {
        f.block {
            div {
                b { text "Group ID: " }
                text instance?.groupId
            }
            div {
                b { text "Group name: " }
                text instance?.groupName
            }
            div {
                b { text "Group path: " }
                text instance?.groupPath
            }
        }
    }
    
    f.section(title: "GitLab Folder Authorization") {
        f.block {
            table("class": "center-align global-matrix-authorization-strategy-table", name: "permissionTable", id: "permissionTable") {
                tr {
                    td("class": "pane-header", rowspan: "2") {
                        text("Permission")
                        br()
                        text("Role")
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
    
                instance.folderPermissionIdentities.each { identity ->
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
        }
    }
}