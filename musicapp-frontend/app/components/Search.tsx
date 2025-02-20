"use client";

import { useState } from "react";

export default function Search({ accessToken }: { accessToken: string | null }) {
    const [query, setQuery] = useState<string>("");
    const [results, setResults] = useState<any[]>([]);

    async function searchSpotify() {
        if (!accessToken) return;
        const response = await fetch(
            `https://api.spotify.com/v1/search?q=${encodeURIComponent(query)}&type=track`,
            { headers: { Authorization: `Bearer ${accessToken}` } }
        );
        if (response.ok) {
            const data = await response.json();
            setResults(data.tracks.items);
        } else {
            console.error("Failed to search Spotify.");
        }
    }

    return (
        <div>
            <h2 className="text-2xl font-semibold mb-4">🔍 Search for Songs</h2>
            <div className="flex items-center gap-2">
                <input
                    type="text"
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                    placeholder="Search for songs..."
                    className="border px-4 py-2 rounded w-full"
                />
                <button onClick={searchSpotify} className="px-4 py-2 bg-green-500 text-white rounded">
                    Search
                </button>
            </div>

            {results.length > 0 && (
                <ul className="mt-4">
                    {results.map((track) => (
                        <li key={track.id} className="my-2">
                            🎵 {track.name} by {track.artists.map((a: any) => a.name).join(", ")}
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
}
