
const token = localStorage.getItem('token');
const user = localStorage.getItem('username') || 'User';
const rolesStr = localStorage.getItem('roles') || '[]';

function isLikelyJwt(t) {
    return typeof t === 'string' && t.split('.').length === 3
}

if (!isLikelyJwt(token)) {
    localStorage.removeItem('token');
    window.location.href = '/auth.html';
}
let rolesList;
try {
    rolesList = JSON.parse(rolesStr);
} catch (e) {
    rolesList = Array.isArray(rolesStr) ? rolesStr : [rolesStr];
}

document.getElementById('welcome').textContent = 'Welcome, ' + user;
document.getElementById('me-username').textContent = user;

document.getElementById('logout').addEventListener('click', () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('roles');
    window.location.href = '/auth.html';
});

const authHeaders = {'Authorization': 'Bearer ' + token};

function authFetch(url, options = {}) {
    const headers = Object.assign({}, options.headers || {}, authHeaders);
    return fetch(url, {...options, headers});
}

const usersEl = document.getElementById('users');
const detailsEl = document.getElementById('details');
const detailActions = document.getElementById('detail-actions');
let selectedUser = null;
let allUsers = [];

function renderDetails(u) {
    if (!u) {
        detailsEl.innerHTML = '<span class="muted">Select a user to view details</span>';
        detailActions.style.display = 'none';
        return;
    }

    detailsEl.classList.remove('muted');
    detailsEl.innerHTML = `
            <div class="kv">
                <div>Username</div><div><strong>${u.username || ''}</strong></div>
            </div>
            <div class="kv">
                <div>Email</div><div>${u.email || ''}</div>
            </div>

        `;

    // Only show actions if user has permission
    detailActions.style.display = 'flex';
}

function renderList(users) {
    allUsers = users;
    if (!users || users.length === 0) {
        usersEl.innerHTML = '<span class="muted">No users</span>';
        renderDetails(null);
        return;
    }

    usersEl.innerHTML = '';
    users.forEach((u, idx) => {
        const item = document.createElement('div');
        item.className = 'user-item' + (idx === 0 ? ' active' : '');

        item.innerHTML = `
                <div class="user-info">
                    <div><strong>${u.username}</strong></div>
                    <div class="muted">${u.email}</div>
                </div>

            `;

        item.addEventListener('click', (e) => {
            if (!e.target.closest('.user-actions')) {
                document.querySelectorAll('.user-item').forEach(el => el.classList.remove('active'));
                item.classList.add('active');
                selectedUser = u;
                renderDetails(u);
            }
        });

        usersEl.appendChild(item);
    });

    if (users.length > 0) {
        selectedUser = users[0];
        renderDetails(users[0]);
    }
}

authFetch('/api/admin')
    .then(r => r.ok ? r.json() : r.text().then(t => Promise.reject({status: r.status, text: t})))
    .then(renderList)
    .catch(err => {
        usersEl.innerHTML = (err && err.status === 403)
            ? '<span class="muted">Forbidden: Admins only</span>'
            : '<span class="muted">Failed to load users</span>';
        detailsEl.innerHTML = '<span class="muted">No user selected</span>';
    });

// Add user submit
document.getElementById('add-user-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const msg = document.getElementById('au-msg');
    msg.textContent = '';
    const username = document.getElementById('au-username').value.trim();
    const email = document.getElementById('au-email').value.trim();
    const password = document.getElementById('au-password').value;
    const confirm = document.getElementById('au-confirm').value;

    if (!username || !email || !password) {
        msg.textContent = 'Please fill all fields';
        return;
    }
    if (password.length < 6) {
        msg.textContent = 'Password must be at least 6 characters';
        return;
    }
    if (password !== confirm) {
        msg.textContent = 'Passwords do not match';
        return;
    }

    const btn = document.getElementById('au-btn');
    btn.disabled = true;
    btn.textContent = 'Adding...';

    try {
        const res = await fetch('/api/admin/signup', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...authHeaders
            },
            body: JSON.stringify({username, email, password})
        });

        if (!res.ok) {
            const t = await res.json().catch(() => ({message: 'Failed to add user'}));
            throw new Error(t.message || 'Failed to add user');
        }

        msg.textContent = 'User added successfully';
        document.getElementById('add-user-form').reset();

        // Refresh users list
        const usersRes = await authFetch('/api/admin');
        if (usersRes.ok) {
            const users = await usersRes.json();
            renderList(users);
        }
    } catch (err) {
        msg.textContent = err.message || 'Failed to add user';
    } finally {
        btn.disabled = false;
        btn.textContent = 'Add User';
    }
});

