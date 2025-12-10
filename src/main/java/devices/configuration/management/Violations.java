package devices.configuration.management;

public record Violations(boolean operatorNotAssigned, boolean providerNotAssigned, boolean locationMissing,
                         boolean showOnMapButMissingLocation, boolean showOnMapButNoPublicAccess) {

    public static Builder builder() {
        return new Builder();
    }

    public boolean isValid() {
        return !operatorNotAssigned && !providerNotAssigned && !locationMissing && !showOnMapButMissingLocation && !showOnMapButNoPublicAccess;
    }

    public static class Builder {
        private boolean operatorNotAssigned;
        private boolean providerNotAssigned;
        private boolean locationMissing;
        private boolean showOnMapButMissingLocation;
        private boolean showOnMapButNoPublicAccess;

        public Builder operatorNotAssigned(boolean value) {
            this.operatorNotAssigned = value;
            return this;
        }

        public Builder providerNotAssigned(boolean value) {
            this.providerNotAssigned = value;
            return this;
        }

        public Builder locationMissing(boolean value) {
            this.locationMissing = value;
            return this;
        }

        public Builder showOnMapButMissingLocation(boolean value) {
            this.showOnMapButMissingLocation = value;
            return this;
        }

        public Builder showOnMapButNoPublicAccess(boolean value) {
            this.showOnMapButNoPublicAccess = value;
            return this;
        }

        public Violations build() {
            return new Violations(operatorNotAssigned, providerNotAssigned, locationMissing, showOnMapButMissingLocation, showOnMapButNoPublicAccess);
        }
    }
}
