package com.jinkim.musicapp.controllers;

import com.jinkim.musicapp.models.User;
import com.jinkim.musicapp.services.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class SpotifyAuthController {

    @Value("${spotify.client.id}")
    private String clientId;

    @Value("${spotify.client.secret}")
    private String clientSecret;

    @Value("${spotify.redirect.uri}")
    private String redirectUri;

    private final UserService userService;
    private final RestTemplate restTemplate;

    public SpotifyAuthController(UserService userService) {
        this.userService = userService;
        this.restTemplate = new RestTemplate();
    }

    // Step 1: Redirect users to Spotify's login page
    @GetMapping("/login")
    public ResponseEntity<Void> login() {
        String spotifyAuthUrl = "https://accounts.spotify.com/authorize"
                + "?client_id=" + clientId
                + "&response_type=code"
                + "&redirect_uri=" + redirectUri
                + "&scope=user-read-private%20user-read-email%20playlist-modify-public%20playlist-modify-private";

        // Redirect directly instead of returning the URL
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", spotifyAuthUrl)
                .build();
    }

    // Step 2: Handle the callback after user logs in
    @GetMapping("/callback")
    public ResponseEntity<Void> callback(@RequestParam("code") String code) {
        // Spotify token URL
        String tokenUrl = "https://accounts.spotify.com/api/token";

        // Prepare request headers and body
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("redirect_uri", redirectUri);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        // Send the POST request to get the access token
        ResponseEntity<Map> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, request, Map.class);
        Map<String, Object> responseBody = response.getBody();

        if (responseBody == null || !responseBody.containsKey("access_token")
                || !responseBody.containsKey("refresh_token")) {
            return ResponseEntity.badRequest().build();
        }

        String accessToken = (String) responseBody.get("access_token");
        String refreshToken = (String) responseBody.get("refresh_token");

        // Fetch user profile using the access token
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> userRequest = new HttpEntity<>(userHeaders);

        ResponseEntity<Map> userResponse = restTemplate.exchange(
                "https://api.spotify.com/v1/me",
                HttpMethod.GET,
                userRequest,
                Map.class);

        Map<String, Object> userProfile = userResponse.getBody();
        if (userProfile == null) {
            return ResponseEntity.badRequest().build();
        }

        String spotifyId = (String) userProfile.get("id");
        String name = (String) userProfile.get("display_name");
        String email = (String) userProfile.get("email");
        String profilePicture = "";

        // Check for profile picture
        if (userProfile.containsKey("images") && userProfile.get("images") instanceof List<?> imagesList
                && !imagesList.isEmpty()) {
            Map<String, String> firstImage = (Map<String, String>) imagesList.get(0);
            profilePicture = firstImage.get("url");
        }

        // Save user to database if they don't already exist
        User user = userService.getUserBySpotifyId(spotifyId)
                .orElse(new User(null, spotifyId, name, email, profilePicture, null, null));

        userService.createOrUpdateUser(user);

        // Redirect to frontend with tokens
        String redirectUrl = "http://localhost:3000/callback"
                + "?access_token=" + accessToken
                + "&refresh_token=" + refreshToken;

        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", redirectUrl)
                .build();
    }

    // Step 3: Refresh access token
    @GetMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshAccessToken(@RequestParam String refreshToken) {
        String tokenUrl = "https://accounts.spotify.com/api/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", refreshToken);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
        Map<String, Object> responseBody = response.getBody();

        if (responseBody == null || !responseBody.containsKey("access_token")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to refresh token."));
        }

        String newAccessToken = (String) responseBody.get("access_token");

        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", newAccessToken);

        return ResponseEntity.ok(tokens);
    }
}