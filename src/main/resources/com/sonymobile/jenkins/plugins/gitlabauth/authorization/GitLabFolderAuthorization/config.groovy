package com.sonymobile.jenkins.plugins.gitlabauth.authorization.GitLabFolderAuthorization

def f = namespace("/lib/form")

def itemPermissionGroup = descriptor.itemPermissionGroup

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
        link rel: "stylesheet", href: rootURL+"/plugin/gitlab-auth/table.css", type: "text/css"

        table("class": "center-align global-matrix-authorization-strategy-table", name: "permissionTable") {
            tr {
                td("class": "pane-header", rowspan: "2") {
                    text "Permission"
                    br()
                    text "Role"
                }
                
                td("class": "pane-header", colspan: itemPermissionGroup.permissions.size()) {
                    text itemPermissionGroup.title
                }
            }
            
            tr {
                itemPermissionGroup.each { permission ->
                    td {
                        if (permission.enabled) {
                            text permission.name
                        } else {
                            i { text permission.name }
                        }
                    }
                }
            }

            descriptor.allRoles.each { role ->
                tr(name: role) {
                    td {
                        text role
                    }
                    
                    itemPermissionGroup.permissions.each { permission ->
                        td {
                            f.checkbox name: "[${permission.id}]", checked: instance?.isPermissionSet(role, permission)
                        }
                    }
                }
            }
        }
    }
}

