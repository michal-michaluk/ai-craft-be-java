package devices.configuration.management;

public record Settings(
        boolean autoStart,
        boolean remoteControl,
        boolean billing,
        boolean reimbursement,
        boolean showOnMap,
        boolean publicAccess
) {

    public static Settings defaultSettings() {
        return new Settings(false, false, false, false, false, false);
    }

    public Settings apply(SettingsDiff diff) {
        return new Settings(
                diff.autoStart() != null ? diff.autoStart() : autoStart,
                diff.remoteControl() != null ? diff.remoteControl() : remoteControl,
                diff.billing() != null ? diff.billing() : billing,
                diff.reimbursement() != null ? diff.reimbursement() : reimbursement,
                diff.showOnMap() != null ? diff.showOnMap() : showOnMap,
                diff.publicAccess() != null ? diff.publicAccess() : publicAccess
        );
    }

    public record SettingsDiff(
            Boolean autoStart,
            Boolean remoteControl,
            Boolean billing,
            Boolean reimbursement,
            Boolean showOnMap,
            Boolean publicAccess) {

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private Boolean autoStart;
            private Boolean remoteControl;
            private Boolean billing;
            private Boolean reimbursement;
            private Boolean showOnMap;
            private Boolean publicAccess;

            public Builder autoStart(Boolean autoStart) {
                this.autoStart = autoStart;
                return this;
            }

            public Builder remoteControl(Boolean remoteControl) {
                this.remoteControl = remoteControl;
                return this;
            }

            public Builder billing(Boolean billing) {
                this.billing = billing;
                return this;
            }

            public Builder reimbursement(Boolean reimbursement) {
                this.reimbursement = reimbursement;
                return this;
            }

            public Builder showOnMap(Boolean showOnMap) {
                this.showOnMap = showOnMap;
                return this;
            }

            public Builder publicAccess(Boolean publicAccess) {
                this.publicAccess = publicAccess;
                return this;
            }

            public SettingsDiff build() {
                return new SettingsDiff(autoStart, remoteControl, billing, reimbursement, showOnMap, publicAccess);
            }
        }
    }
}
