const token = localStorage.getItem('token');

function isJwt(t) {
    return typeof t === 'string' && t.split('.').length === 3;
}

if (!isJwt(token)) {
    localStorage.clear();
    window.location.href = '/auth.html';
}
const authHeaders = {Authorization: 'Bearer ' + token};

function authFetch(url, options = {}) {
    const base = options.headers || {};
    const headers = Object.assign({}, base, authHeaders);
    return fetch(url, {...options, headers});
}

async function readError(res) {
    try {
        const j = await res.json();
        return j?.message || JSON.stringify(j);
    } catch {
        try {
            return await res.text();
        } catch {
            return 'Unknown error';
        }
    }
}

const me = {id: null, username: localStorage.getItem('username') || 'User'};
document.getElementById('welcome').textContent = 'Welcome, ' + me.username;
document.getElementById('logout').addEventListener('click', () => {
    localStorage.clear();
    window.location.href = '/auth.html';
});

const listEl = document.getElementById('list');
const detailsEl = document.getElementById('details');

function renderDetails(meeting) {
    if (!meeting) {
        detailsEl.innerHTML = '<span class="muted">Select a meeting to view details</span>';
        return;
    }

    detailsEl.innerHTML = `
      <h3>
          ${meeting.title || 'Untitled'}
          <button onclick="deleteMeeting('${meeting.id}')" style="margin-left:10px;color:red;">Delete</button>
          <button onclick="loadMeetingForEdit('${meeting.id}')" style="margin-left:10px;color:blue;">Edit</button>
      </h3>
      <div class="kv">
          <div>Date</div><div>${meeting.date || ''}</div>
          <div>Time</div><div>${meeting.time || ''}</div>
          <div>Duration</div><div>${meeting.duration || ''} hours</div>
          <div>Type</div><div>${meeting.type || ''} </div>
          <div>Location</div><div>${meeting.location || ''}</div>
          <div>Status</div><div>${meeting.status || ''}</div>
          <div>Client Name</div><div>${meeting.clientName || ''}</div>
          
          <div>Notes</div>
          <div>
              ${Array.isArray(meeting.notes) ? meeting.notes.map((note) => `
                  <div>${note} 
                      <button onclick="deleteNote('${meeting.id}', '${note}')" style="color:red; margin-left:10px; border:none; background:none; cursor:pointer;">×</button>
                  </div>
              `).join('') : '<span class="muted">No notes</span>'}
              
              <div style="margin-top: 10px;">
                  <input type="text" id="new-note-${meeting.id}" placeholder="Add new note" 
                         style="padding:4px; border:1px solid #cbd5e1; border-radius:4px;">
                  <button onclick="addNote('${meeting.id}')" 
                          style="margin-left:5px; padding:4px 8px; background:#ef4444; color:white; border:none; border-radius:4px; cursor:pointer;">
                      Add Note
                  </button>
              </div>
          </div>
      </div>
  `;
}

function renderList(meetings) {
    if (!meetings || meetings.length === 0) {
        listEl.innerHTML = '<span class="muted">No meetings</span>';
        renderDetails(null);
        return;
    }
    listEl.innerHTML = '';
    meetings.forEach((meeting, idx) => {
        const item = document.createElement('div');
        item.className = 'item' + (idx === 0 ? ' active' : '');
        item.textContent = `${meeting.title || 'Meeting'} (${meeting.status || ''})`;
        item.addEventListener('click', () => {
            document.querySelectorAll('.item').forEach((el) => el.classList.remove('active'));
            item.classList.add('active');
            renderDetails(meeting);
        });
        listEl.appendChild(item);
    });
    renderDetails(meetings[0]);
}

async function deleteMeeting(id) {
    if (!confirm('Are you sure you want to delete this meeting?')) return;
    const res = await authFetch(`/api/meetings/${id}`, {method: 'DELETE'});
    if (res.ok) {
        alert('Meeting deleted successfully');
        window.location.reload();
    } else {
        const err = await readError(res);
        alert('Failed to delete meeting: ' + err);
    }
}

async function loadMe() {
    const info = await authFetch('/api/auth/user').then((r) => r.json()).catch(() => null);
    if (info && info.id) {
        me.id = info.id;
        me.username = info.username;
        document.getElementById('welcome').textContent = 'Welcome, ' + me.username;
    }
    await loadUsers(); // بعد ما نجيب me نجيب users
}

function setCurrentUserAsDefault(select) {
    select.innerHTML = '';
    const opt = document.createElement('option');
    opt.value = me.id || '';
    opt.textContent = me.username || 'Current User';
    select.appendChild(opt);
    if (me.id) select.value = me.id;
}

