<?php

namespace App\Notifications;

use CraigPaul\Mail\TemplatedMailMessage;
use Illuminate\Bus\Queueable;
use Illuminate\Notifications\Notification;


class Newsletter extends Notification
{
    use Queueable;

    public string $title;
    public string $content;

    /**
     * Create a new notification instance.
     */
    public function __construct($title, $content)
    {
        $this->title = $title;
        $this->content = $content;
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
            ->identifier(34510560)
            ->include([
                'title' => $this->title,
                'body' => $this->content,
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
