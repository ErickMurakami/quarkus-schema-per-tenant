package dev.erick.multitenancy.integration;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/** Isolated PostgreSQL and signing keys; CI can supply its own disposable service and key pair. */
public class IntegrationTestResource implements QuarkusTestResourceLifecycleManager {
    private PostgreSQLContainer postgres;
    private Path keyDirectory;

    @Override
    public Map<String, String> start() {
        Map<String, String> config = new HashMap<>();
        try {
            String databaseUrl = System.getenv("TEST_DATABASE_URL");
            if (databaseUrl == null || databaseUrl.isBlank()) {
                postgres = new PostgreSQLContainer("postgres:17-alpine")
                        .withDatabaseName("multitenant")
                        .withUsername("quarkus")
                        .withPassword("quarkus");
                postgres.start();
                config.put("quarkus.datasource.jdbc.url", postgres.getJdbcUrl());
                config.put("quarkus.datasource.username", postgres.getUsername());
                config.put("quarkus.datasource.password", postgres.getPassword());
            } else {
                config.put("quarkus.datasource.jdbc.url", databaseUrl);
                config.put("quarkus.datasource.username", System.getenv().getOrDefault("TEST_DATABASE_USERNAME", "quarkus"));
                config.put("quarkus.datasource.password", System.getenv().getOrDefault("TEST_DATABASE_PASSWORD", "quarkus"));
            }

            String privateKey = System.getenv("TEST_JWT_PRIVATE_KEY");
            String publicKey = System.getenv("TEST_JWT_PUBLIC_KEY");
            if (privateKey == null && publicKey == null) {
                keyDirectory = Files.createTempDirectory("tenant-integration-keys-");
                var generator = KeyPairGenerator.getInstance("RSA");
                generator.initialize(2048);
                var pair = generator.generateKeyPair();
                privateKey = writePem("privateKey.pem", "PRIVATE KEY", pair.getPrivate().getEncoded());
                publicKey = writePem("publicKey.pem", "PUBLIC KEY", pair.getPublic().getEncoded());
            } else if (privateKey == null || publicKey == null) {
                throw new IllegalArgumentException("Provide both TEST_JWT_PRIVATE_KEY and TEST_JWT_PUBLIC_KEY, or neither.");
            }
            config.put("smallrye.jwt.sign.key.location", Path.of(privateKey).toUri().toString());
            config.put("mp.jwt.verify.publickey.location", Path.of(publicKey).toUri().toString());
            config.put("quarkus.http.test-port", "0");
            return config;
        } catch (Exception exception) {
            stop();
            throw new IllegalStateException("Unable to prepare the integration test environment", exception);
        }
    }

    private String writePem(String filename, String type, byte[] encoded) throws IOException {
        Path path = keyDirectory.resolve(filename);
        String body = Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(encoded);
        Files.writeString(path, "-----BEGIN " + type + "-----\n" + body + "\n-----END " + type + "-----\n");
        return path.toString();
    }

    @Override
    public void stop() {
        try {
            if (postgres != null) {
                postgres.stop();
            }
        } finally {
            if (keyDirectory != null) {
                try {
                    Files.deleteIfExists(keyDirectory.resolve("privateKey.pem"));
                    Files.deleteIfExists(keyDirectory.resolve("publicKey.pem"));
                    Files.deleteIfExists(keyDirectory);
                } catch (IOException exception) {
                    throw new IllegalStateException("Unable to remove integration test keys", exception);
                }
            }
        }
    }
}
