package com.vaguehope.loctxt.reporter;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class SessionReporter implements HttpSessionListener, ReportProvider {

	protected static final Logger LOG = Logger.getLogger(SessionReporter.class.getName());
	
	private final AtomicInteger counter = new AtomicInteger(0);
	
	@Override
	public void sessionCreated (HttpSessionEvent se) {
		this.counter.incrementAndGet();
	}

	@Override
	public void sessionDestroyed (HttpSessionEvent se) {
		this.counter.decrementAndGet();
	}
	
	@Override
	public void appendReport (StringBuilder r) {
		r.append(this.counter.get()).append(" sessions.");
	}

}
