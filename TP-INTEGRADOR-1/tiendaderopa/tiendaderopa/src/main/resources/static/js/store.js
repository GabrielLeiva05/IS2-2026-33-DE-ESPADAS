document.addEventListener("DOMContentLoaded", () => {
    const drawer = document.getElementById("cart-drawer");
    const content = document.getElementById("cart-drawer-content");
    const backdrop = document.getElementById("cart-backdrop");
    const trigger = document.querySelector(".cart-trigger[aria-controls='cart-drawer']");

    if (!drawer || !content || !backdrop || !trigger) {
        return;
    }

    const closeButton = drawer.querySelector("[data-cart-close]");

    function updateCount() {
        const count = content.querySelectorAll(".drawer-line").length;
        const badge = trigger.querySelector(".cart-count");
        if (badge) {
            badge.textContent = String(count);
        }
    }

    function closeDrawer() {
        drawer.classList.remove("is-open");
        drawer.setAttribute("aria-hidden", "true");
        drawer.inert = true;
        backdrop.hidden = true;
        document.body.classList.remove("cart-open");
        trigger.setAttribute("aria-expanded", "false");
        trigger.focus();
    }

    async function refreshCart() {
        const response = await fetch("/carrito/panel", {
            headers: { "X-Requested-With": "XMLHttpRequest" }
        });
        if (!response.ok) {
            throw new Error("No se pudo actualizar el carrito.");
        }
        content.innerHTML = await response.text();
        updateCount();
    }

    function openDrawer(refresh = true) {
        drawer.classList.add("is-open");
        drawer.setAttribute("aria-hidden", "false");
        drawer.inert = false;
        backdrop.hidden = false;
        document.body.classList.add("cart-open");
        trigger.setAttribute("aria-expanded", "true");
        closeButton.focus();

        if (refresh) {
            refreshCart().catch(error => {
                const message = document.createElement("p");
                message.className = "feedback error";
                message.setAttribute("role", "alert");
                message.textContent = error.message;
                content.replaceChildren(message);
            });
        }
    }

    trigger.setAttribute("aria-expanded", "false");
    trigger.addEventListener("click", event => {
        event.preventDefault();
        openDrawer();
    });
    closeButton.addEventListener("click", closeDrawer);
    backdrop.addEventListener("click", closeDrawer);
    document.addEventListener("keydown", event => {
        if (event.key === "Escape" && drawer.classList.contains("is-open")) {
            closeDrawer();
        }
    });

    document.addEventListener("submit", async event => {
        const form = event.target;
        const isAdd = form.matches(".cart-add-form");
        const isRemove = form.matches("#cart-drawer form[action*='/carrito/items/']");
        if (!isAdd && !isRemove) {
            return;
        }

        event.preventDefault();
        const submitButton = form.querySelector("button[type='submit']");
        if (submitButton) {
            submitButton.disabled = true;
        }

        try {
            const response = await fetch(form.action, {
                method: "POST",
                body: new FormData(form),
                headers: { "X-Requested-With": "XMLHttpRequest" }
            });
            if (!response.ok) {
                throw new Error("No se pudo actualizar el carrito.");
            }
            content.innerHTML = await response.text();
            updateCount();
            if (isAdd) {
                openDrawer(false);
            }
        } catch (error) {
            const message = document.createElement("p");
            message.className = "feedback error";
            message.setAttribute("role", "alert");
            message.textContent = error.message;
            content.prepend(message);
        } finally {
            if (submitButton) {
                submitButton.disabled = false;
            }
        }
    });

    updateCount();
    refreshCart().catch(() => {});
});