class Meetings {
    static init() {
        this.token = localStorage.getItem('token');
        this.me = {id: null, username: localStorage.getItem('username') || 'User'};

        if (!this.isJwt(this.token)) {
            localStorage.clear();
            window.location.href = '/auth/auth.html';
            return;
        }

        document.getElementById('welcome').textContent = 'Welcome, ' + this.me.username;
        document.getElementById('logout').addEventListener('click', this.logout);

        this.authHeaders = {Authorization: 'Bearer ' + this.token};

        this.loadMe()
            .then(() => this.loadLeads())
            .then(() => this.loadUsers())
            .then(() => this.loadMeetings())
            .catch(error => console.error('Initialization error:', error));

        document.getElementById('meeting-form')
            .addEventListener('submit', this.createMeeting.bind(this));
        document.getElementById('edit-meeting-form')
            .addEventListener('submit', this.updateMeeting.bind(this));
        document.getElementById('add-note-btn')
            .addEventListener('click', this.addNoteInModal.bind(this));

        document.getElementById('edit-meeting-btn').addEventListener('click', () => {
            const activeMeeting = document.querySelector('.item.active');
            if (activeMeeting) {
                this.loadMeetingForEdit(activeMeeting.dataset.id);
            }
        });

        document.getElementById('delete-meeting-btn').addEventListener('click', () => {
            const activeMeeting = document.querySelector('.item.active');
            if (activeMeeting) {
                this.deleteMeeting(activeMeeting.dataset.id);
            }
        });
    }

    static isJwt(t) {
        return typeof t === 'string' && t.split('.').length === 3;
    }

    static authFetch(url, options = {}) {
        const headers = Object.assign({}, options.headers || {}, this.authHeaders);
        return fetch(url, {...options, headers});
    }

