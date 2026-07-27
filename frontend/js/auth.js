// Phase 4 Frontend - Authentication UI Logic

(function () {
    const demoUser = {
        userId: 1,
        firstName: "Demo",
        lastName: "User",
        email: "demo@sports.test",
        phoneNumber: "09120000000",
        role: "USER"
    };

    document.addEventListener("DOMContentLoaded", function () {
        bindTabs();
        bindLoginForm();
        bindSignupForm();
        bindOtpForms();
        bindDemoButtons();
        bindSessionButtons();
        refreshSessionBox();
    });

    function bindTabs() {
        document.querySelectorAll("[data-auth-tab]").forEach(function (button) {
            button.addEventListener("click", function () {
                const tab = button.getAttribute("data-auth-tab");
                scrollToPanel(tab + "Panel");
            });
        });
    }

    function bindLoginForm() {
        const form = document.getElementById("loginForm");

        if (!form) {
            return;
        }

        form.addEventListener("submit", async function (event) {
            event.preventDefault();
            ApiClient.hideMessage("authMessage");

            const payload = readForm(form);

            try {
                const result = await ApiClient.post("/api/auth/login", payload, { useMockOnError: true });
                handleAuthResult(result, "Login successful.");
            } catch (error) {
                ApiClient.showMessage("authMessage", "Login failed: " + error.message, "error");
            }
        });
    }

    function bindSignupForm() {
        const form = document.getElementById("signupForm");

        if (!form) {
            return;
        }

        form.addEventListener("submit", async function (event) {
            event.preventDefault();
            ApiClient.hideMessage("authMessage");

            const payload = readForm(form);

            try {
                const result = await ApiClient.post("/api/auth/signup", payload, { useMockOnError: true });
                handleAuthResult(result, "Signup successful.");
            } catch (error) {
                ApiClient.showMessage("authMessage", "Signup failed: " + error.message, "error");
            }
        });
    }

    function bindOtpForms() {
        const requestForm = document.getElementById("requestOtpForm");
        const verifyForm = document.getElementById("verifyOtpForm");

        if (requestForm) {
            requestForm.addEventListener("submit", async function (event) {
                event.preventDefault();
                ApiClient.hideMessage("authMessage");

                const payload = readForm(requestForm);

                try {
                    const result = await ApiClient.post("/api/auth/request-otp", payload, { useMockOnError: true });
                    const message = result.message || "OTP requested successfully.";
                    ApiClient.showMessage("authMessage", message, "success");

                    const verifyPhoneInput = document.getElementById("verifyPhone");
                    if (verifyPhoneInput) {
                        verifyPhoneInput.value = payload.phoneNumber || "";
                    }
                } catch (error) {
                    ApiClient.showMessage("authMessage", "OTP request failed: " + error.message, "error");
                }
            });
        }

        if (verifyForm) {
            verifyForm.addEventListener("submit", async function (event) {
                event.preventDefault();
                ApiClient.hideMessage("authMessage");

                const payload = readForm(verifyForm);

                try {
                    const result = await ApiClient.post("/api/auth/verify-otp", payload, { useMockOnError: true });
                    handleAuthResult(result, "OTP verified successfully.");
                } catch (error) {
                    ApiClient.showMessage("authMessage", "OTP verification failed: " + error.message, "error");
                }
            });
        }
    }

    function bindDemoButtons() {
        const fillLoginDemo = document.getElementById("fillLoginDemo");
        const fillSignupDemo = document.getElementById("fillSignupDemo");

        if (fillLoginDemo) {
            fillLoginDemo.addEventListener("click", function () {
                setValue("loginEmail", demoUser.email);
                setValue("loginPassword", "demo1234");
            });
        }

        if (fillSignupDemo) {
            fillSignupDemo.addEventListener("click", function () {
                setValue("signupFirstName", demoUser.firstName);
                setValue("signupLastName", demoUser.lastName);
                setValue("signupEmail", demoUser.email);
                setValue("signupPhone", demoUser.phoneNumber);
                setValue("signupPassword", "demo1234");
            });
        }

        setValue("otpPhone", demoUser.phoneNumber);
        setValue("verifyPhone", demoUser.phoneNumber);
        setValue("otpCode", "123456");
    }

    function bindSessionButtons() {
        const refreshButton = document.getElementById("refreshSessionButton");
        const clearButton = document.getElementById("clearSessionButton");
        const mockLoginButton = document.getElementById("mockLoginButton");

        if (refreshButton) {
            refreshButton.addEventListener("click", refreshSessionBox);
        }

        if (clearButton) {
            clearButton.addEventListener("click", function () {
                ApiClient.clearToken();
                refreshSessionBox();
                ApiClient.showMessage("authMessage", "Session cleared.", "success");
            });
        }

        if (mockLoginButton) {
            mockLoginButton.addEventListener("click", function () {
                ApiClient.setToken("mock-user-token");
                ApiClient.setCurrentUser(demoUser);
                refreshSessionBox();
                ApiClient.showMessage("authMessage", "Mock login enabled for frontend demo.", "success");
            });
        }
    }

    function handleAuthResult(result, successMessage) {
        const token = findToken(result);
        const user = findUser(result);

        if (token) {
            ApiClient.setToken(token);
        }

        if (user) {
            ApiClient.setCurrentUser(user);
        }

        if (!token && !user && result && result.success !== false) {
            ApiClient.setToken("mock-or-api-token");
            ApiClient.setCurrentUser(demoUser);
        }

        refreshSessionBox();
        ApiClient.showMessage("authMessage", successMessage, "success");
    }

    function findToken(result) {
        if (!result) {
            return null;
        }

        if (result.token) {
            return result.token;
        }

        if (result.jwt) {
            return result.jwt;
        }

        if (result.accessToken) {
            return result.accessToken;
        }

        if (result.data && result.data.token) {
            return result.data.token;
        }

        if (result.data && result.data.accessToken) {
            return result.data.accessToken;
        }

        return null;
    }

    function findUser(result) {
        if (!result) {
            return null;
        }

        if (result.user) {
            return result.user;
        }

        if (result.data && result.data.user) {
            return result.data.user;
        }

        if (result.data && result.data.email) {
            return result.data;
        }

        return null;
    }

    function refreshSessionBox() {
        const sessionBox = document.getElementById("sessionBox");

        if (!sessionBox) {
            return;
        }

        const data = {
            apiBaseUrl: ApiClient.getBaseUrl(),
            token: ApiClient.getToken(),
            currentUser: ApiClient.getCurrentUser()
        };

        sessionBox.textContent = JSON.stringify(data, null, 2);
    }

    function readForm(form) {
        const formData = new FormData(form);
        const payload = {};

        formData.forEach(function (value, key) {
            payload[key] = String(value).trim();
        });

        return payload;
    }

    function setValue(id, value) {
        const element = document.getElementById(id);

        if (element) {
            element.value = value;
        }
    }

    function scrollToPanel(id) {
        const panel = document.getElementById(id);

        if (panel) {
            panel.scrollIntoView({ behavior: "smooth", block: "start" });
        }
    }
})();
