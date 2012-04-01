package com.vaguehope.loctxt.reporter;

public class JvmReporter implements ReportProvider {

	private static final long BYTES_IN_MB = 1024L * 1024L;

	@Override
	public void appendReport (StringBuilder r) {
		long heapFreeSize = Runtime.getRuntime().freeMemory() / BYTES_IN_MB;
		long heapSize = Runtime.getRuntime().totalMemory() / BYTES_IN_MB;
		r.append(heapSize - heapFreeSize).append(" mb of ").append(heapSize).append(" mb heap used.");
	}

}
