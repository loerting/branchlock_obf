<?php

use App\Http\Controllers\Api\Token;
use App\Http\Controllers\Auth\LoginController;
use App\Http\Controllers\Auth\OAuthController;
use App\Http\Controllers\Auth\RegisterController;
use App\Http\Controllers\ChangelogController;
use App\Http\Controllers\DocsController;
use App\Http\Controllers\DownloadController;
use App\Http\Controllers\FeedbackController;
use App\Http\Controllers\IndexController;
use App\Http\Controllers\InteractiveDemoController;
use App\Http\Controllers\JarController;
use App\Http\Controllers\LibraryController;
use App\Http\Controllers\LicenseController;
use App\Http\Controllers\ObfuscatorController;
use App\Http\Controllers\ProjectController;
use Illuminate\Routing\Middleware\ThrottleRequests;
use Illuminate\Support\Facades\App;
use Illuminate\Support\Facades\Cookie;
use Illuminate\Support\Facades\Route;

/*
|--------------------------------------------------------------------------
| Web Routes
|--------------------------------------------------------------------------
*/


// Home routes
Route::group(['as' => 'home.'], function () {

    Route::get('/', [IndexController::class, 'index'])->name('index');

    Route::get('/changelog', [ChangelogController::class, 'show'])->name('changelog');

    Route::get('/licenses', [LicenseController::class, 'show'])->name('pricing');

    Route::get('/docs', [DocsController::class, 'show'])->name('docs');

    Route::get('/terms', function () {
        return view('home.sites.terms');
    })->name('terms');

    Route::get('/privacy', function () {
        return view('home.sites.privacy');
    })->name('privacy');

    Route::get('/impressum', function () {
        return view('home.sites.imprint');
    })->name('imprint');

});

// Cache theme to prevent flashing
Route::get('/theme/{theme}', function ($theme) {
    if (in_array($theme, ['light', 'dark'])) {
        $cookie = Cookie::forever('theme', $theme);
        return response('Theme changed')->withCookie($cookie);
    }
    return response('Theme not changed', 400);
})->name('change.theme');

// Interactive demo route
Route::post('/demo', [InteractiveDemoController::class, 'process'])
    ->name('demo')
    ->middleware(ThrottleRequests::class . ':15,1');

// Local environment routes for devs
if (App::environment('local')) {
    Route::get('/login/offline', [OAuthController::class, 'offlineLogin'])->name('login.offline');

    Route::get('/phpinfo', function () {
        return phpinfo();
    });
}


// Guest routes
Route::group(['middleware' => ['guest']], function () {

    Route::post('/login/sandbox', [OAuthController::class, 'sandboxLogin'])->name('login.sandbox');

    // Legacy email login
    Route::view('/legacy/login', 'auth.sites.legacy.signin')->name('login.legacy');
    Route::post('/legacy/login', LoginController::class)->name('login.legacy.perform');

    Route::view('/legacy/register', 'auth.sites.legacy.signup')->name('register.legacy');
    Route::post('/legacy/register', RegisterController::class)->name('register.legacy.perform');

    Route::view('/legacy/forgot-password', 'auth.sites.legacy.forgot-password')->name('password.reset');
    Route::post('/legacy/forgot-password', [LoginController::class, 'sendResetLinkEmail'])->name('password.email');
    Route::get('/legacy/reset-password/{token}', function ($token) {
        return view('auth.sites.legacy.reset-password', ['token' => $token]);
    })->name('password.reset.token');
    Route::post('/legacy/reset-password', [LoginController::class, 'resetPassword'])->name('password.update');

    // Magic login AKA OAuth
    Route::get('/login/{plan?}', [OAuthController::class, 'show'])->name('login.show');

    Route::group([
        'prefix' => '/oauth/{provider}',
        'controller' => OAuthController::class,
        'as' => 'oauth.',
        'where' => [
            'provider' => implode('|', array_keys(config('settings.oauth_providers'))),
        ]
    ], function () {
        Route::get('/', 'redirectToProvider')->name('redirect');
        Route::get('/callback', 'providerCallback');
    });

});

