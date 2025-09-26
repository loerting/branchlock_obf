<?php

namespace App\Events;

use App\Models\Project;
use Illuminate\Broadcasting\InteractsWithSockets;
use Illuminate\Broadcasting\PrivateChannel;
use Illuminate\Contracts\Broadcasting\ShouldBroadcastNow;
use Illuminate\Foundation\Events\Dispatchable;
use Illuminate\Queue\SerializesModels;

class OutputFileAvailable implements ShouldBroadcastNow
{
    use Dispatchable, InteractsWithSockets, SerializesModels;

    public string $userId;
    public string $projectId;
    public string $fileName;
    public bool $jarDeleted;
    public bool $libsDeleted;

    /**
     * Create a new event instance.
     */
    public function __construct(string $userId, string $projectId, bool $jarDeleted, bool $libsDeleted)
    {
        $this->userId = $userId;
        $this->projectId = $projectId;
        $this->jarDeleted = $jarDeleted;
        $this->libsDeleted = $libsDeleted;

        $project = Project::findOrFail($this->projectId);
        $this->fileName = pathinfo($project['jar_original_name'], PATHINFO_FILENAME) . '-obf.jar';
        $this->fileName = htmlspecialchars($this->fileName, ENT_QUOTES, 'UTF-8');

    }


    /**
     * Get the channels the event should broadcast on.
     *
     * @return PrivateChannel
     */
    public function broadcastOn(): PrivateChannel
    {
        return new PrivateChannel("App.Models.User.{$this->userId}");
    }
}
