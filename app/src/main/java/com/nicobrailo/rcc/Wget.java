package com.nicobrailo.rcc;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Wget extends AsyncTask<String, Void, String> {
    public interface Callback {
        void onConnectionError(final String message);
        void onResponse(final String result);
        void onHttpNotOkResponse();
    }

    private static final int CONN_TIMEOUT_MS = 10000;
    private static final int HTTP_RESPONSE_UNAUTHORIZED = 401;
    private static final int HTTP_RESPONSE_OK = 200;

    private final Callback callback;
    private Exception request_exception;
    private int httpRetCode;
    private boolean stillRunning;

    public Wget(Callback callback) {
        this.callback = callback;
        this.request_exception = null;
        this.stillRunning = false;
    }

    public boolean wget(final String url) {
        if (stillRunning) return false;
        this.execute(url);
        return true;
    }

    private HttpURLConnection getConnection(final String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) (new URL(url)).openConnection();
        conn.setReadTimeout(CONN_TIMEOUT_MS);
        conn.setConnectTimeout(CONN_TIMEOUT_MS);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        return conn;
    }

    private String readAll(HttpURLConnection conn) throws IOException {
        try (InputStream is = conn.getInputStream()) {
            java.util.Scanner s = new java.util.Scanner(is, "UTF-8").useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        }
    }

    @Override
    protected String doInBackground(String... urls) {
        if (stillRunning) {
            return null;
        }

        stillRunning = true;

        try {
            HttpURLConnection conn = getConnection(urls[0]);
            httpRetCode = conn.getResponseCode();
            String content = readAll(conn);
            conn.disconnect();
            stillRunning = false;
            return content;
        } catch (IOException e) {
            request_exception = e;
            return null;
        } finally {
            stillRunning = false;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (request_exception != null) {
            callback.onConnectionError(request_exception.getMessage());
            request_exception = null;
        } else if (httpRetCode == HTTP_RESPONSE_OK) {
            callback.onResponse(result);
        } else {
            callback.onHttpNotOkResponse();
        }
    }
}