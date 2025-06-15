$(document).ready(function() {
    // Load paginator into the container
    $('#paginator-container').load('../paginator/paginator.html', function() {
        // Set the active button based on the current page
        const currentPage = getCurrentPage();
        $(`.paginator-button[data-page="${currentPage}"]`).addClass('active');
        
        // Handle paginator button clicks
        $('.paginator-button').click(function() {
            const page = $(this).data('page');
            
            // Don't navigate if we're already on this page
            if (page === currentPage) return;
            
            // Handle navigation based on which button was clicked
            switch(page) {
                case 'chat':
                    window.location.href = '../mainscreen/main-screen.html';
                    break;
                case 'friends':
                    window.location.href = '../friend-management/friend-management.html';
                    break;
                case 'setting':
                    window.location.href = '../setting/setting.html';
                    break;
            }
        });
    });
    
    function getCurrentPage() {
        const path = window.location.pathname;
        
        if (path.includes('friend-management')) {
            return 'friends';
        } else if (path.includes('setting')) {
            return 'setting';
        } else {
            return 'chat'; // Default to chat for main screen
        }
    }
});
