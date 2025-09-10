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

// Load products
async function loadProducts() {
    try {
        const res = await authFetch('/api/products');
        if (res.ok) {
            const data = await res.json();
            renderList(data);
        } else {
            throw new Error('Failed to load products');
        }
    } catch (error) {
        console.error('Error loading products:', error);
        document.getElementById('list').innerHTML = '<div class="muted">Failed to load products</div>';
    }
}

// Render products list
function renderList(products) {
    const listEl = document.getElementById('list');

    if (!products || products.length === 0) {
        listEl.innerHTML = '<div class="muted">No products found</div>';
        renderDetails(null);
        return;
    }

    listEl.innerHTML = products.map(product => `
            <div class="item" data-id="${product.id}">
                <div class="item-header">
                    <div class="item-name">${product.name || 'Unnamed Product'}</div>
                    <span class="badge ${product.amount > 0 ? 'badge-success' : 'badge-danger'}">
                        ${product.amount > 0 ? 'In Stock' : 'Out of Stock'}
                    </span>
                </div>
                <div class="item-price">$${product.price || '0'}</div>
                <div class="item-details">
                    <span>Amount: ${product.amount || '0'}</span>
                </div>
            </div>
        `).join('');

    // Add click event to items
    document.querySelectorAll('.item').forEach((item, idx) => {
        item.addEventListener('click', () => {
            document.querySelectorAll('.item').forEach(i => i.classList.remove('active'));
            item.classList.add('active');
            const product = products.find(p => p.id == item.dataset.id);
            renderDetails(product);
        });

        // Select first item by default
        if (idx === 0) {
            item.classList.add('active');
            renderDetails(products[0]);
        }
    });
}

// Show product details
function renderDetails(product) {

    const detailsEl = document.getElementById('details');
    const actionsEl = document.getElementById('detail-actions');

    if (!product) {
        detailsEl.innerHTML = '<span class="muted">Select a product to view details</span>';
        actionsEl.style.display = 'none';
        return;
    }

    detailsEl.innerHTML = `
            <div class="kv">
                <strong>Name:</strong>
                <span>${product.name || 'Unnamed Product'}</span>
            </div>
            <div class="kv">
                <strong>Price:</strong>
                <span>$${product.price || '0'}</span>
            </div>
         <div class="kv">
              <strong>Amount:</strong>
             <div class="amount-control">
              <button onclick="changeAmountLocally('${product.id}', -1)">-</button>
              <span id="amount-${product.id}">${product.amount || 0}</span>
              <button onclick="changeAmountLocally('${product.id}', 1)">+</button>
            </div>


            </div>

            <div class="kv">
                <strong>Description:</strong>
                <span>${product.description || 'No description'}</span>
            </div>
          
        `;

    actionsEl.style.display = 'flex';

    // Set up edit and delete buttons
    document.getElementById('edit-product-btn').onclick =
        () => loadProductForEdit(product.id);
    document.getElementById('delete-product-btn').onclick =
        () => deleteProduct(product.id);
}

// Load product for editing
async function loadProductForEdit(productId) {
    try {
        const res = await authFetch(`/api/products/${productId}`);
        if (res.ok) {
            const product = await res.json();

            // Fill edit form
            document.getElementById('edit-id').value = product.id;
            document.getElementById('edit-name').value = product.name || '';
            document.getElementById('edit-price').value = product.price || '';
            document.getElementById('edit-amount').value = product.amount || '';
            document.getElementById('edit-description').value = product.description || '';

            // Open modal
            document.getElementById('editProductModal').style.display = 'flex';
        } else {
            const error = await readError(res);
            alert('Failed to load product: ' + error);
        }
    } catch (e) {
        console.error('Error loading product for edit:', e);
        alert('Failed to load product details');
    }
}

// Close edit product modal
function closeEditProductModal() {
    document.getElementById('editProductModal').style.display = 'none';
}

// Delete product
async function deleteProduct(productId) {
    if (!confirm('Are you sure you want to delete this product?')) return;

    try {
        const res = await authFetch(`/api/products/${productId}`, {method: 'DELETE'});
        if (res.ok) {
            alert('Product deleted successfully');
            loadProducts();
            document.getElementById('details').innerHTML = '<div class="muted">Select a product to view details</div>';
            document.getElementById('detail-actions').style.display = 'none';
        } else {
            const error = await readError(res);
            alert('Failed to delete product: ' + error);
        }
    } catch (e) {
        console.error('Error deleting product:', e);
        alert('Unexpected error occurred');
    }
}

// Handle form submission for new product
document.getElementById('product-form')
    .addEventListener('submit', async function (e) {
        e.preventDefault();

        const btn = document.getElementById('pf-btn');
        const msg = document.getElementById('pf-msg');

        btn.disabled = true;
        msg.textContent = 'Creating product...';

        const body = {
            name: document.getElementById('pf-name').value.trim(),
            price: parseFloat(document.getElementById('pf-price').value),
            amount: parseInt(document.getElementById('pf-amount').value),
            description: document.getElementById('pf-description').value.trim()
        };

        try {
            const res = await authFetch('/api/products', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(body)
            });

            if (res.ok) {
                msg.textContent = 'Product created successfully!';
                msg.style.color = 'var(--success)';

                // Reset form
                document.getElementById('product-form').reset();

                // Reload products
                loadProducts();
            } else {
                const error = await readError(res);
                throw new Error(error);
            }
        } catch (err) {
            msg.textContent = err.message || 'Failed to create product';
            msg.style.color = 'var(--danger)';
        } finally {
            btn.disabled = false;
            setTimeout(() => {
                msg.textContent = '';
            }, 3000);
        }
    });

