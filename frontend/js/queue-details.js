/**
 * Logic for queue-details.html
 */

const urlParams = new URLSearchParams(window.location.search);
const queueId = urlParams.get('id');

let currentQueueData = null;
// We store the tokenId in localStorage so it survives page reloads for the user!
const myTokenKey = `my_token_for_queue_${queueId}`;

document.addEventListener('DOMContentLoaded', async () => {
    if (!queueId) {
        showMessage('main-message', 'No Queue ID provided in URL!', 'error');
        return;
    }
    await refreshAllData();
});

async function refreshAllData() {
    try {
        await loadQueueDetails();
        await loadAnalytics();
        await loadServiceRecords();
        await loadStaffDropdown(); // Need businessId from queue details
        checkMyTokenStatus();
        document.getElementById('loading-msg').classList.add('hidden');
        document.getElementById('dashboard-content').classList.remove('hidden');
    } catch (e) {
        // Errors handled inside functions
    }
}

// --- 1. Load Core Queue Details ---
async function loadQueueDetails() {
    try {
        const res = await getQueueById(queueId);
        currentQueueData = res.data;
        
        // Update UI headers
        document.getElementById('q-name').textContent = currentQueueData.name;
        document.getElementById('q-biz-id').textContent = currentQueueData.businessId;
        document.getElementById('q-cap').textContent = `${currentQueueData.maxCapacity} (${currentQueueData.capacityType})`;
        document.getElementById('q-avg-time').textContent = (currentQueueData.averageServiceTimeMinutes || currentQueueData.defaultServiceTimeMinutes).toFixed(1);
        
        // Status Badge
        const badge = document.getElementById('q-status-badge');
        badge.textContent = currentQueueData.status;
        badge.className = 'status-badge'; // reset
        if (currentQueueData.status === 'OPEN') badge.classList.add('status-open');
        else if (currentQueueData.status === 'PAUSED') badge.classList.add('status-paused');
        else if (currentQueueData.status === 'CLOSED') badge.classList.add('status-closed');

        // Populate update forms
        document.getElementById('upd-name').value = currentQueueData.name;
        document.getElementById('upd-maxCap').value = currentQueueData.maxCapacity;
        document.getElementById('upd-defTime').value = currentQueueData.defaultServiceTimeMinutes;
        
    } catch (err) {
        showMessage('main-message', `Failed to load queue details: ${err.message}`, 'error');
        throw err;
    }
}

// --- 2. Update Queue Logic ---
document.getElementById('updateQueueForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    hideMessage('manage-message');
    const data = {
        name: document.getElementById('upd-name').value,
        maxCapacity: parseInt(document.getElementById('upd-maxCap').value),
        defaultServiceTimeMinutes: parseInt(document.getElementById('upd-defTime').value)
    };
    try {
        await updateQueue(queueId, data);
        showMessage('manage-message', 'Queue info updated!', 'success');
        refreshAllData();
    } catch (err) {
        showMessage('manage-message', err.message, 'error');
    }
});

document.getElementById('statusForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    hideMessage('manage-message');
    const newStatus = document.getElementById('upd-status').value;
    try {
        await updateQueueStatus(queueId, newStatus);
        showMessage('manage-message', `Status changed to ${newStatus}`, 'success');
        refreshAllData();
    } catch (err) {
        showMessage('manage-message', err.message, 'error');
    }
});

// --- 3. Customer Token Lifecycle ---
const sessionUserStr = localStorage.getItem('demoSessionUser');
let sessionUser = null;
if (sessionUserStr) sessionUser = JSON.parse(sessionUserStr);

document.getElementById('btn-join').addEventListener('click', async () => {
    if (!sessionUser) {
        showMessage('cust-message', 'Please select a demo user on the main page first.', 'error');
        return;
    }
    hideMessage('cust-message');
    try {
        const res = await joinQueue(queueId, sessionUser.id);
        const tokenData = res.data;
        // Save to local storage!
        localStorage.setItem(myTokenKey, JSON.stringify(tokenData));
        showMessage('cust-message', 'Successfully joined queue!', 'success');
        checkMyTokenStatus();
    } catch (err) {
        showMessage('cust-message', err.message, 'error');
    }
});

