package com.cyberpath.smartlearn.util.accesibilidad.auditiva;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface TranslationApi {
    @GET("mapping/{lessonId}")
    Call<MappingResponse> getMapping(@Path("lessonId") String lessonId);
}