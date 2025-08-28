const token = localStorage.getItem('token');
const user = localStorage.getItem('username') || 'User';

const me = {username: user};

function isJwt(t) {
    return typeof t === 'string' && t.split('.').length === 3;
}

if (!isJwt(token)) {
    localStorage.clear();
    window.location.href = '/auth.html';
}
document.getElementById('welcome').textContent = 'Welcome, ' + user;

document.getElementById('logout').addEventListener('click', () => {
    localStorage.clear();
    window.location.href = '/auth.html';
});

const authHeaders = {'Authorization': 'Bearer ' + token};

function authFetch(url, options = {}) {
    const headers = Object.assign({}, options.headers || {}, authHeaders);
    return fetch(url, {...options, headers});
}

async function fetchAllUsers() {
    try {
        const response = await authFetch('/api/admin');
        if (!response.ok) {
            throw new Error('Failed to fetch users');
        }
        const users = await response.json();
        populateUserDropdown(users);
    } catch (error) {
        console.error('Error fetching users:', error);
        // Fallback: استخدام المستخدم الحالي فقط
        const select = document.getElementById('lf-assigned');
        select.innerHTML = '';
        const option = document.createElement('option');
        option.value = me.username;
        option.textContent = me.username;
        select.appendChild(option);
    }
}

// دالة لملء dropdown بالمستخدمين
function populateUserDropdown(users) {
    const dropdown = document.getElementById('lf-assigned');

    // مسح الخيارات الحالية باستثناء الخيار الأول
    while (dropdown.options.length > 1) {
        dropdown.remove(1);
    }

    // إضافة المستخدمين إلى dropdown
    users.forEach(user => {
        const option = document.createElement('option');
        option.value = user.id; // إرسال ID المستخدم للخلفية
        option.textContent = user.username; // عرض اسم المستخدم في dropdown
        dropdown.appendChild(option);
    });

    // تعيين المستخدم الحالي كافتراضي إذا كان موجوداً
    if (me.username) {
        const currentUserOption = Array.from(dropdown.options).find(opt => opt.textContent === me.username);
        if (currentUserOption) {
            dropdown.value = currentUserOption.value;
        }
    }
}

const listEl = document.getElementById('list');
const detailsEl = document.getElementById('details');

//todo
function renderDetails(lead) {
    if (!lead) {
        detailsEl.innerHTML = '<span class="muted">Select a lead to view details</span>';
        return;
    }
    detailsEl.classList.remove('muted');

    // استخدام assignedToName إذا كان متوفراً، أو البحث عن الاسم إذا كان الـ ID متوفراً
    const assignedName = lead.assignedToName ||
        (lead.assignedToId ? getUsernameFromId(lead.assignedToId) : '');

    detailsEl.innerHTML = `
      <h3>
          ${lead.leadName || 'Untitled'}
          <button onclick="deleteLead('${lead.id}')" style="margin-left:10px;color:red;">Delete</button>
          <button onclick="loadLeadForEdit('${lead.id}')" style="margin-left:10px;color:blue;">Edit</button>
      </h3>
      <div class="kv">
          <div>Lead name</div><div><strong>${lead.leadName || ''}</strong></div>
          <div>Phone</div><div>${lead.phone || ''}</div>
          <div>Budget</div><div>${lead.budget ?? ''}</div>
          <div>Source</div><div>${lead.leadSource || ''}</div>
          <div>Status</div><div>${lead.leadStatus || ''}</div>
          <div>Assigned To</div><div>${assignedName || ''}</div>
          <div>Notes</div>
          <div>
              ${(lead.notes && lead.notes.length > 0)
        ? lead.notes.map((note) => `
                  <div>${note} 
                      <button onclick="deleteNote('${lead.id}', '${note.replace(/'/g, "\\'")}')" style="color:red; margin-left:10px; border:none; background:none; cursor:pointer;">×</button>
                  </div>
              `).join('') : '<span class="muted">No notes</span>'}
              
              <div style="margin-top: 10px;">
                  <input type="text" id="new-note-${lead.id}" placeholder="Add new note" 
                         style="padding:4px; border:1px solid #cbd5e1; border-radius:4px;">
                  <button onclick="addNote('${lead.id}')" 
                          style="margin-left:5px; padding:4px 8px; background:#ef4444; color:white; border:none; border-radius:4px; cursor:pointer;">
                      Add Note
                  </button>
              </div>
          </div>
      </div>`;
}

