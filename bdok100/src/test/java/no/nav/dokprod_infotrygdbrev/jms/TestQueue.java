package no.nav.dokprod_infotrygdbrev.jms;

import jakarta.jms.Queue;

/**
 * Test implementation of a Queue
 *
 */
public class TestQueue implements Queue {
	String queueName;

	public TestQueue(String name) {
		this.queueName = name;
	}

	@Override
	public String getQueueName() {
		return queueName;
	}
}
