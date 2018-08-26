package com.logparser.parser.processing;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logparser.parser.config.Config;

public class DataReaderWorker implements Runnable {

	static Logger logger = LoggerFactory.getLogger(DataReaderWorker.class);
	
	private BufferedReader reader;
	private List<String> bufferDataLines;

	public DataReaderWorker(BufferedReader reader, List<String> bufferDataLines) {
		this.reader = reader;
		this.bufferDataLines = bufferDataLines;
	}

	@Override
	public void run() {
		logger.info("Starting reading data thread, going to read max "+Config.CHUNK_LINE_NUMBER+" lines");
		bufferDataLines.clear();
		String line;
		try {
			int currentLineInChunk = 0;
			while(currentLineInChunk < Config.CHUNK_LINE_NUMBER) {
				line = reader.readLine();
				if(line == null || line.length()==0) {
					//end of file
					break;
				}
				bufferDataLines.add(line);
				currentLineInChunk++;
			}
			logger.info("Finished, read "+bufferDataLines.size() +" lines");
		} catch (IOException e) {
			logger.error("Read failed",e);
		}
	}

}
