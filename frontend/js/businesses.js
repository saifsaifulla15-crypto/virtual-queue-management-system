/**
 * Logic for businesses.html
 */

document.addEventListener('DOMContentLoaded', async () => {
    // 1. Load initial businesses list
    loadBusinesses();

    // 2. Pre-fetch users to populate the Owner dropdown (filtered by OWNER) 
    // and Staff dropdown (filtered by STAFF)
    loadUsersForDropdowns();
});

// --- UI Elements ---
const businessTableBody = document.getElementById('business-table-body');
const registerPanel = document.getElementById('register-panel');
const detailsPanel = document.getElementById('details-panel');

// --- Feature 1: View all businesses ---
async function loadBusinesses() {
    try {
        document.getElementById('business-list-loading').style.display = 'block';
        const response = await getBusinesses();
        const businesses = response.data || [];
        
        businessTableBody.innerHTML = ''; // clear existing rows

        if (businesses.length === 0) {
            businessTableBody.innerHTML = '<tr><td colspan="2">No businesses found.</td></tr>';
        } else {
            businesses.forEach(b => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${b.name}</td>
                    <td>
                        <button class="btn btn-secondary" onclick="viewBusinessDetails(${b.id})">Manage</button>
                    </td>
                `;
                businessTableBody.appendChild(tr);
            });
        }
    } catch (error) {
        showMessage('main-message', `Error loading businesses: ${error.message}`, 'error');
    } finally {
        document.getElementById('business-list-loading').style.display = 'none';
    }
}

// Populate dropdowns to prevent guessing IDs
async function loadUsersForDropdowns() {
    try {
        const response = await getUsers();
        const users = response.data || [];
        
        const ownerSelect = document.getElementById('reg-owner');
        const staffSelect = document.getElementById('add-staff-select');
        
        ownerSelect.innerHTML = '<option value="">-- Select Owner --</option>';
        staffSelect.innerHTML = '<option value="">-- Select Staff User --</option>';

        users.forEach(u => {
            // BusinessService requires owner to have OWNER role
            if (u.role === 'OWNER') {
                const opt = document.createElement('option');
                opt.value = u.id;
                opt.textContent = `${u.name} (ID: ${u.id})`;
                ownerSelect.appendChild(opt);
            }
            // StaffService requires user to have STAFF role
            if (u.role === 'STAFF') {
                const opt = document.createElement('option');
                opt.value = u.id;
                opt.textContent = `${u.name} (ID: ${u.id})`;
                staffSelect.appendChild(opt);
            }
        });
    } catch (e) {
        console.error("Failed to load users for dropdowns", e);
    }
}

// --- Feature 2: Register a business ---
document.getElementById('show-register-btn').addEventListener('click', () => {
    registerPanel.classList.remove('hidden');
    detailsPanel.classList.add('hidden');
});
document.getElementById('cancel-register-btn').addEventListener('click', () => {
    registerPanel.classList.add('hidden');
});

document.getElementById('registerForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    hideMessage('main-message');
    
    const newBusiness = {
        name: document.getElementById('reg-name').value,
        address: document.getElementById('reg-address').value,
        phone: document.getElementById('reg-phone').value,
        email: document.getElementById('reg-email').value,
        ownerId: parseInt(document.getElementById('reg-owner').value)
    };

    try {
        await registerBusiness(newBusiness);
        showMessage('main-message', 'Business registered successfully!', 'success');
        registerPanel.classList.add('hidden');
        document.getElementById('registerForm').reset();
        loadBusinesses(); // Refresh list dynamically!
    } catch (error) {
        showMessage('main-message', error.message, 'error');
    }
});

// --- Feature 3: View business details ---
async function viewBusinessDetails(id) {
    try {
        hideMessage('main-message');
        hideMessage('staff-message');
        registerPanel.classList.add('hidden');
        
        const response = await getBusinessById(id);
        const b = response.data;
        
        // Populate update form
        document.getElementById('upd-id').value = b.id;
        document.getElementById('upd-name').value = b.name;
        document.getElementById('upd-address').value = b.address;
        
        document.getElementById('details-title').textContent = `${b.name} (Owner: ${b.ownerId})`;
        detailsPanel.classList.remove('hidden');
        
        // Load staff for this business
        loadStaff(b.id);
    } catch (error) {
        showMessage('main-message', `Failed to load details: ${error.message}`, 'error');
    }
}

document.getElementById('close-details-btn').addEventListener('click', () => {
    detailsPanel.classList.add('hidden');
});

// --- Feature 4: Update business ---
document.getElementById('updateForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = document.getElementById('upd-id').value;
    const patchData = {
        name: document.getElementById('upd-name').value,
        address: document.getElementById('upd-address').value
    };
    
    try {
        await updateBusiness(id, patchData);
        showMessage('main-message', 'Business updated successfully!', 'success');
        loadBusinesses(); // refresh list
    } catch (error) {
        showMessage('main-message', error.message, 'error');
    }
});

// --- Feature 5: View business staff ---
async function loadStaff(businessId) {
    const tbody = document.getElementById('staff-table-body');
    tbody.innerHTML = '<tr><td colspan="4">Loading...</td></tr>';
    
    try {
        const response = await getStaffByBusiness(businessId);
        const staffList = response.data || [];
        
        tbody.innerHTML = '';
        if (staffList.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4">No active staff found.</td></tr>';
            return;
        }
        
        staffList.forEach(s => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${s.id}</td>
                <td>${s.userId}</td>
                <td>${s.active ? 'Yes' : 'No'}</td>
                <td>
                    <button class="btn btn-secondary" onclick="toggleStaff(${s.id}, ${businessId})">Toggle Status</button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch (error) {
        tbody.innerHTML = '<tr><td colspan="4">Failed to load staff.</td></tr>';
    }
}

// --- Feature 6: Add a user as staff ---
document.getElementById('addStaffForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    hideMessage('staff-message');
    const businessId = document.getElementById('upd-id').value;
    const userId = document.getElementById('add-staff-select').value;
    
    try {
        await addStaff(businessId, userId);
        showMessage('staff-message', 'Staff added successfully!', 'success');
        document.getElementById('add-staff-select').value = '';
        loadStaff(businessId); // Refresh staff table
    } catch (error) {
        showMessage('staff-message', error.message, 'error');
    }
});

// --- Feature 7: Toggle staff status ---
async function toggleStaff(staffId, businessId) {
    hideMessage('staff-message');
    try {
        await toggleStaffStatus(staffId);
        showMessage('staff-message', 'Staff status toggled successfully!', 'success');
        loadStaff(businessId); // Refresh staff table
    } catch (error) {
        showMessage('staff-message', error.message, 'error');
    }
}
