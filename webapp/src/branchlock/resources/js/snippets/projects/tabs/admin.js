$(document).ready(function () {
    const copyButtons = document.querySelectorAll('.config-copy-btn');
    copyButtons.forEach(button => {
        button.addEventListener('click', () => {
            const targetId = button.dataset.copyTarget;
            const targetElement = document.getElementById(targetId);

            copyToClipboard(getTextContent(targetElement, false));
        });
    });

});
