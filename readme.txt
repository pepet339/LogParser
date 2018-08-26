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
	As a parameter file path with logs should be provided. Sample fikle is in test_file directory, it contains 	308 event

*** Program description ***