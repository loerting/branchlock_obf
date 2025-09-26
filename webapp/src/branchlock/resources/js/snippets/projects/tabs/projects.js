$(document).ready(function () {
    const projectNameInput = $("#newProjectName");
    const projectIdInput = $("#newProjectId");

    function generateProjectId(projectName) {
        return projectName.trim().toLowerCase().replace(/\s+/g, '-').replace(/[^a-z0-9-]/g, '');
    }

    function formatProjectIdManually() {
        projectIdInput.val(projectIdInput.val().toLowerCase().replace(/[^a-z0-9\s-]/g, '').replace(/\s+/g, '-').replace(/-{2,}/g, '-'));
    }

    projectNameInput.on("input", function () {
        projectIdInput.val(generateProjectId(projectNameInput.val()));
    });

    projectIdInput.on("input", formatProjectIdManually);

});
