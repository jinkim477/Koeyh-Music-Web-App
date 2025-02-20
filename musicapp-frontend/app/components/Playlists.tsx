"use client";

import { useEffect, useState } from "react";

type Playlist = {
	id: string;
	name: string;
	description: string;
	trackIds: string[];
};

export default function Playlists({
	accessToken,
}: {
	accessToken: string | null;
}) {
	const [playlists, setPlaylists] = useState<Playlist[]>([]);
	const [newPlaylist, setNewPlaylist] = useState({ name: "", description: "" });
	const [loading, setLoading] = useState(false);
	const [userId, setUserId] = useState<string | null>(null);

	// Fetch Spotify User ID
	const fetchUserId = async () => {
		const token = localStorage.getItem("spotifyAccessToken");
		if (!token) {
			console.error("No access token available.");
			return;
		}

		const response = await fetch("http://localhost:8080/auth/me", {
			method: "GET",
			headers: {
				Authorization: `Bearer ${token}`, // Pass access token
				"Content-Type": "application/json",
			},
		});

		if (response.ok) {
			const data = await response.json();
			setUserId(data.userId);
			console.log("Fetched User ID:", data.userId);
		} else {
			console.error("Failed to fetch user ID. Status:", response.status);
		}
	};

	// Fetch user playlists from your backend
	// Fetch user playlists from your backend
	const fetchPlaylists = async () => {
		if (!userId) {
			console.error("User ID not available.");
			return;
		}

		setLoading(true);
		try {
			const response = await fetch(
				`http://localhost:8080/api/playlists/user/${userId}`
			);
			if (response.ok) {
				const data = await response.json();
				setPlaylists(data); // Even if empty, this will now work.
				console.log("Playlists fetched:", data);
			} else {
				console.error("Failed to fetch playlists. Status:", response.status);
				setPlaylists([]); // Ensure the UI doesn't break.
			}
		} catch (error) {
			console.error("Error fetching playlists:", error);
			setPlaylists([]); // Fallback to empty list on error.
		} finally {
			setLoading(false);
		}
	};

	// Create a new playlist
	const createPlaylist = async () => {
		if (!newPlaylist.name || !userId) {
			alert("Playlist name and user ID are required!");
			return;
		}

		try {
			const response = await fetch(
				"http://localhost:8080/api/playlists/create",
				{
					method: "POST",
					headers: { "Content-Type": "application/json" },
					body: JSON.stringify({
						userId,
						name: newPlaylist.name,
						description: newPlaylist.description,
						trackIds: [],
					}),
				}
			);

			if (response.ok) {
				setNewPlaylist({ name: "", description: "" });
				fetchPlaylists();
			} else {
				console.error("Failed to create playlist.");
			}
		} catch (error) {
			console.error("Error creating playlist:", error);
		}
	};

	// Delete a playlist
	const deletePlaylist = async (id: string) => {
		if (!id) return;

		try {
			const response = await fetch(
				`http://localhost:8080/api/playlists/delete/${id}`,
				{
					method: "DELETE",
				}
			);

			if (response.ok) {
				fetchPlaylists();
			} else {
				console.error("Failed to delete playlist.");
			}
		} catch (error) {
			console.error("Error deleting playlist:", error);
		}
	};

	// Load user ID and playlists on component mount
	useEffect(() => {
		fetchUserId();
	}, [accessToken]);

	useEffect(() => {
		if (userId) fetchPlaylists();
	}, [userId]);

	return (
		<div>
			<h2 className="text-2xl font-semibold mb-4">🎶 Your Playlists</h2>

			{/* Create New Playlist */}
			<div className="mb-6">
				<h3 className="text-lg font-medium">Create New Playlist</h3>
				<input
					type="text"
					placeholder="Playlist Name"
					value={newPlaylist.name}
					onChange={(e) =>
						setNewPlaylist({ ...newPlaylist, name: e.target.value })
					}
					className="border p-2 rounded w-full mt-2"
				/>
				<input
					type="text"
					placeholder="Description"
					value={newPlaylist.description}
					onChange={(e) =>
						setNewPlaylist({ ...newPlaylist, description: e.target.value })
					}
					className="border p-2 rounded w-full mt-2"
				/>
				<button
					onClick={createPlaylist}
					className="mt-2 px-4 py-2 bg-green-500 text-white rounded"
				>
					Create Playlist
				</button>
			</div>

			{/* Display Playlists */}
			{loading ? (
				<p>Loading playlists...</p>
			) : playlists.length > 0 ? (
				<ul>
					{playlists.map((playlist) => (
						<li key={playlist.id} className="mb-4 border p-4 rounded bg-white">
							<h3 className="text-xl font-bold">{playlist.name}</h3>
							<p>{playlist.description || "No description available."}</p>
							<button
								onClick={() => deletePlaylist(playlist.id)}
								className="mt-2 px-3 py-1 bg-red-500 text-white rounded"
							>
								Delete
							</button>
						</li>
					))}
				</ul>
			) : (
				<p>No playlists found.</p>
			)}
		</div>
	);
}