    static async readError(res) {
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

    static logout() {
        localStorage.clear();
        window.location.href = '/auth/auth.html';
    }

    static async loadMe() {
        try {
            const info = await this.authFetch('/api/auth/user').then(r => r.json());
            if (info && info.id) {
                this.me.id = info.id;
                this.me.username = info.username;
                document.getElementById('welcome').textContent = 'Welcome, ' + this.me.username;
            }
        } catch (e) {
            console.error('Error loading user info:', e);
        }
    }

    static async loadLeads() {
        try {
            const res = await this.authFetch('/api/lead');
            if (res.ok) {
                const leads = await res.json();
                const select = document.getElementById('mf-clientId');
                const editSelect = document.getElementById('edit-clientId');

                select.innerHTML = '<option value="">-- Select Client --</option>';
                if (editSelect) editSelect.innerHTML = '<option value="">-- Select Client --</option>';

                leads.forEach(lead => {
                    const opt = document.createElement('option');
                    opt.value = lead.id;
                    opt.textContent = lead.leadName || lead.name || 'Unnamed Lead';
                    select.appendChild(opt);

                    if (editSelect) {
                        const editOpt = opt.cloneNode(true);
                        editSelect.appendChild(editOpt);
                    }
                });

                // إضافة هذا الجزء - تحديث الـ dropdown إذا كان فيه meeting محمل
                if (window.currentMeeting && window.currentMeeting.clientId) {
                    setTimeout(() => {
                        document.getElementById('edit-clientId').value = window.currentMeeting.clientId;
                    }, 100);
                }

                return leads;
            }
        } catch (e) {
            console.error("Error loading leads", e);
        }
        return [];
    }
    static async loadUsers() {
        const select = document.getElementById('mf-assignedTo');
        const editSelect = document.getElementById('edit-assignedTo');

        try {
            const res = await this.authFetch('/api/admin');
            if (res.ok) {
                const users = await res.json();

                select.innerHTML = '<option value="">-- Assign To --</option>';
                if (editSelect) editSelect.innerHTML = '<option value="">-- Assign To --</option>';

                users.forEach(user => {
                    const opt = document.createElement('option');
                    opt.value = user.id;
                    opt.textContent = user.username;
                    select.appendChild(opt);

                    if (editSelect) {
                        const editOpt = opt.cloneNode(true);
                        editSelect.appendChild(editOpt);
                    }
                });

                if (this.me.id && users.some(u => u.id === this.me.id)) {
                    select.value = this.me.id;
                }
                if (window.currentMeeting && window.currentMeeting.assignedToId) {
                    setTimeout(() => {
                        document.getElementById('edit-assignedTo').value = window.currentMeeting.assignedToId;
                    }, 100);
                }
            } else {
                this.setCurrentUserAsDefault(select);
                if (editSelect) this.setCurrentUserAsDefault(editSelect);
            }
        } catch (e) {
            console.error('Error loading users', e);
            this.setCurrentUserAsDefault(select);
            if (editSelect) this.setCurrentUserAsDefault(editSelect);
        }
    }

    static setCurrentUserAsDefault(select) {
        select.innerHTML = '';
        const opt = document.createElement('option');
        opt.value = this.me.id || '';
        opt.textContent = this.me.username || 'Current User';
        select.appendChild(opt);
        if (this.me.id) select.value = this.me.id;
    }

    static async loadMeetings() {
        try {
            const res = await this.authFetch('/api/meetings');
            if (res.ok) {
                const data = await res.json();
                this.renderList(data);
            } else {
                const err = await this.readError(res);
                document.getElementById('list').innerHTML = `<span class="muted">Failed to load meetings: ${err}</span>`;
            }
        } catch (e) {
            console.error('Error loading meetings', e);
            document.getElementById('list').innerHTML = '<span class="muted">Failed to load meetings</span>';
            this.renderDetails(null);
        }
    }

    static renderList(meetings) {
        const listEl = document.getElementById('list');

        if (!meetings || meetings.length === 0) {
            listEl.innerHTML = '<span class="muted">No meetings scheduled</span>';
            this.renderDetails(null);
            return;
        }

        listEl.innerHTML = meetings.map(meeting => `
            <div class="item" data-id="${meeting.id}">
                <div class="item-header">
                    <div class="item-name">${meeting.title || 'Untitled Meeting'}</div>
                    <span class="badge ${this.getStatusBadgeClass(meeting.status)}">
                        ${this.formatStatusText(meeting.status)}
                    </span>
                </div>
                <div class="item-details">
                    <span>${meeting.date || 'No date'} ${meeting.time || ''}</span>
                    <span>${meeting.clientName || 'No client'}</span>
                </div>
            </div>
        `).join('');

        // Add click event to items
        document.querySelectorAll('.item').forEach((item, idx) => {
            item.addEventListener('click', () => {
                document.querySelectorAll('.item').forEach(i => i.classList.remove('active'));
                item.classList.add('active');
                const meeting = meetings.find(m => m.id == item.dataset.id);
                this.renderDetails(meeting);
            });

            // Select first item by default
            if (idx === 0) {
                item.classList.add('active');
                this.renderDetails(meetings[0]);
            }
        });
    }

    static getStatusBadgeClass(status) {
        switch (status) {
            case 'SCHEDULED':
                return 'badge-info';
            case 'COMPLETED':
                return 'badge-success';
            case 'CANCELLED':
                return 'badge-danger';
            default:
                return 'badge-info';
        }
    }

    static formatStatusText(status) {
        return status ? status.charAt(0).toUpperCase() + status.slice(1).toLowerCase() : 'Unknown';
    }

    static renderDetails(meeting) {
        const detailsEl = document.getElementById('details');
        const actionsEl = document.getElementById('detail-actions');

        if (!meeting) {
            detailsEl.innerHTML = '<span class="muted">Select a meeting to view details</span>';
            if (actionsEl) actionsEl.style.display = 'none';
            return;
        }

        detailsEl.innerHTML = `
            <div class="kv">
                <strong>Title:</strong>
                <span>${meeting.title || 'Untitled Meeting'}</span>
            </div>
            <div class="kv">
                <strong>Date:</strong>
                <span>${meeting.date || 'Not specified'}</span>
            </div>
            <div class="kv">
                <strong>Time:</strong>
                <span>${meeting.time || 'Not specified'}</span>
            </div>
            <div class="kv">
                <strong>Duration:</strong>
                <span>${meeting.duration || '0'} hours</span>
            </div>
            <div class="kv">
                <strong>Type:</strong>
                <span>${meeting.type || 'Not specified'}</span>
            </div>
            <div class="kv">
                <strong>Location:</strong>
                <span>${meeting.location || 'Not specified'}</span>
            </div>
            <div class="kv">
                <strong>Status:</strong>
                <span class="badge ${this.getStatusBadgeClass(meeting.status)}">
                    ${this.formatStatusText(meeting.status)}
                </span>
            </div>
            <div class="kv">
                <strong>Client:</strong>
                <span>${meeting.clientName || 'Not specified'}</span>
            </div>
            <div class="kv">
                <strong>Assigned To:</strong>
                <span>${meeting.assignedToName || 'Not specified'}</span>
            </div>
            <div class="kv">
                <strong>Notes:</strong>
                <div>
                    ${Array.isArray(meeting.notes) && meeting.notes.length > 0 ?
            meeting.notes.map(note => `    
                            <div class="note-item">
                                <span>${note}</span>
                                <div class="note-actions">
                                    <button class="note-delete" onclick="Meetings.deleteNote('${meeting.id}', '${note.replace(/'/g, "\\'")}')">
                                        <i class="fas fa-times"></i>
                                    </button>
                                </div>
                            </div>
                        `).join('') :
            '<span class="muted">No notes</span>'
        }
                    <div class="add-note-form">
                        <input type="text" id="new-note-${meeting.id}" class="add-note-input" placeholder="Add a new note">
                        <button type="button" class="btn-primary" onclick="Meetings.addNote('${meeting.id}')" style="padding: 8px 12px;">
                            <i class="fas fa-plus"></i> Add
                        </button>
                    </div>
                </div>
            </div>
        `;

        if (actionsEl) actionsEl.style.display = 'flex';
    }