async function loadUsers() {
    const select = document.getElementById('mf-assignedTo');
    try {
        //todo
        const res = await authFetch('/api/admin'); // مؤقتاً
        if (res.ok) {
            const users = await res.json();
            select.innerHTML = '';
            users.forEach((user) => {
                const opt = document.createElement('option');
                opt.value = user.id;
                opt.textContent = user.username;
                console.log('user object:', user);
                console.log('user ID:', user.id);
                console.log('user name:', user.username);
                select.appendChild(opt);
            });
            if (me.id && users.some((u) => u.id === me.id)) {
                select.value = me.id;
            }
            if (!select.value && me.id) {
                const opt = document.createElement('option');
                opt.value = me.id;
                opt.textContent = me.username;

                select.appendChild(opt);
                select.value = me.id;
            }
        } else {
            console.warn('Failed to load users, falling back to current user only');
            setCurrentUserAsDefault(select);
        }
    } catch (e) {
        console.error('Error loading users', e);
        setCurrentUserAsDefault(select);
    }
}

// ✅ load meetings
async function loadMeetings() {
    try {
        const res = await authFetch('/api/meetings');
        if (res.ok) {
            const data = await res.json();
            renderList(data);
        } else {
            const err = await readError(res);
            listEl.innerHTML = `<span class="muted">Failed to load meetings: ${err}</span>`;
        }
    } catch (e) {
        console.error('Error loading meetings', e);
        listEl.innerHTML = '<span class="muted">Failed to load meetings</span>';
    }
}

async function loadLeads() {
    try {
        const res = await authFetch('/api/lead');
        if (res.ok) {
            const leads = await res.json();
            console.log('Leads response:', leads);
            const select = document.getElementById('mf-clientId');
            select.innerHTML = '<option value="">-- Select Client --</option>';
            leads.forEach(lead => {
                // console.log('Single lead:', lead);
                const opt = document.createElement('option');
                opt.value = lead.id;
                opt.textContent = lead.leadName;

                select.appendChild(opt);
            });
        } else {
            console.error("Failed to load leads");
        }
    } catch (e) {
        console.error("Error loading leads", e);
    }
}

// ✅ create meeting
document.getElementById('meeting-form').addEventListener('submit', createMeeting);

async function createMeeting(e) {
    e.preventDefault();
    console.log("Button clicked")
    const btn = document.getElementById('mf-btn');
    const msg = document.getElementById('mf-msg');

    const title = (document.getElementById('mf-title')?.value || '').trim();
    const clientName = (document.getElementById('mf-clientId')?.value || '').trim();
    const assignedToSelect = document.getElementById('mf-assignedTo');
    let assignedToId = assignedToSelect?.value || '';
    // const assignedToId = (assignedToSelect?.value || me.id || '').trim();

    const date = document.getElementById('mf-date')?.value || null;
    const timeInput = document.getElementById('mf-time');
    const time = timeInput?.value || null;
    const durationRaw = document.getElementById('mf-duration')?.value || '';
    const type = document.getElementById('mf-type')?.value || null;
    const status = document.getElementById('mf-status')?.value || null;
    const location = document.getElementById('mf-location')?.value || null;
    const noteMessage = document.getElementById('mf-notes')?.value || null;

    if (!title) {
        msg.textContent = 'Title is required';
        return;
    }
    if (!clientName) {
        msg.textContent = 'Client name is required';
        return;
    }
    if (!assignedToId && me && me.id) {
        assignedToId = me.id.toString();
    }
    if (!assignedToId) {
        msg.textContent = 'Assigned user is required';
        return;
    }

    const body = {
        title: document.getElementById("mf-title").value,
        clientId: document.getElementById("mf-clientId").value,
        assignedToId: document.getElementById("mf-assignedTo").value,
        date: document.getElementById("mf-date").value,
        time: time,
        // time: document.getElementById("mf-time").value,
        duration: durationRaw ? parseInt(durationRaw, 10) : null,
        type: type || null,
        status: status || null,
        location: location || null,
        notes: noteMessage ? [noteMessage] : null
    };
    console.log('Selected client ID:', document.getElementById("mf-clientId").value);
    console.log('Selected user ID:', document.getElementById("mf-assignedTo").value);
    console.log('POST /api/meetings body =>', body);
    console.log('Time value:', time);
    btn.disabled = true;
    btn.textContent = 'Creating...';
    msg.textContent = '';

    try {
        const res = await authFetch('/api/meetings', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(body)
        });

        if (res.ok) {
            msg.textContent = 'Meeting created';
            document.getElementById('meeting-form').reset();
            if (me.id) document.getElementById('mf-assignedTo').value = me.id;
            await loadMeetings();
        } else {
            const errObj = await res.json();
            const errMessage = errObj.message || 'Failed to create meeting';

            console.error('Failed to create meeting:', errMessage);
            msg.textContent = errMessage;
            alert('Failed to create meeting: ' + errMessage);
        }
    } catch (e) {
        console.error('Error creating meeting', e);
        msg.textContent = 'Unexpected error occurred';
        alert('Unexpected error occurred');
    } finally {
        btn.disabled = false;
        btn.textContent = 'Create Meeting';
    }
}


// Edit Modal Functions
function openEditModal() {
    document.getElementById('editModal').style.display = 'block';
}

function closeEditModal() {
    document.getElementById('editModal').style.display = 'none';
}

