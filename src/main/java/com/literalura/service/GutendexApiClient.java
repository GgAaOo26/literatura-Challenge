package com.literalura.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class GutendexApiClient {

    private static final String BASE_URL = "https://gutendex.com/books/";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GutendexApiClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    // Buscar libros por título
    public JsonNode fetchBooksByTitle(String title) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "?search=" + title))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Error al buscar libros en Gutendex. Código de estado: " + response.statusCode());
        }

        return objectMapper.readTree(response.body());
    }
}
