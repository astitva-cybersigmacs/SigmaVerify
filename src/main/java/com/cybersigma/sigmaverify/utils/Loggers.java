package com.cybersigma.sigmaverify.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Loggers {
	private static Logger LOG = LoggerFactory.getLogger(Loggers.class);
	
	public static void log() {
		LOG.warn(null);
	}
}
