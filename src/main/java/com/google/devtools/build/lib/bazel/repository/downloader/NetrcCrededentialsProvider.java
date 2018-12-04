package com.google.devtools.build.lib.bazel.repository.downloader;

import java.util.Optional;

public class NetrcCrededentialsProvider implements CredentialsProvider {

    @Override
    public Optional<Credentials> getCredentials(String host) {
        return Optional.empty();
    }

}
