drop table temp_batting;
create table if not exists temp_batting (col_value STRING);
LOAD DATA LOCAL INPATH '${hiveconf:inputFile}' OVERWRITE INTO TABLE temp_batting;
drop table batting;
create table if not exists batting (player_id STRING, year INT, runs INT);
INSERT OVERWRITE TABLE batting SELECT regexp_extract(col_value, '^(?:([^,]*)\,?){1}', 1) player_id, regexp_extract(col_value, '^(?:([^,]*)\,?){2}', 1) year, regexp_extract(col_value, '^(?:([^,]*)\,?){9}', 1) run FROM temp_batting;

   


