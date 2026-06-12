package devices.configuration.device;

import lombok.Builder;

@Builder
record Violations(
        boolean operatorNotAssigned,
        boolean providerNotAssigned,
        boolean locationMissing,
        boolean showOnMapButMissingLocation,
        boolean showOnMapButNoPublicAccess) {

    boolean isValid() {
        return !operatorNotAssigned
                && !providerNotAssigned
                && !locationMissing
                && !showOnMapButMissingLocation
                && !showOnMapButNoPublicAccess;
    }
}
