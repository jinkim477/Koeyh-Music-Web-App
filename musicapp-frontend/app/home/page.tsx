"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Playlists from "../components/Playlists";
import Albums from "../components/Albums";
import Search from "../components/Search";

export default function HomePage() {
    const router = useRouter();
    const [activeTab, setActiveTab] = useState<string>("playlists");
    const [accessToken, setAccessToken] = useState<string | null>(null);

    // Get tokens from URL or localStorage
    useEffect(() => {
        const urlParams = new URLSearchParams(window.location.search);
        const accessTokenFromUrl = urlParams.get("access_token");
        const refreshTokenFromUrl = urlParams.get("refresh_token");

        // Store tokens in localStorage if present in URL
        if (accessTokenFromUrl && refreshTokenFromUrl) {
            localStorage.setItem("spotifyAccessToken", accessTokenFromUrl);
            localStorage.setItem("spotifyRefreshToken", refreshTokenFromUrl);
            setAccessToken(accessTokenFromUrl);
            router.push("/home");  // Clean the URL
        } else {
            const storedToken = localStorage.getItem("spotifyAccessToken");
            if (!storedToken) {
                console.warn("No access token found. Redirecting to login...");
                router.push("/");
            } else {
                setAccessToken(storedToken);
            }
        }
    }, []);

    // Handle logout
    const handleLogout = () => {
        localStorage.clear();
        router.push("/");
    };

    return (
        <main className="min-h-screen bg-gray-100 p-6">
            <h1 className="text-4xl font-bold mb-6">🎵 MusicApp Dashboard</h1>

            {/* Navigation Tabs */}
            <div className="flex space-x-4 mb-6">
                {["playlists", "albums", "search"].map((tab) => (
                    <button
                        key={tab}
                        onClick={() => setActiveTab(tab)}
                        className={`px-4 py-2 rounded ${activeTab === tab ? "bg-blue-500 text-white" : "bg-white border"}`}
                    >
                        {tab.charAt(0).toUpperCase() + tab.slice(1)}
                    </button>
                ))}
                <button
                    onClick={handleLogout}
                    className="ml-auto px-4 py-2 bg-red-500 text-white rounded"
                >
                    Log Out
                </button>
            </div>

            {/* Dynamic Tab Content */}
            <div className="bg-white p-4 rounded shadow">
                {activeTab === "playlists" && accessToken && <Playlists accessToken={accessToken} />}
                {activeTab === "albums" && accessToken && <Albums accessToken={accessToken} />}
                {activeTab === "search" && accessToken && <Search accessToken={accessToken} />}
            </div>
        </main>
    );
}
