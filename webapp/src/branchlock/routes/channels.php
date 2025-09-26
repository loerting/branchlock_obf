<?php

use Illuminate\Support\Facades\Broadcast;

/*
|--------------------------------------------------------------------------
| Broadcast Channels
|--------------------------------------------------------------------------
|
| Here you may register all of the event broadcasting channels that your
| application supports. The given channel authorization callbacks are
| used to check if an authenticated user can listen to the channel.
|
*/

Broadcast::channel('App.Models.User.{id}', function ($user, $id) {
    return (int)$user->id === (int)$id;
});

/*
Broadcast::channel('log.user.{userId}.project.{projectId}', function ($user, $userId, $projectId) {
    $project = Project::findOrFail($projectId);
    return (int)$user->id === (int)$userId && $project->user_id === Auth::id();
});

Broadcast::channel('output.user.{userId}.project.{projectId}', function ($user, $userId, $projectId) {
    $project = Project::findOrFail($projectId);
    return (int)$user->id === (int)$userId && $project->user_id === Auth::id();
});
*/
