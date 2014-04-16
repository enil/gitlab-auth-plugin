package com.sonymobile.jenkins.plugins.gitlabauth.GitLabAuthorization

def f = namespace("/lib/form")
def j = namespace("jelly:core")

f.block() {
    link(rel: "stylesheet", href: "/plugin/gitlab-auth/table.css", type: "text/css")
    j.set(var: "permissionGroups", value: descriptor.allPermissionGroups)
    
    table("class": "center-align global-matrix-authorization-strategy-table", name: "permissionTable") {
        tr {
            td("class": "pane-header", rowspan: "2") {
                text("Permission")
                br()
                text("Role")
            }
            
            j.forEach(var: "pg", items: permissionGroups.keySet()) {
                td("class": "pane-header", colspan: permissionGroups.get(pg).size()) {
                    text(pg.title)
                }
            }
        }
        
        tr {
            j.forEach(var: "pg", items: permissionGroups.keySet()) {
                j.forEach(var: "p", items: permissionGroups.get(pg)) {
                    td {
                        text(p.name)
                    }
                }
            }
        }
        
        j.set(var: "roles", value: descriptor.allRoles)
        
        j.forEach(var: "role", items: roles) {
            tr(name: role) {
                td {
                    text(role)
                }
                
                j.forEach(var: "pg", items: permissionGroups.keySet()) {
                    j.forEach(var: "p", items: permissionGroups.get(pg)) {
                        td {
                            f.checkbox(name: p.id, checked: instance.isPermissionSet(role, p))
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