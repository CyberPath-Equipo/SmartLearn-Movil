package com.cyberpath.smartlearn.data.remote.api;

import android.content.Context;
import androidx.annotation.NonNull;
import com.cyberpath.smartlearn.util.preferences.PreferencesManager;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private final Context ctx;

    public AuthInterceptor(Context ctx) {
        this.ctx = ctx.getApplicationContext();
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request();

        Request.Builder b = original.newBuilder()
                .header("X-Client-Type", "mobile")               // evita BrowserBlockFilter
                .header("User-Agent", "Smartlearn-Mobile/1.0");  // user-agent controlado

        String token = PreferencesManager.getToken(ctx); // tu método para obtener token
        if (token != null && !token.isEmpty()) {
            b.header("Authorization", "Bearer " + token);
        }

        String trustedDeviceToken = PreferencesManager.getTrustedDeviceToken(ctx);
        if (trustedDeviceToken != null && !trustedDeviceToken.isEmpty()) {
            b.header("X-Trusted-Device-Token", trustedDeviceToken);
        }

        Request r = b.build();
        return chain.proceed(r);
    }
}