// Edit user functionality
async function loadUserForEdit(userId) {
    try {
        // In a real app, you might fetch user details from an API
        const user = allUsers.find(u => u.id === userId);
        if (!user) {
            alert('User not found');
            return;
        }

        // Fill the edit form
        document.getElementById('edit-id').value = user.id;
        document.getElementById('edit-username').value = user.username || '';
        document.getElementById('edit-email').value = user.email || '';

        // Clear password field
        document.getElementById('edit-password').value = '';

        // Show modal
        document.getElementById('editModal').style.display = 'flex';
    } catch (e) {
        console.error('Error loading user for edit:', e);
        alert('Failed to load user details');
    }
}

// Update user
document.getElementById('edit-user-form').addEventListener('submit', async (e) => {
    e.preventDefault();

    const userId = document.getElementById('edit-id').value;
    const username = document.getElementById('edit-username').value.trim();
    const email = document.getElementById('edit-email').value.trim();
    const role = document.getElementById('edit-role').value;
    const password = document.getElementById('edit-password').value;

    const btn = e.target.querySelector('button[type="submit"]');
    const msg = document.getElementById('edit-user-msg');

    btn.disabled = true;
    btn.innerHTML = '<div class="loading"></div> Updating...';
    msg.textContent = '';

    try {
        const updateData = { username, email, roles: [role] };
        if (password) {
            updateData.password = password;
        }

        const res = await authFetch(`/api/admin/${userId}`, {
            method: 'PATCH',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(updateData)
        });

        if (res.ok) {
            msg.textContent = 'User updated successfully!';
            msg.style.color = 'var(--success)';

            // Close modal and reload users
            setTimeout(() => {
                document.getElementById('editModal').style.display = 'none';

                // Refresh users list
                authFetch('/api/admin')
                    .then(r => r.ok ? r.json() : [])
                    .then(renderList)
                    .catch(() => {});
            }, 1000);
        } else {
            const error = await res.text();
            msg.textContent = error || 'Failed to update user';
            msg.style.color = 'var(--danger)';
        }
    } catch (e) {
        console.error('Error updating user:', e);
        msg.textContent = 'Unexpected error occurred';
        msg.style.color = 'var(--danger)';
    } finally {
        btn.disabled = false;
        btn.innerHTML = '<i class="fas fa-save"></i> Update User';
    }
});
// Delete user
async function deleteUser(userId) {
    try {
        // جلب بيانات المستخدم أولاً للتحقق من أدواره
        const userRes = await authFetch(`/api/admin/${userId}`);
        if (!userRes.ok) {
            const error = await userRes.text();
            alert('Failed to fetch user details: ' + error);
            return;
        }

        const user = await userRes.json();

        if (user && user.roles && user.roles.includes('ADMIN')) {
            alert('Cannot delete admin users');
            return;
        }

        if (!confirm('Are you sure you want to delete this user? This action cannot be undone.')) {
            return;
        }

        const deleteRes = await authFetch(`/api/admin/${userId}`, {
            method: 'DELETE'
        });

        if (deleteRes.ok) {
            alert('User deleted successfully');

            const usersRes = await authFetch('/api/admin');
            if (usersRes.ok) {
                const users = await usersRes.json();
                renderList(users);

                if (selectedUser && selectedUser.id === userId) {
                    selectedUser = null;
                    renderDetails(null);
                }
            }
        } else {
            const error = await deleteRes.text();
            alert('Failed to delete user: ' + error);
        }
    } catch (e) {
        console.error('Error deleting user:', e);
        alert('Unexpected error occurred');
    }
}
// Modal close handlers
document.getElementById('close-edit-modal').addEventListener('click', () => {
    document.getElementById('editModal').style.display = 'none';
});

document.getElementById('cancel-edit').addEventListener('click', () => {
    document.getElementById('editModal').style.display = 'none';
});

window.addEventListener('click', (e) => {
    if (e.target === document.getElementById('editModal')) {
        document.getElementById('editModal').style.display = 'none';
    }
});

document.getElementById('edit-user-btn').addEventListener('click', () => {
    if (selectedUser) {
        loadUserForEdit(selectedUser.id);
    }
});

document.getElementById('-user-btn').addEventListener('click', () => {
    if (selectedUser) {
        deleteUser(selectedUser.id);
    }
});
