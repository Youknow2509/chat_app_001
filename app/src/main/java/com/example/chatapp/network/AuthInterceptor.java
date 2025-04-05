package com.example.chatapp.network;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.chatapp.api.EndPoint;
import com.example.chatapp.service.TokenRefreshService;
import com.example.chatapp.utils.session.SessionManager;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class AuthInterceptor implements Interceptor {
    private static final String TAG = "AuthInterceptor";
    private final Context context;
    private final SessionManager sessionManager;

    public AuthInterceptor(Context context) {
        this.context = context.getApplicationContext();
        this.sessionManager = new SessionManager(context);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // Thêm authorization header nếu cần
        if (requiresAuthentication(originalRequest)) {
            String token = sessionManager.getAccessToken();

            if (token != null && !token.isEmpty()) {
                originalRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer " + token)
                        .build();
            }
        }

        // Thực hiện request
        Response response = chain.proceed(originalRequest);

        // Kiểm tra lỗi 401 Unauthorized
        if (response.code() == 401 && requiresAuthentication(originalRequest)) {
            try {
                // Đọc body của response để kiểm tra mã lỗi
                ResponseBody responseBody = response.peekBody(1024);
                String responseBodyString = responseBody.string();

                JSONObject jsonObject = new JSONObject(responseBodyString);
                int errorCode = jsonObject.optInt("code", 0);

                // Mã lỗi 40002 - Validate token failed
                if (errorCode == 40002) {
                    Log.d(TAG, "Token validation failed (code 40002), refreshing token");

                    // Đóng response hiện tại
                    response.close();

                    // Khởi động service để refresh token
                    Intent intent = new Intent(context, TokenRefreshService.class);
                    intent.setAction("com.example.chatapp.FORCE_REFRESH_TOKEN");
                    context.startService(intent);

                    // Chờ một khoảng thời gian ngắn để token được refresh
                    Thread.sleep(1000);

                    // Lấy token mới
                    String newToken = sessionManager.getAccessToken();

                    // Tạo request mới với token mới
                    Request newRequest = originalRequest.newBuilder()
                            .removeHeader("Authorization")
                            .header("Authorization", "Bearer " + newToken)
                            .build();

                    // Thực hiện lại request với token mới
                    return chain.proceed(newRequest);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error handling 401 response", e);
            }
        }

        return response;
    }

    private boolean requiresAuthentication(Request request) {
        // Các endpoints không cần authentication
        String[] noAuthPaths = {
                EndPoint.LOGIN,
                EndPoint.FORGOT_PASSWORD,
                EndPoint.LOGIN,
                EndPoint.REFRESH_TOKEN,
                EndPoint.REGISTER,
                EndPoint.UPGRADE_PASSWORD_REGISTER,
                EndPoint.VERIFY_ACCOUNT,
                EndPoint.UPDATE_USER_NAME_AND_AVATAR,
                EndPoint.VERIFY_FORGOT_PASSWORD,
                EndPoint.VALID_TOKEN
        };

        String path = request.url().encodedPath();

        for (String noAuthPath : noAuthPaths) {
            if (path.contains(noAuthPath)) {
                return false;
            }
        }

        return true;
    }
}