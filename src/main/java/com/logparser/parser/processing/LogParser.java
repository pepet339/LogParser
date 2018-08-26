package com.logparser.parser.processing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logparser.parser.config.Config;
import com.logparser.parser.dao.ILogEventBatchDao;
import com.logparser.parser.entity.LogEntry;
import com.logparser.parser.entity.LogEvent;
import com.logparser.parser.util.LogEntryHelper;

public class LogParser {
	
	static Logger logger = LoggerFactory.getLogger(LogParser.class);
		
	private List<String> dataLines = new ArrayList<String>();
	private List<String> bufferDataLines = new ArrayList<String>();
	private ConcurrentHashMap<Integer,List<LogEntry>> threadUnMatchedLogEntries = new ConcurrentHashMap<Integer,List<LogEntry>>(); 
	private ConcurrentHashMap<Integer,List<LogEvent>> threadEvents = new ConcurrentHashMap<Integer, List<LogEvent>>(); 
	
	private String filePath;
	private ILogEventBatchDao logEventDao;
	private int totalLinesProcesses;
		
	public LogParser(ILogEventBatchDao logEventDao, String filePath) {
		this.logEventDao = logEventDao;
		this.filePath = filePath;
		this.totalLinesProcesses = 0;
	}
		
	public void execute() throws InterruptedException {
		logger.info("Start to process file:"+filePath);
		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))){
		
			//get first chunk of data
			Thread dataReader = new Thread(new DataReaderWorker(reader, bufferDataLines));
			dataReader.start();
			dataReader.join();
			//swap data
			dataLines = bufferDataLines;
			bufferDataLines = new ArrayList<String>();
						
			while(dataLines.size() == Config.CHUNK_LINE_NUMBER) {
				processChunk(true, reader);
				//swap data
				dataLines = bufferDataLines;
				bufferDataLines = new ArrayList<String>();
			}
			//read less then limit so we have all data, process only
			processChunk(false, reader);  
			logger.info("Successfully processed file:"+filePath);
			
			//print totalThreadUnMatchedLogEntries
		} catch (IOException e) {
			logger.error("Could not read from file:"+filePath);
		}	
		
	}
	
	private void processChunk(boolean shouldRead, BufferedReader reader) throws InterruptedException {
		//calculate how many thread do we need to handle this chunk of data
		int processingThreadNumber = (dataLines.size()/Config.LINE_COUNT_PER_THREAD) + (dataLines.size() % Config.LINE_COUNT_PER_THREAD == 0?0:1);
		List<Thread> threadList = new ArrayList<Thread>();
		//starting data processing threads
		logger.info("Starting "+processingThreadNumber+" proccesing threads to handle "+dataLines.size() +" data lines");
		for(int i=0; i < processingThreadNumber; i++) {
			int startLine = i * Config.LINE_COUNT_PER_THREAD;
			LogParserWorker worker = new LogParserWorker(i, dataLines, startLine, Config.LINE_COUNT_PER_THREAD-1,threadUnMatchedLogEntries,threadEvents);
			Thread workerThread =  new Thread(worker);
			workerThread.start();
			threadList.add(workerThread);
		}
		if(shouldRead) {
			//starting data reader thread
			Thread dataReader = new Thread(new DataReaderWorker(reader, bufferDataLines));
			dataReader.start();
			threadList.add(dataReader);
		}
		for (Thread thread : threadList) {
			//'master' thread wait for all -> processing and data read threads 
		    thread.join();
		}
		totalLinesProcesses += dataLines.size();
		logger.info("Number of lines already processed:"+totalLinesProcesses);
		//logs can be splited across threads' chunks, try to reconcile
		ProcessUnmatchedEntries();
		//save to db
		List<LogEvent> eventList = threadEvents.values().stream().flatMap(List::stream)
				.collect(Collectors.toList());
		logEventDao.storeEvents(eventList);
		//clear what is saved and processed
		threadEvents.clear();
		dataLines.clear();
	}

	private void ProcessUnmatchedEntries() {
		Collection<List<LogEntry>> totalLogEntriesUnmatchedLists = threadUnMatchedLogEntries.values();
		List<LogEntry> totalLogEntriesUnMatchList = 
				totalLogEntriesUnmatchedLists.stream()
			        .flatMap(List::stream)
			        .collect(Collectors.toList());
		Map<String,LogEntry> entries = new HashMap<String,LogEntry>();
		List<LogEvent> events = new ArrayList<LogEvent>();
		for(LogEntry entry : totalLogEntriesUnMatchList) {
			LogEntryHelper.TryMatchLogEntries(entry,entries,events);
		}
		threadUnMatchedLogEntries.clear();
		threadEvents.put(-1,events);
		//put unmatched logs for this chunk to map, there should be matching entries in next chunks
		threadUnMatchedLogEntries.put(-1, new ArrayList<LogEntry>(entries.values()));
	}
}
