<?php

namespace App\Notifications;

use CraigPaul\Mail\TemplatedMailMessage;
use Illuminate\Bus\Queueable;
use Illuminate\Notifications\Notification;

class PasswordReset extends Notification
{
    use Queueable;

    public string $actionUrl;
    public string $operatingSystem;
    public string $browserName;

    /**
     * Create a new notification instance.
     */
    public function __construct($actionUrl, $operatingSystem, $browserName)
    {
        $this->actionUrl = $actionUrl;
        $this->operatingSystem = $operatingSystem;
        $this->browserName = $browserName;
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
            ->identifier(34253186)
            ->include([
                'action_url' => $this->actionUrl,
                'product_url' => config('settings.url'),
                'product_name' => 'Branchlock',
                'operating_system' => $this->operatingSystem,
                'browser_name' => $this->browserName,
                'support_url' => config('settings.url'),
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
