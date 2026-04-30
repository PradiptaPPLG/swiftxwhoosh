<?php
$url = 'http://localhost/swift-api/get_user_bookings.php?user_id=42';
$content = file_get_contents($url);
echo $content;
?>
