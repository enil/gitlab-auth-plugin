package com.sonymobile.jenkins.plugins.gitlabauth.authorization.GitLabFolderAuthorization

def f = namespace("/lib/form")
def j = namespace("jelly:core")
def st = namespace("jelly:stapler")

def itemPermissionGroup = descriptor.itemPermissionGroup

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
            link(rel: "stylesheet", href: rootURL+"/plugin/gitlab-auth/table.css", type: "text/css")
            script(type: "text/javascript", src: rootURL+"/plugin/gitlab-auth/authorization.matrix.js")
    
            table("class": "center-align global-matrix-authorization-strategy-table", name: "permissionTable", id: "permissionTable") {
                tr {
                    td("class": "pane-header", rowspan: "2") {
                        text("Permission")
                        br()
                        text("Role")
                    }
                    
                    td("class": "pane-header", colspan: itemPermissionGroup.permissions.size()) {
                        text(itemPermissionGroup.title)
                    }
                }
                
                tr {
                    itemPermissionGroup.each { permission ->
                        td {
                            script(type: "text/javascript") {
                                text('addItem("' + permission.id + '")')
                            }
                            
                            if (permission.enabled) {
                                text(permission.name)
                            } else {
                                i { text(permission.name) }
                            }
                        }
                    }
                }
    
                def identities = (instance != null) ? instance.folderPermissionIdentities : descriptor.staticPermissionIdentities
                
                identities.each { identity ->
                    tr(name: identity) {
                        td {
                            text(identity.displayName)
                        }
                        
                        itemPermissionGroup.permissions.each { permission ->
                            td {
                                f.checkbox(name: "[${permission.id}]", checked: instance?.isPermissionSet(identity, permission))
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