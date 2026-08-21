// We use the API Gateway on port 8765 to route our requests to the microservices.
// The config and api are in the same scope since they are loaded via script tags.

// --- User APIs ---

async function getUsers() {
    try {
        const response = await fetch(`${API_BASE_URL}/user`);
        if (!response.ok) throw new Error(`Failed to load users. Status: ${response.status}`);
        return await response.json();
    } catch (error) {
        console.error("API Error (getUsers):", error);
        throw error;
    }
}

async function registerUser(userData) {
    try {
        const response = await fetch(`${API_BASE_URL}/user/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(userData) 
        });
        const data = await response.json();
        if (!response.ok) {
            throw new Error(data.message || data.error || "Unknown backend error occurred");
        }
        return data;
    } catch (error) {
        console.error("API Error (registerUser):", error);
        throw error;
    }
}

// --- Business APIs ---

async function getBusinesses() {
    try {
        const response = await fetch(`${API_BASE_URL}/business`);
        if (!response.ok) throw new Error(`Failed to load businesses`);
        return await response.json();
    } catch (error) {
        throw error;
    }
}

async function getBusinessById(id) {
    try {
        const response = await fetch(`${API_BASE_URL}/business/get/${id}`);
        if (!response.ok) throw new Error(`Failed to load business ${id}`);
        return await response.json();
    } catch (error) {
        throw error;
    }
}

async function registerBusiness(businessData) {
    try {
        const response = await fetch(`${API_BASE_URL}/business/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(businessData)
        });
        const data = await response.json();
        if (!response.ok) throw new Error(data.message || data.error || "Unknown backend error");
        return data;
    } catch (error) {
        throw error;
    }
}

async function updateBusiness(id, businessData) {
    try {
        const response = await fetch(`${API_BASE_URL}/business/${id}`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(businessData)
        });
        const data = await response.json();
        if (!response.ok) throw new Error(data.message || data.error || "Update failed");
        return data;
    } catch (error) {
        throw error;
    }
}

// --- Staff APIs ---

async function getStaffByBusiness(businessId) {
    try {
        const response = await fetch(`${API_BASE_URL}/staff/business/${businessId}/staff`);
        if (!response.ok) {
             const data = await response.json().catch(() => null);
             if (response.status === 404 || (data && data.message && data.message.includes("No Staff"))) {
                 return { data: [] }; 
             }
             throw new Error(`Failed to load staff`);
        }
        return await response.json();
    } catch (error) {
        return { data: [] };
    }
}

async function addStaff(businessId, userId) {
    try {
        const response = await fetch(`${API_BASE_URL}/staff/business/${businessId}/${userId}`, {
            method: 'POST'
        });
        const data = await response.json();
        if (!response.ok) throw new Error(data.message || data.error || "Failed to add staff");
        return data;
    } catch (error) {
        throw error;
    }
}

async function toggleStaffStatus(staffId) {
    try {
        const response = await fetch(`${API_BASE_URL}/staff/status/${staffId}`, {
            method: 'PATCH'
        });
        const data = await response.json();
        if (!response.ok) throw new Error(data.message || data.error || "Failed to toggle status");
        return data;
    } catch (error) {
        throw error;
    }
}

// --- Queue APIs ---

async function getQueues() {
    try {
        const response = await fetch(`${API_BASE_URL}/queue`);
        if (!response.ok) throw new Error(`Failed to load queues`);
        return await response.json();
    } catch (error) {
        throw error;
    }
}

async function getQueueById(id) {
    try {
        const response = await fetch(`${API_BASE_URL}/queue/${id}`);
        if (!response.ok) throw new Error(`Failed to load queue ${id}`);
        return await response.json();
    } catch (error) {
        throw error;
    }
}

async function createQueue(queueData) {
    try {
        const response = await fetch(`${API_BASE_URL}/queue`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(queueData)
        });
        const data = await response.json();
        if (!response.ok) throw new Error(data.message || data.error || "Failed to create queue");
        return data;
    } catch (error) {
        throw error;
    }
}

