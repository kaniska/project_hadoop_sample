BATTING1 = load 'hdfs://localhost:8020/user/train/data/baseball/Batting.csv' using PigStorage(',');
RUNS = foreach BATTING1 generate $0 as playerID, $1 as year, $8 as runs;
GRP_DATA = group RUNS by (year);
MAX_RUNS = foreach GRP_DATA generate group as grp, MAX(RUNS.runs) as max_runs;
JOIN_MAX_RUNS = join MAX_RUNS by ($0, max_runs), RUNS by (year, runs);
FINAL_OUTPUT = foreach JOIN_MAX_RUNS generate $0 as year, $2 as playerID, $1 as runs;
STORE FINAL_OUTPUT into '$outputDir';