package com.flymaccin.thedaw;

import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private static final String SERVER_URL = "server_url";
    private static final int GOLD = Color.rgb(214, 179, 90);
    private static final int BACKGROUND = Color.rgb(9, 9, 12);
    private static final int PANEL = Color.rgb(24, 24, 31);
    private static final int WHITE = Color.rgb(240, 240, 244);
    private static final int MUTED = Color.rgb(170, 170, 180);

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private String connectedOrigin;
    private WebView webView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(BACKGROUND);
        showConnectScreen();
    }

    private TextView text(String value, int size, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setPadding(18, 12, 18, 12);
        return view;
    }

    private Button button(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextColor(WHITE);
        button.setBackgroundColor(PANEL);
        return button;
    }

    private LinearLayout column() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 18, 20, 18);
        layout.setBackgroundColor(BACKGROUND);
        return layout;
    }

    private void showConnectScreen() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
            webView = null;
        }
        LinearLayout layout = column();
        layout.addView(text("theDAW", 30, GOLD));
        layout.addView(text("Connect to theDAW running on your computer", 17, WHITE));
        layout.addView(text(
                "Use the computer's LAN address and frontend port, for example "
                        + "http://192.168.1.20:5173. Both devices must be on the same network.",
                14, MUTED));

        TextView label = text("Desktop/server address", 14, WHITE);
        EditText address = new EditText(this);
        address.setId(View.generateViewId());
        label.setLabelFor(address.getId());
        address.setSingleLine(true);
        address.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        address.setHint("http://192.168.1.20:5173");
        address.setText(getPreferences(MODE_PRIVATE).getString(SERVER_URL, ""));
        address.setTextColor(WHITE);
        address.setHintTextColor(MUTED);
        address.setSelectAllOnFocus(true);
        layout.addView(label);
        layout.addView(address);

        ProgressBar progress = new ProgressBar(this);
        progress.setVisibility(View.GONE);
        Button connect = button("Connect");
        connect.setOnClickListener(view -> connect(address.getText().toString(), connect, progress));
        layout.addView(connect);
        layout.addView(progress);
        setContentView(layout);
    }

    private void connect(String input, Button connect, ProgressBar progress) {
        final Uri origin;
        try {
            origin = parseOrigin(input);
        } catch (IllegalArgumentException error) {
            Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        connect.setEnabled(false);
        progress.setVisibility(View.VISIBLE);
        executor.execute(() -> {
            String error = checkBackend(origin);
            runOnUiThread(() -> {
                if (isFinishing()) return;
                connect.setEnabled(true);
                progress.setVisibility(View.GONE);
                if (error != null) {
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                    return;
                }
                connectedOrigin = origin.toString();
                getPreferences(MODE_PRIVATE).edit().putString(SERVER_URL, connectedOrigin).apply();
                showCompanion();
            });
        });
    }

    private Uri parseOrigin(String input) {
        Uri uri = Uri.parse(input.trim());
        String scheme = uri.getScheme();
        String path = uri.getPath();
        if (!("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                || uri.getHost() == null || uri.getUserInfo() != null
                || uri.getQuery() != null || uri.getFragment() != null
                || !(path == null || path.isEmpty() || "/".equals(path)
                || "/mobile.html".equals(path))) {
            throw new IllegalArgumentException(
                    "Enter an http(s) server address only, such as http://192.168.1.20:5173");
        }
        return new Uri.Builder()
                .scheme(scheme.toLowerCase())
                .encodedAuthority(uri.getEncodedAuthority())
                .build();
    }

    private String checkBackend(Uri origin) {
        HttpURLConnection connection = null;
        try {
            URL healthUrl = new URL(origin.toString() + "/api/health");
            connection = (HttpURLConnection) healthUrl.openConnection();
            connection.setConnectTimeout(4000);
            connection.setReadTimeout(4000);
            connection.setRequestMethod("GET");
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "The server responded with HTTP " + connection.getResponseCode() + ".";
            }
            StringBuilder body = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) body.append(line);
            }
            if (!"ok".equals(new org.json.JSONObject(body.toString()).optString("status"))) {
                return "That address responded, but it doesn't look like theDAW backend.";
            }
            return null;
        } catch (Exception error) {
            return "Can't reach theDAW at " + origin
                    + ". Check the address, network, and that the desktop app is running.";
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private void showCompanion() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(BACKGROUND);

        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setGravity(android.view.Gravity.CENTER_VERTICAL);
        Button disconnect = button("Servers");
        disconnect.setOnClickListener(view -> showConnectScreen());
        toolbar.addView(disconnect);
        toolbar.addView(text("Connected to " + Uri.parse(connectedOrigin).getHost(), 13, MUTED));
        layout.addView(toolbar);

        webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        webView.setBackgroundColor(BACKGROUND);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri target = request.getUrl();
                if (sameOrigin(target, connectedOrigin)) return false;
                if ("http".equals(target.getScheme()) || "https".equals(target.getScheme())) {
                    try {
                        startActivity(new android.content.Intent(
                                android.content.Intent.ACTION_VIEW, target));
                    } catch (Exception ignored) {
                        Toast.makeText(MainActivity.this, "Unable to open that link.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            }
        });
        layout.addView(webView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
        setContentView(layout);
        webView.loadUrl(connectedOrigin + "/mobile.html");
    }

    private boolean sameOrigin(Uri target, String originText) {
        Uri origin = Uri.parse(originText);
        return target.getScheme() != null && target.getScheme().equalsIgnoreCase(origin.getScheme())
                && target.getHost() != null && target.getHost().equalsIgnoreCase(origin.getHost())
                && target.getPort() == origin.getPort();
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
            webView = null;
        }
        executor.shutdownNow();
        super.onDestroy();
    }
}
