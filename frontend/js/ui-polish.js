(function () {
    "use strict";

    function textOf(element) {
        return (element && element.textContent ? element.textContent : "").trim();
    }

    function lowerText(element) {
        return textOf(element).toLowerCase();
    }

    function findPanelFromHeading(heading) {
        if (!heading) return null;

        return (
            heading.closest(".card") ||
            heading.closest(".panel") ||
            heading.closest(".section-card") ||
            heading.closest("section") ||
            heading.closest("article") ||
            heading.closest("form")
        );
    }

    function isJsonLike(text) {
        var trimmed = (text || "").trim();
        return (
            trimmed.startsWith("{") ||
            trimmed.startsWith("[") ||
            trimmed.includes('"success"') ||
            trimmed.includes('"apiBaseUrl"') ||
            trimmed.includes('"token"') ||
            trimmed.includes('"currentUser"')
        );
    }

    function wrapDebugBlocks() {
        Array.from(document.querySelectorAll("pre")).forEach(function (pre) {
            if (pre.closest("details")) return;
            if (!isJsonLike(pre.textContent)) return;

            var details = document.createElement("details");
            details.className = "api-response-details";

            var summary = document.createElement("summary");
            summary.textContent = "View API response";

            pre.parentNode.insertBefore(details, pre);
            details.appendChild(summary);
            details.appendChild(pre);
        });
    }

    function findAuthTabGroup() {
        var buttons = Array.from(document.querySelectorAll("button"));

        for (var i = 0; i < buttons.length; i++) {
            var start = buttons[i];

            for (var node = start.parentElement, depth = 0; node && depth < 4; node = node.parentElement, depth++) {
                var groupButtons = Array.from(node.querySelectorAll("button"));
                var login = groupButtons.find(function (button) {
                    return lowerText(button) === "login";
                });
                var signup = groupButtons.find(function (button) {
                    return lowerText(button) === "signup";
                });
                var otp = groupButtons.find(function (button) {
                    return lowerText(button) === "otp";
                });

                if (login && signup && otp) {
                    return {
                        container: node,
                        login: login,
                        signup: signup,
                        otp: otp
                    };
                }
            }
        }

        return null;
    }

    function polishAuthPage() {
        var pageText = lowerText(document.body);

        if (!pageText.includes("login") || !pageText.includes("signup") || !pageText.includes("otp")) {
            return;
        }

        var panels = {
            login: null,
            signup: null,
            otp: null
        };

        Array.from(document.querySelectorAll("h2, h3")).forEach(function (heading) {
            var label = lowerText(heading);
            var panel = findPanelFromHeading(heading);

            if (!panel) return;

            if (label === "login") {
                panels.login = panel;
            } else if (label === "signup") {
                panels.signup = panel;
            } else if (label.includes("otp")) {
                panels.otp = panel;
            }
        });

        if (!panels.login || !panels.signup || !panels.otp) {
            return;
        }

        Object.keys(panels).forEach(function (key) {
            panels[key].setAttribute("data-auth-panel", key);
        });

        var tabGroup = findAuthTabGroup();

        if (!tabGroup) {
            return;
        }

        tabGroup.container.classList.add("phase4-auth-switcher");
        tabGroup.login.setAttribute("data-auth-tab", "login");
        tabGroup.signup.setAttribute("data-auth-tab", "signup");
        tabGroup.otp.setAttribute("data-auth-tab", "otp");

        function showPanel(name) {
            Object.keys(panels).forEach(function (key) {
                panels[key].classList.toggle("phase4-hidden", key !== name);
                panels[key].classList.toggle("phase4-active-panel", key === name);
            });

            [tabGroup.login, tabGroup.signup, tabGroup.otp].forEach(function (button) {
                var active = button.getAttribute("data-auth-tab") === name;
                button.classList.toggle("active", active);
                button.setAttribute("aria-pressed", active ? "true" : "false");
            });
        }

        tabGroup.container.addEventListener("click", function (event) {
            var target = event.target.closest("[data-auth-tab]");
            if (!target) return;

            event.preventDefault();
            showPanel(target.getAttribute("data-auth-tab"));
        });

        showPanel("login");
    }

    function polishNavigation() {
        var token = "";

        try {
            token = localStorage.getItem("sports_ticket_auth_token") || "";
        } catch (error) {
            token = "";
        }

        Array.from(document.querySelectorAll("a, button")).forEach(function (item) {
            var label = lowerText(item);

            if (label === "login" && !item.hasAttribute("data-auth-tab")) {
                item.classList.toggle("phase4-hidden", Boolean(token));
            }

            if (label === "logout") {
                item.classList.toggle("phase4-hidden", !token);
            }
        });
    }

    function addCancelConfirmation() {
        document.addEventListener("click", function (event) {
            var button = event.target.closest("button, a");
            if (!button) return;

            var label = lowerText(button);
            var isCancelAction = label === "cancel" || label.includes("cancel reservation");

            if (!isCancelAction) return;

            var confirmed = window.confirm("Are you sure you want to cancel this reservation?");
            if (!confirmed) {
                event.preventDefault();
                event.stopPropagation();
            }
        }, true);
    }

    function polishAdminNotes() {
        Array.from(document.querySelectorAll("h2, h3")).forEach(function (heading) {
            if (!lowerText(heading).includes("frontend-only admin page")) return;

            heading.textContent = "Integration notes";
            var panel = findPanelFromHeading(heading);

            if (!panel || panel.closest("details")) return;

            var details = document.createElement("details");
            details.className = "integration-notes";

            var summary = document.createElement("summary");
            summary.textContent = "View integration notes";

            panel.parentNode.insertBefore(details, panel);
            details.appendChild(summary);
            details.appendChild(panel);
        });
    }

    function polishSearchSourceText() {
        Array.from(document.querySelectorAll("*")).forEach(function (node) {
            if (!node.childNodes || node.childNodes.length !== 1) return;

            var current = textOf(node);
            if (current === "mock fallback") {
                node.textContent = "Mock fallback";
            }
        });
    }

    function autoHealthHint() {
        var healthButton = Array.from(document.querySelectorAll("button")).find(function (button) {
            return lowerText(button).includes("check health");
        });

        if (!healthButton) return;

        healthButton.setAttribute("title", "Checks GET /api/health on the Java backend");
    }

    document.addEventListener("DOMContentLoaded", function () {
        document.body.classList.add("phase4-polished");

        wrapDebugBlocks();
        polishAuthPage();
        polishNavigation();
        addCancelConfirmation();
        polishAdminNotes();
        polishSearchSourceText();
        autoHealthHint();
    });
})();
