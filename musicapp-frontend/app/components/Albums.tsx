"use client";

import { useEffect, useState } from "react";

export default function Albums({ accessToken }: { accessToken: string | null }) {
    const [albums, setAlbums] = useState<any[]>([]);

    useEffect(() => {
        if (!accessToken) return;

        async function fetchAlbums() {
            const response = await fetch("https://api.spotify.com/v1/me/albums", {
                headers: { Authorization: `Bearer ${accessToken}` },
            });

            if (response.ok) {
                const data = await response.json();
                setAlbums(data.items);
            } else {
                console.error("Failed to fetch albums.");
            }
        }

        fetchAlbums();
    }, [accessToken]);

    return (
        <div>
            <h2 className="text-2xl font-semibold mb-4">📀 Your Saved Albums</h2>
            {albums.length > 0 ? (
                <ul>
                    {albums.map((item) => (
                        <li key={item.album.id} className="my-2">
                            🎸 {item.album.name} by {item.album.artists.map((a: any) => a.name).join(", ")}
                        </li>
                    ))}
                </ul>
            ) : (
                <p>No saved albums found.</p>
            )}
        </div>
    );
}
