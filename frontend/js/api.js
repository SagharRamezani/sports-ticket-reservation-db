const API_BASE_URL = "http://localhost:8080";

async function apiGet(path) {
    const response = await fetch(API_BASE_URL + path);
    return response.json();
}

async function apiPost(path, body) {
    const response = await fetch(API_BASE_URL + path, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(body)
    });

    return response.json();
}
