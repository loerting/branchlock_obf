// Auo-save

document.addEventListener('DOMContentLoaded', () => {
    const projects = document.querySelectorAll('.project');
    projects.forEach((project) => {
        project.addEventListener('input', debounce(() => {
            AutoSaveModule.handleAutoSave(project.dataset.projectId);
        }, 1337));
    });

// send-feedback-button listener
    const feedbackSendButton = document.getElementById("send-feedback-button");
    if (feedbackSendButton) {
        feedbackSendButton.addEventListener("click", function () {
            const route = feedbackSendButton.getAttribute("data-route");
            axios.post(route, {
                feedbackMessage: document.getElementById("feedbackMessage").value
            })
                .then(function (response) {
                    console.log(response);
                    document.getElementById("feedbackMessage").value = "";

                    if (response.data.success) {
                        showSuccess(response.data.message);
                        const feedbackModal = document.getElementById("feedbackModal");
                        const modal = bootstrap.Modal.getInstance(feedbackModal);
                        modal.hide();
                    } else {
                        showError(response.data.message);
                    }
                })
                .catch(function (error) {
                    showError(error.response.data.message);
                });
        });
    }
});

export const AutoSaveModule = {
    handleAutoSave: function (projectId) {
        setTimeout(() => {
            const project = document.querySelector('#project-form_' + projectId);
            const formData = new FormData(project);

            const projectName = project.dataset.projectName;

            //console.log(`Auto-save for project "${projectName}" initiated.`);

            axios.post(`/app/project/${projectId}/save`, formData)
                .then((response) => {
                    //console.log('Auto-save successful.');

                    if (response.data.config !== null) {
                        const jsonBox = document.querySelector('#admin-json-box_' + projectId);
                        if (jsonBox) {
                            const jsonString = response.data.config;
                            try {
                                const parsedConfig = JSON.parse(jsonString);
                                jsonBox.textContent = JSON.stringify(parsedConfig, null, 2);
                                jsonBox.removeAttribute('data-highlighted');
                                hljs.highlightElement(jsonBox);
                            } catch (error) {
                                console.error('Invalid JSON format:', error);
                            }
                        }
                    }

                })
                .catch((error) => {
                    console.log('Auto-save failed.');
                    console.log(error);
                });
        }, 666);
    }
};

export function showErrorToast(message) {
    const errorToast = document.getElementById('error-toast');
    const toastBody = errorToast.querySelector('#error-toast-message');

    toastBody.textContent = message;

    const toast = new bootstrap.Toast(errorToast);
    toast.show();

    const notificationSound = new Audio('/audio/error.mp3');
    notificationSound.volume = 0.4;
    notificationSound.play();
}

export function showSuccessToast(message) {
    const successToast = document.getElementById('success-toast');
    const toastBody = successToast.querySelector('#success-toast-message');

    toastBody.textContent = message;

    const toast = new bootstrap.Toast(successToast);
    toast.show();

    const notificationSound = new Audio('/audio/click.mp3');
    notificationSound.volume = 0.4;
    notificationSound.play();
}

function debounce(func, wait, immediate) {
    let timeout;
    return function () {
        const context = this;
        const args = arguments;
        const later = () => {
            timeout = null;
            if (!immediate) {
                func.apply(context, args);
            }
        };
        const callNow = immediate && !timeout;
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
        if (callNow) {
            func.apply(context, args);
        }
    };
}
