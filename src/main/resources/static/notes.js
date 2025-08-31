
// Basic data
const token = localStorage.getItem('token');
const user = localStorage.getItem('username') || 'User';

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

const authHeaders = { 'Authorization': 'Bearer ' + token };

function authFetch(url, options = {}) {
    const headers = Object.assign({}, options.headers || {}, authHeaders);
    return fetch(url, { ...options, headers });
}

// Handle form submission
document.getElementById('note-form').addEventListener('submit', async function(e) {
    e.preventDefault();

    const btn = document.getElementById('n-btn');
    const msg = document.getElementById('n-msg');

    btn.disabled = true;
    btn.innerHTML = '<div class="loading"></div> Submitting...';
    msg.textContent = '';

    const formData = {
        username: document.getElementById('n-username').value.trim(),
        gender: document.getElementById('n-gender').value,
        email: document.getElementById('n-email').value.trim(),
        phone: document.getElementById('n-phone').value.trim(),
        content: document.getElementById('n-content').value.trim()
    };

    // Basic validation
    if (!formData.username || !formData.gender || !formData.email || !formData.phone || !formData.content) {
        msg.textContent = 'Please fill all required fields';
        msg.style.color = 'var(--danger)';
        btn.disabled = false;
        btn.innerHTML = '<i class="fas fa-plus"></i> Submit Note';
        return;
    }

    // Phone validation
    const phoneRegex = /^[0-9]{10,12}$/;
    if (!phoneRegex.test(formData.phone)) {
        msg.textContent = 'Please enter a valid phone number (10-12 digits)';
        msg.style.color = 'var(--danger)';
        btn.disabled = false;
        btn.innerHTML = '<i class="fas fa-plus"></i> Submit Note';
        return;
    }

    // Email validation
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(formData.email)) {
        msg.textContent = 'Please enter a valid email address';
        msg.style.color = 'var(--danger)';
        btn.disabled = false;
        btn.innerHTML = '<i class="fas fa-plus"></i> Submit Note';
        return;
    }

    try {
        const res = await authFetch('/api/notes', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(formData)
        });

        if (res.ok) {
            msg.textContent = 'Note created successfully!';
            msg.style.color = 'var(--success)';

            // Reset form
            document.getElementById('note-form').reset();
        } else {
            const error = await res.text();
            msg.textContent = 'Failed to create note: ' + error;
            msg.style.color = 'var(--danger)';
        }
    } catch (e) {
        console.error('Error creating note:', e);
        msg.textContent = 'Unexpected error occurred';
        msg.style.color = 'var(--danger)';
    } finally {
        btn.disabled = false;
        btn.innerHTML = '<i class="fas fa-plus"></i> Submit Note';
        setTimeout(() => { msg.textContent = ''; }, 3000);
    }
});
