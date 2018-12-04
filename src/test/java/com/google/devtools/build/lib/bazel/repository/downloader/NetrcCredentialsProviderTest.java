package com.google.devtools.build.lib.bazel.repository.downloader;

import com.google.devtools.build.lib.bazel.repository.downloader.CredentialsProvider.Credentials;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@RunWith(JUnit4.class)
public class NetrcCredentialsProviderTest {

    @Test(expected = NetrcCredentialsProviderException.class)
    public void getCredentials_ThrowsFileNotFoundExceptionOnNonExistingGivenNetrcFilePath() {
        CredentialsProvider credentialsProvider = new NetrcCredentialsProvider(Paths.get("non_existing"));

        credentialsProvider.getCredentials(HOST);
    }

    @Test
    public void getCredentials_ReturnsEmptyResultWhenNetrcFileIsEmpty() {
        givenNetrcFileExistsButEmpty();

        CredentialsProvider credentialsProvider = new NetrcCredentialsProvider(NETRC_FILE_PATH);

        Optional<Credentials> actual = credentialsProvider.getCredentials(HOST);
        Assert.assertEquals(Optional.empty(), actual);
    }

    @Test
    public void getCredentials_ReturnsCredentialsWithEmptyUserAndPasswordWhenMachineFoundButWithoutLoginAndPassword() {
        givenNetrcFileWith(toNetrcRowWihMachine(HOST));

        CredentialsProvider credentialsProvider = new NetrcCredentialsProvider(NETRC_FILE_PATH);

        Optional<Credentials> actual = credentialsProvider.getCredentials(HOST);
        Assert.assertEquals(Optional.of(new Credentials()), actual);
    }

    @Test
    public void getCredentials_ReturnsCredentialsWithUserAndEmptyPasswordWhenMachineFoundWithLoginButWithoutPassword() {
        givenNetrcFileWith(toNetrcRowWithMachineAndLogin(HOST, LOGIN));

        CredentialsProvider credentialsProvider = new NetrcCredentialsProvider(NETRC_FILE_PATH);

        Optional<Credentials> actual = credentialsProvider.getCredentials(HOST);
        Assert.assertEquals(Optional.of(new Credentials(LOGIN, null)), actual);
    }

    @Test
    public void getCredentials_ReturnsCredentialsWithPasswordAndEmptyUserWhenMachineFoundWithPasswordButWithoutLogin() {
        givenNetrcFileWith(toNetrcRowWithMachineAndPassword(HOST, PASS));

        CredentialsProvider credentialsProvider = new NetrcCredentialsProvider(NETRC_FILE_PATH);

        Optional<Credentials> actual = credentialsProvider.getCredentials(HOST);
        Assert.assertEquals(Optional.of(new Credentials(null, PASS)), actual);
    }

    @Test
    public void getCredentials_ReturnsCredentialsWithUserAndPassword() {
        givenNetrcFileWith(toNetrcRow(HOST, LOGIN, PASS));

        CredentialsProvider credentialsProvider = new NetrcCredentialsProvider(NETRC_FILE_PATH);

        Optional<Credentials> actual = credentialsProvider.getCredentials(HOST);
        Assert.assertEquals(Optional.of(new Credentials(LOGIN, PASS)), actual);
    }

    @Test
    public void getCredentials_ReturnsCredentialsForSubDomain() {
        givenNetrcFileWith(toNetrcRow(HOST, LOGIN, PASS));

        CredentialsProvider credentialsProvider = new NetrcCredentialsProvider(NETRC_FILE_PATH);

        Optional<Credentials> actual = credentialsProvider.getCredentials(SUBDOMAIN);
        Assert.assertEquals(Optional.of(new Credentials(LOGIN, PASS)), actual);
    }

    @Test
    public void getCredentials_ReturnsCredentialsForFirstMatchOfMachine() {
        givenNetrcFileWith(
                toNetrcRow(HOST, LOGIN, PASS),
                toNetrcRow(SUBDOMAIN, LOGIN2, PASS2)
        );

        CredentialsProvider credentialsProvider = new NetrcCredentialsProvider(NETRC_FILE_PATH);

        Optional<Credentials> actual = credentialsProvider.getCredentials(SUBDOMAIN);
        Assert.assertEquals(Optional.of(new Credentials(LOGIN, PASS)), actual);
    }

