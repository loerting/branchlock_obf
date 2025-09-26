<?php

use App\Http\Controllers\Api\CreateJob;
use App\Http\Controllers\Api\Download;
use App\Http\Controllers\Api\JobStatus;
use App\Http\Controllers\LicenseController;
use Illuminate\Routing\Middleware\ThrottleRequests;
use Illuminate\Support\Facades\Route;

/*
|--------------------------------------------------------------------------
| API Routes
|--------------------------------------------------------------------------
*/

Route::group(['middleware' => ['auth:sanctum', 'nosandbox', 'project.exists']], function () {

    Route::post('/job/new', CreateJob::class)
        ->middleware(ThrottleRequests::class . ':5,1');

    Route::get('/job/{project_id}/status', JobStatus::class);

    Route::get('/job/{project_id}/download', Download::class)->name('api.download');

});


