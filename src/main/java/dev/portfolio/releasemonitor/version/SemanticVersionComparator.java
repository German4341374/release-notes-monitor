package dev.portfolio.releasemonitor.version;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class SemanticVersionComparator {

    private static final Pattern SEMANTIC_VERSION = Pattern.compile(
            "^[vV]?(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?(?:-([0-9A-Za-z.-]+))?(?:\\+[0-9A-Za-z.-]+)?$");

    public int compare(String left, String right) {
        SemanticVersion leftVersion = parse(left);
        SemanticVersion rightVersion = parse(right);

        int coreComparison = leftVersion.major().compareTo(rightVersion.major());
        if (coreComparison == 0) {
            coreComparison = leftVersion.minor().compareTo(rightVersion.minor());
        }
        if (coreComparison == 0) {
            coreComparison = leftVersion.patch().compareTo(rightVersion.patch());
        }
        if (coreComparison != 0) {
            return coreComparison;
        }

        return comparePreRelease(leftVersion.preRelease(), rightVersion.preRelease());
    }

    public boolean isUpdateAvailable(String installedVersion, String latestVersion) {
        return compare(latestVersion, installedVersion) > 0;
    }

    public boolean isValid(String version) {
        try {
            parse(version);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private SemanticVersion parse(String version) {
        if (version == null || version.isBlank()) {
            throw new IllegalArgumentException("Version must not be blank.");
        }

        Matcher matcher = SEMANTIC_VERSION.matcher(version.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Version is not valid semantic version syntax.");
        }

        return new SemanticVersion(
                new BigInteger(matcher.group(1)),
                numberOrZero(matcher.group(2)),
                numberOrZero(matcher.group(3)),
                splitIdentifiers(matcher.group(4)));
    }

    private BigInteger numberOrZero(String value) {
        return value == null ? BigInteger.ZERO : new BigInteger(value);
    }

    private List<String> splitIdentifiers(String preRelease) {
        if (preRelease == null) {
            return List.of();
        }
        String[] parts = preRelease.toLowerCase(Locale.ROOT).split("\\.");
        List<String> identifiers = new ArrayList<>(parts.length);
        for (String part : parts) {
            if (part.isBlank()) {
                throw new IllegalArgumentException("Pre-release identifiers must not be empty.");
            }
            identifiers.add(part);
        }
        return List.copyOf(identifiers);
    }

    private int comparePreRelease(List<String> left, List<String> right) {
        if (left.isEmpty() && right.isEmpty()) {
            return 0;
        }
        if (left.isEmpty()) {
            return 1;
        }
        if (right.isEmpty()) {
            return -1;
        }

        int sharedLength = Math.min(left.size(), right.size());
        for (int index = 0; index < sharedLength; index++) {
            int identifierComparison = compareIdentifier(left.get(index), right.get(index));
            if (identifierComparison != 0) {
                return identifierComparison;
            }
        }
        return Integer.compare(left.size(), right.size());
    }

    private int compareIdentifier(String left, String right) {
        boolean leftNumeric = left.chars().allMatch(Character::isDigit);
        boolean rightNumeric = right.chars().allMatch(Character::isDigit);
        if (leftNumeric && rightNumeric) {
            return new BigInteger(left).compareTo(new BigInteger(right));
        }
        if (leftNumeric) {
            return -1;
        }
        if (rightNumeric) {
            return 1;
        }
        return left.compareTo(right);
    }

    private record SemanticVersion(
            BigInteger major, BigInteger minor, BigInteger patch, List<String> preRelease) {}
}
