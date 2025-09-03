/**
 * Loading utility functions
 */

// Show loading animation on a button
function showButtonLoading(buttonElement, loadingText = 'Processing...') {
    const $button = $(buttonElement);
    
    // Save original content
    $button.data('original-html', $button.html());
    $button.data('original-width', $button.outerWidth());
    
    // Set a min-width to prevent button from shrinking
    $button.css('min-width', $button.outerWidth() + 'px');
    
    // Replace content with loading spinner and text
    $button.html(`
        <span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
        <span class="ms-1">${loadingText}</span>
    `);
    
    // Disable the button
    $button.prop('disabled', true);
}

// Restore button to original state
function hideButtonLoading(buttonElement) {
    const $button = $(buttonElement);
    
    // Restore original content
    $button.html($button.data('original-html'));
    
    // Remove min-width
    $button.css('min-width', '');
    
    // Enable the button
    $button.prop('disabled', false);
}

// Show loading overlay on an element
function showElementLoading(element, message = 'Loading...') {
    const $element = $(element);
    const position = $element.css('position');
    
    // If not positioned, add relative positioning
    if (position !== 'absolute' && position !== 'relative' && position !== 'fixed') {
        $element.css('position', 'relative');
    }
    
    // Create loading overlay
    const $overlay = $(`
        <div class="loading-overlay">
            <div class="loading-spinner"></div>
            <div class="loading-message">${message}</div>
        </div>
    `);
    
    // Add overlay to element
    $element.append($overlay);
}

// Remove loading overlay from an element
function hideElementLoading(element) {
    $(element).find('.loading-overlay').remove();
    
    // If we added relative positioning, we would need to remove it here
    // For simplicity, we're assuming the element's original positioning was intended
}

// Add CSS styles for loading elements
const loadingStyles = `
<style>
    /* Loading overlay */
    .loading-overlay {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background-color: rgba(255, 255, 255, 0.7);
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        z-index: 1000;
        border-radius: inherit;
    }
    
    .loading-spinner {
        width: 40px;
        height: 40px;
        border: 4px solid #f3f3f3;
        border-top: 4px solid #0084ff;
        border-radius: 50%;
        animation: spin 1s linear infinite;
    }
    
    .loading-message {
        margin-top: 10px;
        font-size: 14px;
        color: #333;
    }
    
    /* Spinner animation for button loading */
    .spinner-border {
        display: inline-block;
        width: 1rem;
        height: 1rem;
        vertical-align: text-bottom;
        border: 0.2em solid currentColor;
        border-right-color: transparent;
        border-radius: 50%;
        animation: spin 0.75s linear infinite;
    }
    
    @keyframes spin {
        0% { transform: rotate(0deg); }
        100% { transform: rotate(360deg); }
    }
</style>
`;

// Add the styles to the document head when the file is loaded
$(document).ready(function() {
    $('head').append(loadingStyles);
});
