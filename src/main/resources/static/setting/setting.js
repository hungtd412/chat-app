$(document).ready(function() {
    // Check if user is logged in by verifying token in localStorage
    const token = localStorage.getItem('access_token');
    if (!token) {
        window.location.href = '../signin/signin.html';
        return;
    }
    
    // Load paginator into the container
    $('#paginator-container').load('../paginator/paginator.html', function() {
        // Set the active button based on the current page
        $(`.paginator-button[data-page="setting"]`).addClass('active');
        
        // Handle paginator button clicks
        $('.paginator-button').click(function() {
            const page = $(this).data('page');
            
            // Don't navigate if we're already on the setting page
            if (page === 'setting') return;
            
            // Handle navigation based on which button was clicked
            switch(page) {
                case 'chat':
                    window.location.href = '../mainscreen/main-screen.html';
                    break;
                case 'friends':
                    window.location.href = '../friend-management/friend-management.html';
                    break;
            }
        });
    });
    
    // Load tab contents
    $('#general-info').load('general-info.html');
    $('#avatar').load('avatar.html');
    $('#change-password').load('change-password.html');
    $('#change-email').load('change-email.html');
    
    // Tab navigation
    $('.tab-button').click(function() {
        const tabId = $(this).data('tab');
        
        // Update active tab button
        $('.tab-button').removeClass('active');
        $(this).addClass('active');
        
        // Show selected tab content
        $('.tab-content').removeClass('active');
        $(`#${tabId}`).addClass('active');
    });
    
    // Load user profile data - now shared across tabs
    loadUserProfile();
    
    // Function to load user profile data from the API
    function loadUserProfile() {
        $.ajax({
            url: 'http://localhost:9000/users/profile',
            type: 'GET',
            headers: {
                'Authorization': 'Bearer ' + token
            },
            success: function(response) {
                // Store profile data in localStorage for other components
                localStorage.setItem('user_profile', JSON.stringify(response.data));
                
                // Trigger custom event that tab components can listen for
                $(document).trigger('profileLoaded', [response.data]);
                
                // Optionally show a notification that profile loaded
                // showSuccessPopup('Profile loaded successfully');
            },
            error: function(xhr, status, error) {
                console.error('Error loading profile:', error);
                
                // Extract error message from the response
                const errorMessage = extractErrorMessage(xhr, 'Error loading profile data. Please try again later.');
                
                // Use popup instead of alert
                showErrorPopup(errorMessage);
            }
        });
    }
});