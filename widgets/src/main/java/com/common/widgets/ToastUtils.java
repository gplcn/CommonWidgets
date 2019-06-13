package com.common.widgets;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

class ToastUtils {
    private static Toast sToast;
    private static Handler sHandler = new Handler(Looper.getMainLooper());
    public static boolean sIsCancel;

    private ToastUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    public static void showShortToastSafe(final Context context, final CharSequence text) {
        sHandler.post(new Runnable() {
            public void run() {
                ToastUtils.showToast(context, text, 0);
            }
        });
    }

    public static void showShortToastSafe(final Context context, final int resId) {
        sHandler.post(new Runnable() {
            public void run() {
                ToastUtils.showToast(context, resId, 0);
            }
        });
    }

    public static void showShortToastSafe(final Context context, final int resId, final Object... args) {
        sHandler.post(new Runnable() {
            public void run() {
                ToastUtils.showToast(context, resId, 0, args);
            }
        });
    }

    public static void showShortToastSafe(final Context context, final String format, final Object... args) {
        sHandler.post(new Runnable() {
            public void run() {
                ToastUtils.showToast(context, format, 0, args);
            }
        });
    }

    public static void showLongToastSafe(final Context context, final CharSequence text) {
        sHandler.post(new Runnable() {
            public void run() {
                ToastUtils.showToast(context, text, 1);
            }
        });
    }

    public static void showLongToastSafe(final Context context, final int resId) {
        sHandler.post(new Runnable() {
            public void run() {
                ToastUtils.showToast(context, resId, 1);
            }
        });
    }

    public static void showLongToastSafe(final Context context, final int resId, final Object... args) {
        sHandler.post(new Runnable() {
            public void run() {
                ToastUtils.showToast(context, resId, 1, args);
            }
        });
    }

    public static void showLongToastSafe(final Context context, final String format, final Object... args) {
        sHandler.post(new Runnable() {
            public void run() {
                ToastUtils.showToast(context, format, 1, args);
            }
        });
    }

    public static void showShortToast(Context context, CharSequence text) {
        showToast(context, text, 0);
    }

    public static void showShortToast(Context context, int resId) {
        showToast(context, resId, 0);
    }

    public static void showShortToast(Context context, int resId, Object... args) {
        showToast(context, resId, 0, args);
    }

    public static void showShortToast(Context context, String format, Object... args) {
        showToast(context, format, 0, args);
    }

    public static void showLongToast(Context context, CharSequence text) {
        showToast(context, text, 1);
    }

    public static void showLongToast(Context context, int resId) {
        showToast(context, resId, 1);
    }

    public static void showLongToast(Context context, int resId, Object... args) {
        showToast(context, resId, 1, args);
    }

    public static void showLongToast(Context context, String format, Object... args) {
        showToast(context, format, 1, args);
    }

    private static void showToast(Context context, CharSequence text, int duration) {
        if (sToast == null) {
            sToast = Toast.makeText(context, text, duration);
        } else {
            if (sIsCancel) {
                sToast.cancel();
            }

            sToast.setText(text);
        }

        sToast.show();
    }

    private static void showToast(Context context, int resId, int duration) {
        showToast(context, context.getResources().getText(resId).toString(), duration);
    }

    private static void showToast(Context context, int resId, int duration, Object... args) {
        showToast(context, String.format(context.getResources().getString(resId), args), duration);
    }

    private static void showToast(Context context, String format, int duration, Object... args) {
        showToast(context, String.format(format, args), duration);
    }

    public static void cancelToast() {
        if (sToast != null) {
            sToast.cancel();
        }

    }
}
