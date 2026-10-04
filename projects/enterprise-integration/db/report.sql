SELECT * FROM partner_report ORDER BY code;
SELECT status, COUNT(*) AS messages FROM messages GROUP BY status;
SELECT message_id, status, attempts, error_code FROM messages ORDER BY received_at DESC;
