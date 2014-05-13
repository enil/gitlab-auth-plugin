package com.sonymobile.jenkins.plugins.gitlabauth.GitLabAuthorization

def f = namespace("/lib/form")
def j = namespace("jelly:core")

f.block() {
    link(rel: "stylesheet", href: rootURL+"/plugin/gitlab-auth/table.css", type: "text/css")
    script(type: "text/javascript", src: rootURL+"/plugin/gitlab-auth/authorization.matrix.js") 

    def permissionGroups = descriptor.allPermissionGroups

    table("class": "center-align global-matrix-authorization-strategy-table", name: "permissionTable", id: "permissionTable") {
        tr {
            td("class": "pane-header", rowspan: "2") {
                text("Permission")
                br()
                text("Identity")
            }

            permissionGroups.each { permissionGroup, permissions ->
                td("class": "pane-header", colspan: permissions.size()) {
                    text(permissionGroup.title)
                }
            }
        }

        tr {
            permissionGroups.each { permissionGroup, permissions ->
                permissions.each { permission ->
                    td {
                        script(type: "text/javascript") {
                            text('addItem("' + permission.id + '")')
                        }
                        
                        if(!permission.enabled) {
                            i {
                                text(permission.name)
                            }
                        } else {
                            text(permission.name)
                        }
                    }
                }
            }
        }

        descriptor.permissionIdentities.each { identity ->
            tr(name: identity) {
                td {
                    text(identity.displayName)
                }

                permissionGroups.each { permissionGroup, permissions ->
                    permissions.each { permission ->
                        td {
                            f.checkbox(name: "["+permission.id+"]", checked: instance?.isPermissionSet(identity, permission))
                        }
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