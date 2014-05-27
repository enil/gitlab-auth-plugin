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

package com.sonymobile.jenkins.plugins.gitlabauth.configuration.GitLabManageFolderAction

def l = namespace("/lib/layout")
def f = namespace("/lib/form")
def st = namespace("jelly:stapler")

def nonExistingFolders = my.nonExistingFolders

l.layout(title: "Create a new GitLab Folder", permission: app.READ, norefresh: "true") {
    st.include(page: "sidepanel.groovy")
    l.main_panel() {
        h1 {
            text("Create a new GitLab Folder");
        }
        
        if (nonExistingFolders.isEmpty()) {
            text("There are no folders that can be created.")
        } else {
            form(method: "POST", name: "createFolders", action: "createFolders") {
                table(id: "Test", "class": "sortable pane bigtable") {
                    tr(style: "text-align:left") {
                        th(width: "10px")
                        
                        th(width: "400px") {
                            text("Group name")
                        }
                        th(width: "100px") {
                            text("Group role")
                        }
                        th {
                            text("Group path")
                        }
                        th(width: "400px") {
                            text("Group URL")
                        }
                    }
                    nonExistingFolders.each { group ->
                        tr {
                            td {
                                input(type: "checkbox", name: group.groupId)
                            }
                            td {
                                img(src: imagesURL+"/32x32/folder.png", width: "32px", height: "32px", style: "padding-right: 10px")
                                text(group.groupName)
                            }
                            td {
                                text(my.getGitLabAccessLevel(group.groupId))
                            }
                            td {
                                text(group.groupPath)
                            }
                            td {
                                a(href: group.groupUrl, target: "_blank") {
                                    text(group.groupUrl)
                                }
                            }
                        }
                    }
                }
                div("class": "bottom-sticker-edge")
                div("class": "bottom-sticker-inner") {
                    input(type: "submit", value: "Create marked folders")
                }
            }
        }
    }
}