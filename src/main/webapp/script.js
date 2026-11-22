
/* =========================================
   EASYPAY - FRONTEND DEMO
   JavaScript Functionality
========================================= */


/* =========================================
   1. WALLET BALANCE SHOW / HIDE
========================================= */

const balanceAmount = document.querySelector("#balanceAmount");
const toggleBalance = document.querySelector("#toggleBalance");

const originalBalance = "Rs. 24,850<span>.00</span>";

let balanceVisible = true;

if (balanceAmount && toggleBalance) {
    toggleBalance.addEventListener("click", () => {
        balanceVisible = !balanceVisible;

        balanceAmount.innerHTML = balanceVisible
            ? originalBalance
            : "Rs. ••••••";

        toggleBalance.setAttribute(
            "aria-label",
            balanceVisible ? "Hide balance" : "Show balance"
        );
    });
}


/* =========================================
   2. ELEMENT SELECTION
========================================= */

const modalBackdrop = document.querySelector("#modalBackdrop");
const modalTitle = document.querySelector("#modalTitle");
const modalDescription = document.querySelector("#modalDescription");
const modalSymbol = document.querySelector("#modalSymbol");

const recipientLabel = document.querySelector("#recipientLabel");
const recipientField = document.querySelector("#recipientField");

const demoForm = document.querySelector("#demoForm");
const modalClose = document.querySelector("#modalClose");

const toast = document.querySelector("#toast");


/* =========================================
   3. ACTION DETAILS
========================================= */

const actionDetails = {

    send: {
        title: "Send Money",
        symbol: "↗",
        label: "Recipient name or mobile number",
        placeholder: "e.g. 03XX XXXXXXX",
        description: "Enter demo details to preview a money transfer."
    },

    add: {
        title: "Add Money",
        symbol: "＋",
        label: "Source (demo)",
        placeholder: "e.g. Bank account",
        description: "Preview how adding money to your wallet could work."
    },

    bills: {
        title: "Pay Bills",
        symbol: "▤",
        label: "Consumer or reference number",
        placeholder: "Enter a sample reference",
        description: "Preview a utility bill payment."
    },

    mobile: {
        title: "Mobile Load",
        symbol: "▯",
        label: "Mobile number",
        placeholder: "03XX XXXXXXX",
        description: "Preview a mobile top-up. No actual load will be purchased."
    }

};


/* =========================================
   4. TOAST NOTIFICATION
========================================= */

function showToast(message) {

    if (!toast) return;

    toast.textContent = message;
    toast.classList.add("show");

    clearTimeout(showToast.timeoutId);

    showToast.timeoutId = setTimeout(() => {
        toast.classList.remove("show");
    }, 2600);

}


/* =========================================
   5. OPEN ACTION MODAL
========================================= */

function openAction(action) {

    const details = actionDetails[action];

    if (!details || !modalBackdrop) return;

    modalTitle.textContent = details.title;
    modalSymbol.textContent = details.symbol;

    modalDescription.textContent = details.description;

    recipientLabel.textContent = details.label;
    recipientField.placeholder = details.placeholder;

    demoForm.reset();

    modalBackdrop.hidden = false;

    document.body.style.overflow = "hidden";

    recipientField.focus();

}


/* =========================================
   6. CLOSE ACTION MODAL
========================================= */

function closeAction() {

    if (!modalBackdrop) return;

    modalBackdrop.hidden = true;

    document.body.style.overflow = "";

}


/* =========================================
   7. EXPLORE SERVICES
========================================= */

function exploreServices() {

    const servicesSection = document.querySelector("#payments");

    if (servicesSection) {

        servicesSection.scrollIntoView({
            behavior: "smooth",
            block: "center"
        });

        showToast(
            "Explore our services: Send Money, Add Money, Pay Bills and Mobile Load."
        );

    }

}


/* =========================================
   8. HANDLE ALL DATA-ACTION BUTTONS
========================================= */

document.querySelectorAll("[data-action]").forEach((button) => {

    button.addEventListener("click", (event) => {

        const action = button.dataset.action;

        switch (action) {

            case "notifications":

                showToast(
                    "You're all caught up — no new notifications."
                );

                break;


            case "help":

                showToast(
                    "Help centre preview — connect this to your support page."
                );

                break;


            case "logout":

                event.preventDefault();

                showToast(
                    "Logout is a demo interaction only."
                );

                break;


            case "explore":

                event.preventDefault();

                exploreServices();

                break;


            case "send":
            case "add":
            case "bills":
            case "mobile":

                openAction(action);

                break;


            default:

                console.warn("Unknown action:", action);

        }

    });

});


