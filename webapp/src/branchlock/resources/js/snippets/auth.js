function validate(event) {
    event.preventDefault();
    showSpinner();
    hcaptcha.execute();
}

function showSpinner() {
    const btn = document.getElementById("submit_btn");
    btn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Loading...';
    btn.disabled = true;
}

document.addEventListener('DOMContentLoaded', () => {
    const element = document.getElementById('submit_btn');
    element.onclick = validate;

    document.querySelectorAll(".toggle-password").forEach(function (toggle) {
        toggle.addEventListener("click", function () {
            const targetId = this.getAttribute("data-target");
            const passwordField = document.getElementById(targetId);

            if (passwordField) {
                const type = passwordField.getAttribute("type") === "password" ? "text" : "password";
                passwordField.setAttribute("type", type);
                this.classList.toggle('fa-eye');
                this.classList.toggle('fa-eye-slash');
            }
        });
    });
});


export function onSubmit(token) {
    document.getElementById('form1').submit();
}

export function onClose() {
    const btn = document.getElementById("submit_btn");
    btn.innerHTML = 'Continue';
    btn.disabled = false;
}

export function initializeGlobalFunctions() {
    window.onSubmit = onSubmit;
    window.onClose = onClose;
}

initializeGlobalFunctions();