async function updateQueue(id, queueData) {
    try {
        const response = await fetch(`${API_BASE_URL}/queue/${id}`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(queueData)
        });
        const data = await response.json();
        if (!response.ok) throw new Error(data.message || data.error || "Update failed");
        return data;
    } catch (error) {
        throw error;
    }
}

async function updateQueueStatus(id, newStatus) {
    try {
        const response = await fetch(`${API_BASE_URL}/queue/status/${id}/${newStatus}`, {
            method: 'PATCH'
        });
        const data = await response.json();
        if (!response.ok) throw new Error(data.message || data.error || "Failed to update status");
        return data;
    } catch (error) {
        throw error;
    }
}

// --- Queue Token APIs ---

async function joinQueue(queueId, userId) {
    try {
        const response = await fetch(`${API_BASE_URL}/token/${queueId}/join/${userId}`, {
            method: 'POST'
        });
        const data = await response.json();
        if (!response.ok) throw new Error(data.message || data.error || "Failed to join queue");
        return data;
    } catch (error) {
        throw error;
    }
}

async function getTokenStatus(tokenId) {
    try {
        const response = await fetch(`${API_BASE_URL}/token/${tokenId}`);
        const data = await response.json();
        if (!response.ok) throw new Error(data.message || data.error || "Failed to get token status");
        return data;
    } catch (error) {
        throw error;
    }
}

async function callNextToken(queueId) {
    try {
        const response = await fetch(`${API_BASE_URL}/token/${queueId}/next`, {
            method: 'POST'
        });
        const data = await response.json();
        if (!response.ok) throw new Error(data.message || data.error || "Failed to call next");
        return data;
    } catch (error) {
        throw error;
    }
}

async function startTokenService(tokenId) {
    try {
        const response = await fetch(`${API_BASE_URL}/token/${tokenId}/start`, {
            method: 'POST'
        });
        const data = await response.json();
        if (!response.ok) throw new Error(data.message || data.error || "Failed to start service");
        return data;
    } catch (error) {
        throw error;
    }
}

async function completeTokenService(tokenId, staffId) {
    try {
        const response = await fetch(`${API_BASE_URL}/token/${tokenId}/complete/${staffId}`, {
            method: 'POST'
        });
        const data = await response.json();
        if (!response.ok) throw new Error(data.message || data.error || "Failed to complete service");
        return data;
    } catch (error) {
        throw error;
    }
}

async function cancelToken(tokenId) {
    try {
        const response = await fetch(`${API_BASE_URL}/token/${tokenId}/cancel`, {
            method: 'PATCH'
        });
        const data = await response.json();
        if (!response.ok) throw new Error(data.message || data.error || "Failed to cancel token");
        return data;
    } catch (error) {
        throw error;
    }
}

async function skipToken(tokenId) {
    try {
        const response = await fetch(`${API_BASE_URL}/token/${tokenId}/skip`, {
            method: 'POST'
        });
        const data = await response.json();
        if (!response.ok) throw new Error(data.message || data.error || "Failed to skip token");
        return data;
    } catch (error) {
        throw error;
    }
}

// --- Service Record (Analytics) APIs ---

async function getQueueAnalytics(queueId) {
    try {
        const response = await fetch(`${API_BASE_URL}/serviceRecords/queue/analytics/${queueId}`);
        const data = await response.json();
        if (!response.ok) throw new Error(data.message || data.error || "Failed to load analytics");
        return data;
    } catch (error) {
        throw error;
    }
}

async function getQueueRecords(queueId) {
    try {
        const response = await fetch(`${API_BASE_URL}/serviceRecords/queue/${queueId}`);
        if (!response.ok) {
             const data = await response.json().catch(() => null);
             if (response.status === 404 || (data && data.message && data.message.includes("No Service Records"))) {
                 return { data: [] }; 
             }
             throw new Error(`Failed to load records`);
        }
        return await response.json();
    } catch (error) {
        return { data: [] };
    }
}