function getUsernameFromId(userId) {
    try {
        const dropdown = document.getElementById('lf-assigned');
        if (!dropdown) return userId;

        const option = Array.from(dropdown.options).find(opt => opt.value === userId);
        return option ? option.textContent : userId;
    } catch (e) {
        console.error('Error getting username from ID:', e);
        return userId;
    }
}

function renderList(leads) {
    if (!leads || leads.length === 0) {
        listEl.innerHTML = '<span class="muted">No leads</span>';
        renderDetails(null);
        return;
    }
    listEl.innerHTML = '';
    leads.forEach((l, idx) => {
        const item = document.createElement('div');
        item.className = 'item' + (idx === 0 ? ' active' : '');

        const textSpan = document.createElement('span');
        textSpan.textContent = `${l.leadName || 'Lead'} (${l.leadStatus || ''})`;
        textSpan.addEventListener('click', () => {
            document.querySelectorAll('.item').forEach(el => el.classList.remove('active'));
            item.classList.add('active');
            renderDetails(l);
        });
        item.appendChild(textSpan);
        listEl.appendChild(item);
    });
    renderDetails(leads[0]);
}

async function loadLeads() {
    // try admin view
    let res = await authFetch('/api/lead');
    if (res.status === 200) {
        const data = await res.json();
        renderList(data);
        return;
    }
    if (res.status === 403) {
        // fallback to user-specific
        const userData = await authFetch('/api/auth/user').then(r => r.json());
        const data = await authFetch(`/api/lead/${userData.id}/user`).then(r => r.ok ? r.json() : []);
        renderList(data);
        return;
    }
    listEl.innerHTML = '<span class="muted">Failed to load leads</span>';
}

document.getElementById('lead-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const msg = document.getElementById('lf-msg');
    msg.textContent = '';

    const assignedSelect = document.getElementById('lf-assigned');
    const assignedToId = assignedSelect.value;

    const notesText = document.getElementById('lf-notes').value.trim();
    const notes = notesText ? [notesText] : [];

    const body = {
        leadName: document.getElementById('lf-name').value.trim(),
        phone: document.getElementById('lf-phone').value.trim(),
        budget: parseFloat(document.getElementById('lf-budget').value),
        leadSource: document.getElementById('lf-source').value,
        leadStatus: document.getElementById('lf-status').value,
        assignedToId: assignedToId,
        notes: notes
    };
    console.log("body :", body)

    const btn = document.getElementById('lf-btn');
    btn.disabled = true;
    btn.textContent = 'Creating...';

    try {
        const res = await authFetch('/api/lead', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(body)
        });

        if (!res.ok) {
            const t = await res.json().catch(() => ({error: 'Failed'}));
            throw new Error(t.error || 'Failed to create lead');
        }
        msg.textContent = 'Lead created';
        document.getElementById('lead-form').reset();
        loadLeads();
    } catch (err) {
        msg.textContent = err.message || 'Failed to create lead';
    } finally {
        btn.disabled = false;
        btn.textContent = 'Create Lead';
    }
});


// دالة فتح وإغلاق modal التعديل
function openEditLeadModal() {
    document.getElementById('editLeadModal').style.display = 'block';
}

function closeEditLeadModal() {
    document.getElementById('editLeadModal').style.display = 'none';
}

// دالة تحميل الـ lead للتعديل
async function loadLeadForEdit(leadId) {
    try {
        const res = await authFetch(`/api/lead/${leadId}`);
        if (res.ok) {
            const lead = await res.json();

            // ملء نموذج التعديل
            document.getElementById('edit-id').value = lead.id;
            document.getElementById('edit-name').value = lead.leadName || '';
            document.getElementById('edit-phone').value = lead.phone || '';
            document.getElementById('edit-budget').value = lead.budget || '';
            document.getElementById('edit-source').value = lead.leadSource || '';
            document.getElementById('edit-status').value = lead.leadStatus || '';
            document.getElementById('edit-notes').value = Array.isArray(lead.notes) ? lead.notes.join('\n') : '';

            // تحميل المستخدمين للـ dropdown
            await loadUsersForEditDropdown();

            // تعيين القيمة المحددة للمستخدم
            document.getElementById('edit-assigned').value = lead.assignedToId || '';

            openEditLeadModal();
        } else {
            const error = await readError(res);
            alert('Failed to load lead: ' + error);
        }
    } catch (e) {
        console.error('Error loading lead for edit:', e);
        alert('Failed to load lead details');
    }
}

