    // Basic data
    const token = localStorage.getItem('token');
    const user = localStorage.getItem('username') || 'User';
    const me = {username: user};
    
    // Check token validity
    function isJwt(t) {
        return typeof t === 'string' && t.split('.').length === 3;
    }
    
    if (!isJwt(token)) {
        localStorage.clear();
        window.location.href = '/auth.html';
    }
    
    // Update UI
    document.getElementById('welcome').textContent = 'Welcome, ' + user;
    
    // Logout
    document.getElementById('logout').addEventListener('click', () => {
        localStorage.clear();
        window.location.href = '/auth.html';
    });
    
    const authHeaders = {'Authorization': 'Bearer ' + token};
    
    function authFetch(url, options = {}) {
        const headers = Object.assign({}, options.headers || {}, authHeaders);
        return fetch(url, {...options, headers});
    }
    
    // Function to get badge class based on status
    function getStatusBadgeClass(status) {
        switch (status) {
            case 'FRESH_LEAD':
                return 'badge-info';
            case 'FOLLOW_UP':
                return 'badge-warning';
            case 'SCHEDULED_VISIT':
                return 'badge-info';
            case 'OPEN_DEAL':
                return 'badge-success';
            case 'CLOSED_DEAL':
                return 'badge-success';
            case 'NO_ANSWER':
                return 'badge-danger';
            default:
                return 'badge-info';
        }
    }
    
    // Function to format status text
    function formatStatusText(status) {
        return status.toLowerCase()
            .split('_')
            .map(word => word.charAt(0).toUpperCase() + word.slice(1))
            .join(' ');
    }
    
    // Load all users for dropdown
    async function fetchAllUsers() {
        try {
            const response = await authFetch('/api/admin');
            if (!response.ok) {
                throw new Error('Failed to fetch users');
            }
            const users = await response.json();
            populateUserDropdowns(users);
        } catch (error) {
            console.error('Error fetching users:', error);
            // Fallback: use current user only
            const selects = document.querySelectorAll('select[id$="assigned"]');
            selects.forEach(select => {
                select.innerHTML = '';
                const option = document.createElement('option');
                option.value = me.username;
                option.textContent = me.username;
                select.appendChild(option);
            });
        }
    }
    
    // Populate user dropdowns
    function populateUserDropdowns(users) {
        const dropdowns = document.querySelectorAll('select[id$="assigned"]');
    
        dropdowns.forEach(dropdown => {
            // Clear existing options except the first one
            while (dropdown.options.length > 1) {
                dropdown.remove(1);
            }
    
            // Add users to dropdown
            users.forEach(user => {
                const option = document.createElement('option');
                option.value = user.id;
                option.textContent = user.username;
                dropdown.appendChild(option);
            });
    
            // Set current user as default if exists
            if (me.username) {
                const currentUserOption = Array.from(dropdown.options).find(opt => opt.textContent === me.username);
                if (currentUserOption) {
                    dropdown.value = currentUserOption.value;
                }
            }
        });
    }
    
    // Load leads
    async function loadLeads() {
        // try admin view first
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
        document.getElementById('list').innerHTML = '<div class="muted">Failed to load leads</div>';
    }
    
    // Render leads list
    function renderList(leads) {
        const listEl = document.getElementById('list');
    
        if (!leads || leads.length === 0) {
            listEl.innerHTML = '<div class="muted">No leads found</div>';
            renderDetails(null);
            return;
        }
    
        listEl.innerHTML = leads.map(lead => `
                    <div class="item" data-id="${lead.id}">
                        <div class="item-header">
                            <div class="item-name">${lead.leadName || lead.name || 'Unnamed Lead'}</div>
                            <span class="badge ${getStatusBadgeClass(lead.leadStatus || lead.status)}">
                                ${formatStatusText(lead.leadStatus || lead.status)}
                            </span>
                        </div>
                        <div class="item-phone">${lead.phone || 'No phone'}</div>
                        <div class="item-details">
                            <span>Budget: $${lead.budget || '0'}</span>
                            <span>Assigned: ${lead.assignedToName}</span>
                        </div>
                    </div>
                `).join('');
    
        // Add click event to items
        document.querySelectorAll('.item').forEach((item, idx) => {
            item.addEventListener('click', () => {
                document.querySelectorAll('.item').forEach(i => i.classList.remove('active'));
                item.classList.add('active');
                const lead = leads.find(l => l.id == item.dataset.id);
                renderDetails(lead);
            });
    
            // Select first item by default
            if (idx === 0) {
                item.classList.add('active');
                renderDetails(leads[0]);
            }
        });
    }
    
    //todo
    // Get username from ID
    function getUsernameFromId(userId) {
        try {
            console.log("🔎 looking for:", userId);
    
            const dropdown = document.getElementById('lf-assigned');
            if (!dropdown) {
                console.warn("⚠️ dropdown not found!");
                return userId;
            }
    
            const option = Array.from(dropdown.options)
                .find(opt => opt.value === String(userId));
    
            console.log("👉 matched option:", option);
    
            return option ? option.textContent : 'Unassigned';
        } catch (e) {
            console.error('Error getting username from ID:', e);
            return 'Unassigned';
        }
    }
    
    
    // Show lead details
    function renderDetails(lead) {
        const detailsEl = document.getElementById('details');
        const actionsEl = document.getElementById('detail-actions');
    
        if (!lead) {
            detailsEl.innerHTML = '<span class="muted">Select a lead to view details</span>';
            actionsEl.style.display = 'none';
            return;
        }
    //todo
        // Use assignedToName if available, or find name if ID is available
        const assignedName = lead.assignedToName;
    
        detailsEl.innerHTML = `
                    <div class="kv">
                        <strong>Name:</strong>
                        <span>${lead.leadName || lead.name || 'Unnamed Lead'}</span>
                    </div>
                    <div class="kv">
                        <strong>Phone:</strong>
                        <span>${lead.phone || 'Not provided'}</span>
                    </div>
                    <div class="kv">
                        <strong>Budget:</strong>
                        <span>$${lead.budget || '0'}</span>
                    </div>
                    <div class="kv">
                        <strong>Source:</strong>
                        <span>${lead.leadSource || lead.source || 'Not specified'}</span>
                    </div>
                    <div class="kv">
                        <strong>Status:</strong>
                        <span class="badge ${getStatusBadgeClass(lead.leadStatus || lead.status)}">
                            ${formatStatusText(lead.leadStatus || lead.status)}
                        </span>
                        <select id="status-dropdown-${lead.id}" onchange="updateLeadStatus('${lead.id}', this.value)">
                            <option value="FRESH_LEAD" ${lead.leadStatus === 'FRESH_LEAD' ? 'selected' : ''}>Fresh Lead</option>
                            <option value="FOLLOW_UP" ${lead.leadStatus === 'FOLLOW_UP' ? 'selected' : ''}>Follow Up</option>
                            <option value="SCHEDULED_VISIT" ${lead.leadStatus === 'SCHEDULED_VISIT' ? 'selected' : ''}>Scheduled Visit</option>
                            <option value="OPEN_DEAL" ${lead.leadStatus === 'OPEN_DEAL' ? 'selected' : ''}>Open Deal</option>
                            <option value="CLOSED_DEAL" ${lead.leadStatus === 'CLOSED_DEAL' ? 'selected' : ''}>Closed Deal</option>
                            <option value="NO_ANSWER" ${lead.leadStatus === 'NO_ANSWER' ? 'selected' : ''}>No Answer</option>
                        </select>
                    </div>
    
                    <div class="kv">
                        <strong>Assigned To:</strong>
                        <span>${assignedName || 'Unassigned'}</span>
                    </div>
                    <div class="kv">
                        <strong>Notes:</strong>
                        <div>
                            ${(lead.notes && lead.notes.length > 0)
            ? lead.notes.map(note => `
                                    <div class="note-item">
                                        <span>${note}</span>
                                        <div class="note-actions">
                                            <button class="note-delete" onclick="deleteNote('${lead.id}', '${note.replace(/'/g, "\\'")}')">
                                                <i class="fas fa-times"></i>
                                            </button>
                                        </div>
                                    </div>
                                `).join('')
            : '<span class="muted">No notes</span>'
        }
                            <div class="add-note-form">
                                <input type="text" id="new-note-${lead.id}" class="add-note-input" placeholder="Add a new note">
                                <button type="button" class="btn-primary" onclick="addNote('${lead.id}')" style="padding: 8px 12px;">
                                    <i class="fas fa-plus"></i> Add
                                </button>
                            </div>
                        </div>
                    </div>
                `;
    
        actionsEl.style.display = 'flex';
    
        // Set up edit and delete buttons
        document.getElementById('edit-lead-btn').onclick = () => loadLeadForEdit(lead.id);
        document.getElementById('delete-lead-btn').onclick = () => deleteLead(lead.id);
    }
    
    // Load lead for editing
    // Load lead for editing
    async function loadLeadForEdit(leadId) {
        try {
            const res = await authFetch(`/api/lead/${leadId}`);
            if (res.ok) {
                const lead = await res.json();

                // Fill edit form
                document.getElementById('edit-id').value = lead.id;
                document.getElementById('edit-name').value = lead.leadName || lead.name || '';
                document.getElementById('edit-phone').value = lead.phone || '';
                document.getElementById('edit-budget').value = lead.budget || '';
                document.getElementById('edit-source').value = lead.leadSource || lead.source || '';
                document.getElementById('edit-status').value = lead.leadStatus || lead.status || '';

                // Load users for dropdown
                await fetchAllUsers();

                document.getElementById('edit-assigned').value = lead.assignedToId || '';

                // Render notes
                renderNotesList(lead.notes || []);

                // Open modal
                document.getElementById('editLeadModal').style.display = 'flex';
            } else {
                const error = await readError(res);
                alert('Failed to load lead: ' + error);
            }
        } catch (e) {
            console.error('Error loading lead for edit:', e);
            alert('Failed to load lead details');
        }
    }
    
    // Render notes list in edit modal
    function renderNotesList(notes) {
        const notesList = document.getElementById('notes-list');
        notesList.innerHTML = '';
    
        if (notes && notes.length > 0) {
            notes.forEach(note => {
                const noteItem = document.createElement('div');
                noteItem.className = 'note-item';
                noteItem.innerHTML = `
                            <span>${note}</span>
                            <div class="note-actions">
                                <button class="note-delete" onclick="deleteNoteFromEdit('${note.replace(/'/g, "\\'")}')">
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
    
    // Delete note from edit modal
    function deleteNoteFromEdit(noteContent) {
        const notesList = document.getElementById('notes-list');
        const notes = Array.from(notesList.querySelectorAll('.note-item span'))
            .map(span => span.textContent)
            .filter(text => text !== noteContent);
    
        renderNotesList(notes);
    }
    
    // Close edit lead modal
    function closeEditLeadModal() {
        document.getElementById('editLeadModal').style.display = 'none';
    }
    
    // Delete lead
    async function deleteLead(leadId) {
        if (!confirm('Are you sure you want to delete this lead?')) return;
    
        try {
            const res = await authFetch(`/api/lead/${leadId}`, {method: 'DELETE'});
            if (res.ok) {
                alert('Lead deleted successfully');
                loadLeads();
                document.getElementById('details').innerHTML = '<div class="muted">Select a lead to view details</div>';
                document.getElementById('detail-actions').style.display = 'none';
            } else {
                const error = await readError(res);
                alert('Failed to delete lead: ' + error);
            }
        } catch (e) {
            console.error('Error deleting lead:', e);
            alert('Unexpected error occurred');
        }
    }
    
    // Add note to lead
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
                noteInput.value = ''; // Clear input field
                loadLeads(); // Reload leads to refresh the view
            } else {
                const error = await readError(res);
                alert('Failed to add note: ' + error);
            }
        } catch (e) {
            console.error('Error adding note:', e);
            alert('Unexpected error occurred');
        }
    }
    
    // Delete note from lead
    async function deleteNote(leadId, noteContent) {
        if (!confirm('Are you sure you want to delete this note?')) return;
    
        try {
            const res = await authFetch(`/api/lead/${leadId}/notes`, {
                method: 'DELETE',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({content: noteContent})
            });
    
            if (res.ok) {
                loadLeads(); // Reload leads to refresh the view
            } else {
                const error = await readError(res);
                alert('Failed to delete note: ' + error);
            }
        } catch (e) {
            console.error('Error deleting note:', e);
            alert('Unexpected error occurred');
        }
    }
    
    // Handle form submission for new lead
    document.getElementById('lead-form')
        .addEventListener('submit', async function (e) {
            e.preventDefault();
    
            const btn = document.getElementById('lf-btn');
            const msg = document.getElementById('lf-msg');
    
            btn.disabled = true;
            msg.textContent = 'Creating lead...';
    
            const notesText = document.getElementById('lf-notes').value.trim();
            const notes = notesText ? [notesText] : [];
    
            const body = {
                leadName: document.getElementById('lf-name').value.trim(),
                phone: document.getElementById('lf-phone').value.trim(),
                budget: parseFloat(document.getElementById('lf-budget').value),
                leadSource: document.getElementById('lf-source').value,
                leadStatus: document.getElementById('lf-status').value,
                assignedToId: document.getElementById('lf-assigned').value,
                notes: notes
            };
    
            try {
                const res = await authFetch('/api/lead', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify(body)
                });
    
                if (res.ok) {
                    msg.textContent = 'Lead created successfully!';
                    msg.style.color = 'var(--success)';
    
                    // Reset form
                    document.getElementById('lead-form').reset();
    
                    // Reload leads
                    loadLeads();
                } else {
                    const error = await readError(res);
                    throw new Error(error);
                }
            } catch (err) {
                msg.textContent = err.message || 'Failed to create lead';
                msg.style.color = 'var(--danger)';
            } finally {
                btn.disabled = false;
                setTimeout(() => {
                    msg.textContent = '';
                }, 3000);
            }
        });
    
    // Handle form submission for editing lead
    document.getElementById('edit-lead-form')
        .addEventListener('submit', async function (e) {
            e.preventDefault();
    
            const msg = document.getElementById('edit-lead-msg');
            const btn = this.querySelector('button[type="submit"]');
    
            msg.textContent = 'Updating lead...';
            btn.disabled = true;
    
            // Get notes from the list
            const notes = Array.from(document.getElementById('notes-list').querySelectorAll('.note-item span'))
                .map(span => span.textContent);
    
            const body = {
                id: document.getElementById('edit-id').value,
                leadName: document.getElementById('edit-name').value,
                phone: document.getElementById('edit-phone').value,
                assignedToName: document.getElementById('edit-assigned').value,
                budget: parseFloat(document.getElementById('edit-budget').value),
                leadSource: document.getElementById('edit-source').value,
                leadStatus: document.getElementById('edit-status').value,
                assignedToId: document.getElementById('edit-assigned').value,
                notes: notes
            };
    
            try {
                const res = await authFetch(`/api/lead/${body.id}`, {
                    method: 'PUT',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify(body)
                });
    
                if (res.ok) {
                    msg.textContent = 'Lead updated successfully!';
                    msg.style.color = 'var(--success)';
    
                    // Close modal and reload leads
                    setTimeout(() => {
                        closeEditLeadModal();
                        loadLeads();
                    }, 1000);
                } else {
                    const error = await readError(res);
                    throw new Error(error);
                }
            } catch (err) {
                msg.textContent = err.message || 'Failed to update lead';
                msg.style.color = 'var(--danger)';
            } finally {
                btn.disabled = false;
            }
        });
    
    // Add note in edit modal
    document.getElementById('add-note-btn')
        .addEventListener('click', function () {
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
        });
    
    async function updateLeadStatus(leadId, newStatus) {
        try {
            const res = await authFetch(`/api/lead/${leadId}`, {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({leadStatus: newStatus})
            });
    
            if (res.ok) {
                const updatedLead = await res.json();
                renderDetails(updatedLead); // refresh view
                loadLeads(); // refresh list
            } else {
                const error = await readError(res);
                alert("Error updating status: " + error);
            }
        } catch (err) {
            console.error(err);
            alert("Unexpected error occurred");
        }
    }
    
    // Helper function to read error response
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
    
    // Initialize page
    document.addEventListener('DOMContentLoaded', function () {
        fetchAllUsers();
        loadLeads();
    });
