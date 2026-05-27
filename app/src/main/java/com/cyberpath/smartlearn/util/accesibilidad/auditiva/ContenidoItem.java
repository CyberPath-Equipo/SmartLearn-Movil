package com.cyberpath.smartlearn.util.accesibilidad.auditiva;

public class ContenidoItem {
    public Type type;
    public String url;
    public int durationMs;
    public ContenidoItem(Type type, String url, int durationMs) {
        this.type = type;
        this.url = url;
        this.durationMs = durationMs <= 0 ? 800 : durationMs;
    }

    public enum Type {IMAGE, VIDEO}
}