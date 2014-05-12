package com.sonymobile.jenkins.plugins.gitlabauth.GitLabFolderAuthorization

def f = namespace("/lib/form")
def j = namespace("jelly:core")
def st = namespace("jelly:stapler")

f.section(title: "GitLab Folder Authorization") {
    f.block() {
        link(rel: "stylesheet", href: rootURL+"/plugin/gitlab-auth/table.css", type: "text/css")
        
        def itemPermissionGroup = descriptor.itemPermissionGroup
        
        table("class": "center-align global-matrix-authorization-strategy-table", name: "permissionTable") {
            tr {
                td("class": "pane-header", rowspan: "2") {
                    text("Permission")
                    br()
                    text("Role")
                }
                
                td("class": "pane-header", colspan: itemPermissionGroup.getPermissions().size()) {
                    text(itemPermissionGroup.title)
                }
            }
            
            tr {
                for (p in itemPermissionGroup.getPermissions()) {
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
            
            def identities;
            
            if (instance != null) {
                identities = instance.folderPermissionIdentities
            } else {
                identities = descriptor.staticPermissionIdentities
            }
            
            for (identity in identities) {
                tr(name: identity) {
                    td {
                        text(identity.displayName)
                    }
                    
                    for (p in itemPermissionGroup.getPermissions()) {
                        td {
                            f.checkbox(name: "["+p.id+"]", checked: (instance != null) ? instance.isPermissionSet(identity, p) : false)
                        }
                    }
                }
            }
        }
    }
}

