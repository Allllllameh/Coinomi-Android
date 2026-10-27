package com.coinomi.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int NATIVE_SCANNER_REQUEST_CODE = 200; // کد درخواست برای اسکنر بومی
    
    // وضعیت‌ها برای مدیریت دکمه Back
    private boolean isAssetActive = false; 
    private boolean isScannerActive = false; 

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // تنظیم رنگ نوار وضعیت در ابتدا (مشکی برای لودینگ)
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(Color.parseColor("#000000"));
        }
        
        webView = new WebView(this);
        setContentView(webView);

        // درخواست دسترسی دوربین در همان ابتدا
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true); // برای ذخیره دیتابیس محلی (localStorage)
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);

        // اتصال به جاوااسکریپت
        webView.addJavascriptInterface(new WebAppInterface(this), "Android");
        webView.setWebViewClient(new WebViewClient());
        
        // مدیریت دسترسی دوربین برای وب‌ویو (هرچند الان از اسکنر بومی استفاده می‌کنیم، برای اطمینان نگه می‌داریم)
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                runOnUiThread(() -> {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        request.grant(request.getResources());
                    } else {
                        request.deny();
                    }
                });
            }
        });

        webView.loadUrl("file:///android_asset/index.html");
    }

    // این کلاس پل ارتباطی بین جاوا و جاوااسکریپت است
    public class WebAppInterface {
        Context mContext;
        WebAppInterface(Context c) { mContext = c; }

        @JavascriptInterface
        public String getClipboardText() {
            ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null && clipboard.hasPrimaryClip()) {
                ClipData clipData = clipboard.getPrimaryClip();
                if (clipData != null && clipData.getItemCount() > 0) {
                    return clipData.getItemAt(0).getText().toString();
                }
            }
            return "";
        }

        // مدیریت وضعیت صفحه‌ها
        @JavascriptInterface
        public void setMainScreenActive() { 
            isAssetActive = false; 
        }

        @JavascriptInterface
        public void setAssetScreenActive() { 
            isAssetActive = true; 
        }

        // مدیریت وضعیت اسکنر
        @JavascriptInterface
        public void setScannerActive(boolean active) {
            isScannerActive = active;
        }

        // تغییر رنگ نوار وضعیت (بالای صفحه) از طریق جاوااسکریپت
        @JavascriptInterface
        public void setStatusBarColor(final String colorHex) {
            runOnUiThread(() -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setStatusBarColor(Color.parseColor(colorHex));
                }
            });
        }

        // ================== تغییرات گام ۶ ==================
        // فراخوانی اسکنر بومی (کاتلین/جاوا) از روی HTML
        @JavascriptInterface
        public void openNativeScanner() {
            Intent intent = new Intent(MainActivity.this, QrScannerActivity.class);
            startActivityForResult(intent, NATIVE_SCANNER_REQUEST_CODE);
        }
        // ===================================================
    }

    // ================== تغییرات گام ۶ ==================
    // دریافت نتیجه از اسکنر بومی و فرستادنش به جاوااسکریپت
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NATIVE_SCANNER_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String qrResult = data.getStringExtra("QR_RESULT");
            if (qrResult != null) {
                // فرستادن کد اسکن شده به HTML
                webView.post(() -> webView.evaluateJavascript("handleNativeQrResult('" + qrResult + "');", null));
            }
        }
    }
    // ===================================================

    // مدیریت دکمه برگشت (Back) گوشی
    @Override
    public void onBackPressed() {
        if (isScannerActive) {
            // اگر دوربین وب‌ویو باز بود (فالبک)
            webView.evaluateJavascript("closeScanner();", null);
        } else if (isAssetActive) {
            // اگر در صفحه جزئیات بودیم، برو به صفحه اصلی
            webView.evaluateJavascript("navigation.showScreen('main-screen');", null);
        } else {
            // اگر در صفحه اصلی بودیم، برنامه را ببند
            super.onBackPressed();
        }
    }
}
