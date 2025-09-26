<?php

namespace App\Policies;

use App\Models\User;
use Illuminate\Auth\Access\HandlesAuthorization;

class AdminPolicy
{
    use HandlesAuthorization;
    public function view(User $user)
    {
        return $user->role === User::ROLE_ADMIN;
    }

    public function create(User $user)
    {
        return $user->role === User::ROLE_ADMIN;
    }

    public function update(User $user)
    {
        return $user->role === User::ROLE_ADMIN;
    }

    public function delete(User $user)
    {
        return $user->role === User::ROLE_ADMIN;
    }
}
