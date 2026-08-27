/**
 * customer.js
 */

let currentUser = null;
let currentQueueId = null;

document.addEventListener('DOMContentLoaded', () => {
    currentUser = protectRoute('CUSTOMER');
    if (!currentUser) return;

    document.getElementById('user-info').textContent = `${currentUser.name} (ID: ${currentUser.id})`;
    setupLogoutButton();

    loadQueues();
});

async function loadQueues() {
    try {
        const res = await getQueues();
        const queues = res.data || [];
        const tbody = document.getElementById('queue-table-body');
        tbody.innerHTML = '';
        document.getElementById('queue-list-loading').style.display = 'none';

        if (queues.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4">No queues found.</td></tr>';
            return;
        }

        queues.forEach(q => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${q.id}</td>
                <td>${q.name}</td>
                <td>${q.status}</td>
                <td><button class="btn btn-secondary btn-sm" onclick="selectQueue(${q.id})">View</button></td>
            `;
            tbody.appendChild(tr);
        });
    } catch (err) {
        showMessage('main-message', err.message, 'error');
    }
}

async function selectQueue(queueId) {
    currentQueueId = queueId;
    hideMessage('main-message');
    document.getElementById('queue-details-placeholder').classList.add('hidden');
    document.getElementById('queue-details-content').classList.remove('hidden');

    try {
        const res = await getQueueById(queueId);
        const q = res.data;

        document.getElementById('q-name').textContent = q.name;
        document.getElementById('q-biz-id').textContent = q.businessId;
        document.getElementById('q-cap').textContent = `${q.maxCapacity} (${q.capacityType})`;
        document.getElementById('q-avg-time').textContent = (q.averageServiceTimeMinutes || q.defaultServiceTimeMinutes).toFixed(1);
        
        const badge = document.getElementById('q-status');
        badge.textContent = q.status;
        badge.className = 'status-badge'; 
        if (q.status === 'OPEN') badge.classList.add('status-open');
        else if (q.status === 'PAUSED') badge.classList.add('status-paused');
        else if (q.status === 'CLOSED') badge.classList.add('status-closed');

        checkMyTokenStatus();

    } catch (err) {
        showMessage('main-message', err.message, 'error');
    }
}

function getTokenStorageKey() {
    return `my_token_queue_${currentQueueId}`;
}

document.getElementById('btn-join').addEventListener('click', async () => {
    try {
        const res = await joinQueue(currentQueueId, currentUser.id);
        const tokenData = res.data;
        localStorage.setItem(getTokenStorageKey(), JSON.stringify(tokenData));
        showMessage('main-message', 'Successfully joined queue!', 'success');
        checkMyTokenStatus();
    } catch (err) {
        showMessage('main-message', err.message, 'error');
    }
});

async function checkMyTokenStatus() {
    if (!currentQueueId) return;
    
    const tokenStr = localStorage.getItem(getTokenStorageKey());
    const joinSec = document.getElementById('join-section');
    const tokenSec = document.getElementById('token-section');

    if (!tokenStr) {
        joinSec.classList.remove('hidden');
        tokenSec.classList.add('hidden');
        return;
    }

    joinSec.classList.add('hidden');
    tokenSec.classList.remove('hidden');

    const tokenObj = JSON.parse(tokenStr);

    try {
        const res = await getTokenStatus(tokenObj.id);
        const st = res.data;

        document.getElementById('my-token-num').textContent = st.tokenNumber;
        document.getElementById('my-token-status').textContent = st.status;
        document.getElementById('my-people-ahead').textContent = st.peopleAhead || 0;
        document.getElementById('my-est-wait').textContent = st.estimatedWaitingMinutes || 0;

        const cancelBtn = document.getElementById('btn-cancel-token');
        if (st.status === 'WAITING') {
            cancelBtn.style.display = 'inline-block';
        } else {
            cancelBtn.style.display = 'none';
        }
    } catch (err) {
        if (err.message.toLowerCase().includes("completed") || 
            err.message.toLowerCase().includes("cancelled") || 
            err.message.toLowerCase().includes("skipped")) {
            
            showMessage('main-message', `Your token finished: ${err.message}`, 'success');
            localStorage.removeItem(getTokenStorageKey());
            checkMyTokenStatus(); 
        } else {
            document.getElementById('my-token-status').textContent = "Unknown / Error";
        }
    }
}

document.getElementById('btn-refresh-token').addEventListener('click', checkMyTokenStatus);

document.getElementById('btn-cancel-token').addEventListener('click', async () => {
    const tokenStr = localStorage.getItem(getTokenStorageKey());
    if (!tokenStr) return;
    const tokenObj = JSON.parse(tokenStr);
    
    try {
        await cancelToken(tokenObj.id);
        localStorage.removeItem(getTokenStorageKey());
        showMessage('main-message', 'Token cancelled successfully.', 'success');
        checkMyTokenStatus();
    } catch (err) {
        showMessage('main-message', err.message, 'error');
    }
});
