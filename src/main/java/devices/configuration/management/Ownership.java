package devices.configuration.management;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record Ownership(String operator, String provider) {

    // Compact constructor for validation
    public Ownership {
        if ((operator == null && provider != null) || (operator != null && provider == null)) {
            throw new IllegalArgumentException("Ownership must be either unowned (both null) or owned (both set)");
        }
    }

    // Factory methods for commonly used instances
    public static Ownership unowned() {
        return new Ownership(null, null);
    }

    public static Ownership of(String operator, String provider) {
        return new Ownership(operator, provider);
    }

    // Predicate methods expressing domain concepts
    @JsonIgnore
    public boolean isUnowned() {
        return operator == null && provider == null;
    }

    @JsonIgnore
    public boolean isOwned() {
        return operator != null && provider != null;
    }
}
