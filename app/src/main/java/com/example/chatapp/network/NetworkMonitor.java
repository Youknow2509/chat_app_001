package com.example.chatapp.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.chatapp.utils.NetworkUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Theo dõi liên tục trạng thái kết nối mạng và thông báo cho các thành phần đăng ký
 * - Tự động cập nhật trạng thái mạng theo thời gian thực
 * - Cung cấp cơ chế đăng ký/hủy đăng ký dễ sử dụng
 * - Sử dụng WeakReference để tránh memory leaks
 */
public class NetworkMonitor {
    private static final String TAG = "NetworkMonitor";

    private static WeakReference<NetworkMonitor> instanceRef;
    private final Context context;
    private final List<WeakReference<NetworkStateListener>> listeners = new ArrayList<>();
    private boolean isNetworkAvailable;
    private BroadcastReceiver networkReceiver;
    private ConnectivityManager.NetworkCallback networkCallback;
    private ConnectivityManager connectivityManager;
    private boolean isRegistered = false;
    private final AtomicBoolean isDestroyed = new AtomicBoolean(false);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface NetworkStateListener {
        void onNetworkStateChanged(boolean isAvailable);
    }

    private NetworkMonitor(Context context) {
        this.context = context.getApplicationContext();
        this.isNetworkAvailable = NetworkUtil.isNetworkAvailable(context);
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // Đăng ký finalization callback để đảm bảo tài nguyên được giải phóng
        // ngay cả khi người dùng quên gọi unregister
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!isDestroyed.get()) {
                unregisterAll();
            }
        }));
    }

    /**
     * Lấy instance của NetworkMonitor
     */
    public static synchronized NetworkMonitor getInstance(Context context) {
        NetworkMonitor instance = null;
        if (instanceRef != null) {
            instance = instanceRef.get();
        }

        if (instance == null || instance.isDestroyed.get()) {
            instance = new NetworkMonitor(context);
            instanceRef = new WeakReference<>(instance);
        }

        return instance;
    }

    /**
     * Kiểm tra trạng thái mạng hiện tại
     */
    public boolean isNetworkAvailable() {
        // Luôn kiểm tra trạng thái mạng hiện tại thay vì sử dụng giá trị đã lưu
        boolean currentState = NetworkUtil.isNetworkAvailable(context);
        if (isNetworkAvailable != currentState) {
            isNetworkAvailable = currentState;
        }
        return isNetworkAvailable;
    }

    /**
     * Đăng ký lắng nghe thay đổi kết nối mạng, sử dụng WeakReference để tránh memory leaks
     */
    public void addListener(NetworkStateListener listener) {
        if (listener == null || isDestroyed.get()) return;

        synchronized (listeners) {
            // Kiểm tra xem listener đã tồn tại chưa
            boolean exists = false;
            for (WeakReference<NetworkStateListener> ref : listeners) {
                NetworkStateListener existingListener = ref.get();
                if (existingListener == listener) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                // Thêm mới listener với WeakReference
                listeners.add(new WeakReference<>(listener));

                // Nếu đây là listener đầu tiên, đăng ký nhận thông báo thay đổi mạng
                if (listeners.size() == 1 && !isRegistered) {
                    registerNetworkCallback();
                }

                // Thông báo trạng thái mạng hiện tại cho listener mới
                final boolean networkState = isNetworkAvailable();

                // Đảm bảo callback trên main thread
                mainHandler.post(() -> {
                    try {
                        listener.onNetworkStateChanged(networkState);
                    } catch (Exception e) {
                        Log.e(TAG, "Error notifying new listener", e);
                    }
                });
            }

            // Dọn dẹp các WeakReference trống
            cleanupReferences();
        }
    }

    /**
     * Hủy đăng ký lắng nghe thay đổi kết nối mạng
     */
    public void removeListener(NetworkStateListener listener) {
        if (listener == null) return;

        synchronized (listeners) {
            Iterator<WeakReference<NetworkStateListener>> iterator = listeners.iterator();
            while (iterator.hasNext()) {
                WeakReference<NetworkStateListener> ref = iterator.next();
                NetworkStateListener existingListener = ref.get();

                if (existingListener == null || existingListener == listener) {
                    iterator.remove();
                }
            }

            // Nếu không còn listener nào, hủy đăng ký nhận thông báo thay đổi mạng
            if (listeners.isEmpty() && isRegistered) {
                unregisterNetworkCallback();
            }
        }
    }

    /**
     * Thông báo cho tất cả các listeners về trạng thái mạng mới
     */
    private void notifyListeners() {
        final boolean currentNetworkState = isNetworkAvailable();

        // Luôn thông báo trên main thread
        mainHandler.post(() -> {
            synchronized (listeners) {
                Iterator<WeakReference<NetworkStateListener>> iterator = listeners.iterator();
                while (iterator.hasNext()) {
                    WeakReference<NetworkStateListener> ref = iterator.next();
                    NetworkStateListener listener = ref.get();

                    if (listener != null) {
                        try {
                            listener.onNetworkStateChanged(currentNetworkState);
                        } catch (Exception e) {
                            Log.e(TAG, "Error notifying listener", e);
                        }
                    } else {
                        // Xóa listener không còn tồn tại
                        iterator.remove();
                    }
                }
            }
        });
    }

    /**
     * Đăng ký lắng nghe thay đổi mạng sử dụng phương thức phù hợp với phiên bản Android
     */
    private synchronized void registerNetworkCallback() {
        if (isRegistered || isDestroyed.get()) return;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Sử dụng NetworkCallback trên Android 7.0+ (API 24+)
                if (networkCallback == null) {
                    networkCallback = new ConnectivityManager.NetworkCallback() {
                        @Override
                        public void onAvailable(Network network) {
                            if (isDestroyed.get()) return;

                            boolean previousState = isNetworkAvailable;
                            isNetworkAvailable = true;

                            if (previousState != isNetworkAvailable) {
                                Log.d(TAG, "Network became available");
                                notifyListeners();
                            }
                        }

                        @Override
                        public void onLost(Network network) {
                            if (isDestroyed.get()) return;

                            // Kiểm tra xem còn mạng nào khác không
                            boolean hasNetwork = false;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                Network activeNetwork = connectivityManager.getActiveNetwork();
                                if (activeNetwork != null) {
                                    NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
                                    hasNetwork = capabilities != null &&
                                            (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                                                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
                                }
                            } else {
                                hasNetwork = NetworkUtil.isNetworkAvailable(context);
                            }

                            boolean previousState = isNetworkAvailable;
                            isNetworkAvailable = hasNetwork;

                            if (previousState != isNetworkAvailable) {
                                Log.d(TAG, "Network lost");
                                notifyListeners();
                            }
                        }
                    };
                }

                NetworkRequest request = new NetworkRequest.Builder()
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .build();

                connectivityManager.registerNetworkCallback(request, networkCallback);
            } else {
                // Sử dụng BroadcastReceiver trên Android 6.0- (API 23-)
                if (networkReceiver == null) {
                    networkReceiver = new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            if (isDestroyed.get()) return;

                            boolean previousState = isNetworkAvailable;
                            isNetworkAvailable = NetworkUtil.isNetworkAvailable(context);

                            if (previousState != isNetworkAvailable) {
                                Log.d(TAG, "Network state changed: " + isNetworkAvailable);
                                notifyListeners();
                            }
                        }
                    };
                }

                IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
                context.registerReceiver(networkReceiver, filter);
            }

            isRegistered = true;
            Log.d(TAG, "Network monitor registered");
        } catch (Exception e) {
            Log.e(TAG, "Error registering network callback", e);
            isRegistered = false;
        }
    }

    /**
     * Hủy đăng ký lắng nghe thay đổi mạng
     */
    private synchronized void unregisterNetworkCallback() {
        if (!isRegistered) return;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Hủy đăng ký NetworkCallback
                if (networkCallback != null) {
                    try {
                        connectivityManager.unregisterNetworkCallback(networkCallback);
                    } catch (IllegalArgumentException e) {
                        // Callback có thể đã được hủy đăng ký
                        Log.e(TAG, "NetworkCallback was already unregistered", e);
                    }
                    networkCallback = null;
                }
            } else {
                // Hủy đăng ký BroadcastReceiver
                if (networkReceiver != null) {
                    try {
                        context.unregisterReceiver(networkReceiver);
                    } catch (IllegalArgumentException e) {
                        // Receiver có thể đã được hủy đăng ký
                        Log.e(TAG, "BroadcastReceiver was already unregistered", e);
                    }
                    networkReceiver = null;
                }
            }

            isRegistered = false;
            Log.d(TAG, "Network monitor unregistered");
        } catch (Exception e) {
            Log.e(TAG, "Error unregistering network callback", e);
        }
    }

    /**
     * Dọn dẹp các WeakReference không còn tồn tại
     */
    private void cleanupReferences() {
        synchronized (listeners) {
            Iterator<WeakReference<NetworkStateListener>> iterator = listeners.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().get() == null) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * Hủy tất cả đăng ký và giải phóng tài nguyên
     */
    public synchronized void unregisterAll() {
        if (isDestroyed.get()) return;

        try {
            isDestroyed.set(true);

            // Dừng Handler
            mainHandler.removeCallbacksAndMessages(null);

            // Hủy đăng ký callback mạng
            unregisterNetworkCallback();

            // Xóa tất cả listeners
            synchronized (listeners) {
                listeners.clear();
            }

            // Đánh dấu instance là null để GC
            if (instanceRef != null && instanceRef.get() == this) {
                instanceRef.clear();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in unregisterAll", e);
        }
    }

    /**
     * Phương thức public gọi từ bên ngoài để giải phóng tài nguyên
     */
    public static void shutdown() {
        NetworkMonitor instance = instanceRef != null ? instanceRef.get() : null;
        if (instance != null) {
            instance.unregisterAll();
        }
    }
}