package com.coretix.installer;

public enum InstallTarget {
    COREX("CoreX Only", true, false, false, false),
    SHIPX("CoreX + ShipX", true, true, false, false),
    CAREX("CoreX + CareX", true, false, true, false),
    PAYROLLX("CoreX + PayrollX", true, false, false, true),
    ALL("CoreX + All Applications", true, true, true, true);

    private final String label;
    private final boolean includeCorex;
    private final boolean includeShipx;
    private final boolean includeCarex;
    private final boolean includePayrollx;

    InstallTarget(String label, boolean includeCorex, boolean includeShipx, boolean includeCarex, boolean includePayrollx) {
        this.label = label;
        this.includeCorex = includeCorex;
        this.includeShipx = includeShipx;
        this.includeCarex = includeCarex;
        this.includePayrollx = includePayrollx;
    }

    public boolean includeCorex() {
        return includeCorex;
    }

    public boolean includeShipx() {
        return includeShipx;
    }

    public boolean includeCarex() {
        return includeCarex;
    }

    public boolean includePayrollx() {
        return includePayrollx;
    }

    @Override
    public String toString() {
        return label;
    }
}
