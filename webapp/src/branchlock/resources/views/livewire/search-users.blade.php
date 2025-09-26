<div>
    <div class="mb-3">
        <label for="userSearch" class="form-label">Search for users</label>
        <input wire:model.live="search" type="text" class="form-control" id="userSearch" placeholder="name@example.com">
    </div>


    @if ($users->count() > 0)
        <p>Total users: {{ $users->count() }}</p>
        <div class="table-responsive rounded-3 mt-4">
            <table class="table table-striped table-hover text-body">
                <thead>
                <tr>
                    <th scope="col">Email</th>
                    <th scope="col">Auth Type</th>
                    <!--th scope="col">Username</th-->
                    <!--th scope="col">Name</th-->
                    <th scope="col">Plan</th>
                    <th scope="col">Created</th>
                    <th scope="col"></th>
                </tr>
                </thead>
                <tbody>
                @foreach ($users as $user)
                    <tr>
                        <td>{{ $user->email }}
                            @if($user->role !== \App\Models\User::ROLE_USER)
                                <span @class([
                            'text-danger fw-bold' => $user->role === \App\Models\User::ROLE_ADMIN,
                            'text-muted' => $user->role === \App\Models\User::ROLE_SANDBOX,
                            ])>({{ $user->role }})</span>
                            @endif
                        </td>
                        <td>{{ $user->auth_type }}</td>
                        <!--td>{{ $user->username }}</td-->
                        <!--td>{{ $user->name }}</td-->
                        <td @class([
                            'text-muted fw-bold' => $user->plan === \App\Models\User::PLAN_FREE,
                            'text-success fw-bold' => $user->plan === \App\Models\User::PLAN_SOLO,
                            'text-warning fw-bold' => $user->plan === \App\Models\User::PLAN_GROUP,
                            'text-primary fw-bold' => $user->plan === \App\Models\User::PLAN_ENTERPRISE,
                            ])>{{ $user->plan }}</td>
                        <td>@if(isset($user->created_at, $user->updated_at))
                                {{ \Carbon\Carbon::parse($user->created_at)->format('F j, Y') }}
                            @else

                            @endif
                        </td>
                        <td>
                            <!--button wire:click="signInAsUser('{{ $user->id }}')" type="button"
                                    class="btn btn-sm btn-success me-1" @disabled($user->id === auth()->user()->id)>
                                <i class="fa-solid fa-lock-open"></i>
                            </button-->
                            <!--button wire:click="changePassword({{ $user->id }})" type="button" class="btn btn-sm btn-primary" data-bs-toggle="modal"
                                    data-bs-target="#changePasswordModal">
                                <i class="fa-solid fa-key"></i>
                            </button-->
                            <button wire:click="confirmUserDeletion('{{ $user->id }}')" type="button" class="btn btn-sm btn-danger"
                                    data-bs-toggle="modal" data-bs-target="#deleteUserModal" @disabled($user->id === auth()->user()->id)>
                                <i class="fa-solid fa-trash"></i>
                            </button>
                        </td>
                    </tr>
                @endforeach
                </tbody>
            </table>
        </div>
    @else
        <p class="text-muted">No users found.</p>
    @endif


    @teleport('body')
    <!-- Delete User Modal -->
    <div wire:ignore.self class="modal fade" id="deleteUserModal" tabindex="-1" role="dialog" aria-labelledby="deleteUserModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-fullscreen-xl-down modal-dialog-centered" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="deleteUserModalLabel">Confirm Deletion</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    Are you sure you want to delete this user?
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                    <button wire:click="deleteUser('{{ $userToDelete }}')" type="button" class="btn btn-danger" data-bs-dismiss="modal">Delete</button>
                </div>
            </div>
        </div>
    </div>
    @endteleport

</div>
