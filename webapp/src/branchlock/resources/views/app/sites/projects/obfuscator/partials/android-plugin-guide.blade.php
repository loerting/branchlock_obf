
<div class="card bg-light rounded-4 shadow-none">
    <div class="card-header rounded-top-4 d-flex align-items-center justify-content-between">
        <ul class="nav nav-pills side-menu side-menu-horizontal fw-bold" id="pills-tab" role="tablist">
            <li class="nav-item" role="presentation">
                <button class="nav-link active" id="tab-groovy" data-bs-toggle="pill" data-bs-target="#groovy-code-{{ $project->id }}" type="button" role="tab"
                        aria-controls="groovy-code" aria-selected="true"><i class="fa-solid fa-file-code text-primary"></i>
                    Groovy
                </button>
            </li>
            <li class="nav-item" role="presentation">
                <button class="nav-link" id="tab-kotlin" data-bs-toggle="pill" data-bs-target="#kotlin-code-{{ $project->id }}" type="button" role="tab"
                        aria-controls="kotlin-code" aria-selected="true"><i class="fa-solid fa-file-code text-primary"></i>
                    Kotlin
                </button>
            </li>
        </ul>
    </div>
    <div class="card-body pb-0">
        <div class="tab-content m-0" id="pills-tabContent">
            <div class="tab-pane fade show active" id="groovy-code-{{ $project->id }}" role="tabpanel" aria-labelledby="tab-groovy-code" tabindex="0">
                <div class="step">
                    <div class="circle">1</div>
                    <p>Ensure the Gradle JVM is set to Java 11 or newer</p>
                </div>

                <div class="step">
                    <div class="circle">2</div>
                    <p>Add the plugin dependency to your app <code>build.gradle</code></p>
                </div>

                <div class="position-relative">
                    <div class="rounded-4" style="background: var(--side-menu-link-bg); overflow-wrap: break-word;">

            <pre class="py-3 px-2 font-monospace w-100 overflow-y-auto overflow-x-hidden" id="plugin-box_{{ $project->id }}"
                 style="white-space: pre-wrap;overflow-wrap: break-word;"><code class="language-gradle overflow-hidden">buildscript {
    repositories {
        maven { url = "{{ config('settings.url') }}/storage/maven/" }
    }
    dependencies {
        classpath 'net.branchlock:obfuscation:2.1.2'
    }
}</code></pre>
                    </div>
                </div>

                <div class="step mt-4">
                    <div class="circle">3</div>
                    <p>Apply the plugin in your app <code>build.gradle</code></p>
                </div>

                <div class="position-relative">
                    <div class="rounded-4" style="background: var(--side-menu-link-bg); overflow-wrap: break-word;">

            <pre class="py-3 px-2 font-monospace w-100 overflow-y-auto overflow-x-hidden" id="plugin-box2_{{ $project->id }}"
                 style="white-space: pre-wrap;overflow-wrap: break-word;"><code class="language-gradle overflow-hidden">/*
Do not place this inside the plugins block.
To test on debug builds use BLADebugPlugin instead.
*/
apply plugin: net.branchlock.obfuscation.android.BLAPlugin

branchlock {
    branchlockApiUrl = '{{ config('settings.url') }}/api'
    bearerToken = '' // The bearer token of your Branchlock account
    projectId = '{{ $project->project_id }}' // The project ID of your Branchlock project
}</code></pre>
                    </div>
                </div>

                <div class="step mt-4">
                    <div class="circle">4</div>
                    <p>Generate a bearer token in user settings and append it to the plugin config</p>
                </div>

                <div class="step">
                    <div class="circle">5</div>
                    <p>Configure your preferred settings in this web panel</p>
                </div>

                <div class="step">
                    <div class="circle">6</div>
                    <p>Build your project</p>
                </div>

            </div>
            <div class="tab-pane fade" id="kotlin-code-{{ $project->id }}" role="tabpanel" aria-labelledby="tab-kotlin-code" tabindex="0">
                <div class="step">
                    <div class="circle">1</div>
                    <p>Ensure the Gradle JVM is set to Java 11 or newer</p>
                </div>

                <div class="step">
                    <div class="circle">2</div>
                    <p>Add the plugin dependency to your app <code>build.gradle.kts</code></p>
                </div>

                <div class="position-relative">
                    <div class="rounded-4" style="background: var(--side-menu-link-bg); overflow-wrap: break-word;">

            <pre class="py-3 px-2 font-monospace w-100 overflow-y-auto overflow-x-hidden" id="plugin-box_{{ $project->id }}"
                 style="white-space: pre-wrap;overflow-wrap: break-word;"><code class="language-gradle overflow-hidden">buildscript {
    repositories {
        maven("{{ config('settings.url') }}/storage/maven/")
    }
    dependencies {
        classpath("net.branchlock:obfuscation:2.1.2")
    }
}</code></pre>
                    </div>
                </div>

                <div class="step mt-4">
                    <div class="circle">3</div>
                    <p>Apply the plugin in your app <code>build.gradle.kts</code></p>
                </div>

                <div class="position-relative">
                    <div class="rounded-4" style="background: var(--side-menu-link-bg); overflow-wrap: break-word;">

            <pre class="py-3 px-2 font-monospace w-100 overflow-y-auto overflow-x-hidden" id="plugin-box2_{{ $project->id }}"
                 style="white-space: pre-wrap;overflow-wrap: break-word;"><code class="language-gradle overflow-hidden">/*
Do not place this inside the plugins block.
To test on debug builds use BLADebugPlugin instead.
*/
apply&lt;net.branchlock.obfuscation.desktop.BLDPlugin&gt;()

configure&lt;BranchlockExtension&gt; {
    branchlockApiUrl = "{{ config('settings.url') }}/api"
    bearerToken = "" // The bearer token of your Branchlock account
    projectId = "{{ $project->project_id }}" // The project ID of your Android project
}</code></pre>
                    </div>
                </div>

                <div class="step mt-4">
                    <div class="circle">4</div>
                    <p>Generate a bearer token in user settings and append it to the plugin config</p>
                </div>

                <div class="step">
                    <div class="circle">5</div>
                    <p>Configure your preferred settings in this web panel</p>
                </div>

                <div class="step">
                    <div class="circle">6</div>
                    <p>Build your project</p>
                </div>
            </div>
        </div>
    </div>
</div>