    @Test
    public void getCredentials_FindsHostInTheMiddle() {
        givenNetrcFileWith(
                toNetrcRowWithNewLinesAndTabs(HOST, LOGIN, PASS),
                toNetrcRow(HOST2, LOGIN2, PASS2),
                toNetrcRow(HOST3, LOGIN3, PASS3)
        );

        CredentialsProvider credentialsProvider = new NetrcCredentialsProvider(NETRC_FILE_PATH);

        Optional<Credentials> actual = credentialsProvider.getCredentials(HOST2);
        Assert.assertEquals(Optional.of(new Credentials(LOGIN2, PASS2)), actual);
    }

    @Test
    public void getCredentials_SupportsDefinitionsWithNewLines() {
        givenNetrcFileWith(toNetrcRowWithNewLinesAndTabs(HOST, LOGIN, PASS));

        CredentialsProvider credentialsProvider = new NetrcCredentialsProvider(NETRC_FILE_PATH);

        Optional<Credentials> actual = credentialsProvider.getCredentials(HOST);
        Assert.assertEquals(Optional.of(new Credentials(LOGIN, PASS)), actual);
    }

    @Test
    public void getCredentials_ReturnEmptyResultWhenHostIsNotFound() {
        givenNetrcFileWith(
                toNetrcRow(HOST, LOGIN, PASS),
                toNetrcRow(HOST2, LOGIN2, PASS2)
        );

        CredentialsProvider credentialsProvider = new NetrcCredentialsProvider(NETRC_FILE_PATH);

        Optional<Credentials> actual = credentialsProvider.getCredentials(HOST3);
        Assert.assertEquals(Optional.empty(), actual);
    }

    @After
    public void afterEach() {
        try {
            Files.delete(NETRC_FILE_PATH);
        } catch (NoSuchFileException e) {
            // do nothing
        } catch (IOException e) {
            Assert.fail(String.format("failed to delete file %s", NETRC_FILE_PATH));
            e.printStackTrace();
        }
    }

    private void givenNetrcFileExistsButEmpty() {
        try {
            Files.createFile(NETRC_FILE_PATH);
        } catch (IOException e) {
            Assert.fail("could not create .netrc file");
        }
    }

    private void givenNetrcFileWith(String... netrcRows) {
        StringBuilder fileContent = new StringBuilder();
        for (String row : netrcRows) {
            fileContent.append(row).append(System.lineSeparator());
        }
        try {
            Files.write(NETRC_FILE_PATH,fileContent.toString().getBytes(Charset.forName("UTF-8")));
        } catch (IOException e) {
            Assert.fail("could not create .netrc file");
        }
    }

    private String toNetrcRow(String host, String login, String password) {
        String s = String.format("machine %s login %s password %s", host, login, password);
        System.out.println(s);
        return s;
    }

    private String toNetrcRowWithNewLinesAndTabs(String host, String login, String password) {
        String s = String.format("machine %s%s\tlogin %s%s\t\tpassword %s",
                host, System.lineSeparator(), login, System.lineSeparator(), password);
        System.out.println(s);
        return s;
    }


    private String toNetrcRowWihMachine(String machine) {
        return String.format("machine %s", machine);
    }

    private String toNetrcRowWithMachineAndLogin(String host, String login) {
        return String.format("machine %s login %s", host, login);
    }

    private String toNetrcRowWithMachineAndPassword(String host, String password) {
        return String.format("machine %s password %s", host, password);
    }

    private static final Path NETRC_FILE_PATH = Paths.get("./.netrc");
    private static final String HOST = "some.host" + UUID.randomUUID().toString();
    private static final String HOST2 = "some.host2" + UUID.randomUUID().toString();
    private static final String HOST3 = "some.host3" + UUID.randomUUID().toString();
    private static final String SUBDOMAIN = "sub." + HOST;
    private static final String LOGIN = "login_" + UUID.randomUUID().toString();
    private static final String LOGIN2 = "login_" + UUID.randomUUID().toString();
    private static final String LOGIN3 = "login_" + UUID.randomUUID().toString();
    private static final String PASS = "pass_" + UUID.randomUUID().toString();
    private static final String PASS2 = "pass_" + UUID.randomUUID().toString();
    private static final String PASS3 = "pass_" + UUID.randomUUID().toString();
}
