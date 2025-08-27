const token = localStorage.getItem('token');

// ✅ check JWT
function isJwt(t) {
    return typeof t === 'string' && t.split('.').length === 3;
}
if (!isJwt(token)) {
    localStorage.clear();
    window.location.href = '/auth.html';
}
const authHeaders = { Authorization: 'Bearer ' + token };

function authFetch(url, options = {}) {
    const base = options.headers || {};
    const headers = Object.assign({}, base, authHeaders);
    return fetch(url, { ...options, headers });
}

// ✅ error reader helper
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

// ✅ current user
const me = { id: null, username: localStorage.getItem('username') || 'User' };
document.getElementById('welcome').textContent = 'Welcome, ' + me.username;
document.getElementById('logout').addEventListener('click', () => {
    localStorage.clear();
    window.location.href = '/auth.html';
});

const listEl = document.getElementById('list');
const detailsEl = document.getElementById('details');

// ✅ details view
function renderDetails(m) {
    if (!m) {
        detailsEl.innerHTML = '<span class="muted">Select a meeting to view details</span>';
        return;
    }
    detailsEl.innerHTML = `
      <h3>
          ${m.title || 'Untitled'}
          <button onclick="deleteMeeting('${m.id}')" style="margin-left:10px;color:red;">Delete</button>
      </h3>
      <div class="kv">
          <div>Date</div><div>${m.date || ''}</div>
          <div>Duration</div><div>${m.duration || ''} hours</div>
          <div>Location</div><div>${m.location || ''}</div>
          <div>Status</div><div>${m.status || ''}</div>
          <div>Client Name</div><div>${m.clientName  || ''}</div>
          <div>Notes</div><div>
              ${
        Array.isArray(m.notes)
            ? m.notes.map((n) => `<div>${n}</div>`).join('')
            : m.notes || '<span class="muted">No notes</span>'
    }
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
    meetings.forEach((m, idx) => {
        const item = document.createElement('div');
        item.className = 'item' + (idx === 0 ? ' active' : '');
        item.textContent = `${m.title || 'Meeting'} (${m.status || ''})`;
        item.addEventListener('click', () => {
            document.querySelectorAll('.item').forEach((el) => el.classList.remove('active'));
            item.classList.add('active');
            renderDetails(m);
        });
        listEl.appendChild(item);
    });
    renderDetails(meetings[0]);
}

// ✅ delete meeting
async function deleteMeeting(id) {
    if (!confirm('Are you sure you want to delete this meeting?')) return;
    const res = await authFetch(`/api/meetings/${id}`, { method: 'DELETE' });
    if (res.ok) {
        alert('Meeting deleted successfully');
        window.location.reload();
    } else {
        const err = await readError(res);
        alert('Failed to delete meeting: ' + err);
    }
}

// ✅ load current user
async function loadMe() {
    const info = await authFetch('/api/auth/user').then((r) => r.json()).catch(() => null);
    if (info && info.id) {
        me.id = info.id;
        me.username = info.username;
        document.getElementById('welcome').textContent = 'Welcome, ' + me.username;
    }
    await loadUsers(); // بعد ما نجيب me نجيب users
}

// ✅ helper: حط اختيار المستخدم الحالي في الـ dropdown (fallback)
function setCurrentUserAsDefault(select) {
    select.innerHTML = '';
    const opt = document.createElement('option');
    opt.value = me.id || '';
    opt.textContent = me.username || 'Current User';
    select.appendChild(opt);
    if (me.id) select.value = me.id;
}

// ✅ load all users for dropdown (مع fallback لو فشل)
async function loadUsers() {
    const select = document.getElementById('mf-assignedTo');
    try {
        const res = await authFetch('/api/admin'); // مؤقتاً
        if (res.ok) {
            const users = await res.json();
            select.innerHTML = '';
            users.forEach((u) => {
                const opt = document.createElement('option');
                opt.value = u.id; // لازم يكون String الـ ObjectId
                opt.textContent = u.username;
                select.appendChild(opt);
            });
            // الافتراضي
            if (me.id && users.some((u) => u.id === me.id)) {
                select.value = me.id;
            }
            // لو مش موجود ضمن اللستة لأي سبب
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
            const select = document.getElementById('mf-clientId');
            select.innerHTML = '<option value="">-- Select Client --</option>'; // اعادة القيمة الافتراضية
            leads.forEach(l => {
                const opt = document.createElement('option');
                opt.value = l.id;       // ObjectId أو id
                opt.textContent = l.name; // الاسم للمستخدم
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
    const clientName = (document.getElementById('mf-clientName')?.value || '').trim();
    const assignedToSelect = document.getElementById('mf-assignedTo');
    const assignedToId = (assignedToSelect?.value || me.id || '').trim();
    const date = document.getElementById('mf-date')?.value || null;
    const time = document.getElementById('mf-time')?.value || null;
    const durationRaw = document.getElementById('mf-duration')?.value || '';
    const type = document.getElementById('mf-type')?.value || null;
    const status = document.getElementById('mf-status')?.value || null;
    const location = document.getElementById('mf-location')?.value || null;
    const noteMessage = document.getElementById('mf-notes')?.value || null;
    // console.log("Sending request:", body);

    // ✅ checks بسيطة علشان أخطاء الـ backend تقل
    if (!title) {
        msg.textContent = 'Title is required';
        return;
    }
    if (!clientName) {
        msg.textContent = 'Client name is required';
        return;
    }
    if (!assignedToId) {
        msg.textContent = 'Assigned user is required';
        return;
    }

    const body = {
        title: document.getElementById("mf-title").value,
        clientId: document.getElementById("mf-clientName").value,     // 👈 ID مش الاسم
        assignedToId: document.getElementById("mf-assignedTo").value, // 👈 ID مش object
        date: document.getElementById("mf-date").value,
        time: document.getElementById("mf-time").value,
        duration: durationRaw ? parseInt(durationRaw, 10) : null,
        type: type || null,
        status: status || null,
        location: location || null,
        noteMessage: noteMessage || null
    };

    console.log("Sending request:", body);
    console.log('POST /api/meetings body =>', body);

    btn.disabled = true;
    btn.textContent = 'Creating...';
    msg.textContent = '';

    try {
        const res = await authFetch('/api/meetings', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });

        if (res.ok) {
            msg.textContent = 'Meeting created';
            document.getElementById('meeting-form').reset();
            // رجّع الافتراضي للمستخدم الحالي
            if (me.id) document.getElementById('mf-assignedTo').value = me.id;
            await loadMeetings();
        } else {
            const err = await readError(res);
            console.error('Failed to create meeting:', err);
            msg.textContent = err || 'Failed to create meeting';
            alert('Failed to create meeting: ' + err);
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

// ✅ init
loadMe().then(loadMeetings);
document.addEventListener("DOMContentLoaded", async () => {
    await loadLeads();
    await loadMe();
    await loadMeetings();
});