$(document).ready(function () {
    const socials = document.getElementById('socials');
    socials.classList.remove('d-none');

    const toggleAllTasksButtons = document.querySelectorAll('.toggle-all');
    const items = document.querySelectorAll('.task-switch[type="checkbox"]');
    const changingInterval = 666;
    let changingTimer;

    toggleAllTasksButtons.forEach((button) => {
        button.addEventListener('click', (e) => {
            e.preventDefault();
            toggleAllTasks();
            clearTimeout(changingTimer);
            changingTimer = setTimeout(getInteractiveDemoCode, changingInterval);
        });
    });

    items.forEach(item => {
        item.addEventListener('change', function (e) {
            e.preventDefault();
            clearTimeout(changingTimer);
            changingTimer = setTimeout(getInteractiveDemoCode, changingInterval);
        });
    });

    function toggleAllTasks() {
        const switches = document.querySelectorAll(`.task-switch[type="checkbox"]`);

        let allEnabled = Array.from(switches).every(switchElem => switchElem.checked);

        switches.forEach((item) => {
            if (!item.disabled) {
                item.checked = !allEnabled;
            }
        });

        allEnabled = !allEnabled;
    }

    function getInteractiveDemoCode() {
        const code = $('#interactive-demo-code');
        const spinner = document.getElementById('interactive-demo-spinner');
        spinner.style.display = "flex";

        const postData = {
            tasks: {}
        };

        items.forEach(item => {
            if (!item.disabled) {
                postData.tasks[item.id] = item.checked;
            }
        });

        const route = document.getElementById('interactive-demo').dataset.demoRoute;

        axios.post(route, postData)
            .then(response => {
                code.fadeOut(666, function () {
                    code.html(response.data.code);
                    code.removeAttr('data-highlighted');
                    hljs.highlightElement(code[0]);
                    hljs.initLineNumbersOnLoad();
                    spinner.style.display = "none";
                    code.fadeIn(666);
                });
            })
            .catch(error => {
                code.fadeOut(666, function () {
                    code.html("/* Something went wrong... */");
                    code.removeAttr('data-highlighted');
                    hljs.highlightAll();
                    hljs.initLineNumbersOnLoad();
                    spinner.style.display = "none";
                    code.fadeIn(666);
                });
            });
    }
});
