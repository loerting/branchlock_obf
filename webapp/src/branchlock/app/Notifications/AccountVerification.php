<?php

namespace App\Notifications;

use CraigPaul\Mail\TemplatedMailMessage;
use Illuminate\Bus\Queueable;
use Illuminate\Contracts\Queue\ShouldQueue;
use Illuminate\Notifications\Messages\MailMessage;
use Illuminate\Notifications\Notification;

class AccountVerification extends Notification
{
    use Queueable;

    public string $actionUrl;

    /**
     * Create a new notification instance.
     */
    public function __construct($actionUrl)
    {
        $this->actionUrl = $actionUrl;
    }

    /**
     * Get the notification's delivery channels.
     *
     * @return array<int, string>
     */
    public function via(object $notifiable): array
    {
        return ['mail'];
    }

    /**
     * Get the mail representation of the notification.
     */
    public function toMail($notifiable)
    {
        return (new TemplatedMailMessage)
            ->identifier(34510666)
            ->include([
                'action_url' => $this->actionUrl,
                'product_url' => config('settings.url'),
                'product_name' => 'Branchlock',
            ]);
    }

    /**
     * Get the array representation of the notification.
     *
     * @return array<string, mixed>
     */
    public function toArray(object $notifiable): array
    {
        return [
            //
        ];
    }
}
