package com.logparser.parser.dao;

import java.util.List;
import com.logparser.parser.entity.LogEvent;

public interface ILogEventBatchDao {

	void storeEvents(List<LogEvent> totalEvents);
}