// Authenticated users routes
Route::group(['middleware' => ['auth', 'verified.email']], function () {

    Route::group(['prefix' => '/app', 'as' => 'app'], function () {

        Route::get('/', function () {
            return redirect()->route('app.projects');
        });

        // License routes
        Route::get('/acquire-license/{license}/{duration}', [LicenseController::class, 'get'])->name('.license.get');
        Route::post('/acquire-license/{license}/{duration}', [LicenseController::class, 'createPayment'])->name('.license.buy');
        // paid route
        Route::view('/paid', 'app.sites.paid')->name('.paid');

        Route::post('/feedback', [FeedbackController::class, 'sendFeedback'])
            ->name('.feedback')
            ->middleware(ThrottleRequests::class . ':3,1');

        Route::get('/projects', [ProjectController::class, 'show'])->name('.projects');
        Route::post('/project/add', [ProjectController::class, 'add'])->name('.project.add');

        Route::group(['middleware' => 'project.owner'], function () {
            Route::delete('/project/{id}', [ProjectController::class, 'delete'])->name('.project.delete');
            Route::put('/project/{id}/rename', [ProjectController::class, 'rename'])->name('.project.rename');

            Route::post('/project/{id}/uploadJar', [JarController::class, 'upload'])->name('.project.jar.upload');
            Route::delete('/project/{id}/jar', [JarController::class, 'delete'])->name('.project.jar.delete');

            Route::post('/project/{id}/uploadLib', [LibraryController::class, 'upload'])->name('.project.lib.upload');
            Route::delete('/project/{id}/lib/{lid}', [LibraryController::class, 'delete'])->name('.project.lib.delete');
            Route::delete('/project/{id}/libs', [LibraryController::class, 'deleteAll'])->name('.project.libs.delete');

            Route::put('/project/{id}/resetTasks', [ProjectController::class, 'resetTasks'])->name('.project.tasks.reset');

            Route::post('/project/{id}/save', [ProjectController::class, 'save'])->name('.project.save');

            Route::post('/project/{id}/process', [ObfuscatorController::class, 'process'])
                ->name('.project.process')
                ->middleware(ThrottleRequests::class . ':5,1');

            Route::get('/project/{id}/download', [DownloadController::class, 'downloadOutput'])->name('.project.download');
        });

        Route::get('/stacktrace', function () {
            return view('app.sites.stacktrace');
        })->name('.stacktrace');

        Route::get('/settings', function () {
            return view('app.sites.settings');
        })->name('.settings');

        Route::get('/support', function () {
            return view('app.sites.support');
        })->name('.support');

        // Admin routes
        Route::group(['middleware' => 'admin'], function () {

            Route::view('/admin', 'app.sites.admin-panel.index')->name('.admin');

        });
        Route::get('/admin/switchback', function () {
            if (session()->has('original_admin_user')) {
                $originalAdminUser = session('original_admin_user');

                if ($originalAdminUser) {
                    auth()->login($originalAdminUser);

                    session()->forget('original_admin_user');

                    return redirect()->route('app.admin');
                }
            }

            return redirect()->route('app.projects');
        })->name('.admin.switchback');

        // Legacy change password
        Route::put('/settings/password', [LoginController::class, 'changePassword'])->name('.settings.password');

        // route to create token
        Route::post('/token', Token::class)
            ->name('.token.create')
            ->middleware([ThrottleRequests::class . ':2,1', 'nosandbox']);

    });

    Route::get('/logout', [OAuthController::class, 'logout'])->name('logout');

});

//email verification
Route::get('/email/verify', [RegisterController::class, 'verificationNotice'])->name('verification.notice');
Route::get('/verify-email/{token}', [RegisterController::class, 'verifyEmail'])->name('verify.email');


// sellix webhook
Route::post('/sellix/webhook', [LicenseController::class, 'sellixWebhook'])->name('sellix.webhook');
