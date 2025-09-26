//import('./bootstrap');
import './watermark';
import _ from 'lodash';
import axios from 'axios';
import * as bootstrap from 'bootstrap'
import jQuery from 'jquery';
import {Dropzone} from "dropzone";
import hljs from 'highlight.js/lib/core';
import java from 'highlight.js/lib/languages/java';
import plain from 'highlight.js/lib/languages/plaintext';
import json from 'highlight.js/lib/languages/json';
import gradle from 'highlight.js/lib/languages/gradle';
import xml from 'highlight.js/lib/languages/xml';

import * as global from './global';
import './snippets/copy-buttons';
import './snippets/generate-token';
import Echo from "laravel-echo";
import Pusher from 'pusher-js';
import 'simplebar';

window.Pusher = Pusher;

window.Echo = new Echo({
    broadcaster: 'pusher',
    key: import.meta.env.VITE_PUSHER_APP_KEY,
    cluster: import.meta.env.VITE_PUSHER_APP_CLUSTER,
    wsHost: window.location.hostname,
    wsPort: 8080,
    wssHost: window.location.hostname,
    wssPort: 443,
    enabledTransports: ['ws', 'wss'],
    forceTLS: false,
    disableStats: true,
});

window.showError = global.showErrorToast;
window.showSuccess = global.showSuccessToast;
window.autoSave = global.AutoSaveModule;

hljs.registerLanguage('java', java);
hljs.registerLanguage('plain', plain);
hljs.registerLanguage('json', json);
hljs.registerLanguage('gradle', gradle);
hljs.registerLanguage('xml', xml);
hljs.registerLanguage('custom-log', function (hljs) {
    return {
        case_insensitive: true,
        keywords: '',
        contains: [
            /*{
                className: 'log-line',
                begin: '\\d{2}:\\d{2}:\\d{2} INFO :',
                end: '$'
            },*/
            /*{
                className: 'save-output',
                begin: '\\d{2}:\\d{2}:\\d{2} INFO : Saving output',
                end: '$'
            },*/
            {
                className: 'timestamp',
                begin: '\\d{2}:\\d{2}:\\d{2}',
            },
            {
                className: 'string',
                begin: '"', end: '"'
            },
            {
                className: 'number',
                begin: '\\b\\d+\\.?\\d*\\b'
            },
            {
                className: 'meta',
                begin: '@[\u00C0-\u02B8a-zA-Z_$][\u00C0-\u02B8a-zA-Z_$0-9]*',
                contains: [
                    {
                        begin: /\(/,
                        end: /\)/,
                        contains: ["self"] // allow nested () inside our annotation
                    }
                ]
            },
        ]
    }
});

window._ = _;

window.axios = axios;

window.axios.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';
window.$ = jQuery;
window.bootstrap = bootstrap;
window.hljs = hljs;


window.Dropzone = Dropzone;

import("highlightjs-line-numbers.js").then(() => {
    hljs.initLineNumbersOnLoad();
});

hljs.highlightAll()

const popoverTriggerList = document.querySelectorAll('[data-bs-toggle="popover"]');
const popoverList = [...popoverTriggerList].map(popoverTriggerEl => new bootstrap.Popover(popoverTriggerEl));

const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]');
const tooltipList = [...tooltipTriggerList].map(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl));

const toastElList = document.querySelectorAll('.toast');
const toastList = [...toastElList].map(toastEl => new bootstrap.Toast(toastEl));

// Color mode handling
(() => {
    'use strict';

    const toggleTheme = () => {
        const currentTheme = document.documentElement.getAttribute('data-bs-theme') || 'light';
        const newTheme = currentTheme === 'light' ? 'dark' : 'light';
        document.documentElement.setAttribute('data-bs-theme', newTheme);
        localStorage.setItem('theme', newTheme);
    };

    const initializeTheme = () => {
        const storedTheme = localStorage.getItem('theme');
        const preferredTheme = window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
        const themeToUse = storedTheme || preferredTheme;
        document.documentElement.setAttribute('data-bs-theme', themeToUse);
    };

    window.addEventListener('DOMContentLoaded', () => {
        const toggleThemeButtons = document.querySelectorAll('.themeToggle');
        toggleThemeButtons.forEach(button => {
                button.addEventListener('click', () => {
                    toggleTheme();
                    cacheTheme();
                });
            }
        );
    });

    const cacheTheme = () => {
        const currentTheme = document.documentElement.getAttribute('data-bs-theme') || 'light';

        axios.get('/theme/' + currentTheme, {}).then(() => {
           // console.log('Theme cached');
        }).catch((error) => {
            console.log(error);
        });
    };

})();

