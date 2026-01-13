package com.lavaderosepulveda.crm.api.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.lavaderosepulveda.crm.config.ConfigManager;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ApiClient {

    private static ApiClient instance;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final ConfigManager config;
    private final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private ApiClient() {
        this.config = ConfigManager.getInstance();
        this.httpClient = crearHttpClient();
        this.gson = crearGson();
    }

    public static synchronized ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }

    private OkHttpClient crearHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(config.getConnectTimeout(), TimeUnit.SECONDS)
                .readTimeout(config.getReadTimeout(), TimeUnit.SECONDS)
                .writeTimeout(config.getWriteTimeout(), TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder();

                    // Agregar headers comunes
                    requestBuilder.header("Content-Type", "application/json");
                    requestBuilder.header("Accept", "application/json");

                    // Agregar token de autenticación si está habilitado
                    if (config.isAuthEnabled() && !config.getAuthToken().isEmpty()) {
                        requestBuilder.header("Authorization", "Bearer " + config.getAuthToken());
                    }

                    Request request = requestBuilder.build();
                    log.debug("Request: {} {}", request.method(), request.url());

                    return chain.proceed(request);
                })
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    Response response = chain.proceed(request);

                    log.debug("Response: {} - Status: {}", request.url(), response.code());

                    return response;
                });

        return builder.build();
    }

    private Gson crearGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> LocalDateTime
                                .parse(json.getAsString(), DateTimeFormatter.ISO_DATE_TIME))
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonSerializer<LocalDateTime>) (src, typeOfSrc,
                                context) -> new JsonPrimitive(src.format(DateTimeFormatter.ISO_DATE_TIME)))
                .registerTypeAdapter(LocalDate.class,
                        (JsonDeserializer<LocalDate>) (json, typeOfT, context) -> LocalDate.parse(json.getAsString(),
                                DateTimeFormatter.ISO_DATE))
                .registerTypeAdapter(LocalDate.class,
                        (JsonSerializer<LocalDate>) (src, typeOfSrc,
                                context) -> new JsonPrimitive(src.format(DateTimeFormatter.ISO_DATE)))
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create();
    }

    /**
     * GET request
     */
    public <T> T get(String url, Class<T> responseType) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Error en GET: " + response.code() + " - " + response.message());
            }

            String responseBody = response.body().string();
            log.debug("GET Response: {}", responseBody);

            return gson.fromJson(responseBody, responseType);
        }
    }

    /**
     * GET request que retorna String (para respuestas no JSON)
     */
    public String getRaw(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Error en GET: " + response.code() + " - " + response.message());
            }

            return response.body().string();
        }
    }

    /**
     * POST request
     */
    public <T, R> R post(String url, T body, Class<R> responseType) throws IOException {
        String jsonBody = gson.toJson(body);
        log.debug("POST Body: {}", jsonBody);

        RequestBody requestBody = RequestBody.create(jsonBody, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Sin cuerpo";
                throw new IOException("Error en POST: " + response.code() + " - " + errorBody);
            }

            String responseBody = response.body().string();
            log.debug("POST Response: {}", responseBody);

            if (responseType == Void.class) {
                return null;
            }

            return gson.fromJson(responseBody, responseType);
        }
    }

    /**
     * POST request que devuelve el JSON raw como String
     */
    public <T> String postRaw(String url, T body) throws IOException {
        String jsonBody = gson.toJson(body);
        log.debug("POST Body: {}", jsonBody);

        RequestBody requestBody = RequestBody.create(jsonBody, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Sin cuerpo";
                throw new IOException("Error en POST: " + response.code() + " - " + errorBody);
            }

            String responseBody = response.body().string();
            log.debug("POST Response: {}", responseBody);
            return responseBody;
        }
    }

    /**
     * PUT request
     */
    public <T, R> R put(String url, T body, Class<R> responseType) throws IOException {
        String jsonBody = body != null ? gson.toJson(body) : "{}";
        log.debug("PUT Body: {}", jsonBody);

        RequestBody requestBody = RequestBody.create(jsonBody, JSON);
        Request request = new Request.Builder()
                .url(url)
                .put(requestBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Sin cuerpo";
                throw new IOException("Error en PUT: " + response.code() + " - " + errorBody);
            }

            String responseBody = response.body().string();
            log.debug("PUT Response: {}", responseBody);

            if (responseType == Void.class) {
                return null;
            }

            return gson.fromJson(responseBody, responseType);
        }
    }

    /**
     * DELETE request
     */
    public void delete(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .delete()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Error en DELETE: " + response.code() + " - " + response.message());
            }

            log.debug("DELETE exitoso");
        }
    }

    /**
     * Verifica la conexión con la API
     */
    public boolean testConnection() {
        try {
            String baseUrl = config.getApiBaseUrl();
            Request request = new Request.Builder()
                    .url(baseUrl + "/actuator/health") // Endpoint de health si existe
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                boolean success = response.isSuccessful();
                log.info("Test de conexión API: {}", success ? "OK" : "FALLO");
                return success;
            }
        } catch (Exception e) {
            log.error("Error al probar conexión con la API", e);
            return false;
        }
    }

    public Gson getGson() {
        return gson;
    }

    /**
     * PUT request que devuelve el JSON raw como String
     */
    public <T> String putRaw(String url, T body) throws IOException {
        String jsonBody = gson.toJson(body);
        log.debug("PUT Body: {}", jsonBody);

        RequestBody requestBody = RequestBody.create(jsonBody, JSON);
        Request request = new Request.Builder()
                .url(url)
                .put(requestBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Sin cuerpo";
                throw new IOException("Error en PUT: " + response.code() + " - " + errorBody);
            }

            String responseBody = response.body().string();
            log.debug("PUT Response: {}", responseBody);
            return responseBody;
        }
    }

    /**
     * GET request que devuelve bytes (para descargar archivos)
     */
    public byte[] getBytes(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Error en GET: " + response.code() + " - " + response.message());
            }

            return response.body().bytes();
        }
    }
}
