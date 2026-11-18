/*
  删除不必要的表和数据
*/

--  删除以ai_名称开头的表
-- 先查询对应的表
SELECT table_name
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_type = 'BASE TABLE'
  AND table_name LIKE 'ai\_%';

-- 直接删除对应的表
SET SESSION group_concat_max_len = 1000000;
SELECT GROUP_CONCAT(
               CONCAT('`', table_name, '`') SEPARATOR ','
       )
INTO @tables
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_type = 'BASE TABLE'
  AND table_name LIKE 'ai\_%';

SET
@sql = IF(
        @tables IS NOT NULL,
        CONCAT('DROP TABLE IF EXISTS ', @tables),
        'SELECT ''没有找到 ai_ 开头的表'' AS result'
           );
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


-- 删除 CRM/BPM/ERP/AI 国际化
DELETE
FROM infra_i18n_key
WHERE message_key LIKE 'crm.%'
   OR message_key LIKE 'bpm.%'
   OR message_key LIKE 'erp.%';

DELETE
FROM infra_i18n_message
WHERE message_key LIKE 'crm.%'
   OR message_key LIKE 'bpm.%'
   OR message_key LIKE 'erp.%';

-- 删除非 system_/infra_ 的字典国际化
DELETE
FROM infra_i18n_key
WHERE message_key LIKE 'dict.%'
  AND message_key NOT LIKE 'dict.system\_%'
  AND message_key NOT LIKE 'dict.infra\_%'
  AND message_key NOT LIKE 'dict.bpm\_%'
  AND message_key NOT LIKE 'dict.erp\_%'
  AND message_key NOT LIKE 'dict.crm\_%';


DELETE
FROM infra_i18n_message
WHERE message_key LIKE 'dict.%'
  AND message_key NOT LIKE 'dict.system\_%'
  AND message_key NOT LIKE 'dict.infra\_%'
  AND message_key NOT LIKE 'dict.bpm\_%'
  AND message_key NOT LIKE 'dict.erp\_%'
  AND message_key NOT LIKE 'dict.crm\_%';

-- 删除非 system_/infra_ 的字典类型
DELETE
FROM system_dict_type
WHERE type NOT LIKE 'system\_%'
  AND type NOT LIKE 'infra\_%'
  AND type NOT LIKE 'bpm\_%'
  AND type NOT LIKE 'erp\_%'
  AND type NOT LIKE 'crm\_%';


-- 删除非 system_/infra_ 的字典数据
DELETE
FROM system_dict_data
WHERE dict_type NOT LIKE 'system\_%'
  AND dict_type NOT LIKE 'infra\_%'
  AND dict_type NOT LIKE 'bpm\_%'
  AND dict_type NOT LIKE 'erp\_%'
  AND dict_type NOT LIKE 'crm\_%';


DELIMITER
$$
CREATE PROCEDURE delete_deleted_data()
BEGIN
    DECLARE
done INT DEFAULT 0;
    DECLARE
v_table_name VARCHAR(255);

    DECLARE
cur CURSOR FOR
SELECT DISTINCT TABLE_NAME
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND COLUMN_NAME = 'deleted';

DECLARE
CONTINUE HANDLER FOR NOT FOUND SET done = 1;

OPEN cur;

read_loop
:
    LOOP
        FETCH cur INTO v_table_name;

        IF
done = 1 THEN
            LEAVE read_loop;
END IF;

        SET
@sql = CONCAT(
                'DELETE FROM `',
                REPLACE(v_table_name, '`', '``'),
                '` WHERE `deleted` = b''1'''
                   );

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
END LOOP;

CLOSE cur;
END$$
DELIMITER ;
CALL delete_deleted_data();
DROP PROCEDURE delete_deleted_data;
