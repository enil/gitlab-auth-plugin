# gitlab-auth-plugin

A Jenkins which plugin provides authentication for [GitLab][] users.

## Usage

1. Make sure the [GitLab API Plugin][] is configured.
2. Select and configure the desired folder synchronization method in the section *Folder creation strategy* under
*Configure System*.
3. Configure Jenkins security in *Security*:
    1. Enable security in Jenkins by checking the check box.
    2. Chose *GitLab Authentication* as *Security Realm*.
    3. Chose *GitLab Authorization Strategy* as *Authorization* and configure the global permissions.
4. Log in using your GitLab credentials.

## License

The MIT License (MIT)

Copyright (c) 2014 Andreas Alanko, Emil Nilsson, Sony Mobile Communications AB.
All rights reserved.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.

[GitLab]:               https://www.gitlab.com/
[GitLab API Plugin]:    https://github.com/jenkinsci/gitlab-api-plugin