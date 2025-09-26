import * as projects from './tabs/projects.js';
import * as files from './tabs/files.js';
import * as config from './tabs/config.js';
import * as ranges from './tabs/ranges.js';
import * as process from './tabs/process.js';
import * as admin from './tabs/admin.js';

export {projects, files, config, ranges, process, admin};

/*
const tabChange = document.querySelector('.nav-pills');
const notificationSound = new Audio('/audio/click.mp3');
notificationSound.volume = 0.1;

$('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
    notificationSound.play();
})
*/