/* =========================================
   9. MODAL CLOSE EVENTS
========================================= */

if (modalClose) {

    modalClose.addEventListener("click", closeAction);

}


if (modalBackdrop) {

    modalBackdrop.addEventListener("click", (event) => {

        if (event.target === modalBackdrop) {
            closeAction();
        }

    });

}


/* Close modal with Escape key */

document.addEventListener("keydown", (event) => {

    if (
        event.key === "Escape" &&
        modalBackdrop &&
        !modalBackdrop.hidden
    ) {

        closeAction();

    }

});


/* =========================================
   10. DEMO FORM SUBMISSION
========================================= */

if (demoForm) {

    demoForm.addEventListener("submit", (event) => {

        event.preventDefault();

        const formData = new FormData(demoForm);

        const recipient = String(
            formData.get("recipient") || ""
        ).trim();

        const amount = Number(formData.get("amount"));

        if (!recipient) {

            showToast("Please enter the required details.");

            recipientField.focus();

            return;

        }

        if (
            !Number.isFinite(amount) ||
            amount <= 0 ||
            amount > 1000000
        ) {

            showToast("Please enter a valid amount.");

            return;

        }

        closeAction();

        demoForm.reset();

        showToast(
            "Demo successful! No real transaction was processed."
        );

    });

}


/* =========================================
   11. TRANSACTION FILTERS
========================================= */

const filterChips = document.querySelectorAll(".filter-chip");

const transactionRows = document.querySelectorAll(
    ".transaction-row"
);


filterChips.forEach((chip) => {

    chip.addEventListener("click", () => {

        filterChips.forEach((item) => {
            item.classList.remove("active");
        });

        chip.classList.add("active");

        const filter = chip.dataset.filter;

        transactionRows.forEach((row) => {

            const transactionType = row.dataset.type;

            row.hidden = (
                filter !== "all" &&
                transactionType !== filter
            );

        });

    });

});


/* =========================================
   12. VIEW ALL TRANSACTIONS
========================================= */

const viewAllButton = document.querySelector("#viewAll");

if (viewAllButton) {

    viewAllButton.addEventListener("click", () => {

        const allFilter = document.querySelector(
            '[data-filter="all"]'
        );

        if (allFilter) {
            allFilter.click();
        }

        showToast("Showing all sample transactions.");

    });

}


/* =========================================
   13. MOBILE SIDEBAR MENU
========================================= */

const menuToggle = document.querySelector("#menuToggle");

const sidebar = document.querySelector(".sidebar");


if (menuToggle && sidebar) {

    menuToggle.addEventListener("click", () => {

        const isOpen = sidebar.classList.toggle("open");

        menuToggle.setAttribute(
            "aria-expanded",
            String(isOpen)
        );

        menuToggle.setAttribute(
            "aria-label",
            isOpen ? "Close navigation" : "Open navigation"
        );

    });


    /* Close sidebar after clicking navigation links */

    document.querySelectorAll(".side-nav .nav-link").forEach((link) => {

        link.addEventListener("click", () => {

            sidebar.classList.remove("open");

            menuToggle.setAttribute(
                "aria-expanded",
                "false"
            );

            menuToggle.setAttribute(
                "aria-label",
                "Open navigation"
            );

        });

    });


    /* Close sidebar when clicking outside it */

    document.addEventListener("click", (event) => {

        const clickedInsideSidebar = sidebar.contains(event.target);

        const clickedMenuButton = menuToggle.contains(event.target);

        if (
            !clickedInsideSidebar &&
            !clickedMenuButton &&
            sidebar.classList.contains("open")
        ) {

            sidebar.classList.remove("open");

            menuToggle.setAttribute(
                "aria-expanded",
                "false"
            );

            menuToggle.setAttribute(
                "aria-label",
                "Open navigation"
            );

        }

    });

}


/* =========================================
   14. CONSOLE MESSAGE
========================================= */

console.log(
    "Easypay frontend demo loaded successfully!"
);
