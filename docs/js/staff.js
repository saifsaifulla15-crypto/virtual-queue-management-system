/**
 * staff.js
 */

let currentUser = null;
let currentStaffEntity = null;
let currentBusinessId = null;
let currentQueueId = null;
let activeStaffTokenId = null;

document.addEventListener('DOMContentLoaded', async () => {
    currentUser = protectRoute('STAFF');
    if (!currentUser) return;

    document.getElementById('user-info').textContent = `${currentUser.name} (User ID: ${currentUser.id})`;
    setupLogoutButton();

    await findMyStaffEntity();
});

async function findMyStaffEntity() {
    try {
        const res = await getBusinesses();
        const businesses = res.data || [];
        
        let foundStaff = null;
        for (const biz of businesses) {
            const staffRes = await getStaffByBusiness(biz.id);
            const staffList = staffRes.data || [];
            foundStaff = staffList.find(s => s.userId === currentUser.id);
            if (foundStaff) {
                currentBusinessId = biz.id;
                break;
            }
        }

        if (!foundStaff) {
            showMessage('main-message', 'You are not assigned to any business as staff.', 'error');
            document.getElementById('queue-list-loading').style.display = 'none';
            return;
        }

        currentStaffEntity = foundStaff;
        document.getElementById('my-biz-id').textContent = currentBusinessId;
        document.getElementById('my-staff-id').textContent = currentStaffEntity.id;

        loadMyQueues();
    } catch (err) {
        showMessage('main-message', `Failed to load staff details: ${err.message}`, 'error');
    }
}

async function loadMyQueues() {
    try {
        const res = await getQueues();
        // Filter manually since API Gateway /queue returns all. (Though /queue/business/{id} could be used if implemented in api.js)
        const allQueues = res.data || [];
        const myQueues = allQueues.filter(q => q.businessId === currentBusinessId);
        
        const tbody = document.getElementById('queue-table-body');
        tbody.innerHTML = '';
        document.getElementById('queue-list-loading').style.display = 'none';

        if (myQueues.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4">No queues found for your business.</td></tr>';
            return;
        }

        myQueues.forEach(q => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${q.id}</td>
                <td>${q.name}</td>
                <td>${q.status}</td>
                <td><button class="btn btn-secondary btn-sm" onclick="selectQueue(${q.id}, '${q.name}')">Manage</button></td>
            `;
            tbody.appendChild(tr);
        });
    } catch (err) {
        showMessage('main-message', err.message, 'error');
    }
}

async function selectQueue(queueId, queueName) {
    currentQueueId = queueId;
    document.getElementById('cp-qname').textContent = `Manage: ${queueName}`;
    document.getElementById('control-panel').classList.remove('hidden');
    hideMessage('staff-message');
    setActiveStaffTokenUI(null, null, null);
    
    // Load staff dropdown specifically for this queue's business
    await loadStaffDropdown();
}

function setActiveStaffTokenUI(tokenId, tokenNum, status) {
    const displaySpan = document.getElementById('active-staff-token');
    const statusSpan = document.getElementById('active-staff-status');
    
    if (!tokenId) {
        displaySpan.textContent = "None";
        statusSpan.textContent = "";
        activeStaffTokenId = null;
    } else {
        displaySpan.textContent = `Token #${tokenNum} (ID:${tokenId})`;
        statusSpan.textContent = status;
        activeStaffTokenId = tokenId;
    }
}

document.getElementById('btn-call-next').addEventListener('click', async () => {
    if (!currentQueueId) return;
    hideMessage('staff-message');
    try {
        const res = await callNextToken(currentQueueId);
        setActiveStaffTokenUI(res.data.id, res.data.tokenNumber, res.data.status);
        showMessage('staff-message', 'Next token called!', 'success');
    } catch (err) {
        showMessage('staff-message', err.message, 'error');
    }
});

document.getElementById('btn-start').addEventListener('click', async () => {
    hideMessage('staff-message');
    if (!activeStaffTokenId) return showMessage('staff-message', 'You must call a token first!', 'error');
    try {
        const res = await startTokenService(activeStaffTokenId);
        setActiveStaffTokenUI(res.data.id, res.data.tokenNumber, res.data.status);
        showMessage('staff-message', 'Service started.', 'success');
    } catch (err) {
        showMessage('staff-message', err.message, 'error');
    }
});

document.getElementById('btn-skip').addEventListener('click', async () => {
    hideMessage('staff-message');
    if (!activeStaffTokenId) return showMessage('staff-message', 'No active token.', 'error');
    try {
        await skipToken(activeStaffTokenId);
        setActiveStaffTokenUI(null);
        showMessage('staff-message', 'Token skipped.', 'success');
    } catch (err) {
        showMessage('staff-message', err.message, 'error');
    }
});

// Dropdown population
async function loadStaffDropdown() {
    if (!currentBusinessId) return;
    try {
        const response = await getStaffByBusiness(currentBusinessId);
        const staffList = response.data || [];
        const select = document.getElementById('staff-select');
        select.innerHTML = '<option value="">-- Select Active Staff --</option>';
        
        staffList.forEach(s => {
            if (s.active) {
                const opt = document.createElement('option');
                // EXACT option value required by backend (Staff Entity ID)
                opt.value = s.id; 
                opt.textContent = `Staff: ${s.id} | User ID: ${s.userId} | Biz: ${s.businessId}`;
                select.appendChild(opt);
            }
        });
    } catch (err) {
        console.error("Staff load error", err);
    }
}

// COMPLETE SERVICE LOGIC (DEBUGGED)
document.getElementById('completeForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    hideMessage('staff-message');
    if (!activeStaffTokenId) return showMessage('staff-message', 'No active token to complete!', 'error');
    
    const staffId = document.getElementById('staff-select').value;
    
    // Explicit Debug Logging as requested
    const finalUrl = `${API_BASE_URL}/token/${activeStaffTokenId}/complete/${staffId}`;
    console.log("--- COMPLETE SERVICE DEBUG INFO ---");
    console.log({
        queueId: currentQueueId,
        businessId: currentBusinessId,
        tokenId: activeStaffTokenId,
        selectedStaffValue_StaffEntityId: staffId
    });
    console.log("Complete Service URL:", finalUrl);
    console.log("-----------------------------------");
    
    try {
        // We call the API endpoint exactly as mapped: POST /token/{tokenId}/complete/{staffId}
        const res = await completeTokenService(activeStaffTokenId, staffId);
        setActiveStaffTokenUI(null);
        showMessage('staff-message', 'Service completed successfully! Saved to records.', 'success');
    } catch (err) {
        // If the backend throws "staff is doesnot belong to this business", it will show here!
        showMessage('staff-message', err.message, 'error');
    }
});
