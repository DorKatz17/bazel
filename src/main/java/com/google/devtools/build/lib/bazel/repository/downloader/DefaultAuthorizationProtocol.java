package com.google.devtools.build.lib.bazel.repository.downloader;

public class DefaultAuthorizationProtocol implements AuthorizationProtocol {

    @Override
    public String getAuthProtocol(String token) {
        return token;
    }
}
