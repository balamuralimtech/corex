--  ############# PROCEDURE UpdateQuotationStatus ################

DELIMITER //
CREATE PROCEDURE UpdateQuotationStatus()
BEGIN
    -- Update the quotation status to 6 if the validity_date is one day less than today's date
    -- and the current quotation_status is in (1, 2, 3, 4, 5)
    UPDATE quotation
    SET quotation_status = 6
    WHERE 
    quotation_id > 0 
        AND validity_date < CURDATE() -- Check if the validity_date has crossed today
      AND quotation_status IN (1, 2, 3, 4, 5);
END //
DELIMITER ;

-- ############# EVENT RUNUPDATEQUOTATIONSTATUS ################
DELIMITER //

CREATE EVENT IF NOT EXISTS RunUpdateQuotationStatus
ON SCHEDULE EVERY 1 DAY
STARTS CURRENT_DATE + INTERVAL 0 HOUR -- Starts today at 12:00 AM
DO
BEGIN
    CALL UpdateQuotationStatus();
END //

DELIMITER ;


SHOW PROCEDURE STATUS WHERE Name = 'UpdateQuotationStatus';


CALL UpdateQuotationStatus();

SHOW VARIABLES LIKE 'event_scheduler';