// دالة تحميل المستخدمين لـ dropdown التعديل
async function loadUsersForEditDropdown() {
    const select = document.getElementById('edit-assigned');
    try {
        const res = await authFetch('/api/admin');
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

document.getElementById('edit-lead-form')
    .addEventListener('submit', async function (e) {
        e.preventDefault();

        const leadId = document.getElementById('edit-id').value;
        const btn = this.querySelector('button[type="submit"]');
        const msg = document.getElementById('edit-lead-msg');

        const body = {
            leadName: document.getElementById('edit-name').value,
            phone: document.getElementById('edit-phone').value,
            budget: parseFloat(document.getElementById('edit-budget').value),
            leadSource: document.getElementById('edit-source').value,
            leadStatus: document.getElementById('edit-status').value,
            assignedToId: document.getElementById('edit-assigned').value,
            notes: document.getElementById('edit-notes').value ?
                document.getElementById('edit-notes').value.split('\n') : []
        };
        console.log('Request Body:', body);

        btn.disabled = true;
        btn.textContent = 'Updating...';
        msg.textContent = '';

        try {
            const res = await authFetch(`/api/lead/${leadId}`, {
                method: 'PUT',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(body)
            });

            if (res.ok) {
                msg.textContent = 'Lead updated successfully';
                setTimeout(() => {
                    closeEditLeadModal();
                    loadLeads();
                }, 1000);
            } else {
                const error = await readError(res);
                msg.textContent = 'Failed to update lead: ' + error;
            }
        } catch (e) {
            console.error('Error updating lead:', e);
            msg.textContent = 'Unexpected error occurred';
        } finally {
            btn.disabled = false;
            btn.textContent = 'Update Lead';
        }
    });

// دالة حذف الـ lead
async function deleteLead(leadId) {
    if (!confirm('Are you sure you want to delete this lead?')) return;

    try {
        const res = await authFetch(`/api/lead/${leadId}`, {method: 'DELETE'});
        if (res.ok) {
            alert('Lead deleted successfully');
            loadLeads(); // إعادة تحميل القائمة
        } else {
            const error = await readError(res);
            alert('Failed to delete lead: ' + error);
        }
    } catch (e) {
        console.error('Error deleting lead:', e);
        alert('Unexpected error occurred');
    }
}

// دالة إضافة ملاحظة
async function addNote(leadId) {
    const noteInput = document.getElementById(`new-note-${leadId}`);
    const noteContent = noteInput.value.trim();

    if (!noteContent) {
        alert('Please enter a note');
        return;
    }

    try {
        const res = await authFetch(`/api/lead/${leadId}/notes`, {
            method: 'PATCH',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({content: noteContent})
        });

        if (res.ok) {
            noteInput.value = ''; // مسح حقل الإدخال
            loadLeads(); // إعادة تحميل القائمة
        } else {
            const error = await readError(res);
            alert('Failed to add note: ' + error);
        }
    } catch (e) {
        console.error('Error adding note:', e);
        alert('Unexpected error occurred');
    }
}

// دالة حذف ملاحظة
async function deleteNote(leadId, noteContent) {
    if (!confirm('Are you sure you want to delete this note?')) return;

    try {
        const res = await authFetch(`/api/lead/${leadId}/notes`, {
            method: 'DELETE',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({content: noteContent})
        });

        if (res.ok) {
            loadLeads(); // إعادة تحميل القائمة
        } else {
            const error = await readError(res);
            alert('Failed to delete note: ' + error);
        }
    } catch (e) {
        console.error('Error deleting note:', e);
        alert('Unexpected error occurred');
    }
}

// دالة مساعدة لقراءة الأخطاء
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

// استدعاء الدوال عند تحميل الصفحة
document.addEventListener('DOMContentLoaded', function () {
    fetchAllUsers();
    loadLeads();
});