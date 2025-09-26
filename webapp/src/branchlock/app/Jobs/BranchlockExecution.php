<?php

namespace App\Jobs;

use App\Events\LogOutputAvailable;
use App\Jobs\Middleware\BranchlockLimiter;
use Exception;
use Illuminate\Bus\Queueable;
use Illuminate\Contracts\Queue\ShouldBeEncrypted;
use Illuminate\Contracts\Queue\ShouldQueue;
use Illuminate\Foundation\Bus\Dispatchable;
use Illuminate\Queue\InteractsWithQueue;
use Illuminate\Queue\SerializesModels;
use Illuminate\Support\Facades\Log;
use Illuminate\Support\Facades\Storage;

class BranchlockExecution implements ShouldQueue, ShouldBeEncrypted
{
    use Dispatchable, InteractsWithQueue, Queueable, SerializesModels;

    public $timeout = 10 * 60;
    public $printTimeout = 5 * 60;
    public $failOnTimeout = true;

    public mixed $userId;
    public mixed $projectId;
    public string $runClass;
    public string $blPath;
    public string $jsonPath;
    public bool $sync;
    public bool $api;
    public bool $javaDebug = false;
    private array $output = [];

    /**
     * Create a new job instance.     */
    public function __construct(string $blPath, string $runClass, string $jsonPath, $userId = null, $projectId = null,
                                bool   $sync = false, bool $api = false, bool $javaDebug = false)
    {
        $this->userId = $userId;
        $this->projectId = $projectId;
        $this->runClass = $runClass;
        $this->blPath = $blPath;
        $this->jsonPath = $jsonPath;
        $this->sync = $sync;
        $this->api = $api;
        $this->javaDebug = $javaDebug;
    }

    public function middleware(): array
    {
        return [new BranchlockLimiter()];
    }

    /**
     * Execute the job.     */
    public function handle()
    {
        if ($this->api) {
            $this->handleApiInfo('API execution initiated. If not initiated by you, update your API token promptly.');
        }

        try {
            $BlPath = escapeshellarg($this->blPath);
            $RunClass = escapeshellarg($this->runClass);
            $JsonPath = escapeshellarg($this->jsonPath);

            $cmd = 'java';

            if ($this->javaDebug) {
                $cmd .= ' -verbose:gc';
            }

            $cmd .= ' -cp ' . escapeshellcmd("$BlPath $RunClass $JsonPath") . ' 2>&1';


            Log::info('Executing command: ' . $cmd);

            $pid = pcntl_fork();

            if ($pid == -1) {
                Log::error('Error forking process.');
            } elseif ($pid == 0) {
                $dummy = $this->runChildProcess($cmd, $this->printTimeout);
            } else {
                pcntl_wait($status);
            }


            if ($this->sync) {
                return $this->output;
            } else {
                // save log to file
                $log = implode("", $this->output);
                Storage::disk('uploads')->put($this->projectId . "-log", $log);
            }

        } catch (Exception $e) {
            Log::error('Error executing BranchlockExecution job: ' . $e->getMessage());
        }

        Log::info('Execution finished.');

        return null;
    }

    protected function runChildProcess($cmd, $printTimeout)
    {
        $descriptors = [
            0 => ['pipe', 'r'],   // stdin
            1 => ['pipe', 'w'],   // stdout
            //  2 => ['pipe', 'w']    // stderr
        ];

        $process = proc_open($cmd, $descriptors, $pipes);

        if (!is_resource($process)) {
            Log::error('Error executing BranchlockExecution job');
            return null;
        }

        stream_set_blocking($pipes[1], 0); // Set stdout to non-blocking

        $lastOutputTime = time();

        while (true) {
            $elapsedTime = time() - $lastOutputTime;
            if ($elapsedTime >= $printTimeout) {
                $this->handleError('Execution timeout exceeded.');

                // Force kill the process
                proc_terminate($process, SIGKILL);
                break;
            }

            $line = fgets($pipes[1]);

            if ($line !== false) {
                $this->handleOutput($line);
                $lastOutputTime = time();
            } else {
                // Check if process ended
                $status = proc_get_status($process);
                if (!$status['running']) {
                    $this->handleOutput(false);
                    break;
                }
            }

            usleep(5000); // Small delay to prevent CPU overload
        }

        fclose($pipes[0]);
        fclose($pipes[1]);
        //fclose($pipes[2]);

        proc_close($process);
    }


    protected function handleError($message): void
    {
        Log::error($message . ' Killing the process.');
        $this->handleOutput("Error : $message");

        if (!$this->sync) event(new LogOutputAvailable($this->userId, $this->projectId, false));
    }

    protected function handleApiInfo($message): void
    {
        if (!$this->sync) event(new LogOutputAvailable($this->userId, $this->projectId, "api:$message"));
    }

    protected function handleOutput($line): void
    {
        //Log::info($line);
        $this->output[] = $line;
        if (!$this->sync) event(new LogOutputAvailable($this->userId, $this->projectId, $line));
    }

}
