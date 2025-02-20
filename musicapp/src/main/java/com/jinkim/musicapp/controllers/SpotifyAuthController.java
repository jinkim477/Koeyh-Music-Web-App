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
import java.util.Optional;

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
        String tokenUrl = "https://accounts.spotify.com/api/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("redirect_uri", redirectUri);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        try {
            // Exchange code for tokens
            ResponseEntity<Map> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, requestEntity, Map.class);
            Map<String, Object> tokens = response.getBody();

            if (tokens == null || !tokens.containsKey("access_token")) {
                return ResponseEntity.badRequest().build();
            }

            String accessToken = (String) tokens.get("access_token");
            String refreshToken = (String) tokens.get("refresh_token");

            // Fetch user profile from Spotify
            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.set("Authorization", "Bearer " + accessToken);
            HttpEntity<String> userRequest = new HttpEntity<>(userHeaders);

            ResponseEntity<Map> userResponse = restTemplate.exchange(
                    "https://api.spotify.com/v1/me",
                    HttpMethod.GET,
                    userRequest,
                    Map.class);

            Map<String, Object> userProfile = userResponse.getBody();
            if (userProfile == null || !userProfile.containsKey("id")) {
                return ResponseEntity.badRequest().build();
            }

            // Extract user info
            String spotifyId = (String) userProfile.get("id");
            String name = (String) userProfile.get("display_name");
            String email = (String) userProfile.get("email");

            // Create or update user
            User user = userService.getUserBySpotifyId(spotifyId)
                    .orElse(User.builder()
                            .spotifyId(spotifyId)
                            .name(name)
                            .email(email)
                            .profilePicture(
                                    userProfile.get("images") != null
                                            ? ((List<Map<String, String>>) userProfile.get("images"))
                                                    .stream().findFirst().map(img -> img.get("url")).orElse(null)
                                            : null)
                            .savedAlbums(List.of())
                            .playlists(List.of())
                            .accessToken(accessToken)
                            .refreshToken(refreshToken)
                            .build());

            user.setAccessToken(accessToken);
            user.setRefreshToken(refreshToken);
            userService.createOrUpdateUser(user);

            // Redirect to frontend with tokens in URL
            String redirectUrl = String.format(
                    "http://localhost:3000/home?access_token=%s&refresh_token=%s",
                    accessToken, refreshToken);

            return ResponseEntity.status(HttpStatus.FOUND).header("Location", redirectUrl).build();

        } catch (Exception e) {
            System.err.println("Error during callback: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Missing or invalid token."));
        }

        String accessToken = authHeader.substring(7); // Extract token from Bearer header

        // Find user by access token in the database
        Optional<User> user = userService.getUserByAccessToken(accessToken);

        if (user.isPresent()) {
            String userId = user.get().getId();
            return ResponseEntity.ok(Map.of("userId", userId));
        } else {
            return ResponseEntity.status(404).body(Map.of("error", "User not found with provided access token."));
        }
    }
}