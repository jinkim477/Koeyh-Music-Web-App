// app/home/page.tsx
"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";

export default function HomePage() {
    const router = useRouter();
    const [accessToken, setAccessToken] = useState<string | null>(null);
    const [query, setQuery] = useState<string>("");
    const [results, setResults] = useState<any[]>([]);

    // Refresh Access Token
    async function refreshAccessToken() {
        const refreshToken = localStorage.getItem("spotifyRefreshToken");

        if (!refreshToken) {
            console.error("No refresh token available.");
            return;
        }

        const response = await fetch(`http://localhost:8080/auth/refresh?refreshToken=${refreshToken}`);
        if (response.ok) {
            const data = await response.json();
            localStorage.setItem("spotifyAccessToken", data.access_token);
            console.log("Access token refreshed successfully.");
            setAccessToken(data.access_token);  // Update state with new token
        } else {
            console.error("Failed to refresh access token.");
            localStorage.clear();
            router.push("/");
        }
    }

    // Search Spotify API
    async function searchSpotify() {
        if (!accessToken) {
            console.error("No access token for search.");
            return;
        }

        const response = await fetch(
            `https://api.spotify.com/v1/search?q=${encodeURIComponent(query)}&type=track`,
            {
                headers: { Authorization: `Bearer ${accessToken}` },
            }
        );

        if (response.ok) {
            const data = await response.json();
            setResults(data.tracks.items);
        } else {
            console.error("Failed to search Spotify.");
        }
    }

    // Check token on page load
    useEffect(() => {
        const token = localStorage.getItem("spotifyAccessToken");

        if (!token) {
            console.error("No access token found. Redirecting to login...");
            router.push("/");
        } else {
            setAccessToken(token);
        }

        // Auto-refresh token every 50 minutes
        const interval = setInterval(() => {
            console.log("Refreshing access token...");
            refreshAccessToken();
        }, 50 * 60 * 1000); // 50 minutes

        return () => clearInterval(interval);  // Clean up interval
    }, []);

    return (
        <main className="flex min-h-screen items-center justify-center bg-gray-100">
            <div className="text-center">
                <h1 className="text-4xl font-bold mb-6">🎵 Welcome to MusicApp!</h1>

                {accessToken ? (
                    <div>
                        <p>Your Spotify access token is stored and ready for use.</p>

                        {/* Spotify Search Section */}
                        <div className="mt-6">
                            <input
                                type="text"
                                value={query}
                                onChange={(e) => setQuery(e.target.value)}
                                placeholder="Search for songs..."
                                className="border px-4 py-2 rounded"
                            />
                            <button
                                onClick={searchSpotify}
                                className="ml-2 px-4 py-2 bg-blue-500 text-white rounded"
                            >
                                Search
                            </button>
                        </div>

                        {/* Search Results */}
                        {results.length > 0 && (
                            <div className="mt-4 text-left">
                                <h2 className="text-xl font-semibold">Search Results:</h2>
                                {results.map((track) => (
                                    <div key={track.id} className="my-2">
                                        🎵 {track.name} by {track.artists.map((a: any) => a.name).join(", ")}
                                    </div>
                                ))}
                            </div>
                        )}

                        {/* Log Out Button */}
                        <button
                            onClick={() => {
                                localStorage.clear();
                                router.push("/");
                            }}
                            className="mt-6 px-4 py-2 bg-red-500 text-white rounded"
                        >
                            Log Out
                        </button>
                    </div>
                ) : (
                    <p>Checking authentication...</p>
                )}
            </div>
        </main>
    );
}
