// generate-token button: generate token with axios

import {showErrorToast, showSuccessToast} from "../global";

const generateTokenButton = document.getElementById('generate-token');
if (generateTokenButton) {
    generateTokenButton.addEventListener('click', function () {

        axios.post('/app/token')
            .then((response) => {
                const tokenArea = document.getElementById('token-area');
                tokenArea.innerHTML = '';

                // Create input group
                const inputGroup = document.createElement('div');
                inputGroup.classList.add('input-group');

                // Create input field
                const tokenInput = document.createElement('input');
                tokenInput.classList.add('form-control', 'rounded-3');
                tokenInput.value = response.data.token;
                tokenInput.readOnly = true;
                inputGroup.appendChild(tokenInput);

                // Create copy button
                const copyButton = document.createElement('button');
                copyButton.classList.add('btn', 'btn-secondary', 'ms-2', 'rounded-3');
                copyButton.type = 'button';
                copyButton.innerHTML = '<i class="fa-solid fa-copy"></i>';
                copyButton.addEventListener('click', () => {
                    copyToClipboard(response.data.token);
                    showSuccessToast('Token copied to clipboard');
                });

                // Append copy button to input group
                const inputGroupAppend = document.createElement('div');
                inputGroupAppend.classList.add('input-group-append');
                inputGroupAppend.appendChild(copyButton);
                inputGroup.appendChild(inputGroupAppend);

                // Append input group to token area
                tokenArea.appendChild(inputGroup);

                showSuccessToast(response.data.message);
                generateTokenButton.dataset.token = response.data.token;
            })
            .catch((error) => {
                showErrorToast(error.response.data.message);
            });

        function copyToClipboard(text) {
            navigator.clipboard.writeText(text)
                .catch(err => {
                    console.error('Unable to copy:', err);
                });
        }

    });
}
