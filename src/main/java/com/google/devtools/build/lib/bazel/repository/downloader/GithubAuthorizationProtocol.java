package com.google.devtools.build.lib.bazel.repository.downloader;

public class GithubAuthorizationProtocol implements AuthorizationProtocol {

    @Override
    public String getAuthProtocol(String token) {
        return token.isEmpty() ? "" : "token "+ token;
    }
}
