/**
 * Display a success popup notification
 * @param {string} message - Success message to display
 * @param {number} duration - Duration in milliseconds to show the popup (default 3000ms)
 */
function showSuccessPopup(message, duration = 3000) {
    // Create popup element if it doesn't exist already
    let popup = document.getElementById('success-popup');
    
    if (!popup) {
        popup = document.createElement('div');
        popup.id = 'success-popup';
        
        // Add styles to the popup
        popup.style.position = 'fixed';
        popup.style.bottom = '20px';
        popup.style.right = '20px';
        popup.style.backgroundColor = '#2ecc71';
        popup.style.color = 'white';
        popup.style.padding = '12px 20px';
        popup.style.borderRadius = '4px';
        popup.style.boxShadow = '0 2px 10px rgba(0,0,0,0.2)';
        popup.style.zIndex = '10000';
        popup.style.opacity = '0';
        popup.style.transform = 'translateY(20px)';
        popup.style.transition = 'opacity 0.3s, transform 0.3s';
        popup.style.maxWidth = '300px';
        popup.style.wordWrap = 'break-word';
        popup.style.fontFamily = 'Segoe UI, Arial, sans-serif';
        
        // Add success icon
        const iconSpan = document.createElement('span');
        iconSpan.innerHTML = '✓ ';
        iconSpan.style.marginRight = '8px';
        iconSpan.style.fontWeight = 'bold';
        popup.appendChild(iconSpan);
        
        // Create text container
        const textSpan = document.createElement('span');
        textSpan.className = 'success-popup-message';
        popup.appendChild(textSpan);
        
        document.body.appendChild(popup);
    }
    
    // Set message text
    const textSpan = popup.querySelector('.success-popup-message');
    if (textSpan) {
        textSpan.textContent = message;
    }
    
    // Show the popup with animation
    setTimeout(() => {
        popup.style.opacity = '1';
        popup.style.transform = 'translateY(0)';
    }, 10);
    
    // Hide the popup after duration
    const hideTimeout = setTimeout(() => {
        popup.style.opacity = '0';
        popup.style.transform = 'translateY(20px)';
        
        // Remove the element after transition completes
        setTimeout(() => {
            if (popup.parentNode) {
                popup.parentNode.removeChild(popup);
            }
        }, 300);
    }, duration);
    
    // Allow clicking on popup to dismiss it early
    popup.addEventListener('click', () => {
        clearTimeout(hideTimeout);
        popup.style.opacity = '0';
        popup.style.transform = 'translateY(20px)';
        
        setTimeout(() => {
            if (popup.parentNode) {
                popup.parentNode.removeChild(popup);
            }
        }, 300);
    });
}

/**
 * Display an error popup notification
 * @param {string} message - Error message to display
 * @param {number} duration - Duration in milliseconds to show the popup (default 3000ms)
 */
function showErrorPopup(message, duration = 3000) {
    // Create popup element if it doesn't exist already
    let popup = document.getElementById('error-popup');
    
    if (!popup) {
        popup = document.createElement('div');
        popup.id = 'error-popup';
        
        // Add styles to the popup
        popup.style.position = 'fixed';
        popup.style.bottom = '20px';
        popup.style.right = '20px';
        popup.style.backgroundColor = '#e74c3c';
        popup.style.color = 'white';
        popup.style.padding = '12px 20px';
        popup.style.borderRadius = '4px';
        popup.style.boxShadow = '0 2px 10px rgba(0,0,0,0.2)';
        popup.style.zIndex = '10000';
        popup.style.opacity = '0';
        popup.style.transform = 'translateY(20px)';
        popup.style.transition = 'opacity 0.3s, transform 0.3s';
        popup.style.maxWidth = '300px';
        popup.style.wordWrap = 'break-word';
        popup.style.fontFamily = 'Segoe UI, Arial, sans-serif';
        
        // Add error icon
        const iconSpan = document.createElement('span');
        iconSpan.innerHTML = '✕ ';
        iconSpan.style.marginRight = '8px';
        iconSpan.style.fontWeight = 'bold';
        popup.appendChild(iconSpan);
        
        // Create text container
        const textSpan = document.createElement('span');
        textSpan.className = 'error-popup-message';
        popup.appendChild(textSpan);
        
        document.body.appendChild(popup);
    }
    
    // Set message text
    const textSpan = popup.querySelector('.error-popup-message');
    if (textSpan) {
        textSpan.textContent = message;
    }
    
    // Show the popup with animation
    setTimeout(() => {
        popup.style.opacity = '1';
        popup.style.transform = 'translateY(0)';
    }, 10);
    
    // Hide the popup after duration
    const hideTimeout = setTimeout(() => {
        popup.style.opacity = '0';
        popup.style.transform = 'translateY(20px)';
        
        // Remove the element after transition completes
        setTimeout(() => {
            if (popup.parentNode) {
                popup.parentNode.removeChild(popup);
            }
        }, 300);
    }, duration);
    
    // Allow clicking on popup to dismiss it early
    popup.addEventListener('click', () => {
        clearTimeout(hideTimeout);
        popup.style.opacity = '0';
        popup.style.transform = 'translateY(20px)';
        
        setTimeout(() => {
            if (popup.parentNode) {
                popup.parentNode.removeChild(popup);
            }
        }, 300);
    });
}
