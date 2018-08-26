package com.logparser.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.logparser.parser.dao.LogEventBatchDao;
import com.logparser.parser.processing.LogParser;

@SpringBootApplication
public class ParserApplication implements CommandLineRunner {

	static Logger logger = LoggerFactory.getLogger(ParserApplication.class);

	public static void main(String[] args) {
		logger.info("STARTING THE APPLICATION");
		SpringApplication.run(ParserApplication.class, args);
		logger.info("APPLICATION FINISHED");
	}

	@Override
	public void run(String... args) throws InterruptedException {
		logger.info("EXECUTING : command line runner");

		if(args.length < 1) {
			logger.error("Provide log file path as program parameter");
			System.exit(-1);
		}
		LogParser parser = new LogParser(new LogEventBatchDao(), args[0]);
		parser.execute();
	}

}
