<?php

namespace App\Models;

use App\Helpers\CustomHelper;
use Carbon\Carbon;
use Illuminate\Support\Facades\Storage;
use Illuminate\Support\Str;
use MongoDB\Laravel\Eloquent\Model;

class Project extends Model
{
    protected $table = 'projects';

    protected $attributes = [
        'user_id' => null,
        'name' => null,
        'project_id' => null,
        'android' => false,
        'jar' => null,
        'libs' => [],
        'config' => [
            'general' => [
                'random_seed' => '',
            ],
        ],
    ];

    public function __construct(array $attributes = [])
    {
        parent::__construct($attributes);

        $this->attributes['config']['general']['random_seed'] = Str::random(10);
        if ($this->attributes['android']) {
            $this->attributes['config']['delete_jar'] = true;
            $this->attributes['config']['delete_libs'] = true;
        }
    }

    protected $fillable = [
        'user_id',
        'name',
        'project_id',
        'android',
        'config'
    ];

    public function addInput($jarFile): void
    {
        $project = $this;
        $jarContents = file_get_contents($jarFile->getRealPath());

        $newFileName = isset($project['jar']) ? $project['jar'] : CustomHelper::uniqueFileName();
        Storage::disk('uploads')->put($newFileName, $jarContents);

        $project['jar'] = $newFileName;
        $project['jar_original_name'] = $jarFile->getClientOriginalName();
        $project['jar_size'] = $jarFile->getSize();
        $project['jar_uploaded_at'] = Carbon::now()->toDateTimeString();
        $project->save();
        $project->refresh();
    }

    public function addLib($libFile): string
    {
        $project = $this;
        // if project already has this library, then don't add it again
        if (collect($project->libs)->where('name', $libFile->getClientOriginalName())->where('size', $libFile->getSize())->first()) {
            return false;
        }

        $jarContents = file_get_contents($libFile->getRealPath());

        $newFileName = CustomHelper::uniqueFileName();
        Storage::disk('uploads')->put($newFileName, $jarContents);

        $libData = [
            'name' => $libFile->getClientOriginalName(),
            'size' => $libFile->getSize(),
            'file' => $newFileName
        ];

        $project->push('libs', $libData);
        $project->refresh();

        return $newFileName;
    }

    public function removeInput(): void
    {
        $project = $this;
        if (isset($project['jar'])) {
            if (Storage::disk('uploads')->exists($project->jar)) {
                Storage::disk('uploads')->delete($project->jar);
            }
        }

        $project->jar = null;
        $project->save();
        $project->refresh();
    }

    public function removeLib($library_id): void
    {
        $project = $this;
        $lib = collect($project->libs)->where('file', $library_id)->first();

        if (Storage::disk('uploads')->exists($lib['file'])) {
            Storage::disk('uploads')->delete($lib['file']);
        }

        $project->pull('libs', ['file' => $lib['file']]);
        $project->refresh();
    }

    public function removeAllLibs(): void
    {
        $project = $this;
        if (isset($project->libs)) {
            foreach ($project->libs as $lib) {
                if (Storage::disk('uploads')->exists($lib['file'])) {
                    Storage::disk('uploads')->delete($lib['file']);
                }
            }

            $project->libs = [];
            $project->save();
            $project->refresh();
        }
    }

    public function deleteAll(): void
    {
        $project = $this;

        if (isset($project['jar'])) {
            $outputFileName = CustomHelper::reverseFileName($project['jar']);
            if (Storage::disk('uploads')->exists($outputFileName)) {
                Storage::disk('uploads')->delete($outputFileName);
            }

            $configFileName = $outputFileName . '.json';
            if (Storage::disk('uploads')->exists($configFileName)) {
                Storage::disk('uploads')->delete($configFileName);
            }

            $logFileName = $project['id'] . '-log';
            if (Storage::disk('uploads')->exists($logFileName)) {
                Storage::disk('uploads')->delete($logFileName);
            }
        }

        self::removeAllLibs();
        self::removeInput();

        $project->delete();
    }
}
