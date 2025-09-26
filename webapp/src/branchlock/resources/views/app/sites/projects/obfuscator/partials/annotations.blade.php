<div class="offcanvas offcanvas-start my-auto border-0 shadow rounded-end-4 w-auto" style="z-index: 10000;" data-bs-backdrop="true"
     tabindex="-1" id="offcanvasAnnotations"
     aria-labelledby="offcanvasAnnotationsLabel">
    <div class="offcanvas-header">
        <h5 class="offcanvas-title" id="offcanvasAnnotationsLabel">Branchlock Annotations</h5>
        <button type="button" class="btn-close text-light" data-bs-dismiss="offcanvas" aria-label="Close"></button>
    </div>
    <div class="offcanvas-body">
        <h6>Gradle</h6>

        <div class="position-relative">
            <div class="rounded-4" style="background: var(--side-menu-link-bg); overflow-wrap: break-word;">

            <pre class="py-3 px-2 font-monospace w-100 overflow-y-auto overflow-x-hidden"
                 style="white-space: pre-wrap;overflow-wrap: break-word;"><code class="language-gradle overflow-hidden">repositories {
    maven { url = "{{ config('settings.url') }}/storage/maven/" }
    ...
}

dependencies {
    implementation 'net.branchlock:annotations:1.1.0'
    ...
}</code></pre>
            </div>
        </div>

        {{--
        <h6 class="mt-4">Maven</h6>

        <div class="position-relative">
            <div class="rounded-4" style="background: var(--side-menu-link-bg); overflow-wrap: break-word;">

            <pre class="py-3 px-2 font-monospace w-100 overflow-y-auto overflow-x-hidden"
                 style="white-space: pre-wrap;overflow-wrap: break-word;"><code class="language-xml overflow-hidden">&lt;repositories&gt;
    &lt;repository&gt;
        &lt;id&gt;branchlock-repo&lt;/id&gt;
        &lt;url&gt;{{ config('settings.url') }}/storage/maven/&lt;/url&gt;
    &lt;/repository&gt;
    ...
&lt;/repositories&gt;

&lt;dependencies&gt;
    &lt;dependency&gt;
        &lt;groupId&gt;net.branchlock&lt;/groupId&gt;
        &lt;artifactId&gt;annotations&lt;/artifactId&gt;
        &lt;version&gt;1.1.0&lt;/version&gt;
    &lt;/dependency&gt;
    ...
&lt;/dependencies&gt;
</code></pre>
            </div>
        </div>
        --}}
    </div>
</div>