    static async createMeeting(e) {
        e.preventDefault();

        const btn = document.getElementById('mf-btn');
        const msg = document.getElementById('mf-msg');

        btn.disabled = true;
        btn.innerHTML = '<div class="loading"></div> Creating...';
        msg.textContent = '';

        const noteMessage = document.getElementById('mf-notes').value.trim();
        const notes = noteMessage ? [noteMessage] : [];

        const body = {
            title: document.getElementById('mf-title').value.trim(),
            clientId: document.getElementById('mf-clientId').value,
            assignedToId: document.getElementById('mf-assignedTo').value,
            date: document.getElementById('mf-date').value,
            time: document.getElementById('mf-time').value,
            duration: document.getElementById('mf-duration').value ? parseInt(document.getElementById('mf-duration').value) : null,
            type: document.getElementById('mf-type').value || null,
            status: document.getElementById('mf-status').value || null,
            location: document.getElementById('mf-location').value || null,
            notes: notes
        };

        try {
            const res = await this.authFetch('/api/meetings', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(body)
            });

            if (res.ok) {
                msg.textContent = 'Meeting scheduled successfully!';
                msg.style.color = 'var(--success)';

                // Reset form
                document.getElementById('meeting-form').reset();

                // Reload meetings
                await this.loadMeetings();
            } else {
                let errMessage = 'Failed to create meeting';
                try {
                    // حاول تقرأه JSON
                    const errObj = await res.json();
                    errMessage = errObj.message || errMessage;
                } catch (parseError) {
                    // لو مش JSON، هاته كـ Text
                    const errText = await res.text();
                    errMessage = errText;
                }
                msg.textContent = errMessage;
                msg.style.color = 'var(--danger)';
            }
        } catch (e) {
            msg.textContent = 'Unexpected error occurred';
            msg.style.color = 'var(--danger)';
        } finally {
            btn.disabled = false;
            btn.innerHTML = '<i class="fas fa-plus"></i> Schedule Meeting';
            setTimeout(() => {
                msg.textContent = '';
            }, 3000);
        }
    }

    static renderNotesList(notes) {
        const notesList = document.getElementById('notes-list');
        notesList.innerHTML = '';

        if (notes && notes.length > 0) {
            notes.forEach(note => {
                const noteItem = document.createElement('div');
                noteItem.className = 'note-item';
                noteItem.innerHTML = `
                    <span>${note}</span>
                    <div class="note-actions">
                        <button class="note-delete" onclick="this.parentElement.parentElement.remove()">
                            <i class="fas fa-times"></i>
                        </button>
                    </div>
                `;
                notesList.appendChild(noteItem);
            });
        } else {
            notesList.innerHTML = '<div class="muted">No notes</div>';
        }
    }
    static async loadMeetingForEdit(meetingId) {
        try {
            const res = await this.authFetch(`/api/meetings/${meetingId}`);
            if (res.ok) {
                const meeting = await res.json();

                // حفظ البيانات مؤقتاً
                window.currentMeeting = meeting;

                // Fill basic fields
                document.getElementById('edit-id').value = meeting.id;
                document.getElementById('edit-title').value = meeting.title || '';
                document.getElementById('edit-date').value = meeting.date || '';
                document.getElementById('edit-time').value = meeting.time || '';
                document.getElementById('edit-duration').value = meeting.duration || '';
                document.getElementById('edit-type').value = meeting.type || '';
                document.getElementById('edit-status').value = meeting.status || '';
                document.getElementById('edit-location').value = meeting.location || '';

                // Load dropdowns and wait for them to complete
                await this.loadLeads();
                await this.loadUsers();

                // Set values after dropdowns are loaded
                document.getElementById('edit-clientId').value = meeting.clientId || '';
                document.getElementById('edit-assignedTo').value = meeting.assignedToId || '';

                // Render notes
                this.renderNotesList(meeting.notes || []);

                this.openEditModal();

                // تنظيف البيانات المؤقتة بعد ثانية
                setTimeout(() => delete window.currentMeeting, 1000);
            }
        } catch (e) {
            console.error('Error loading meeting for edit:', e);
            alert('Failed to load meeting details');
        }
    }    static openEditModal() {
        document.getElementById('editModal').style.display = 'flex';
    }

    static closeEditModal() {
        document.getElementById('editModal').style.display = 'none';
    }

    static async updateMeeting(e) {
        e.preventDefault();

        const meetingId = document.getElementById('edit-id').value;
        const btn = e.target.querySelector('button[type="submit"]');
        const msg = document.getElementById('edit-meeting-msg');

        btn.disabled = true;
        btn.innerHTML = '<div class="loading"></div> Updating...';
        msg.textContent = '';

        // Get notes from the list
        const notes = Array.from(document.getElementById('notes-list').querySelectorAll('.note-item span'))
            .map(span => span.textContent);

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
            notes: notes
        };

        try {
            const res = await this.authFetch(`/api/meetings/${meetingId}`, {
                method: 'PUT',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(body)
            });

            if (res.ok) {
                msg.textContent = 'Meeting updated successfully!';
                msg.style.color = 'var(--success)';

                // Close modal and reload meetings
                setTimeout(() => {
                    this.closeEditModal();
                    this.loadMeetings();
                }, 1000);
            } else {
                const error = await this.readError(res);
                msg.textContent = error;
                msg.style.color = 'var(--danger)';
            }
        } catch (e) {
            console.error('Error updating meeting:', e);
            msg.textContent = 'Unexpected error occurred';
            msg.style.color = 'var(--danger)';
        } finally {
            btn.disabled = false;
            btn.innerHTML = '<i class="fas fa-save"></i> Update Meeting';
        }
    }

    static async deleteMeeting(meetingId) {
        if (!confirm('Are you sure you want to delete this meeting?')) return;

        try {
            const res = await this.authFetch(`/api/meetings/${meetingId}`, {method: 'DELETE'});
            if (res.ok) {
                alert('Meeting deleted successfully');
                this.loadMeetings();
                document.getElementById('details').innerHTML = '<span class="muted">Select a meeting to view details</span>';
                document.getElementById('detail-actions').style.display = 'none';
            } else {
                const error = await this.readError(res);
                alert('Failed to delete meeting: ' + error);
            }
        } catch (e) {
            console.error('Error deleting meeting:', e);
            alert('Unexpected error occurred');
        }
    }

    static async addNote(meetingId) {
        const noteInput = document.getElementById(`new-note-${meetingId}`);
        const noteContent = noteInput.value.trim();

        if (!noteContent) {
            alert('Please enter a note');
            return;
        }

        try {
            const res = await this.authFetch(`/api/meetings/${meetingId}/notes`, {
                method: 'PATCH',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({content: noteContent})
            });

            if (res.ok) {
                noteInput.value = '';
                this.loadMeetings();
            } else {
                const error = await this.readError(res);
                alert('Failed to add note: ' + error);
            }
        } catch (e) {
            console.error('Error adding note:', e);
            alert('Unexpected error occurred');
        }
    }

    static addNoteInModal() {
        const noteInput = document.getElementById('new-note');
        const noteContent = noteInput.value.trim();

        if (!noteContent) {
            alert('Please enter a note');
            return;
        }

        const notesList = document.getElementById('notes-list');
        const noteItem = document.createElement('div');
        noteItem.className = 'note-item';
        noteItem.innerHTML = `
            <span>${noteContent}</span>
            <div class="note-actions">
                <button class="note-delete" onclick="this.parentElement.parentElement.remove()">
                    <i class="fas fa-times"></i>
                </button>
            </div>
        `;

        if (notesList.querySelector('.muted')) {
            notesList.innerHTML = '';
        }

        notesList.appendChild(noteItem);
        noteInput.value = '';
    }

    static async deleteNote(meetingId, noteContent) {
        if (!confirm('Are you sure you want to delete this note?')) return;

        try {
            const res = await this.authFetch(`/api/meetings/${meetingId}/notes`, {
                method: 'DELETE',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({content: noteContent})
            });

            if (res.ok) {
                this.loadMeetings();
            } else {
                const error = await this.readError(res);
                alert('Failed to delete note: ' + error);
            }
        } catch (e) {
            console.error('Error deleting note:', e);
            alert('Unexpected error occurred');
        }
    }
}

document.addEventListener('DOMContentLoaded', () => {
    Meetings.init();
});