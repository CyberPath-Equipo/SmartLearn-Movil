package com.cyberpath.smartlearn.util.accesibilidad.auditiva;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class MappingResponse {
    @SerializedName("frases")
    public List<Phrase> frases;

    @SerializedName("words")
    public Map<String, List<String>> words;

    @SerializedName("glossData")
    public Map<String, GlossInfo> glossData;

    public static class Phrase {
        @SerializedName("text")
        public String text;

        @SerializedName("glosses")
        public List<String> glosses;
    }

    public static class GlossInfo {
        @SerializedName("type")
        public String type;

        @SerializedName("file")
        public String file;

        @SerializedName("url")
        public String url;

        @SerializedName("durationMs")
        public Integer durationMs;
    }
}