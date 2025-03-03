package project;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DeezerService {
    private static final String API_BASE_URL = "https://api.deezer.com";

    public List<Song> searchTracks(String query) {
        List<Song> results = new ArrayList<>();

        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
            URL url = new URL(API_BASE_URL + "/search?q=" + encodedQuery);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String response = in.lines().collect(Collectors.joining());
                in.close();

                JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
                JsonArray data = jsonResponse.getAsJsonArray("data");

                for (int i = 0; i < data.size(); i++) {
                    JsonObject trackObject = data.get(i).getAsJsonObject();

                    String id = trackObject.get("id").getAsString();
                    String title = trackObject.get("title").getAsString();

                    JsonElement artistElement = trackObject.get("artist");
                    String artistName = "Unknown Artist";
                    if (artistElement != null && artistElement.isJsonObject()) {
                        JsonObject artist = artistElement.getAsJsonObject();
                        artistName = artist.get("name").getAsString();
                    }

                    String albumCoverUrl = "";
                    JsonElement albumElement = trackObject.get("album");
                    if (albumElement != null && albumElement.isJsonObject()) {
                        JsonObject album = albumElement.getAsJsonObject();
                        JsonElement coverElement = album.get("cover");
                        if (coverElement != null && coverElement.isJsonObject()) {
                            JsonObject cover = coverElement.getAsJsonObject();
                            JsonElement mediumElement = cover.get("medium");
                            if (mediumElement != null) {
                                albumCoverUrl = mediumElement.getAsString();
                            }
                        }
                    }

                    String previewUrl = "";
                    JsonElement previewElement = trackObject.get("preview");
                    if (previewElement != null) {
                        previewUrl = previewElement.getAsString();
                    }

                    Song song = new Song();
                    song.setId(id);
                    song.setName(title);
                    song.setArtist(artistName);
                    song.setImageUrl(albumCoverUrl);
                    song.setPreviewUrl(previewUrl);

                    results.add(song);
                }
            } else {
                System.out.println("HTTP error: " + responseCode);
            }

        } catch (Exception e) {
            System.out.println("Error searching tracks: " + e.getMessage());
            e.printStackTrace();
        }

        return results;
    }
}