package com.cyberpath.smartlearn.util.accesibilidad.auditiva;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class TranslationEngine {
    private static final String DEFAULT_MAPPING_ASSET = "mappingPrincipal.json";

    private final Context context;
    private final String baseUrl;
    private final Map<String, List<String>> phraseMap = new HashMap<>();
    private final Map<String, List<String>> wordMap = new HashMap<>();
    private final Map<String, MappingResponse.GlossInfo> glossData = new HashMap<>();
    private final Set<Integer> candidateWindowSizes = new TreeSet<>(Collections.reverseOrder());

    private volatile boolean mappingLoaded = false;
    private volatile boolean loadingMapping = false;

    public TranslationEngine(Context context, String baseUrl) {
        this.context = context.getApplicationContext();
        this.baseUrl = baseUrl != null && !baseUrl.endsWith("/") ? baseUrl + "/" : baseUrl;
    }

    public TranslationEngine(String baseUrl) {
        this(null, baseUrl);
    }

    /**
     * Carga el mapping desde assets y, si no existe, intenta el formato remoto anterior.
     */
    public void loadMappingAsync(String lessonId, MappingCallback cb) {
        if (mappingLoaded) {
            if (cb != null) cb.onLoaded(true, null);
            return;
        }

        if (loadingMapping) {
            if (cb != null) cb.onLoaded(true, null);
            return;
        }

        loadingMapping = true;
        new Thread(() -> {
            try {
                String assetName = resolveAssetName(lessonId);
                MappingResponse response = loadMappingFromAssets(assetName);
                if (response == null) {
                    throw new IllegalStateException("No se pudo cargar mapping desde assets");
                }

                populateMaps(response);
                mappingLoaded = true;
                loadingMapping = false;
                if (cb != null) cb.onLoaded(true, null);
            } catch (Exception ex) {
                Log.e("TranslationEngine", "💥 Error cargando mapping local: " + ex.getMessage(), ex);
                loadingMapping = false;
                if (cb != null) cb.onLoaded(false, ex.getMessage());
            }
        }, "MappingLoader").start();
    }

    private void populateMaps(MappingResponse root) {
        phraseMap.clear();
        wordMap.clear();
        glossData.clear();
        candidateWindowSizes.clear();

        if (root.frases != null) {
            for (MappingResponse.Phrase p : root.frases) {
                if (p == null || p.text == null) continue;
                String key = normalize(p.text);
                phraseMap.put(key, p.glosses != null ? p.glosses : new ArrayList<>());
                int size = countWords(key);
                if (size > 0) candidateWindowSizes.add(size);
            }
        }

        if (root.words != null) {
            for (Map.Entry<String, List<String>> e : root.words.entrySet()) {
                String k = e.getKey();
                List<String> v = e.getValue();
                if (k == null) continue;
                String key = normalize(k);
                wordMap.put(key, v != null ? v : new ArrayList<>());
                int size = countWords(key);
                if (size > 0) candidateWindowSizes.add(size);
            }
        }

        if (root.glossData != null) {
            for (Map.Entry<String, MappingResponse.GlossInfo> e : root.glossData.entrySet()) {
                if (e.getKey() == null || e.getValue() == null) continue;
                glossData.put(e.getKey(), e.getValue());
            }
        }

        if (candidateWindowSizes.isEmpty()) {
            candidateWindowSizes.add(1);
        }
    }

    private String normalize(String s) {
        if (s == null) return "";
        String tmp = s.toLowerCase(Locale.ROOT).trim();
        // quitar acentos
        tmp = Normalizer.normalize(tmp, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        // quitar caracteres no alfanuméricos, permitir espacios
        tmp = tmp.replaceAll("[^a-z0-9\\s]", "");
        // colapsar espacios
        tmp = tmp.replaceAll("\\s+", " ").trim();
        return tmp;
    }

    public List<ContenidoItem> translate(String text) {
        List<ContenidoItem> out = new ArrayList<>();
        String clean = normalize(text);
        Log.d("TranslationEngine", "🔍 Traduciendo: '" + clean + "'");

        if (clean.isEmpty()) {
            return out;
        }

        // 1. Buscar frase completa
        if (phraseMap.containsKey(clean)) {
            Log.d("TranslationEngine", "✅ Frase encontrada!");
            List<String> glosses = phraseMap.get(clean);
            if (glosses != null) {
                for (String g : glosses) addSignItem(out, g);
            }
            return out;
        }

        String[] tokens = clean.split("\\s+");
        int totalSigns = 0;

        for (int i = 0; i < tokens.length; ) {
            if (tokens[i].isEmpty()) {
                i++;
                continue;
            }

            boolean matched = false;
            for (Integer window : candidateWindowSizes) {
                if (window == null || window <= 0 || i + window > tokens.length) continue;
                String candidate = joinTokens(tokens, i, window);
                List<String> gls = phraseMap.get(candidate);
                if (gls == null) {
                    gls = wordMap.get(candidate);
                }
                if (gls != null && !gls.isEmpty()) {
                    Log.d("TranslationEngine", "✅ Coincidencia '" + candidate + "' → " + gls.size() + " signos");
                    for (String g : gls) addSignItem(out, g);
                    totalSigns += gls.size();
                    i += window;
                    matched = true;
                    break;
                }
            }

            if (!matched) {
                Log.w("TranslationEngine", "❌ Token no reconocido: '" + tokens[i] + "'");
                i++;
            }
        }

        if (out.isEmpty()) {
            Log.e("TranslationEngine", "❌ 0 signos generados para: " + text);
        } else {
            Log.d("TranslationEngine", "✅ Total signos: " + totalSigns);
        }

        return out;
    }

    private void addSignItem(List<ContenidoItem> list, String gloss) {
        MappingResponse.GlossInfo info = glossData.get(gloss);
        String url = null;
        String type = "video";
        int duration = 800;
        if (info != null) {
            if (info.type != null) type = info.type;
            if (info.url != null && !info.url.trim().isEmpty()) url = info.url.trim();
            if ((url == null || url.isEmpty()) && info.file != null && !info.file.trim().isEmpty()) {
                String file = info.file.trim();
                url = file.startsWith("http://") || file.startsWith("https://") ? file : baseUrl + "assets/" + file;
            }
            if (info.durationMs != null) duration = info.durationMs;
        }
        if (url == null || url.trim().isEmpty()) {
            Log.w("TranslationEngine", "⚠️ Sin URL para gloss: " + gloss + ", se omite");
            return;
        }

        list.add(new ContenidoItem("video".equalsIgnoreCase(type) ? ContenidoItem.Type.VIDEO : ContenidoItem.Type.IMAGE, url, duration));
    }

    private MappingResponse loadMappingFromAssets(String assetName) throws IOException {
        if (context == null) {
            return null;
        }
        try (InputStream in = context.getAssets().open(assetName);
             InputStreamReader reader = new InputStreamReader(in)) {
            return new Gson().fromJson(reader, MappingResponse.class);
        }
    }

    private String resolveAssetName(String lessonId) {
        if (context == null) {
            return DEFAULT_MAPPING_ASSET;
        }

        if (lessonId != null && !lessonId.trim().isEmpty()) {
            String normalized = lessonId.trim();
            String[] candidates = new String[]{
                    normalized + ".json",
                    "mapping_" + normalized + ".json",
                    "mapping" + normalized + ".json"
            };

            for (String candidate : candidates) {
                if (assetExists(candidate)) {
                    return candidate;
                }
            }
        }

        return DEFAULT_MAPPING_ASSET;
    }

    private boolean assetExists(String assetName) {
        try (InputStream ignored = context.getAssets().open(assetName)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private int countWords(String value) {
        if (value == null || value.trim().isEmpty()) return 0;
        return value.trim().split("\\s+").length;
    }

    private String joinTokens(String[] tokens, int start, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < start + length; i++) {
            if (tokens[i] == null || tokens[i].isEmpty()) continue;
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(tokens[i]);
        }
        return sb.toString();
    }

    public interface MappingCallback {
        void onLoaded(boolean ok, String message);
    }
}