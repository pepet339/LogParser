package com.logparser.parser.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logparser.parser.entity.LogEvent;

public class LogEventBatchDao implements ILogEventBatchDao {

	static Logger logger = LoggerFactory.getLogger(LogEventBatchDao.class);
	
	@Override
	public void storeEvents(List<LogEvent> totalEvents) {
		Connection con = null;
		Statement stmt = null;
		String statement = null;
		try {
			logger.info("Going to store "+totalEvents.size()+" events to db");
			Class.forName("org.hsqldb.jdbc.JDBCDriver");
			con = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/parserInstance", "SA", "");
			stmt = con.createStatement();

			StringBuilder fullInsert = new StringBuilder("insert into Event values");

			for (LogEvent logEvent : totalEvents) {
				String type = logEvent.getType() == null ? "null" : "'" + logEvent.getType() + "'";
				String host = logEvent.getHost() == null ? "null" : "'" + logEvent.getHost() + "'";
				int alert = logEvent.isAlert() ? 1 : 0;

				fullInsert.append("('" + logEvent.getId() + "'," + logEvent.getDuration() + "," + type + "," + host
						+ "," + alert + "),");
			}
			statement = fullInsert.substring(0, fullInsert.length() - 1).toString();
			logger.debug("Executing statement: "+statement);
			stmt.executeUpdate(statement);
			logger.info("Succesfully stored "+totalEvents.size()+" events to db");

		} catch (Exception e) {
			logger.error("Error while executing statement: "+statement);
		}

	}

}
