package com.sonymobile.jenkins.plugins.gitlab.gitlabauth.GitLabUserProperty

def f = namespace("/lib/form")

p {
    b { u { text("GitLab User Information") } }
    br()
    text("User ID: " + my.userId)
    br()
    text("Username: " + my.username)
    br()
    text("Name: " + my.fullname)
    br()
    text("E-mail: " + my.email)
}