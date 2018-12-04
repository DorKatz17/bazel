package com.google.devtools.build.lib.bazel.repository.downloader;

import java.util.Optional;

public interface CredentialsProvider {

    Optional<Credentials> getCredentials(String host);

    class Credentials {

        private String user = null;

        private String password = null;

        Credentials() {}

        Credentials(String user, String password) {
            this.user = user;
            this.password = password;
        }

        public Optional<String> getUser() {
            return Optional.ofNullable(user);
        }

        public Optional<String> getPassword() {
            return Optional.ofNullable(password);
        }
    }
}
