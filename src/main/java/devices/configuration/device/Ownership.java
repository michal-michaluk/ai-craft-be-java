package devices.configuration.device;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record Ownership(String operator, String provider) {

    public static Ownership unowned() {
        return new Ownership(null, null);
    }

    public static Ownership of(String operator, String provider) {
        return new Ownership(operator, provider);
    }

    public Ownership {
        assert isUnowned() || isOwned();
    }

    @JsonIgnore
    public boolean isUnowned() {
        return operator == null && provider == null;
    }

    @JsonIgnore
    public boolean isOwned() {
        return operator != null && provider != null;
    }
}
