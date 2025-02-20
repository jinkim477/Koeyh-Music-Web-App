// app/page.tsx
"use client";

export default function HomePage() {
    const handleLogin = () => {
        window.location.href = "http://localhost:8080/auth/login";
    };

    return (
        <main className="flex min-h-screen items-center justify-center bg-gray-100">
            <div className="text-center">
                <h1 className="text-4xl font-bold mb-6">Welcome to Koeyh 🎵</h1>
                <p className="mb-4">Log in with your Spotify account to start streaming music.</p>
                <button
                    onClick={handleLogin}
                    className="px-6 py-3 bg-green-500 text-white rounded-lg hover:bg-green-600 transition"
                >
                    Login with Spotify
                </button>
            </div>
        </main>
    );
}
