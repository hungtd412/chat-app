/**
 * Extract error message from an API error response
 * @param {Object} xhr - The XHR object from the AJAX error callback
 * @param {string} defaultMessage - Default message to display if extraction fails
 * @returns {string} Extracted error message
 */
function extractErrorMessage(xhr, defaultMessage = 'An error occurred. Please try again.') {
    if (!xhr || !xhr.responseJSON) {
        return defaultMessage;
    }
    
    const response = xhr.responseJSON;
    
    // Handle standard error message format
    if (response.message) {
        return response.message;
    }
    
    // Handle error code format: { "code": 1041, "message": "Error message" }
    if (response.code && response.message) {
        return response.message;
    }
    
    // Handle array of errors format
    if (response.errors && Array.isArray(response.errors) && response.errors.length > 0) {
        return response.errors[0].message || defaultMessage;
    }
    
    return defaultMessage;
}

/**
 * Display an error message in the specified container
 * @param {string} containerId - ID of the container element
 * @param {string} message - Error message to display
 */
function displayErrorMessage(containerId, message) {
    const container = document.getElementById(containerId);
    if (container) {
        container.textContent = message;
        container.style.display = 'block';
    } else {
        console.error(`Error container with ID '${containerId}' not found`);
    }
}

/**
 * Handle AJAX errors and display appropriate messages
 * @param {Object} xhr - The XHR object from the AJAX error callback
 * @param {string} errorContainerId - ID of the container to display errors
 * @param {string} defaultMessage - Default message to display if extraction fails
 * @param {boolean} showPopup - Whether to also show a popup notification (default: false)
 */
function handleApiError(xhr, errorContainerId, defaultMessage = 'An error occurred. Please try again.', showPopup = false) {
    const errorMessage = extractErrorMessage(xhr, defaultMessage);
    
    if (errorContainerId) {
        displayErrorMessage(errorContainerId, errorMessage);
    }
    
    // Show popup if requested
    if (showPopup && typeof showErrorPopup === 'function') {
        showErrorPopup(errorMessage);
    } else if (!errorContainerId) {
        // Fallback to alert if no container is specified and no popup function available
        alert(errorMessage);
    }
    
    // Log the error to console for debugging
    console.error('API Error:', xhr.status, errorMessage, xhr);
}