<?php

namespace App\Livewire;

use App\Models\User;
use Carbon\Carbon;
use Livewire\Component;

class Feedback extends Component
{
    public function render()
    {
        $this->authorize('view', User::class);

        $feedbacks = \App\Models\Feedback::all()->sortByDesc('created_at');

        // format date human readable, add user email, if user does not exists than fallback string
        $feedbacks->map(function ($feedback) {
            $feedback->date = Carbon::create($feedback->created_at)->diffForHumans();

            $user = User::find($feedback->user_id);
            $feedback->user_email = $user ? $user->email : 'User deleted';
            return $feedback;
        });

        return view('livewire.feedback', [
            'feedbacks' => $feedbacks,
        ]);
    }
}
