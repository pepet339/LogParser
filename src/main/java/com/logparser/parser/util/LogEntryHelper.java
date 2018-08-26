package com.logparser.parser.util;

import java.util.List;
import java.util.Map;

import com.logparser.parser.config.Config;
import com.logparser.parser.entity.LogEntry;
import com.logparser.parser.entity.LogEvent;

public class LogEntryHelper {
	
	public static void TryMatchLogEntries(LogEntry logEntry, Map<String,LogEntry> entriesMap, List<LogEvent> eventList) {
		
		LogEntry existingEntry = entriesMap.get(logEntry.getId());
		if(existingEntry==null) {
			// no match by id, adding to map
			entriesMap.put(logEntry.getId(), logEntry );
		}else {
			//we have both - create event, remove matched entry from map
			long duration = Math.abs(logEntry.getTimestamp() - existingEntry.getTimestamp());
			eventList.add(new LogEvent(logEntry.getId(),logEntry.getHost(), logEntry.getType(), duration, duration > Config.ALERT_THRESHOLD_IN_MILLIS?true:false));
			entriesMap.remove(logEntry.getId());
		}
	}

	
}
