$(document).ready(function () {
    Dropzone.autoDiscover = false;

    const commonDropzoneOptionsJar = {
        dictDefaultMessage: "Drop your input file here to upload",
        paramName: "jar",
        uploadMultiple: false,
        chunking: true,
        forceChunking: true,
        retryChunks: true,
        retryChunksLimit: 3,
        parallelUploads: 2,
        maxFiles: 1,
        acceptedFiles: ".jar",
        addRemoveLinks: false,
        init: function () {
            const projectId = this.element.getAttribute('data-project-id');
            const dropzone = document.querySelector(`.dropzone-container-jar[data-project-id="${projectId}"]`);

            this.on("sending", function (file, xhr, formData) {
                // get the csrf token data-csfr-token
                formData.append("_token", dropzone.getAttribute('data-csrf-token'));
            });

            this.on("success", function (file, response) {
                if (response && response.file && response.size) {
                    setTimeout(() => {
                        //console.log('complete upload');
                        this.element.classList.add('d-none');

                        const fileView = document.querySelector(`.card[data-project-id="${projectId}"]`);

                        if (fileView) {
                            fileView.classList.remove('d-none');
                            fileView.querySelector(".text-primary-emphasis").innerText = response.file;
                            fileView.querySelector(".text-muted.small.size").innerText = response.size;
                        }
                    }, 1000);
                }
            });

            this.on("error", function (file, response) {
                if (typeof response === 'string') {
                    if (response.includes("filesize")) {
                        showError('Your current plan does not allow files larger than '
                            + dropzone.getAttribute('data-max-file-size') +
                            'MB. Please upgrade your plan to upload larger files.'
                        );
                    } else {
                        showError(response);
                    }
                } else {
                    showError(response.message);
                }

                this.removeFile(file);
            });

        }
    };

    const commonDropzoneOptionsLib = {
        dictDefaultMessage: "Drop your Libraries here to upload",
        paramName: "jar",
        uploadMultiple: false,
        chunking: true,
        forceChunking: true,
        retryChunks: true,
        retryChunksLimit: 3,
        parallelUploads: 2,
        maxFiles: 100,
        acceptedFiles: ".jar",
        addRemoveLinks: false,
        init: function () {
            const projectId = this.element.getAttribute('data-project-id');
            const dropzone = document.querySelector(`.dropzone-container-lib[data-project-id="${projectId}"]`);
            const libList = document.getElementById(`lib-list_${projectId}`);

            this.on("sending", function (file, xhr, formData) {
                formData.append("_token", dropzone.getAttribute('data-csrf-token'));
            });

            this.on("success", function (file, response) {
                if (response && response.file && response.size) {
                    setTimeout(() => {
                        // Create a new list item for the uploaded library
                        const listItem = document.createElement("li");
                        listItem.classList.add("list-group-item");
                        listItem.id = `lib-item-${response.file}`;

                        // Add library name, size, and delete button
                        listItem.innerHTML = `
                <span class="text-primary-emphasis fw-semibold">${response.file}</span>
                <a class="text-danger float-end lib-delete-btn" href="#" data-project-id="${projectId}" data-lib-file="${response.id}">
                    <i class="fa-solid fa-trash-can"></i>
                </a>
                <span class="text-muted float-end me-3 small size">${response.size}</span>
            `;

                        // Append the new list item to the library list
                        libList.appendChild(listItem);

                        const countSpan = document.getElementById(`lib-list-count_${projectId}`);
                        countSpan.textContent = libList.childElementCount;

                        // add listener to the delete button
                        const deleteButton = listItem.querySelector('.lib-delete-btn');
                        addLibDeleteButtonListener(deleteButton);

                        this.removeFile(file);
                    }, 1000);
                }
            });
            this.on("error", function (file, response) {
                if (typeof response === 'string') {
                    if (response.includes("filesize")) {
                        showError('Your current plan does not allow files larger than '
                            + dropzone.getAttribute('data-max-file-size') +
                            'MB. Please upgrade your plan to upload larger files.'
                        );
                    } else {
                        showError(response);
                    }
                } else {
                    showError(response.message);
                }

                this.removeFile(file);
            });
        }
    };

    const dropzoneContainersJars = document.querySelectorAll(".dropzone-container-jar");
    dropzoneContainersJars.forEach((container) => {
        commonDropzoneOptionsJar.url = container.getAttribute('data-action');
        commonDropzoneOptionsJar.maxFilesize = container.getAttribute('data-max-file-size');
        new Dropzone(container, commonDropzoneOptionsJar);
    });

    const dropzoneContainersLibs = document.querySelectorAll(".dropzone-container-lib");
    dropzoneContainersLibs.forEach((container) => {
        commonDropzoneOptionsLib.url = container.getAttribute('data-action');
        commonDropzoneOptionsLib.maxFilesize = container.getAttribute('data-max-file-size');
        new Dropzone(container, commonDropzoneOptionsLib);
    });


    const jarDeleteButtons = document.querySelectorAll('.jar-delete-btn');
    jarDeleteButtons.forEach((button) => {
        button.addEventListener('click', (e) => {
            e.preventDefault();

            const projectId = button.getAttribute('data-project-id');

            button.innerHTML = '<i class="fa-solid fa-asterisk fa-spin"></i>';
            button.disabled = true;

            axios.delete('/app/project/' + projectId + '/jar')
                .then(function (response) {
                    setTimeout(() => {
                        if (response.data.status === "success") {
                            const fileView = document.querySelector(`.card[data-project-id="${projectId}"]`);
                            fileView.classList.add('d-none');

                            const dropzone = document.querySelector(`.dropzone-container-jar[data-project-id="${projectId}"]`);
                            dropzone.classList.remove('d-none');
                            dropzone.dropzone.removeAllFiles(true);
                        }
                        button.innerHTML = '<i class="fa-solid fa-trash-can"></i>';
                        button.disabled = false;
                    }, 1000);
                })
                .catch(function (error) {
                    showError(error);
                });
        });
    });

    function addLibDeleteButtonListener(button) {
        button.addEventListener('click', (e) => {
            e.preventDefault();

            const projectId = button.getAttribute('data-project-id');
            const libFile = button.getAttribute('data-lib-file');

            button.innerHTML = '<i class="fa-solid fa-asterisk fa-spin"></i>';
            button.disabled = true;

            axios.delete(`/app/project/${projectId}/lib/${libFile}`)
                .then(function (response) {
                    setTimeout(() => {
                        if (response.data.status === "success") {
                            const liElement = button.closest('li');
                            if (liElement) {
                                liElement.remove();
                            }
                            const countSpan = document.getElementById(`lib-list-count_${projectId}`);
                            const libList = document.getElementById(`lib-list_${projectId}`);
                            countSpan.textContent = libList.childElementCount;
                        }
                    }, 1000);
                })
                .catch(function (error) {
                    button.innerHTML = '<i class="fa-solid fa-trash-can"></i>';
                    button.disabled = false;
                    showError(error);
                });
        });
    }

    // Get all lib delete buttons and add event listeners
    const libDeleteButtons = document.querySelectorAll('.lib-delete-btn');
    libDeleteButtons.forEach(addLibDeleteButtonListener);


    const libsDeleteButtons = document.querySelectorAll('.libs-delete-btn');
    libsDeleteButtons.forEach((button) => {
        button.addEventListener('click', (e) => {
            e.preventDefault();

            const projectId = button.getAttribute('data-project-id');

            button.innerHTML = '<i class="fa-solid fa-circle-notch fa-spin me-1"></i> Loading';
            button.disabled = true;

            axios.delete('/app/project/' + projectId + '/libs')
                .then(function (response) {
                    setTimeout(() => {
                        if (response.data.status === "success") {
                            const libList = document.getElementById(`lib-list_${projectId}`);
                            libList.innerHTML = ''; // Remove all content within the list
                            const countSpan = document.getElementById(`lib-list-count_${projectId}`);
                            countSpan.textContent = '0';
                        }
                        button.innerHTML = '<i class="fa-solid fa-trash-can"></i> Delete all libraries';
                        button.disabled = false;
                    }, 1000);
                })
                .catch(function (error) {
                    showError(error);
                });
        });
    });
});
