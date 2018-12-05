package com.google.devtools.build.lib.bazel.repository.downloader;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AuthorizationDispacher {

    private static final Map<String, AuthorizationProtocol> HOST_TO_PROTOCOL = new HashMap<>();

    public AuthorizationDispacher() {
        HOST_TO_PROTOCOL.put("github.com", new GithubAuthorizationProtocol());
    }

    public String attachAuthorization(String host, Optional<CredentialsProvider.Credentials> credentials){
        return HOST_TO_PROTOCOL.getOrDefault(host, new DefaultAuthorizationProtocol()).getAuthProtocol(extractCredentialsPassword(credentials));
    }

    private String extractCredentialsPassword(Optional<CredentialsProvider.Credentials> credentials){
        if(credentials.isPresent())
            return credentials.get().getPassword().orElse("");
        return "";
    }
}
