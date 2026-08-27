/**
 * Common utilities for the frontend
 */

// Displays a success or error message on the page.
function showMessage(elementId, message, type) {
    const msgElement = document.getElementById(elementId);
    if (!msgElement) return;

    msgElement.textContent = message;
    msgElement.style.display = 'block';
    
    if (type === 'success') {
        msgElement.className = 'message success';
    } else if (type === 'error') {
        msgElement.className = 'message error';
    }
}

// Hides a message block
function hideMessage(elementId) {
    const msgElement = document.getElementById(elementId);
    if (msgElement) {
        msgElement.style.display = 'none';
    }
}
