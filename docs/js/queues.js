/**
 * Logic for queues.html
 */

document.addEventListener('DOMContentLoaded', () => {
    loadQueues();
    loadBusinessesForDropdown();
});

const queueTableBody = document.getElementById('queue-table-body');
const createPanel = document.getElementById('create-panel');

async function loadQueues() {
    try {
        document.getElementById('queue-list-loading').style.display = 'block';
        const response = await getQueues();
        const queues = response.data || [];
        
        queueTableBody.innerHTML = '';

        if (queues.length === 0) {
            queueTableBody.innerHTML = '<tr><td colspan="6">No queues found.</td></tr>';
        } else {
            queues.forEach(q => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${q.id}</td>
                    <td>${q.name}</td>
                    <td>${q.businessId}</td>
                    <td><strong>${q.status}</strong></td>
                    <td>${q.maxCapacity} (${q.capacityType})</td>
                    <td>
                        <a href="queue-details.html?id=${q.id}" class="btn btn-secondary">Enter Queue Dashboard</a>
                    </td>
                `;
                queueTableBody.appendChild(tr);
            });
        }
    } catch (error) {
        showMessage('main-message', `Error loading queues: ${error.message}`, 'error');
    } finally {
        document.getElementById('queue-list-loading').style.display = 'none';
    }
}

async function loadBusinessesForDropdown() {
    try {
        const response = await getBusinesses();
        const businesses = response.data || [];
        const select = document.getElementById('cq-business');
        
        select.innerHTML = '<option value="">-- Select Business --</option>';
        businesses.forEach(b => {
            const opt = document.createElement('option');
            opt.value = b.id;
            opt.textContent = `${b.name} (ID: ${b.id})`;
            select.appendChild(opt);
        });
    } catch (e) {
        console.error("Failed to load businesses", e);
    }
}

document.getElementById('show-create-btn').addEventListener('click', () => {
    createPanel.classList.remove('hidden');
});
document.getElementById('cancel-create-btn').addEventListener('click', () => {
    createPanel.classList.add('hidden');
});

document.getElementById('createForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    hideMessage('main-message');
    
    // Build request body according to backend requirements
    const newQueue = {
        name: document.getElementById('cq-name').value,
        businessId: parseInt(document.getElementById('cq-business').value),
        maxCapacity: parseInt(document.getElementById('cq-maxCap').value),
        defaultServiceTimeMinutes: parseInt(document.getElementById('cq-defTime').value),
        capacityType: document.getElementById('cq-capacityType').value,
        status: document.getElementById('cq-status').value
    };
    
    const desc = document.getElementById('cq-desc').value;
    if(desc) newQueue.description = desc;

    try {
        await createQueue(newQueue);
        showMessage('main-message', 'Queue created successfully!', 'success');
        createPanel.classList.add('hidden');
        document.getElementById('createForm').reset();
        loadQueues(); // Refresh list dynamically!
    } catch (error) {
        showMessage('main-message', error.message, 'error');
    }
});