async function loadMeetingForEdit(meetingId) {
    try {
        const res = await authFetch(`/api/meetings/${meetingId}`);
        if (res.ok) {
            const meeting = await res.json();

            // Fill the edit form
            document.getElementById('edit-id').value = meeting.id;
            document.getElementById('edit-title').value = meeting.title || '';
            document.getElementById('edit-date').value = meeting.date || '';
            document.getElementById('edit-time').value = meeting.time || '';
            document.getElementById('edit-duration').value = meeting.duration || '';
            document.getElementById('edit-type').value = meeting.type || '';
            document.getElementById('edit-status').value = meeting.status || '';
            document.getElementById('edit-location').value = meeting.location || '';
            document.getElementById('edit-notes').value = Array.isArray(meeting.notes) ? meeting.notes.join('\n') : '';

            // Load clients and users for dropdowns
            await loadClientsForEdit();
            await loadUsersForEdit();

            // Set selected values
            document.getElementById('edit-clientId').value = meeting.clientId || '';
            document.getElementById('edit-assignedTo').value = meeting.assignedToId || '';

            openEditModal();
        }
    } catch (e) {
        console.error('Error loading meeting for edit:', e);
        alert('Failed to load meeting details');
    }
}

// Load clients for edit dropdown
async function loadClientsForEdit() {
    const select = document.getElementById('edit-clientId');
    try {
        const res = await authFetch('/api/lead');
        if (res.ok) {
            const leads = await res.json();
            select.innerHTML = '<option value="">-- Select Client --</option>';
            leads.forEach(lead => {
                const opt = document.createElement('option');
                opt.value = lead.id;
                opt.textContent = lead.leadName;
                select.appendChild(opt);
            });
        }
    } catch (e) {
        console.error('Error loading clients for edit:', e);
    }
}

// Load users for edit dropdown
async function loadUsersForEdit() {
    const select = document.getElementById('edit-assignedTo');
    try {
        const res = await authFetch('/api/admin'); // Adjust API endpoint
        if (res.ok) {
            const users = await res.json();
            select.innerHTML = '<option value="">-- Select User --</option>';
            users.forEach(user => {
                const opt = document.createElement('option');
                opt.value = user.id;
                opt.textContent = user.username;
                select.appendChild(opt);
            });
        }
    } catch (e) {
        console.error('Error loading users for edit:', e);
    }
}

// Handle edit form submission
document.getElementById('edit-meeting-form')
    .addEventListener('submit', async function(e) {
    e.preventDefault();

    const meetingId = document.getElementById('edit-id').value;
    const btn = this.querySelector('button[type="submit"]');

    const body = {
        title: document.getElementById('edit-title').value,
        clientId: document.getElementById('edit-clientId').value,
        assignedToId: document.getElementById('edit-assignedTo').value,
        date: document.getElementById('edit-date').value,
        time: document.getElementById('edit-time').value,
        duration: parseInt(document.getElementById('edit-duration').value),
        type: document.getElementById('edit-type').value,
        status: document.getElementById('edit-status').value,
        location: document.getElementById('edit-location').value,
        notes: document.getElementById('edit-notes').value ?
            document.getElementById('edit-notes').value.split('\n') : []
    };
    console.log("Request Body:", body);
    btn.disabled = true;
    btn.textContent = 'Updating...';

    try {
        const res = await authFetch(`/api/meetings/${meetingId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });

        if (res.ok) {
            alert('Meeting updated successfully');
            closeEditModal();
            await loadMeetings(); // Refresh the list
        } else {
            const error = await readError(res);
            alert('Failed to update meeting: ' + error);
        }
    } catch (e) {
        console.error('Error updating meeting:', e);
        alert('Unexpected error occurred');
    } finally {
        btn.disabled = false;
        btn.textContent = 'Update Meeting';
    }
});

async function addNote(meetingId) {
    const noteInput = document.getElementById(`new-note-${meetingId}`);
    const noteContent = noteInput.value.trim();

    if (!noteContent) {
        alert('Please enter a note');
        return;
    }

    try {
        const res = await authFetch(`/api/meetings/${meetingId}/notes`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ content: noteContent })
        });

        if (res.ok) {
            noteInput.value = ''; // clear input
            await loadMeetings(); // refresh list
        } else {
            const error = await readError(res);
            alert('Failed to add note: ' + error);
        }
    } catch (e) {
        console.error('Error adding note:', e);
        alert('Unexpected error occurred');
    }
}

async function deleteNote(meetingId, noteContent) {
    if (!confirm('Are you sure you want to delete this note?')) return;

    try {
        const res = await authFetch(`/api/meetings/${meetingId}/notes`, {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ content: noteContent })
        });

        if (res.ok) {
            await loadMeetings(); // refresh the list
        } else {
            const error = await readError(res);
            alert('Failed to delete note: ' + error);
        }
    } catch (e) {
        console.error('Error deleting note:', e);
        alert('Unexpected error occurred');
    }
}
// ✅ init
loadMe().then(loadMeetings);
document.addEventListener("DOMContentLoaded", async () => {
    await loadLeads();
    await loadMe();
    await loadMeetings();
});