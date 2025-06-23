package com.maapp.matapp;
import android.app.PictureInPictureParams;
import android.util.Rational;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Rational;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.webkit.*;
import android.widget.FrameLayout;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import java.io.ByteArrayInputStream;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private FrameLayout customViewContainer;
    private View mCustomView;
    private WebChromeClient.CustomViewCallback customViewCallback;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        customViewContainer = findViewById(R.id.fullscreen_container);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setDomStorageEnabled(true);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccess(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setUserAgentString(settings.getUserAgentString() + " AnimeApp");
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        WebView.setWebContentsDebuggingEnabled(false);

        webView.setWebViewClient(new AdBlockWebViewClient());
        webView.setWebChromeClient(new MyWebChromeClient());

        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setMimeType(mimetype);
            request.addRequestHeader("User-Agent", userAgent);
            request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir("/Download", URLUtil.guessFileName(url, contentDisposition, mimetype));
            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            dm.enqueue(request);
        });

        webView.loadUrl("https://magyaranime.eu");
    }

    private static class AdBlocker {
        private static final String[] blocked = {
                "ads.", "doubleclick", "googlesyndication", "adservice.google", "google-analytics", "facebook.net", "banner", "popunder"
        };

        public static boolean isAd(String url) {
            for (String b : blocked) {
                if (url.contains(b)) return true;
            }
            return false;
        }
    }

    private static class AdBlockWebViewClient extends WebViewClient {
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            if (AdBlocker.isAd(request.getUrl().toString())) {
                return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream("".getBytes()));
            }
            return super.shouldInterceptRequest(view, request);
        }
    }

    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            if (mCustomView != null) {
                callback.onCustomViewHidden();
                return;
            }

            mCustomView = view;
            customViewContainer.addView(view);
            webView.setVisibility(View.GONE);
            customViewContainer.setVisibility(View.VISIBLE);
            customViewCallback = callback;

            // Immersive fullscreen mód beállítása (API 19+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                customViewContainer.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            } else {
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }

            //FScreen
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        @Override
        public void onHideCustomView() {
            if (mCustomView == null) return;

           
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                customViewContainer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            } else {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }

            customViewContainer.removeView(mCustomView);
            mCustomView = null;
            webView.setVisibility(View.VISIBLE);
            customViewContainer.setVisibility(View.GONE);
            if (customViewCallback != null) {
                customViewCallback.onCustomViewHidden();
                customViewCallback = null;
            }

            
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public void onBackPressed() {
        if (customViewContainer.getVisibility() == View.VISIBLE) {
            ((WebChromeClient) webView.getWebChromeClient()).onHideCustomView();
        } else if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onUserLeaveHint() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && webView != null) {
            Rational aspectRatio = new Rational(webView.getWidth(), webView.getHeight());
            PictureInPictureParams.Builder pipBuilder = new PictureInPictureParams.Builder();
            pipBuilder.setAspectRatio(aspectRatio);
            enterPictureInPictureMode(pipBuilder.build());
        }
    }
}
