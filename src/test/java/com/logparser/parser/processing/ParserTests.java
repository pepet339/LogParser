package com.logparser.parser.processing;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.internal.verification.Times;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logparser.parser.dao.ILogEventBatchDao;
import com.logparser.parser.dao.LogEventBatchDao;
import com.logparser.parser.entity.LogEntry;
import com.logparser.parser.entity.LogEvent;

@RunWith(SpringRunner.class)
public class ParserTests {
	
	@Ignore
	@Test
	public void LogParser_LogsMixed_Successfull() throws InterruptedException {
		
		ILogEventBatchDao daoMock = mock(ILogEventBatchDao.class);
		
		LogParser parser = new LogParser(daoMock,"test_file\\mixed_logs.json");
		parser.execute();
		
		ArgumentCaptor<List<LogEvent>> argument = ArgumentCaptor.forClass(List.class);
		verify(daoMock, new Times(7)).storeEvents(argument.capture());
		List<List<LogEvent>> lists = argument.getAllValues();
				
		List<LogEvent> list = lists.stream().flatMap(List::stream)
				.collect(Collectors.toList());
		Assert.assertTrue(list.size() == 308);
		Map<String,LogEvent> eventMapById = new HashMap<String,LogEvent>();
		for(LogEvent event : list) {
			if(eventMapById.get(event.getId())!=null) {
				Assert.fail("Duplicate event found, key = "+event.getId());
			};
			if(event.getId().equals("0")) {
				Assert.assertNotNull(event.getHost());
				Assert.assertNotNull(event.getType());
			}else {
				Assert.assertNull(event.getHost());
				Assert.assertNull(event.getType());
			}
			Assert.assertTrue(event.isAlert() == (event.getDuration()>4?true:false));
			eventMapById.put(event.getId(), event);
		}
	}
	
	@Test
	public void LogParser_Integration() throws InterruptedException {
		
		LogParser parser = new LogParser(new LogEventBatchDao(),"test_file\\mixed_logs.json");
		parser.execute();
	}
	
	@Ignore
	@Test
	public void LogParser_GenerateFile() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
	    BufferedWriter writer = new BufferedWriter(new FileWriter("testFile.json", true));
		
		for(int i = 0; i < 305; i++)
		try {
			LogEntry logEntry = new LogEntry(""+i,"STARTED",System.currentTimeMillis(),null,null);
			String value = mapper.writeValueAsString(logEntry);
			writer.append(value+"\n");
			Thread.sleep(1);
			logEntry = new LogEntry(""+i,"FINISHED",System.currentTimeMillis(),null,null);
			value = mapper.writeValueAsString(logEntry);
			writer.append(value+"\n");
			
		}catch(Exception e ) {
			e.printStackTrace();
		}
		writer.close();

	 }
	
}
