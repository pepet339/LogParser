package com.logparser.parser.processing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logparser.parser.entity.LogEntry;
import com.logparser.parser.entity.LogEvent;
import com.logparser.parser.util.LogEntryHelper;

public class LogParserWorker implements Runnable{

	static Logger logger = LoggerFactory.getLogger(LogParserWorker.class);
	
	private List<String> dataLines;
	private int lineNumberToStart;
	private int lineNumberToEnd;
	private ConcurrentHashMap<Integer, List<LogEntry>> threadUnMatchedLogEntries;
	private ConcurrentHashMap<Integer, List<LogEvent>> threadEvents;
	private Map<String,LogEntry> logEntries = new HashMap<String,LogEntry>();
	private List<LogEvent> events = new ArrayList<LogEvent>();
	private Integer threadId;

	LogParserWorker(Integer threadId, List<String> dataLines, int lineIndexToStart, int lineNumberToProcess, ConcurrentHashMap<Integer, List<LogEntry>> threadUnMatchedLogEntries, ConcurrentHashMap<Integer, List<LogEvent>> threadEvents){
		this.threadId = threadId;
		this.dataLines = dataLines;
		this.lineNumberToStart = lineIndexToStart;
		this.lineNumberToEnd = lineIndexToStart+lineNumberToProcess;
		this.threadUnMatchedLogEntries = threadUnMatchedLogEntries;
		this.threadEvents = threadEvents;
	}
	
	@Override
	public void run() {
		ObjectMapper mapper = new ObjectMapper();
		//read my part of data 
		for(int i = lineNumberToStart; i <= lineNumberToEnd && dataLines.size() > i; i ++) {
			String line = dataLines.get(i);
			LogEntry logEntry= null;
			try {
				logEntry = mapper.readValue(line, LogEntry.class);
				LogEntryHelper.TryMatchLogEntries(logEntry, logEntries, events);
			} catch (IOException e) {
				logger.warn("Could not deserialize log line:"+line, e);
			}
		}
		threadUnMatchedLogEntries.put(threadId, new ArrayList<LogEntry>(logEntries.values()));
		threadEvents.put(threadId,events);
	}
}
