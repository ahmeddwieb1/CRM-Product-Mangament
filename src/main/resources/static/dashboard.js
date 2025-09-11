// Basic data
const token = localStorage.getItem('token');
const user = localStorage.getItem('username') || 'User';
const rolesStr = localStorage.getItem('roles') || '[]';

// Check token validity
function isLikelyJwt(t) {
    return typeof t === 'string' && t.split('.').length === 3;
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

// Update UI
document.getElementById('welcome').textContent = 'Welcome, ' + user;
document.getElementById('s-username').textContent = user;
document.getElementById('s-roles').textContent = Array.isArray(rolesList) ? rolesList.join(', ') : rolesList;

// Logout
document.getElementById('logout').addEventListener('click', () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('roles');
    window.location.href = '/auth.html';
});

const authHeaders = isLikelyJwt(token) ? {'Authorization': 'Bearer ' + token} : {};

function authFetch(url, options = {}) {
    const headers = Object.assign({}, options.headers || {}, authHeaders);
    return fetch(url, {...options, headers});
}

// Helper functions for loading and error states
function setLoadingState(elementId) {
    const el = document.getElementById(elementId);
    el.innerHTML = `
                <li>
                    <div class="loading"></div>
                    <span>Loading data...</span>
                </li>
            `;
}

function setErrorState(elementId, message = 'Failed to load data') {
    const el = document.getElementById(elementId);
    el.innerHTML = `
                <li>
                    <div class="icon" style="color: var(--danger);">
                        <i class="fas fa-exclamation-circle"></i>
                    </div>
                    <span>${message}</span>
                </li>
            `;
}

function createListItem(icon, text, badge = null) {
    return `
                <li>
                    <div class="icon">
                        <i class="${icon}"></i>
                    </div>
                    <span>${text}</span>
                    ${badge ? `<span class="badge ${badge.class}">${badge.text}</span>` : ''}
                </li>
            `;
}

authFetch('/api/dashboard/summary')
    .then(r => r.ok ? r.json() : Promise.reject(r))
    .then(data => {
        document.getElementById('total-leads').textContent = data.leads || 0;
        document.getElementById('total-meetings').textContent = data.meetings || 0;
        document.getElementById('total-products').textContent = data.products || 0;
    })
    .catch((error) => {
        console.error('Failed to load summary data:', error);
        document.getElementById('total-leads').textContent = '0';
        document.getElementById('total-meetings').textContent = '0';
        document.getElementById('total-products').textContent = '0';
    });

// Fetch leads
authFetch('/api/lead')
    .then(r => {
        if (r.ok) return r.json();
        return r.text().then(t => Promise.reject({status: r.status, text: t}));
    })
    .then(leads => {
        const el = document.getElementById('leads-list');
        if (leads && leads.length > 0) {
            el.innerHTML = leads.slice(0, 5).map(lead => {
                const badgeClass = lead.status === 'new' ? 'badge-success' :
                    lead.status === 'followup' ? 'badge-warning' : 'badge-danger';
                return createListItem(
                    'fas fa-user',
                    lead.leadName || lead.name || 'Lead',
                    {class: badgeClass, text: lead.status || ''}
                );
            }).join('');
        } else {
            el.innerHTML = createListItem('fas fa-users', 'No leads available');
        }
    })
    .catch(err => {
        const el = document.getElementById('leads-list');
        if (err.status === 403) {
            el.innerHTML = createListItem('fas fa-lock', 'Forbidden: Admins only');
        } else {
            setErrorState('leads-list', 'Failed to load leads');
        }
    });

// Fetch meetings
authFetch('/api/meetings')
    .then(r => r.ok ? r.json() : Promise.reject(r))
    .then(meetings => {
        const el = document.getElementById('meetings-list');
        if (meetings && meetings.length > 0) {
            el.innerHTML = meetings.slice(0, 5).map(meeting => {
                const badgeClass = meeting.status === 'completed' ? 'badge-success' :
                    meeting.status === 'scheduled' ? 'badge-warning' : 'badge-danger';
                return createListItem(
                    'fas fa-video',
                    meeting.title || 'Meeting',
                    {class: badgeClass, text: meeting.status || ''}
                );
            }).join('');
        } else {
            el.innerHTML = createListItem('fas fa-calendar-times', 'No meetings scheduled');
        }
    })
    .catch(() => {
        setErrorState('meetings-list', 'Failed to load meetings');
    });

// Fetch products
authFetch('/api/products')
    .then(r => r.ok ? r.json() : Promise.reject(r))
    .then(products => {
        const el = document.getElementById('products-list');
        if (products && products.length > 0) {
            el.innerHTML = products.slice(0, 5).map(product =>
                createListItem('fas fa-cube', `${product.name} (${product.price ? '$' + product.price : 'No price'})`)
            ).join('');
        } else {
            el.innerHTML = createListItem('fas fa-box-open', 'No products available');
        }
    })
    .catch(() => {
        setErrorState('products-list', 'Failed to load products');
    });

// Fetch recent activity
authFetch('/api/activity')
    .then(r => r.ok ? r.json() : Promise.reject(r))
    .then(activities => {
        const el = document.getElementById('recent-activity');
        if (activities && activities.length > 0) {
            let html = '';
            activities.slice(0, 4).forEach(activity => {
                html += `
                            <div class="stat-item">
                                <div class="stat-icon" style="background: rgba(67, 97, 238, 0.1); color: var(--primary);">
                                    <i class="fas ${activity.icon || 'fa-bell'}"></i>
                                </div>
                                <div class="stat-info">
                                    <div>${activity.action}</div>
                                    <div class="stat-label">${activity.time}</div>
                                </div>
                            </div>
                        `;
            });
            el.innerHTML = html;
        } else {
            el.innerHTML = '<p class="muted" style="text-align: center;">No recent activity</p>';
        }
    })
    .catch(() => {
        document.getElementById('recent-activity').innerHTML =
            '<p class="muted" style="text-align: center;">Failed to load activities</p>';
    });
