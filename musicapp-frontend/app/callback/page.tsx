"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";

export default function CallbackPage() {
    const router = useRouter();

    useEffect(() => {
        const params = new URLSearchParams(window.location.search);
        const accessToken = params.get("access_token");
        const refreshToken = params.get("refresh_token");

        if (accessToken && refreshToken) {
            localStorage.setItem("spotifyAccessToken", accessToken);
            localStorage.setItem("spotifyRefreshToken", refreshToken);
            router.push("/home");  // Redirect after storing tokens
        } else {
            console.error("Failed to retrieve tokens.");
            router.push("/");
        }
    }, []);

    return (
        <div>
            <h1>Processing login...</h1>
            <p>Redirecting to home page...</p>
        </div>
    );
}
