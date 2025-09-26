$(document).ready(function () {
    const obfuscateButtons = document.querySelectorAll('.obfuscate-btn');
    obfuscateButtons.forEach((button) => {
        button.addEventListener('click', (e) => {
            e.preventDefault();

            const projectId = button.getAttribute('data-project-id');

            const processTab = document.querySelector('#pills-tab-process_' + projectId);
            const tab = new bootstrap.Tab(processTab);
            tab.show();

            const formData = new FormData(document.querySelector('#project-form_' + projectId));

            button.disabled = true;
            button.innerHTML = '<i class="fa-solid fa-spinner fa-spin me-1"></i> Loading...';

            // disable output download button
            const downloadButton = document.querySelector('.download-output[data-project-id="' + projectId + '"]');
            downloadButton.classList.add('disabled');
            downloadButton.classList.remove('animate__animated', 'animate__bounce');

            axios.post('/app/project/' + projectId + '/process', formData)
                .then((response) => {
                    autoSave.handleAutoSave(projectId);
                    //console.log(response);
                    showSuccess(response.data.message);
                })
                .catch((error) => {
                    //console.error(error);
                    showError(error.response.data.message);

                    startCooldown(button, 1);
                });

        });
    });

    function startCooldown(button, seconds) {
        button.disabled = true;

        let remainingTime = seconds;
        if (seconds > 3) button.innerHTML = `<i class="fa-solid fa-hourglass-start me-1"></i> Wait ${remainingTime} seconds`;

        const interval = setInterval(function () {
            remainingTime--;

            if (remainingTime <= 0) {
                button.disabled = false;
                button.innerHTML = '<i class="fa-solid fa-play me-1"></i> Transform';
                clearInterval(interval);
            } else {
                if (seconds > 3) button.innerHTML = `<i class="fa-solid fa-hourglass-start me-1"></i> Wait ${remainingTime} seconds`;
            }
        }, 1000);
    }

    const userData = document.getElementById('projects');
    const userId = userData.dataset.userId;
    const cooldown = userData.dataset.cooldown;

    let finished = true;
    let firstRun = true;

    Echo.private('App.Models.User.' + userId)
        .listen('LogOutputAvailable', (e) => {
            let projectId = e.projectId;
            let responsePanel = $('#temp-response-box_' + projectId);

            let logLine = e.output;
            //console.log(e);

            if (finished) {
                responsePanel.empty();

                if (firstRun) {
                    const processTab = document.querySelector('#pills-tab-process_' + projectId);
                    const tab = new bootstrap.Tab(processTab);
                    tab.show();
                    firstRun = false;
                }

                const projectForm = document.getElementById('project-form_' + projectId);
                if (projectForm.dataset.projectAndroid === 'false') {
                    let button = document.querySelector('.obfuscate-btn[data-project-id="' + projectId + '"]');
                    button.disabled = true;
                    button.innerHTML = '<i class="fa-solid fa-spinner fa-spin me-1"></i> Loading...';

                    const downloadButton = document.querySelector('.download-output[data-project-id="' + projectId + '"]');
                    downloadButton.classList.add('disabled');
                    downloadButton.classList.remove('animate__animated', 'animate__bounce');
                }

                finished = false;
            }

            if (logLine === false) {
                const projectForm = document.getElementById('project-form_' + projectId);
                if (projectForm.dataset.projectAndroid === 'false') {
                    let button = document.querySelector('.obfuscate-btn[data-project-id="' + projectId + '"]');
                    startCooldown(button, cooldown);
                }
                finished = true;
                return;
            }

            const newBlock = document.createElement('code');
            newBlock.classList.add('d-flex', 'justify-content-between', 'align-items-center',
                'animate__animated', 'animate__fadeIn', 'animate__fast');

            const textSpan = document.createElement('span');
            textSpan.classList.add('px-1', 'me-3', 'rounded-end-3');
            textSpan.innerHTML = logLine;

            const icon = document.createElement('i');

            newBlock.appendChild(textSpan);

            // Handle stderr errors and unknown log types
            if (logLine.startsWith("stderr:")) {
                textSpan.innerHTML = logLine.replace("stderr:", "");
                textSpan.classList.add('bg-danger-subtle', 'text-danger-emphasis', 'fw-bold');
                icon.classList.add('fas', 'fa-exclamation-circle', 'text-danger');
                newBlock.appendChild(icon);
            } else if (logLine.startsWith("?:")) {
                textSpan.innerHTML = logLine.replace("?:", "");
                textSpan.classList.add('bg-info-subtle', 'text-info-emphasis', 'fw-bold');
                icon.classList.add('fas', 'fa-circle-info', 'text-info');
                newBlock.appendChild(icon);
            } else if (logLine.startsWith("api:")) {
                textSpan.innerHTML = logLine.replace("api:", "");
                textSpan.classList.add('bg-success-subtle', 'text-success-emphasis', 'fw-bold');
                icon.classList.add('fas', 'fa-satellite-dish', 'text-success');
                newBlock.appendChild(icon);
            } else {
                // Handle regular log types
                const logParts = logLine.split(/\s+/);
                const logType = logParts[1].replace(/[^a-zA-Z ]/g, "");

                switch (logType) {
                    case 'ERROR':
                        textSpan.classList.add('bg-danger-subtle', 'text-danger-emphasis', 'fw-bold');
                        icon.classList.add('fas', 'fa-exclamation-circle', 'text-danger');
                        newBlock.appendChild(icon);
                        break;
                    case 'WARN':
                        textSpan.classList.add('bg-warning-subtle', 'text-danger-emphasis', 'fw-bold');
                        icon.classList.add('fas', 'fa-exclamation-triangle', 'text-warning');
                        newBlock.appendChild(icon);
                        break;
                    case 'DEBUG':
                        textSpan.classList.add('bg-primary-subtle', 'text-primary-emphasis', 'fw-bold');
                        icon.classList.add('fas', 'fa-bug', 'text-primary');
                        newBlock.appendChild(icon);
                        break;
                    default:
                        textSpan.classList.add('custom-log');
                        hljs.highlightElement(textSpan);
                }
            }

            responsePanel.append(newBlock);

            if (responsePanel.length)
                responsePanel.scrollTop(responsePanel[0].scrollHeight - responsePanel.height());
        })
        .listen('OutputFileAvailable', (e) => {
            let projectId = e.projectId;
            const projectForm = document.getElementById('project-form_' + projectId);
            if (projectForm.dataset.projectAndroid === 'false') {
                let button = document.querySelector('.download-output[data-project-id="' + projectId + '"]');

                button.innerHTML = '<i class="fa-solid fa-download me-1"></i> ' + e.fileName;
                button.classList.remove('disabled');
                button.classList.add('animate__animated', 'animate__bounce', 'fw-bold');

                if (e.jarDeleted) {
                    //console.log('Input Jar deleted');
                    const fileView = document.querySelector(`.card[data-project-id="${projectId}"]`);
                    fileView.classList.add('d-none');

                    const dropzone = document.querySelector(`.dropzone-container-jar[data-project-id="${projectId}"]`);
                    dropzone.classList.remove('d-none');
                    dropzone.dropzone.removeAllFiles(true);
                }

                if (e.libsDeleted) {
                    //console.log('Input libs deleted');
                    const libList = document.getElementById(`lib-list_${projectId}`);
                    libList.innerHTML = ''; // Remove all content within the list
                    const countSpan = document.getElementById(`lib-list-count_${projectId}`);
                    countSpan.textContent = '0';
                }
            }


            const notificationSound = new Audio('/audio/bubble.mp3');
            notificationSound.volume = 0.9;
            notificationSound.play();
        });

});
