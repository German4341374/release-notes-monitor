package dev.portfolio.releasemonitor.version;

public class VersionSourceException extends RuntimeException {

    private final boolean temporary;

    public VersionSourceException(String message, boolean temporary) {
        super(message);
        this.temporary = temporary;
    }

    public VersionSourceException(String message, boolean temporary, Throwable cause) {
        super(message, cause);
        this.temporary = temporary;
    }

    public boolean isTemporary() {
        return temporary;
    }
}
