import {defineConfig} from 'vite';
import laravel from 'laravel-vite-plugin';
import autoprefixer from 'autoprefixer';
import webfontDownload from 'vite-plugin-webfont-dl';
import removeComments from './resources/js/remove-comments';

// Load environment variables from .env
//dotenv.config();

export default defineConfig({
    plugins: [
        laravel({
            input: [
                'resources/sass/app.scss',
                'resources/js/app.js',

                'resources/js/snippets/index.js',
                'resources/js/snippets/auth.js',

                'resources/js/snippets/projects/projects.js',
            ],
            refresh: true,
        }),
        webfontDownload([
            'https://fonts.bunny.net/css?family=Roboto:100,200,300,400,500,600,700,800,900',
        ]),

    ],
    css: {
        postcss: {
            plugins: [
                autoprefixer(),
                removeComments(),
            ],
        },
    },
    resolve: {
        alias: {
            '$': 'jQuery',
        },
    },
});
