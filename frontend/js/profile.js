// Phase 4 Frontend - Profile UI Logic

(function () {
    const mockProfile = {
        userId: 1,
        firstName: "Demo",
        lastName: "User",
        email: "demo@sports.test",
        phoneNumber: "09120000000",
        cityId: 1,
        profileImageUrl: ""
    };

    document.addEventListener("DOMContentLoaded", function () {
        bindProfileEvents();
        refreshSessionBox();
        loadProfile();
    });

    function bindProfileEvents() {
        const form = document.getElementById("profileForm");
        const reloadButton = document.getElementById("reloadProfileButton");
        const fillButton = document.getElementById("fillMockProfileButton");
        const clearButton = document.getElementById("clearProfileSessionButton");
        const logoutButton = document.getElementById("logoutButton");

        if (form) {
            form.addEventListener("submit", async function (event) {
                event.preventDefault();
                await updateProfile(form);
            });
        }

        if (reloadButton) {
            reloadButton.addEventListener("click", loadProfile);
        }

        if (fillButton) {
            fillButton.addEventListener("click", function () {
                fillProfileForm(mockProfile);
                ApiClient.showMessage("profileMessage", "Mock profile filled.", "success");
            });
        }

        if (clearButton) {
            clearButton.addEventListener("click", function () {
                ApiClient.clearToken();
                refreshSessionBox();
                ApiClient.showMessage("profileMessage", "Session cleared.", "success");
            });
        }

        if (logoutButton) {
            logoutButton.addEventListener("click", function () {
                ApiClient.clearToken();
                window.location.href = "login.html";
            });
        }
    }

    async function loadProfile() {
        setResult("profileResultBox", "Loading profile...");

        try {
            const response = await ApiClient.get("/api/users/me", { useMockOnError: true });
            const profile = normalizeProfile(response);

            fillProfileForm(profile);
            ApiClient.setCurrentUser(profile);
            refreshSessionBox();

            setResult("profileResultBox", JSON.stringify(response, null, 2));
            ApiClient.showMessage("profileMessage", "Profile loaded successfully.", "success");
        } catch (error) {
            fillProfileForm(ApiClient.getCurrentUser() || mockProfile);
            setResult("profileResultBox", "Profile load failed: " + error.message);
            ApiClient.showMessage("profileMessage", "Profile API failed. Local/mock data is shown.", "error");
        }
    }

    async function updateProfile(form) {
        const payload = readForm(form);

        setResult("profileResultBox", "Updating profile...");

        try {
            const response = await ApiClient.patch("/api/users/me", payload, { useMockOnError: true });
            const profile = normalizeProfile(response);

            if (Object.keys(profile).length) {
                ApiClient.setCurrentUser(profile);
            } else {
                ApiClient.setCurrentUser(payload);
            }

            refreshSessionBox();
            setResult("profileResultBox", JSON.stringify(response, null, 2));
            ApiClient.showMessage("profileMessage", "Profile updated successfully.", "success");
        } catch (error) {
            setResult("profileResultBox", "Profile update failed: " + error.message);
            ApiClient.showMessage("profileMessage", "Profile update failed: " + error.message, "error");
        }
    }

    function normalizeProfile(response) {
        if (!response) {
            return mockProfile;
        }

        if (response.data && !Array.isArray(response.data)) {
            return response.data.user || response.data;
        }

        if (response.user) {
            return response.user;
        }

        return response;
    }

    function fillProfileForm(profile) {
        setValue("firstName", profile.firstName || profile.first_name);
        setValue("lastName", profile.lastName || profile.last_name);
        setValue("email", profile.email);
        setValue("phoneNumber", profile.phoneNumber || profile.phone_number);
        setValue("cityId", profile.cityId || profile.city_id);
        setValue("profileImageUrl", profile.profileImageUrl || profile.profile_image_url);
    }

    function readForm(form) {
        const formData = new FormData(form);
        const payload = {};

        formData.forEach(function (value, key) {
            const cleanValue = String(value).trim();

            if (cleanValue !== "") {
                payload[key] = cleanValue;
            }
        });

        return payload;
    }

    function refreshSessionBox() {
        const box = document.getElementById("profileSessionBox");

        if (!box) {
            return;
        }

        box.textContent = JSON.stringify({
            apiBaseUrl: ApiClient.getBaseUrl(),
            token: ApiClient.getToken(),
            currentUser: ApiClient.getCurrentUser()
        }, null, 2);
    }

    function setValue(id, value) {
        const element = document.getElementById(id);

        if (element && value !== undefined && value !== null) {
            element.value = value;
        }
    }

    function setResult(id, value) {
        const element = document.getElementById(id);

        if (element) {
            element.textContent = value;
        }
    }
})();
