// Phase 4 Frontend - Shared API Helper
// This is the only general access point for backend requests.

(function () {
    const DEFAULT_BASE_URL = "http://localhost:8080";
    const TOKEN_KEY = "sports_ticket_auth_token";
    const USER_KEY = "sports_ticket_current_user";
    const MOCK_MODE_KEY = "sports_ticket_mock_mode";

    function getBaseUrl() {
        const savedUrl = localStorage.getItem("sports_ticket_api_base_url");

        if (savedUrl && savedUrl.trim()) {
            return trimTrailingSlash(savedUrl.trim());
        }

        return DEFAULT_BASE_URL;
    }

    function setBaseUrl(url) {
        if (!url || !url.trim()) {
            localStorage.removeItem("sports_ticket_api_base_url");
            return;
        }

        localStorage.setItem("sports_ticket_api_base_url", trimTrailingSlash(url.trim()));
    }

    function trimTrailingSlash(value) {
        while (value.endsWith("/")) {
            value = value.slice(0, -1);
        }

        return value;
    }

    function getToken() {
        return localStorage.getItem(TOKEN_KEY);
    }

    function setToken(token) {
        if (!token) {
            clearToken();
            return;
        }

        localStorage.setItem(TOKEN_KEY, token);
    }

    function clearToken() {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
    }

    function setCurrentUser(user) {
        if (!user) {
            localStorage.removeItem(USER_KEY);
            return;
        }

        localStorage.setItem(USER_KEY, JSON.stringify(user));
    }

    function getCurrentUser() {
        const raw = localStorage.getItem(USER_KEY);

        if (!raw) {
            return null;
        }

        try {
            return JSON.parse(raw);
        } catch (error) {
            localStorage.removeItem(USER_KEY);
            return null;
        }
    }

    function isMockModeEnabled() {
        return localStorage.getItem(MOCK_MODE_KEY) === "true";
    }

    function setMockMode(enabled) {
        localStorage.setItem(MOCK_MODE_KEY, enabled ? "true" : "false");
    }

    async function request(path, options) {
        const config = options || {};
        const method = config.method || "GET";
        const headers = config.headers || {};
        const token = getToken();

        const requestHeaders = {
            "Accept": "application/json",
            ...headers
        };

        if (config.body !== undefined && !(config.body instanceof FormData)) {
            requestHeaders["Content-Type"] = "application/json";
        }

        if (token) {
            requestHeaders["Authorization"] = "Bearer " + token;
        }

        const requestOptions = {
            method,
            headers: requestHeaders
        };

        if (config.body !== undefined) {
            requestOptions.body = config.body instanceof FormData
                ? config.body
                : JSON.stringify(config.body);
        }

        const url = buildUrl(path);

        try {
            const response = await fetch(url, requestOptions);
            const payload = await readJsonOrText(response);

            if (!response.ok) {
                const message = extractErrorMessage(payload, response.status);
                throw new Error(message);
            }

            return payload;
        } catch (error) {
            if (config.useMockOnError && window.MockData) {
                return window.MockData.resolve(path, method, config.body);
            }

            throw error;
        }
    }

    function get(path, options) {
        return request(path, { ...(options || {}), method: "GET" });
    }

    function post(path, body, options) {
        return request(path, { ...(options || {}), method: "POST", body });
    }

    function patch(path, body, options) {
        return request(path, { ...(options || {}), method: "PATCH", body });
    }

    function del(path, options) {
        return request(path, { ...(options || {}), method: "DELETE" });
    }

    function buildUrl(path) {
        if (!path) {
            return getBaseUrl();
        }

        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }

        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        return getBaseUrl() + path;
    }

    function buildQuery(params) {
        const searchParams = new URLSearchParams();

        Object.keys(params || {}).forEach(function (key) {
            const value = params[key];

            if (value !== null && value !== undefined && String(value).trim() !== "") {
                searchParams.append(key, value);
            }
        });

        const query = searchParams.toString();
        return query ? "?" + query : "";
    }

    async function readJsonOrText(response) {
        const text = await response.text();

        if (!text) {
            return {};
        }

        try {
            return JSON.parse(text);
        } catch (error) {
            return { raw: text };
        }
    }

    function extractErrorMessage(payload, status) {
        if (payload) {
            if (payload.message) {
                return payload.message;
            }

            if (payload.error) {
                return payload.error;
            }

            if (payload.raw) {
                return payload.raw;
            }
        }

        return "Request failed with status " + status;
    }

    function requireAuth() {
        if (!getToken()) {
            window.location.href = "login.html";
            return false;
        }

        return true;
    }

    function formatMoney(value) {
        if (value === null || value === undefined || value === "") {
            return "0";
        }

        const numberValue = Number(value);

        if (Number.isNaN(numberValue)) {
            return String(value);
        }

        return new Intl.NumberFormat("en-US").format(numberValue);
    }

    function formatDateTime(value) {
        if (!value) {
            return "-";
        }

        const date = new Date(value);

        if (Number.isNaN(date.getTime())) {
            return value;
        }

        return date.toLocaleString();
    }

    function readSearchParams() {
        return new URLSearchParams(window.location.search);
    }

    function showMessage(elementId, message, type) {
        const element = document.getElementById(elementId);

        if (!element) {
            return;
        }

        element.className = "alert " + (type || "");
        element.textContent = message;
        element.classList.remove("hidden");
    }

    function hideMessage(elementId) {
        const element = document.getElementById(elementId);

        if (!element) {
            return;
        }

        element.classList.add("hidden");
    }

    window.ApiClient = {
        getBaseUrl,
        setBaseUrl,
        getToken,
        setToken,
        clearToken,
        setCurrentUser,
        getCurrentUser,
        isMockModeEnabled,
        setMockMode,
        request,
        get,
        post,
        patch,
        delete: del,
        buildQuery,
        requireAuth,
        formatMoney,
        formatDateTime,
        readSearchParams,
        showMessage,
        hideMessage
    };
})();
