<?php

namespace App\Models;

use MongoDB\Laravel\Eloquent\Model;

class Task extends Model
{
    protected $table = 'tasks';

    public static function getCategoryOrder(): array
    {
        return ['Basic', 'Encryption', 'Obfuscation', 'Monitoring', 'Other'];
    }

    // get category order interative demo (The "best" tasks should be at the top)
    public static function getInteractiveDemoCategoryOrder(): array
    {
        return ['Encryption', 'Obfuscation', 'Basic', 'Other', 'Monitoring'];
    }

    public static function getInteractiveDemoTasks()
    {
        return Task::where('interactive_demo', true)->get();
    }

    public static function getAvailableTasks(bool $fullAccess, bool $android = false, bool $experimental = true): array
    {
        $query = Task::query()
            ->when(!$android, function ($q) {
                $q->where('desktop', true);
            })
            ->when($android, function ($q) {
                $q->where('android', true);
            })
            ->when(!$fullAccess, function ($q) {
                $q->where('premium', false);
            })
            ->when(!$experimental, function ($q) {
                $q->where('experimental', false);
            });

        $tasks = [];
        foreach ($query->get() as $task) {
            $tasks[$task['backend_name']]['enabled'] = false;
            foreach ($task['settings'] as $optionBackend_name => $option) {
                $tasks[$task['backend_name']][$optionBackend_name] = $option['value'];
            }
        }

        return $tasks;
    }

    public static function getGeneralConfig(): array
    {
        return array_map(function ($option) {
            return $option['value'];
        }, config('branchlock-config.general'));
    }

    public static function getAvailableGeneralOptions(bool $fullAccess, bool $android = false): array
    {
        $options = [];
        foreach (config('branchlock-config.general') as $key => $option) {
            if ($option['premium'] && !$fullAccess) continue;
            if ($android && !$option['android'] || !$android && !$option['desktop']) continue;

            $options[$key] = $option['value'];
        }

        return $options;
    }
}
