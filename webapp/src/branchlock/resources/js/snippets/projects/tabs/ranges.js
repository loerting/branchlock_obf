$(document).ready(function () {

    const rangeInputs = document.querySelectorAll('.input-ranges');
    rangeInputs.forEach((input) => {
        input.addEventListener('input', (e) => {
            e.preventDefault();

            const value = input.value;
            input.value = value.replace(/\s/g, '');
        });
    });

    const rangesButtons = document.querySelectorAll('.ranges-btn');
    rangesButtons.forEach((button) => {
        button.addEventListener('click', (e) => {
            e.preventDefault();

            const projectId = button.getAttribute('data-project-id');
            const taskId = button.getAttribute('data-task-id');
            const rangeType = button.getAttribute('data-range-type');

            const input = document.querySelector(`#input-ranges_${projectId}`);
            const taskOpener = document.querySelector(`#${rangeType}-task-opener_${taskId}_${projectId}`);
            const taskList = document.querySelector(`#${rangeType}-task-list_${taskId}_${projectId}`);

            if (input.value.length > 0) {
                if (taskOpener.classList.contains('d-none')) {
                    taskOpener.classList.remove('d-none');
                }

                // Add range to taskList
                const li = document.createElement('li');
                li.classList.add('list-group-item', 'd-flex', 'justify-content-between', 'align-items-center', 'fw-bold');

                li.textContent = input.value;
                if (input.value === '**') {
                    li.textContent = '** (All classes)';
                }

                const a = document.createElement('a');
                a.classList.add('text-danger', 'remove-range-btn');
                a.setAttribute('href', '#');
                a.setAttribute('data-project-id', projectId);
                a.setAttribute('data-task-id', taskId);
                a.setAttribute('data-range-type', rangeType);
                a.innerHTML = '<i class="fa-solid fa-xmark"></i>';
                li.appendChild(a);
                removeRangeButtonListener(a);

                // add hidden input to form
                const form = document.querySelector(`#project-form_${projectId}`);
                const hiddenInput = document.createElement('input');
                hiddenInput.classList.add('d-none');
                hiddenInput.setAttribute('type', 'hidden');
                if (taskId === 'general') {
                    hiddenInput.setAttribute('name', `general[${rangeType}][]${input.value}`);
                } else {
                    hiddenInput.setAttribute('name', `tasks[${taskId}][${rangeType}][]${input.value}`);
                }
                hiddenInput.setAttribute('value', input.value);
                li.appendChild(hiddenInput);

                taskList.appendChild(li);

                autoSave.handleAutoSave(projectId);
            }
        });
    });

    function removeRangeButtonListener(button) {
        button.addEventListener('click', (e) => {
            e.preventDefault();

            const projectId = button.getAttribute('data-project-id');
            const taskId = button.getAttribute('data-task-id');
            const rangeType = button.getAttribute('data-range-type');

            const taskOpener = document.querySelector(`#${rangeType}-task-opener_${taskId}_${projectId}`);
            const taskList = document.querySelector(`#${rangeType}-task-list_${taskId}_${projectId}`);
            const taskCollapse = document.querySelector(`#${rangeType}-task-collapse_${taskId}_${projectId}`);

            // Remove range from taskList
            button.parentElement.remove();

            autoSave.handleAutoSave(projectId);

            if (taskList.childElementCount === 0) {
                taskOpener.classList.add('d-none');
                const bootstrapCollapse = new bootstrap.Collapse(taskCollapse);
                bootstrapCollapse.hide();
            }
        });
    }

    // Remove range from taskList
    const removeRangeButtons = document.querySelectorAll('.remove-range-btn');
    removeRangeButtons.forEach((button) => {
        removeRangeButtonListener(button);
    });

});
