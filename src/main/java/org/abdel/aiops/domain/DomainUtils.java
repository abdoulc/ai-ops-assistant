package org.abdel.aiops.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.regex.Pattern;

public final class DomainUtils {
    private static final Pattern SHA_256_PATTERN =
            Pattern.compile("^[a-f0-9]{64}$");
    private DomainUtils() {
    }
    public static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " is required"
            );
        }
        return value;
    }

    public static <T> T requireValue(T value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(
                    field + " is required"
            );
        }
        return value;
    }

    public static void validateChecksum(String checksum) {
        if (checksum == null
                || !SHA_256_PATTERN.matcher(checksum).matches()) {
            throw new IllegalArgumentException(
                    "checksum must be a lowercase SHA-256 value"
            );
        }
    }

    public static String calculateChecksum(String content) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    content.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is not available",
                    exception
            );
        }
    }

    public static boolean checksumMatches(
            String content,
            String expectedChecksum
    ) {
        return calculateChecksum(content)
                .equals(expectedChecksum);
    }
}
