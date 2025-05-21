package com.rhino.wallset;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CohereEmbeddingHelper {

    private static final String TAG = "CohereEmbeddingHelper";
    private static final String COHERE_API_KEY = BuildConfig.COHERE_API_KEY;
    private static final String COHERE_URL = "https://api.cohere.ai/v1/embed";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public interface EmbeddingCallback {
        void onSuccess(float[] embedding);
        void onFailure(String error);
    }

    public static void getEmbedding(String inputText, EmbeddingCallback callback) {
        OkHttpClient client = new OkHttpClient.Builder()
                .callTimeout(15, TimeUnit.SECONDS)
                .build();

        try {
            JSONObject bodyJson = new JSONObject();
            JSONArray texts = new JSONArray();
            texts.put(inputText);
            bodyJson.put("texts", texts);
            bodyJson.put("model", "embed-english-v3.0");
            bodyJson.put("input_type", "search_document");

            RequestBody requestBody = RequestBody.create(bodyJson.toString(), JSON);

            Request request = new Request.Builder()
                    .url(COHERE_URL)
                    .addHeader("Authorization", "Bearer " + COHERE_API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    Log.e(TAG, "Cohere request failed", e);
                    callback.onFailure("Request failed: " + e.getMessage());
                }

                @Override
                public void onResponse(okhttp3.Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                        callback.onFailure("HTTP " + response.code() + ": " + errorBody);
                        return;
                    }

                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONArray embeddingsArray = jsonResponse.getJSONArray("embeddings").getJSONArray(0);

                        float[] embedding = new float[embeddingsArray.length()];
                        for (int i = 0; i < embeddingsArray.length(); i++) {
                            embedding[i] = (float) embeddingsArray.getDouble(i);
                        }

                        callback.onSuccess(embedding);
                    } catch (Exception e) {
                        callback.onFailure("Parsing error: " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            callback.onFailure("JSON error: " + e.getMessage());
        }
    }
}
