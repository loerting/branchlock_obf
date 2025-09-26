<?php

namespace App\Livewire;

use App\Models\User;
use GrahamCampbell\Markdown\Facades\Markdown;
use Livewire\Attributes\Validate;
use Livewire\Component;
use Postmark\PostmarkClient;

class Newsletter extends Component
{
    #[Validate('required|max:70')]
    public string $title = '';

    #[Validate('required')]
    public string $body = '';
    public string $htmlBody = '';

    public bool $admins = true;
    public bool $licensedUsers = false;
    public bool $DemoUsers = false;
    public bool $onlyNoDateUsers = false;

    public function render()
    {
        $this->authorize('view', User::class);

        return view('livewire.newsletter');
    }

    public function renderBodyPreview()
    {
        $this->authorize('view', User::class);

        $this->htmlBody = str_replace(array("\r", "\n"), '', Markdown::convertToHtml($this->body));
    }

    public function send()
    {
        $this->authorize('view', User::class);
        $this->validate();

        $users = User::all()->filter(function ($user) {
            return !in_array($user->auth_type, ['offline', 'sandbox']);
        });


        if (!$this->admins) {
            $users = $users->filter(function ($user) {
                return $user->role != User::ROLE_ADMIN;
            });
        }

        if (!$this->licensedUsers) {
            $users = $users->filter(function ($user) {
                return $user->role == User::ROLE_ADMIN || !in_array($user->plan, [User::PLAN_SOLO, User::PLAN_GROUP, User::PLAN_ENTERPRISE]);
            });
        }

        if (!$this->DemoUsers) {
            $users = $users->filter(function ($user) {
                return $user->role == User::ROLE_ADMIN || $user->plan != User::PLAN_FREE;
            });
        }

        if ($this->onlyNoDateUsers) {
            $users = $users->filter(function ($user) {
                return $user->role == User::ROLE_ADMIN || !$user->created_at;
            });
        }

        $usersChunks = array_chunk($users->toArray(), 500);

        //dd($usersChunks);

        foreach ($usersChunks as $chunk) {
            $this->sendBatch($chunk);
        }
    }

    protected function sendBatch($users)
    {
        $emails = [];

        foreach ($users as $user) {
            $emails[] = [
                'To' => $user['email'],
                'From' => config('settings.news_email'),
                'MessageStream' => "broadcast",
                'TemplateId' => 34510560,
                'TemplateModel' => [
                    'title' => $this->title,
                    'body' => $this->htmlBody,
                    'product_url' => config('settings.url'),
                    'product_name' => config('settings.name'),
                ]
            ];
        }

        $pClient = new PostmarkClient(config('settings.postmark_secret'));
        $responses = $pClient->sendEmailBatchWithTemplate($emails);

        //dd($responses);
    }
}
