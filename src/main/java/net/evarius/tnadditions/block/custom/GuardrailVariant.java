package net.evarius.tnadditions.block.custom;

public enum GuardrailVariant {
    STANDARD(true, 2),
    CENTER_POST(true, 1),
    WITHOUT_POSTS(false, 0),
    END_LEFT(true, 1),
    END_RIGHT(true, 1);

    private final boolean supporting;
    private final int maximumPostlessSpan;

    GuardrailVariant(boolean supporting, int maximumPostlessSpan) {
        this.supporting = supporting;
        this.maximumPostlessSpan = maximumPostlessSpan;
    }

    public boolean isSupporting() {
        return supporting;
    }

    public int getMaximumPostlessSpan() {
        return maximumPostlessSpan;
    }
}
