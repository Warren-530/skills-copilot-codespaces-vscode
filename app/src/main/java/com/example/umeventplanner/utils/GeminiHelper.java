package com.example.umeventplanner.utils;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GeminiHelper {
    private static final String API_KEY = "AIzaSyDdH7GDLtECc2vFpXAq-UjPO4YUMT-0Ewc"; // Placeholder
    private static final String URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + API_KEY;

    public interface GeminiCallback {
        void onSuccess(String response);
        void onError(String error);
    }

    public static void generateContent(Context context, String prompt, GeminiCallback callback) {
        RequestQueue queue = Volley.newRequestQueue(context);

        JSONObject jsonBody = new JSONObject();
        try {
            JSONArray parts = new JSONArray();
            JSONObject part = new JSONObject();
            part.put("text", prompt);
            parts.put(part);

            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            content.put("parts", parts);
            contents.put(content);

            jsonBody.put("contents", contents);

        } catch (JSONException e) {
            callback.onError(e.getMessage());
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, jsonBody,
                response -> {
                    try {
                        JSONArray candidates = response.getJSONArray("candidates");
                        if (candidates.length() > 0) {
                            JSONObject content = candidates.getJSONObject(0).getJSONObject("content");
                            JSONArray parts = content.getJSONArray("parts");
                            String text = parts.getJSONObject(0).getString("text");
                            callback.onSuccess(text);
                        } else {
                            callback.onError("No response candidates");
                        }
                    } catch (JSONException e) {
                        callback.onError("Parsing error: " + e.getMessage());
                    }
                },
                error -> callback.onError("Network error: " + error.getMessage())
        );

        queue.add(request);
    }
}
