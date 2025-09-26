<?php

namespace App\Livewire;

use App\Models\User;
use Livewire\Component;

class SearchUsers extends Component
{
    public string $search = '';
    public $userToDelete;

    public function render()
    {
        $this->authorize('view', User::class);

        $planOrder = [
            'enterprise',
            'team',
            'solo',
            'free',
        ];

        $users = User::where('name', 'like', '%' . $this->search . '%')
            ->orWhere('email', 'like', '%' . $this->search . '%')
            ->get();

        // sort by plan
        $users = $users->sortBy(function ($user) use ($planOrder) {
            return array_search($user->plan, $planOrder);
        });

        // role has priority over plan
        $users = $users->sortBy(function ($user) {
            return $user->role;
        });

        return view('livewire.search-users', [
            'users' => $users,
        ]);
    }

    public function confirmUserDeletion($userId)
    {
        $this->userToDelete = $userId;
    }

    public function deleteUser($userId)
    {
        $this->authorize('delete', User::class);

        $user = User::find($userId);

        if ($user) {
            $user->delete();
        }

        $this->render();
    }

    public function signInAsUser($userId)
    {
        return false;

        $adminUser = auth()->user();

        $this->authorize('update', User::class);

        $user = User::find($userId);

        if ($user) {
            auth()->login($user);
            session()->put('original_admin_user', $adminUser);
            return redirect()->route('app.projects');
        }

        return redirect()->route('app.admin');
    }
}





