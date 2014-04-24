package com.sonymobile.jenkins.plugins.gitlabauth.GitLabAuthorization

def f = namespace("/lib/form")
def j = namespace("jelly:core")

f.block() {
    link(rel: "stylesheet", href: rootURL+"/plugin/gitlab-auth/table.css", type: "text/css")

    def permissionGroups = descriptor.allPermissionGroups

    table("class": "center-align global-matrix-authorization-strategy-table", name: "permissionTable") {
        tr {
            td("class": "pane-header", rowspan: "2") {
                text("Permission")
                br()
                text("Role")
            }

            for (pg in permissionGroups.keySet()) {
                td("class": "pane-header", colspan: permissionGroups.get(pg).size()) {
                    text(pg.title)
                }
            }
        }

        tr {
            for (pg in permissionGroups.keySet()) {
                for (p in permissionGroups.get(pg)) {
                    td {
                        if(!p.enabled) {
                            i {
                                text(p.name)
                            }
                        } else {
                            text(p.name)
                        }
                    }
                }
            }
        }

        for (role in descriptor.allRoles) {
            tr(name: role) {
                td {
                    text(role)
                }

                for (pg in permissionGroups.keySet()) {
                    for (p in permissionGroups.get(pg)) {
                        td {
                            f.checkbox(name: "["+p.id+"]", checked: (instance != null) ? instance.isPermissionSet(role, p) : false)
                        }
                    }
                }
            }
        }
    }

    f.entry(title: "Admin usernames", description: "Usernames of GitLab users who will be granted admin rights on the Jenkins server. Usernames should be seperated by commas.") {
        f.checkbox(field: "useGitLabAdmins", title: "Check if all GitLab admins also should be Jenkins admins.")
        f.textbox(field: "adminUsernames")
    }
}