function toggleCollapse(id) {
    const taskSwitch = document.getElementById(id);
    let collapseElement = document.getElementById(`collapse_${id}`);

    if (!collapseElement) return;
    if (taskSwitch.checked && collapseElement.classList.contains('show')) return;
    if (!taskSwitch.checked && !collapseElement.classList.contains('show')) return;

    const collapseElementBs = new bootstrap.Collapse(collapseElement);

    taskSwitch.disabled = true;

    if (taskSwitch.checked) {
        collapseElementBs.show();
    } else {
        collapseElementBs.hide();
    }

    setTimeout(() => {
        taskSwitch.disabled = false;
    }, 300);
}

document.addEventListener('DOMContentLoaded', function () {
    const toggleAllTasksButtons = document.querySelectorAll('.toggle-all');
    toggleAllTasksButtons.forEach(button => {
        button.addEventListener('click', (e) => {
            e.preventDefault();
            const projectId = button.getAttribute('data-project-id');
            const switches = document.querySelectorAll(`#v-pills-tabContent_${projectId} .task-switch`);

            let allEnabled = Array.from(switches).filter(switchElem => !switchElem.disabled).every(switchElem => switchElem.checked);

            button.disabled = true;

            switches.forEach(item => {
                if (!item.disabled) {
                    item.checked = !allEnabled;
                    toggleCollapse(item.id);
                }
            });

            autoSave.handleAutoSave(projectId);

            setTimeout(() => {
                button.disabled = false;
            }, 300);
        });
    });

    const taskSwitches = document.querySelectorAll('.task-switch');
    taskSwitches.forEach(checkbox => {
        checkbox.addEventListener('change', function () {
            toggleCollapse(this.id);
        });
    });

    const resetTaskButtons = document.querySelectorAll('.reset-tasks');
    resetTaskButtons.forEach(button => {
        button.addEventListener('click', (e) => {
            e.preventDefault();
            const route = button.getAttribute('data-route');
            const projectId = button.getAttribute('data-project-id');

            axios.put(route)
                .then(response => {
                    if (!response.data.tasks) {
                        showError('Something went wrong.');
                        return;
                    }

                    const switches = document.querySelectorAll(`#v-pills-tabContent_${projectId} .task-switch`);

                    switches.forEach(item => {
                        if (item.disabled) return;

                        item.checked = false;
                        toggleCollapse(item.id);

                        const taskId = item.id.split('_')[0];
                        const taskName = item.getAttribute('data-task-name');
                        //console.log(taskName + ':');

                        const collapseElement = document.getElementById("collapse_" + taskId + "_" + projectId);


                        if (collapseElement) {
                            //console.log("DETECTED:" + collapseElement);
                            const settingElements = collapseElement.querySelectorAll('[data-setting-name]');
                            settingElements.forEach(settingElement => {
                                //console.log(settingElement);
                                const settingName = settingElement.getAttribute('data-setting-name');
                                let settingValue = response.data.tasks[taskName][settingName];

                                // console.log(" - " + settingName + ": " + settingValue);

                                if (settingValue && !isNaN(settingValue) && settingValue % 1 !== 0) {
                                    settingValue = settingValue * 10;
                                }

                                if (settingElement.type === 'checkbox') {
                                    settingElement.checked = settingValue;
                                } else {
                                    settingElement.value = settingValue;
                                }
                            });
                        }
                    });

                    showSuccess('All tasks have been restored to their default values.');
                    autoSave.handleAutoSave(projectId);
                })
                .catch((error) => {
                    console.log(error);
                    showError(error.response);
                });
        });
    });
});