// Handle form submission for editing product
document.getElementById('edit-product-form')
    .addEventListener('submit', async function (e) {
        e.preventDefault();

        const msg = document.getElementById('edit-product-msg');
        const btn = this.querySelector('button[type="submit"]');

        msg.textContent = 'Updating product...';
        msg.style.color = 'black';
        btn.disabled = true;

        const productId = document.getElementById('edit-id').value;

        const body = {
            name: document.getElementById('edit-name').value,
            price: parseFloat(document.getElementById('edit-price').value),
            amount: parseInt(document.getElementById('edit-amount').value),
            description: document.getElementById('edit-description').value
        };

        try {
            console.log("Request body:", body);

            const res = await authFetch(`/api/products/${productId}`, {
                method: 'PATCH',   // لازم كابيتال
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(body)
            });

            if (res.ok) {
                msg.textContent = 'Product updated successfully!';
                msg.style.color = 'var(--success)';

                setTimeout(() => {
                    closeEditProductModal();
                    loadProducts();
                }, 1000);
            } else {
                const error = await readError(res);
                console.error("Server error response:", error);
                throw new Error(error);
            }
        } catch (err) {
            console.error("Request failed:", err);
            msg.textContent = err.message || 'Failed to update product';
            msg.style.color = 'var(--danger)';
        } finally {
            btn.disabled = false;
        }
    });

let updateTimers = {};

function changeAmountLocally(id, delta) {
    const span = document.getElementById(`amount-${id}`);
    let current = parseInt(span.textContent) || 0;
    let newAmount = Math.max(0, current + delta);
    span.textContent = newAmount;

    clearTimeout(updateTimers[id]);

    updateTimers[id] = setTimeout(() => syncAmount(id, newAmount), 1500);
}

async function syncAmount(id, newAmount) {
    try {
        const res = await authFetch(`/api/products/${id}/amount`, {
            method: 'PATCH',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({ amount: newAmount })
        });
        if (!res.ok) {
            const error = await readError(res);
            console.error("Error syncing:", error);
        }
    } catch (err) {
        console.error("Sync failed", err);
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
document.addEventListener('DOMContentLoaded', () => {
    const excelForm  = document.getElementById('excelForm');
    const excelFile  = document.getElementById('excelFile');
    const uploadBtn  = document.getElementById('excelBtn');
    const resultEl   = document.getElementById('uploadResult');

    if (excelForm) {
        excelForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            if (!excelFile.files.length) {
                alert('Please select an Excel file first!');
                return;
            }

            // (اختياري) فِلتر حجم الملف قبل الإرسال – مثال 20MB
            const maxSize = 20 * 1024 * 1024;
            if (excelFile.files[0].size > maxSize) {
                resultEl.textContent = '❌ File is too large (max 20MB).';
                resultEl.style.color = 'var(--danger)';
                return;
            }

            const fd = new FormData();
            fd.append('file', excelFile.files[0]); // الاسم "file" لازم يطابق @RequestParam("file")

            uploadBtn.disabled = true;
            uploadBtn.innerHTML = '<div class="loading"></div> Uploading...';
            resultEl.textContent = '';
            resultEl.style.color = '';

            try {
                // استخدم authFetch عشان Authorization يتحط تلقائيًا
                const res = await authFetch('/api/import/excel', {
                    method: 'POST',
                    body: fd
                });

                // اقرأ البودي مرة واحدة فقط (JSON لو متاح، وإلا نص)
                const ct = res.headers.get('content-type') || '';
                const payload = ct.includes('application/json')
                    ? await res.json()
                    : await res.text();

                if (!res.ok) {
                    const message = typeof payload === 'string'
                        ? payload
                        : (payload.message || 'Upload failed');
                    throw new Error(message);
                }

                // لو السيرفر راجع JSON بالحقول دي
                if (typeof payload === 'object') {
                    const imported = payload.importedProducts ?? 0;
                    const sent     = Array.isArray(payload.emailsSentTo) ? payload.emailsSentTo.join(', ') : '—';
                    const failed   = Array.isArray(payload.failedEmails) ? payload.failedEmails.join(', ') : '—';

                    resultEl.textContent =
                        `✅ Imported Products: ${imported}\n` +
                        `📧 Emails Sent: ${sent}\n` +
                        `❌ Failed Emails: ${failed}`;
                } else {
                    // fallback لو السيرفر رجّع نص
                    resultEl.textContent = `✅ ${payload}`;
                }

                // نظّف الفورم وحمّل المنتجات تاني
                excelForm.reset();
                await loadProducts();

            } catch (err) {
                console.error('Upload failed:', err);
                resultEl.textContent = `❌ ${err.message || 'Upload failed'}`;
                resultEl.style.color = 'var(--danger)';
            } finally {
                uploadBtn.disabled = false;
                uploadBtn.innerHTML = 'Upload';
            }
        });
    }
});

// Initialize page
document.addEventListener('DOMContentLoaded', function () {
    loadProducts();
});
