package minegame159.lightmap.utils;

public enum Brightness {
    Low(180),
    Normal(220),
    High(255);

    public final int value;

    Brightness(int value) {
        this.value = value;
    }
}
