package devices.configuration.device;

record Visibility(boolean roamingEnabled, ForCustomer forCustomer) {

    static Visibility basedOn(boolean usable, boolean showOnMap) {
        return new Visibility(usable, ForCustomer.calculateForCustomer(usable, showOnMap));
    }

    enum ForCustomer {
        USABLE_AND_VISIBLE_ON_MAP, USABLE_BUT_HIDDEN_ON_MAP, INACCESSIBLE_AND_HIDDEN_ON_MAP;

        private static ForCustomer calculateForCustomer(boolean usable, boolean showOnMap) {
            if (!usable) {
                return ForCustomer.INACCESSIBLE_AND_HIDDEN_ON_MAP;
            } else if (showOnMap) {
                return ForCustomer.USABLE_AND_VISIBLE_ON_MAP;
            } else {
                return ForCustomer.USABLE_BUT_HIDDEN_ON_MAP;
            }
        }
    }
}
