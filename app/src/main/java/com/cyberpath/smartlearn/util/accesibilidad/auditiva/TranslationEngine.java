package com.cyberpath.smartlearn.util.accesibilidad.auditiva;

import android.util.Log;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TranslationEngine {
    private final String baseUrl;
    private final TranslationApi api;

    private final Map<String, List<String>> phraseMap = new HashMap<>();
    private final Map<String, List<String>> wordMap = new HashMap<>();
    private final Map<String, MappingResponse.GlossInfo> glossData = new HashMap<>();

    public interface MappingCallback {
        void onLoaded(boolean ok, String message);
    }

    public TranslationEngine(String baseUrl) {
        // Retrofit requires baseUrl ending with '/'
        if (!baseUrl.endsWith("/")) baseUrl = baseUrl + "/";
        this.baseUrl = baseUrl;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(this.baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(TranslationApi.class);
    }

    /**
     * Carga el mapping en background usando Retrofit (asíncrono).
     * El callback se invoca en el hilo donde Retrofit entrega la respuesta (no necesariamente el UI thread).
     */
    public void loadMappingAsync(String lessonId, MappingCallback cb) {
        Log.d("TranslationEngine", "🔄 Solicitando mapping para: " + lessonId);
        Call<MappingResponse> call = api.getMapping(lessonId);
        call.enqueue(new Callback<MappingResponse>() {
            @Override
            public void onResponse(Call<MappingResponse> call, Response<MappingResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    String msg = "HTTP " + response.code();
                    Log.e("TranslationEngine", "❌ Error cargando mapping: " + msg);
                    cb.onLoaded(false, msg);
                    return;
                }

                try {
                    populateMaps(response.body());
                    Log.d("TranslationEngine", "✅ Mapping cargado (frases=" + phraseMap.size()
                            + " palabras=" + wordMap.size() + " glossData=" + glossData.size() + ")");
                    cb.onLoaded(true, null);
                } catch (Exception ex) {
                    Log.e("TranslationEngine", "💥 Excepción al parsear mapping: " + ex.getMessage(), ex);
                    cb.onLoaded(false, ex.getMessage());
                }
            }

            @Override
            public void onFailure(Call<MappingResponse> call, Throwable t) {
                Log.e("TranslationEngine", "💥 Failure en Retrofit: " + t.getMessage(), t);
                cb.onLoaded(false, t.getMessage());
            }
        });
    }

    private void populateMaps(MappingResponse root) {
        phraseMap.clear();
        wordMap.clear();
        glossData.clear();

        if (root.frases != null) {
            for (MappingResponse.Phrase p : root.frases) {
                if (p == null || p.text == null) continue;
                phraseMap.put(normalize(p.text), p.glosses != null ? p.glosses : new ArrayList<>());
            }
        }

        if (root.words != null) {
            for (Map.Entry<String, List<String>> e : root.words.entrySet()) {
                String k = e.getKey();
                List<String> v = e.getValue();
                if (k == null) continue;
                wordMap.put(normalize(k), v != null ? v : new ArrayList<>());
            }
        }

        if (root.glossData != null) {
            for (Map.Entry<String, MappingResponse.GlossInfo> e : root.glossData.entrySet()) {
                if (e.getKey() == null || e.getValue() == null) continue;
                glossData.put(e.getKey(), e.getValue());
            }
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

    // translate mantiene la lógica anterior pero ya NO hace Toasts, solo devuelve la lista
    public List<ContenidoItem> translate(String text) {
        List<ContenidoItem> out = new ArrayList<>();
        String clean = normalize(text);
        Log.d("TranslationEngine", "🔍 Traduciendo: '" + clean + "'");

        // 1. Buscar frase completa
        if (phraseMap.containsKey(clean)) {
            Log.d("TranslationEngine", "✅ Frase encontrada!");
            for (String g : phraseMap.get(clean)) addSignItem(out, g);
            return out;
        }

        // 2. Intento simple de n-gram / palabra por palabra (aquí por simplicidad: palabra a palabra)
        String[] tokens = clean.split("\\s+");
        int totalSigns = 0;
        for (String t : tokens) {
            if (t.isEmpty()) continue;
            List<String> gls = wordMap.get(t);
            if (gls != null) {
                Log.d("TranslationEngine", "✅ Palabra '" + t + "' → " + gls.size() + " signos");
                for (String g : gls) addSignItem(out, g);
                totalSigns += gls.size();
            } else {
                Log.w("TranslationEngine", "❌ Palabra NO encontrada: '" + t + "'");
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
        String file = "sign_lsm_" + gloss + ".jpg"; // default
        String type = "image";
        int duration = 800;
        if (info != null) {
            if (info.type != null) type = info.type;
            if (info.file != null) file = info.file;
            if (info.durationMs != null) duration = info.durationMs;
        }
        String url = baseUrl + "assets/" + file;
        if ("video".equalsIgnoreCase(type)) {
            list.add(new ContenidoItem(ContenidoItem.Type.VIDEO, url, 0));
        } else {
            list.add(new ContenidoItem(ContenidoItem.Type.IMAGE, url, duration));
        }
    }
}