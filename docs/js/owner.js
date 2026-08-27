/**
 * owner.js
 */

let currentUser = null;
let ownerBusinesses = [];
let allStaffUsers = [];

document.addEventListener('DOMContentLoaded', async () => {
    currentUser = protectRoute('OWNER');
    if (!currentUser) return;

    document.getElementById('user-info').textContent = `${currentUser.name} (User ID: ${currentUser.id})`;
    setupLogoutButton();

    setupTabs();
    await loadInitialData();
});

function setupTabs() {
    const tabs = document.querySelectorAll('.tab-btn');
    const contents = document.querySelectorAll('.tab-content');
    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            tabs.forEach(t => t.classList.remove('active'));
            contents.forEach(c => c.classList.remove('active'));
            tab.classList.add('active');
            document.getElementById(tab.getAttribute('data-target')).classList.add('active');
        });
    });
}

async function loadInitialData() {
    try {
        const usersRes = await getUsers();
        allStaffUsers = (usersRes.data || []).filter(u => u.role === 'STAFF');
        
        await loadMyBusinesses();
        await loadMyQueues();
        populateDropdowns();
    } catch (e) {
        showMessage('main-message', e.message, 'error');
    }
}

// --- BUSINESS ---
async function loadMyBusinesses() {
    document.getElementById('biz-list-loading').style.display = 'block';
    try {
        const res = await getBusinesses();
        const allBiz = res.data || [];
        ownerBusinesses = allBiz.filter(b => b.ownerId === currentUser.id);
        
        const tbody = document.getElementById('biz-table-body');
        tbody.innerHTML = '';
        if (ownerBusinesses.length === 0) {
            tbody.innerHTML = '<tr><td colspan="3">No businesses owned by you.</td></tr>';
        } else {
            ownerBusinesses.forEach(b => {
                const tr = document.createElement('tr');
                tr.innerHTML = `<td>${b.id}</td><td>${b.name}</td><td><button class="btn btn-sm btn-secondary">Owner</button></td>`;
                tbody.appendChild(tr);
            });
        }
    } finally {
        document.getElementById('biz-list-loading').style.display = 'none';
    }
}

document.getElementById('biz-register-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    hideMessage('main-message');
    const payload = {
        name: document.getElementById('bz-name').value,
        email: document.getElementById('bz-email').value,
        phone: document.getElementById('bz-phone').value,
        address: document.getElementById('bz-address').value,
        ownerId: currentUser.id
    };
    try {
        await registerBusiness(payload);
        showMessage('main-message', 'Business registered!', 'success');
        document.getElementById('biz-register-form').reset();
        await loadMyBusinesses();
        populateDropdowns();
    } catch (err) {
        showMessage('main-message', err.message, 'error');
    }
});

function populateDropdowns() {
    const sBizSelect = document.getElementById('staff-biz-select');
    const asBizSelect = document.getElementById('add-staff-biz-select');
    const cqBizSelect = document.getElementById('cq-biz');
    
    [sBizSelect, asBizSelect, cqBizSelect].forEach(sel => {
        if(sel) {
            sel.innerHTML = '<option value="">-- Choose Business --</option>';
            ownerBusinesses.forEach(b => {
                sel.innerHTML += `<option value="${b.id}">${b.name} (ID:${b.id})</option>`;
            });
        }
    });

    const asUserSelect = document.getElementById('add-staff-user-select');
    asUserSelect.innerHTML = '<option value="">-- Select STAFF User --</option>';
    allStaffUsers.forEach(u => {
        asUserSelect.innerHTML += `<option value="${u.id}">${u.name} (User ID:${u.id})</option>`;
    });
}

// --- STAFF ---
document.getElementById('staff-biz-select').addEventListener('change', async (e) => {
    const bizId = e.target.value;
    const tbody = document.getElementById('staff-table-body');
    if (!bizId) { tbody.innerHTML = ''; return; }
    
    try {
        const res = await getStaffByBusiness(bizId);
        const staffList = res.data || [];
        tbody.innerHTML = '';
        if (staffList.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4">No staff found for this business.</td></tr>';
            return;
        }
        staffList.forEach(s => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${s.id}</td>
                <td>${s.userId}</td>
                <td>${s.active ? 'Yes' : 'No'}</td>
                <td><button class="btn btn-secondary btn-sm" onclick="toggleStatus(${s.id}, ${bizId})">Toggle</button></td>
            `;
            tbody.appendChild(tr);
        });
    } catch (err) {
        showMessage('main-message', err.message, 'error');
    }
});

async function toggleStatus(staffId, bizId) {
    try {
        await toggleStaffStatus(staffId);
        document.getElementById('staff-biz-select').value = bizId;
        document.getElementById('staff-biz-select').dispatchEvent(new Event('change'));
    } catch (e) {
        showMessage('main-message', e.message, 'error');
    }
}

document.getElementById('staff-add-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const bizId = document.getElementById('add-staff-biz-select').value;
    const userId = document.getElementById('add-staff-user-select').value;
    try {
        await addStaff(bizId, userId);
        showMessage('main-message', 'Staff added!', 'success');
        document.getElementById('staff-biz-select').value = bizId;
        document.getElementById('staff-biz-select').dispatchEvent(new Event('change'));
    } catch (err) {
        showMessage('main-message', err.message, 'error');
    }
});

// --- QUEUES ---
async function loadMyQueues() {
    try {
        const res = await getQueues();
        const allQueues = res.data || [];
        const myBizIds = ownerBusinesses.map(b => b.id);
        const myQueues = allQueues.filter(q => myBizIds.includes(q.businessId));
        
        const tbody = document.getElementById('owner-queue-list');
        tbody.innerHTML = '';
        myQueues.forEach(q => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${q.id}</td>
                <td>${q.name}</td>
                <td>${q.businessId}</td>
                <td><button class="btn btn-secondary btn-sm" onclick="manageQueue(${q.id})">Manage</button></td>
            `;
            tbody.appendChild(tr);
        });
    } catch (e) {
        console.error(e);
    }
}

