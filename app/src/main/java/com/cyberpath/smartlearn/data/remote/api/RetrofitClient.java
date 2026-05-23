package com.cyberpath.smartlearn.data.remote.api;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    //Marco
    //private static final String BASE_URL = "http://192.168.1.77:8080/";
    //Efrén
    private static final String BASE_URL = "http://192.168.1.110:8080/";
    private static Retrofit retrofit;
    private static volatile ApiService apiService;
    private static Context appContext;

    public static void init(Context context) {
        appContext = context.getApplicationContext();
    }

    public static ApiService getApiService() {
        if (appContext == null) {
            throw new IllegalStateException("RetrofitClient no inicializado. Llama init(context) en Application.");
        }

        if (apiService == null) {
            synchronized (RetrofitClient.class) {
                if (apiService == null) {
                    // Logging (solo DEBUG)
                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message ->
                            android.util.Log.d("HTTP", message));
                    // En desarrollo BODY, en producción cambiar a BASIC o NONE
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY);

                    // Interceptor de autenticación (usa appContext)
                    AuthInterceptor authInterceptor = new AuthInterceptor(appContext);

                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(authInterceptor)
                            .addInterceptor(logging)
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .readTimeout(20, TimeUnit.SECONDS)
                            .writeTimeout(20, TimeUnit.SECONDS)
                            .build();

                    retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    apiService = retrofit.create(ApiService.class);
                }
            }
        }
        return apiService;
    }
}
