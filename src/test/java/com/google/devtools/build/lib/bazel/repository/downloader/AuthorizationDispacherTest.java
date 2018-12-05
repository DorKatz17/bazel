package com.google.devtools.build.lib.bazel.repository.downloader;

import org.junit.Test;

import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;

public class AuthorizationDispacherTest {

    AuthorizationDispacher authorizationDispacher = new AuthorizationDispacher();

    @Test
    public void valid_githubToken_isOk() {
        String token = authorizationDispacher.attachAuthorization(
                "github.com", Optional.of(
                        new CredentialsProvider.Credentials(
                                "user", "123")));
        assertThat(token).isEqualTo("token 123");
    }

    @Test
    public void valid_defaultToken_isOk() {
        String token = authorizationDispacher.attachAuthorization(
                "notgithub.com", Optional.of(
                        new CredentialsProvider.Credentials(
                                "user", "123")));
        assertThat(token).isEqualTo("123");
    }

    @Test
    public void noUser_githubHost_returns_emptyToken() {
        String token = authorizationDispacher.attachAuthorization(
                "github.com", Optional.empty());
        assertThat(token).isEqualTo("");
    }

    @Test
    public void noUser_defaultHost_returns_emptyToken() {
        String token = authorizationDispacher.attachAuthorization(
                "nogithub.com", Optional.empty());
        assertThat(token).isEqualTo("");
    }
}