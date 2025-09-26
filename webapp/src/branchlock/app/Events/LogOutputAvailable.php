<?php

namespace App\Events;

use Illuminate\Broadcasting\Channel;
use Illuminate\Broadcasting\InteractsWithSockets;
use Illuminate\Broadcasting\PrivateChannel;
use Illuminate\Contracts\Broadcasting\ShouldBroadcastNow;
use Illuminate\Foundation\Bus\Dispatchable;
use Illuminate\Queue\SerializesModels;

class LogOutputAvailable implements ShouldBroadcastNow
{
    use Dispatchable;
    use InteractsWithSockets;
    use SerializesModels;

    public mixed $output;
    public string $userId;
    public string $projectId;

    /**
     * Create a new event instance.
     *
     * @param string $output
     */
    public function __construct(string $userId, string $projectId, mixed $output)
    {
        $this->userId = $userId;
        $this->projectId = $projectId;

        if ($output) {
            if (empty($output)) return;

            $escapedOutput = htmlspecialchars($output, ENT_QUOTES, 'UTF-8');

            $maxOutputLength = 444;
            if (mb_strlen($escapedOutput) > $maxOutputLength) {
                $escapedOutput = mb_substr($escapedOutput, 0, $maxOutputLength) . '...';
            }

            $escapedOutput = preg_replace('/[^\PC\s]/u', '?&#8203;', $escapedOutput);
            $escapedOutput = preg_replace('/[^\x00-\x7F]/', '?&#8203;', $escapedOutput);

            $escapedOutput = $this->censorPath($escapedOutput);

            if (mb_strlen($escapedOutput) > 2) {
                $escapedOutput = preg_replace('/\s+/', ' ', trim($escapedOutput));

                // detect other log types
                if (!str_contains($escapedOutput, 'INFO') &&
                    !str_contains($escapedOutput, 'WARN') &&
                    !str_contains($escapedOutput, 'DEBUG') &&
                    !str_contains($escapedOutput, 'ERROR') &&
                    !str_contains($escapedOutput, 'api:')) {

                    if (str_starts_with($escapedOutput, 'Exception') ||
                        str_starts_with($escapedOutput, 'Error') ||
                        str_starts_with($escapedOutput, 'at')) {
                        $escapedOutput = "stderr:" . $this->censorJavaException($escapedOutput);
                    } else {
                        $escapedOutput = "?:$escapedOutput";
                    }
                }
            }

            $this->output = $escapedOutput;
        } else {
            $this->output = $output;
        }
    }

    /**
     * Get the channels the event should broadcast on.
     *
     * @return Channel|array
     */
    public function broadcastOn(): Channel|array
    {
        return new PrivateChannel("App.Models.User.{$this->userId}");
    }

    public function censorJavaException($exceptionMessage): string
    {
        $pattern = '/at\s+[A-Za-z\.]+/';
        return preg_replace($pattern, '***', $exceptionMessage);
    }

    function censorPath($line): string
    {
        return preg_replace('/\/var\/www[^\s]*/', '***', $line);
    }

}
