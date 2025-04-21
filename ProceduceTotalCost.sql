CREATE PROCEDURE sp_calculate_total_cost
    @session_id INT
AS
BEGIN
    SET NOCOUNT ON;

UPDATE s
SET s.total_cost =
        CASE
            WHEN c.type = 'NORMAL' THEN ROUND(s.total_seconds_used * (p.price_per_hour / 3600.0), 0)
            WHEN c.type = 'VIP' THEN ROUND(s.total_seconds_used * (p.price_per_hour / 3600.0), 0)
            ELSE 0
            END
    FROM sessions s
    INNER JOIN computers c ON s.computer_id = c.computer_id
    INNER JOIN prices p ON c.type = p.type
WHERE s.session_id = @session_id
  AND s.status = 'ENDED'
  AND s.total_seconds_used IS NOT NULL;
END;