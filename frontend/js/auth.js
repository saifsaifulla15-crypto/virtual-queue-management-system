/**
 * auth.js
 * Handles user selection, saving to localStorage, and routing based on role.
 */

const SESSION_KEY = 'currentUser';

document.addEventListener('DOMContentLoaded', () => {
    // If we are on index.html, load users.
    if (window.location.pathname.endsWith('index.html') || window.location.pathname === '/' || window.location.pathname === '') {
        loadAuthUsers();
    }
});

async function loadAuthUsers() {
    try {
        const response = await getUsers();
        const users = response.data || [];
        const grid = document.getElementById('user-grid');
        grid.innerHTML = '';
        document.getElementById('users-loading').style.display = 'none';

        if (users.length === 0) {
            grid.innerHTML = '<p>No users found. Please register.</p>';
            return;
        }

        users.forEach(u => {
            const card = document.createElement('div');
            card.className = 'user-card';
            card.innerHTML = `
                <h3>${u.name}</h3>
                <p>${u.email}</p>
                <div class="role-badge role-${u.role}">${u.role}</div>
                <br>
                <button class="btn btn-select-user" data-user='${JSON.stringify(u)}'>Select User</button>
            `;
            grid.appendChild(card);
        });

        // Add event listeners to buttons
        document.querySelectorAll('.btn-select-user').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const userObj = JSON.parse(e.target.getAttribute('data-user'));
                loginAndRedirect(userObj);
            });
        });

    } catch (err) {
        document.getElementById('users-loading').style.display = 'none';
        showMessage('main-message', `Failed to load users: ${err.message}`, 'error');
    }
}

function loginAndRedirect(userObj) {
    localStorage.setItem(SESSION_KEY, JSON.stringify(userObj));
    redirectBasedOnRole(userObj.role);
}

function redirectBasedOnRole(role) {
    if (role === 'CUSTOMER') {
        window.location.href = 'customer.html';
    } else if (role === 'STAFF') {
        window.location.href = 'staff.html';
    } else if (role === 'OWNER') {
        window.location.href = 'owner.html';
    } else {
        alert('Unknown role: ' + role);
    }
}

function logout() {
    localStorage.removeItem(SESSION_KEY);
    window.location.href = 'index.html';
}

/**
 * Call this function at the top of protected pages (customer.html, owner.html, etc.)
 * to ensure the user is logged in and has the correct role.
 */
function protectRoute(requiredRole) {
    const userStr = localStorage.getItem(SESSION_KEY);
    if (!userStr) {
        window.location.href = 'index.html';
        return null;
    }
    
    const user = JSON.parse(userStr);
    if (user.role !== requiredRole) {
        redirectBasedOnRole(user.role);
        return null;
    }
    
    return user;
}

function setupLogoutButton() {
    const btn = document.getElementById('btn-logout');
    if (btn) {
        btn.addEventListener('click', logout);
    }
}
