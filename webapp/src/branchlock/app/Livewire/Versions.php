<?php

namespace App\Livewire;

use App\Branchlock;
use App\Models\User;
use Livewire\Component;

class Versions extends Component
{
    public function render()
    {
        $this->authorize('view', User::class);

        $versions = Branchlock::getVersions();

        //

        return view('livewire.versions', [
            'versions' => $versions,
        ]);
    }
}
