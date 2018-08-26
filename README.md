# LogParser

*** Program Execute ***
1. Execute gradlew.bat bootjar
2. Start database
	2a) Go to db directory
	2b) Execute java -cp hsqldb-2.4.1.jar org.hsqldb.Server -database.0 mydb -dbname.0 parserInstance
	2c) Databse contains empty Event table
	2d) Connection details: url=jdbc:hsqldb:hsql://localhost/parserInstance user=sa password is empty 
3. Program run
	3a) Go to build\libs\ directory
	3b) Sample execution: java -jar parser-0.0.1-SNAPSHOT.jar ..\\..\\test_file\\mixed_logs.json
	File path with logs must be provided as program parameter. Sample file is in test_file directory, it contains 308 events

*** Program description ***
Program processes log file in multi threading way. The data is being read from file in chunks and that chunk is being processed by many threads, each thread handles its part of data. Config.CHUNK_LINE_NUMBER defines how many lines are in one chunk and Config.LINE_COUNT_PER_THREAD defines how many lines single thread is handling. While previous chunk of data is being processed by LogParserWorker thread next chunk of data is being read from file by DataReaderWorker thread. Memory consumption is equal to size of data in 2 consecutive chunks + data structures created during chunk processing. Big files can be processed.