document.getElementById('queue-create-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    hideMessage('main-message');
    const qData = {
        businessId: parseInt(document.getElementById('cq-biz').value),
        name: document.getElementById('cq-name').value,
        capacityType: document.getElementById('cq-cap-type').value,
        maxCapacity: parseInt(document.getElementById('cq-maxcap').value),
        defaultServiceTimeMinutes: parseInt(document.getElementById('cq-deftime').value),
        status: document.getElementById('cq-status').value
    };
    try {
        await createQueue(qData);
        showMessage('main-message', 'Queue created!', 'success');
        document.getElementById('queue-create-form').reset();
        await loadMyQueues();
    } catch (err) {
        showMessage('main-message', err.message, 'error');
    }
});

async function manageQueue(queueId) {
    document.getElementById('queue-manage-panel').classList.remove('hidden');
    document.getElementById('uq-id').value = queueId;
    try {
        const res = await getQueueById(queueId);
        const q = res.data;
        document.getElementById('mq-title').textContent = `Manage: ${q.name}`;
        document.getElementById('uq-name').value = q.name;
        document.getElementById('uq-maxcap').value = q.maxCapacity;
        document.getElementById('uq-deftime').value = q.defaultServiceTimeMinutes;
        document.getElementById('uq-status').value = q.status;
        
        const aRes = await getQueueAnalytics(queueId);
        const a = aRes.data;
        document.getElementById('a-served').textContent = a.totalCustomersServed || 0;
        document.getElementById('a-missed').textContent = (a.cancelled || 0) + (a.skipped || 0);
        document.getElementById('a-wait').textContent = `${(a.averageWaitingTime || 0).toFixed(1)}m`;
        
        await loadQueueRecords(queueId);
    } catch (e) {
        showMessage('main-message', e.message, 'error');
    }
}

async function loadQueueRecords(queueId) {
    const tbody = document.getElementById('records-table-body');
    document.getElementById('records-loading').style.display = 'block';
    tbody.innerHTML = '';
    try {
        const res = await fetch(`${API_BASE_URL}/serviceRecords/queue/${queueId}`);
        if (res.status === 404) {
            tbody.innerHTML = '<tr><td colspan="6">No service records available yet.</td></tr>';
            return;
        }
        if (!res.ok) throw new Error('Failed to fetch records');
        
        const json = await res.json();
        const records = json.data || [];
        
        if (records.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6">No service records available yet.</td></tr>';
            return;
        }
        
        records.forEach(r => {
            const tr = document.createElement('tr');
            const tokenNum = r.queueToken ? r.queueToken.tokenNumber : '?';
            const startTime = r.serviceStartTime ? new Date(r.serviceStartTime).toLocaleTimeString() : '-';
            const endTime = r.serviceEndTime ? new Date(r.serviceEndTime).toLocaleTimeString() : '-';
            const waitTime = (r.waitingDurationMinutes || 0).toFixed(1);
            const svcTime = (r.serviceDurationMinutes || 0).toFixed(1);
            
            tr.innerHTML = `
                <td>${tokenNum}</td>
                <td>${r.staffId || '?'}</td>
                <td>${startTime}</td>
                <td>${endTime}</td>
                <td>${waitTime}</td>
                <td>${svcTime}</td>
            `;
            tbody.appendChild(tr);
        });
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="6" style="color:red;">Error loading records: ${err.message}</td></tr>`;
    } finally {
        document.getElementById('records-loading').style.display = 'none';
    }
}

document.getElementById('queue-update-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const qId = document.getElementById('uq-id').value;
    const data = {
        name: document.getElementById('uq-name').value,
        maxCapacity: parseInt(document.getElementById('uq-maxcap').value),
        defaultServiceTimeMinutes: parseInt(document.getElementById('uq-deftime').value)
    };
    try {
        await updateQueue(qId, data);
        showMessage('main-message', 'Queue details updated!', 'success');
        await loadMyQueues();
    } catch (err) {
        showMessage('main-message', err.message, 'error');
    }
});

document.getElementById('queue-status-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const qId = document.getElementById('uq-id').value;
    const stat = document.getElementById('uq-status').value;
    try {
        await updateQueueStatus(qId, stat);
        showMessage('main-message', 'Queue status updated!', 'success');
    } catch (err) {
        showMessage('main-message', err.message, 'error');
    }
});
