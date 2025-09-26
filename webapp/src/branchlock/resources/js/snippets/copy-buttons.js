function setupCopyListeners(parentSelector = document, buttonClass = 'copy-btn') {
    parentSelector.addEventListener('click', event => {
        const button = event.target.closest(`.${buttonClass}`);

        if (button) {
            const targetId = button.dataset.copyTarget;
            const targetElement = document.getElementById(targetId);
            const clean = button.dataset.clean === 'true';

            copyToClipboard(getTextContent(targetElement, clean));
        }
    });
}

function copyToClipboard(text) {
    navigator.clipboard.writeText(text)
        .then(() => {
            showSuccess('Copied to clipboard');
        })
        .catch(err => {
            console.error('Unable to copy:', err);
        });
}

function getTextContent(element, clean = false) {
    let cleanText = '';
    if (clean) {
        for (const node of element.childNodes) {
            if (node.nodeType === Node.TEXT_NODE) {
                cleanText += node.textContent + '\n'; // Add regular line break
            } else if (node.nodeType === Node.ELEMENT_NODE && node.tagName.toLowerCase() === 'code') {
                cleanText += node.textContent + '\n'; // Add regular line break for code tags
            } else if (node.nodeType === Node.ELEMENT_NODE && node.tagName.toLowerCase() === 'span') {
                cleanText += node.textContent + '\n'; // Add regular line break for span tags
            } else if (node.nodeType === Node.ELEMENT_NODE) {
                cleanText += getTextContent(node); // Recursively traverse nested elements
            }
        }
    } else {
        cleanText = element.textContent;
    }

    return cleanText;
}

setupCopyListeners(document, 'copy-btn');