async function checkMyTokenStatus() {
    const tokenStr = localStorage.getItem(myTokenKey);
    const joinSec = document.getElementById('join-section');
    const myTokSec = document.getElementById('my-token-section');
    
    if (!tokenStr) {
        joinSec.classList.remove('hidden');
        myTokSec.classList.add('hidden');
        return;
    }

    const tokenObj = JSON.parse(tokenStr);
    joinSec.classList.add('hidden');
    myTokSec.classList.remove('hidden');
    
    try {
        // The backend GET /token/{tokenId} returns QueueStatusResponse
        const res = await getTokenStatus(tokenObj.id);
        const statusData = res.data;
        
        document.getElementById('my-token-num').textContent = statusData.tokenNumber;
        document.getElementById('my-token-status').textContent = statusData.status;
        document.getElementById('my-people-ahead').textContent = statusData.peopleAhead || 0;
        document.getElementById('my-est-wait').textContent = statusData.estimatedWaitingMinutes || 0;
        
        // Only allow cancel if WAITING
        const cancelBtn = document.getElementById('btn-cancel-token');
        if (statusData.status === 'WAITING') {
            cancelBtn.style.display = 'inline-block';
        } else {
            cancelBtn.style.display = 'none';
        }

    } catch (err) {
        // If the backend throws an exception (e.g. COMPLETED, CANCELLED), 
        // it means the token is done. We remove it from local storage.
        if (err.message.toLowerCase().includes("completed") || 
            err.message.toLowerCase().includes("cancelled") || 
            err.message.toLowerCase().includes("skipped")) {
            
            showMessage('cust-message', `Your token finished: ${err.message}`, 'success');
            localStorage.removeItem(myTokenKey);
            checkMyTokenStatus(); // Reset UI
            refreshAllData(); // refresh analytics
        } else {
            document.getElementById('my-token-status').textContent = "Status Unknown / Error";
        }
    }
}

document.getElementById('btn-refresh-token').addEventListener('click', checkMyTokenStatus);

document.getElementById('btn-cancel-token').addEventListener('click', async () => {
    const tokenStr = localStorage.getItem(myTokenKey);
    if (!tokenStr) return;
    const tokenObj = JSON.parse(tokenStr);
    try {
        await cancelToken(tokenObj.id);
        localStorage.removeItem(myTokenKey);
        showMessage('cust-message', 'Token cancelled.', 'success');
        checkMyTokenStatus();
        refreshAllData();
    } catch (err) {
        showMessage('cust-message', err.message, 'error');
    }
});

// --- 4. Staff Control Panel ---
// Because we can't GET all tokens, staff relies on the ID returned by Call Next.
let activeStaffTokenId = null;

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
    hideMessage('staff-message');
    try {
        const res = await callNextToken(queueId);
        setActiveStaffTokenUI(res.data.id, res.data.tokenNumber, res.data.status);
        showMessage('staff-message', 'Next token called!', 'success');
    } catch (err) {
        showMessage('staff-message', err.message, 'error');
    }
});

document.getElementById('btn-start').addEventListener('click', async () => {
    hideMessage('staff-message');
    if (!activeStaffTokenId) {
        showMessage('staff-message', 'You must call a token first!', 'error');
        return;
    }
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
        refreshAllData();
    } catch (err) {
        showMessage('staff-message', err.message, 'error');
    }
});

document.getElementById('completeForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    hideMessage('staff-message');
    if (!activeStaffTokenId) return showMessage('staff-message', 'No active token to complete!', 'error');
    
    const staffId = document.getElementById('staff-select').value;
    try {
        await completeTokenService(activeStaffTokenId, staffId);
        setActiveStaffTokenUI(null);
        showMessage('staff-message', 'Service completed successfully! Saved to records.', 'success');
        refreshAllData(); // refresh analytics and records
    } catch (err) {
        showMessage('staff-message', err.message, 'error');
    }
});

async function loadStaffDropdown() {
    if (!currentQueueData) return;
    try {
        const response = await getStaffByBusiness(currentQueueData.businessId);
        const staffList = response.data || [];
        const select = document.getElementById('staff-select');
        select.innerHTML = '<option value="">-- Select Active Staff --</option>';
        
        staffList.forEach(s => {
            // Note: backend only returns active staff anyway, but good to check
            if (s.active) {
                const opt = document.createElement('option');
                opt.value = s.id;
                opt.textContent = `Staff ID: ${s.id} (User: ${s.userId})`;
                select.appendChild(opt);
            }
        });
    } catch (err) {
        console.error("Staff load error", err);
    }
}

// --- 5. Analytics and Records ---
async function loadAnalytics() {
    try {
        const res = await getQueueAnalytics(queueId);
        const a = res.data;
        document.getElementById('a-served').textContent = a.totalCustomersServed || 0;
        document.getElementById('a-missed').textContent = (a.cancelled || 0) + (a.skipped || 0);
        document.getElementById('a-wait').textContent = `${(a.averageWaitingTime || 0).toFixed(1)}m`;
    } catch (err) {
        console.error("Analytics load error", err);
    }
}

async function loadServiceRecords() {
    document.getElementById('records-loading').style.display = 'block';
    try {
        const res = await getQueueRecords(queueId);
        const records = res.data || [];
        const tbody = document.getElementById('records-body');
        tbody.innerHTML = '';
        
        if (records.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4">No service records yet.</td></tr>';
        } else {
            // Show latest 10 records
            records.slice(-10).reverse().forEach(r => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${r.queueToken ? r.queueToken.tokenNumber : '?'}</td>
                    <td>${r.staffId}</td>
                    <td>${(r.waitingDurationMinutes || 0).toFixed(1)}</td>
                    <td>${(r.serviceDurationMinutes || 0).toFixed(1)}</td>
                `;
                tbody.appendChild(tr);
            });
        }
    } catch (err) {
        document.getElementById('records-body').innerHTML = '<tr><td colspan="4">Error loading records.</td></tr>';
    } finally {
        document.getElementById('records-loading').style.display = 'none';
    }
}
