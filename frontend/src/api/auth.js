const API_BASE_URL =
    import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export const loginUser = async (name, email) => {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            name,
            email,
        }),
    });

    const text = await response.text();

    let result = null;

    if (text.trim()) {
        try {
            result = JSON.parse(text);
        } catch (error) {
            console.error('Invalid JSON from server:', text);
            throw new Error('Server returned invalid JSON.');
        }
    }

    if (!response.ok) {
        throw new Error(
            result?.message ||
            result?.error ||
            `Login failed with status ${response.status}`
        );
    }

    if (!result) {
        throw new Error('Login failed: Server returned an empty response.');
    }

    return result;
};