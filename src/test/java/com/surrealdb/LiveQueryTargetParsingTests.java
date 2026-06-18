package com.surrealdb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

/**
 * Regression tests for target parsing in {@link Surreal#selectLive(String)}.
 */
public class LiveQueryTargetParsingTests {

	private static Optional<LiveNotification> awaitOne(LiveStream stream, Runnable trigger) throws Exception {
		AtomicReference<Optional<LiveNotification>> got = new AtomicReference<>(Optional.empty());
		AtomicReference<Throwable> err = new AtomicReference<>();
		CountDownLatch started = new CountDownLatch(1);

		Thread consumer = new Thread(() -> {
			try {
				started.countDown();
				got.set(stream.next());
			} catch (Throwable t) {
				err.set(t);
			}
		});
		consumer.setDaemon(true);
		consumer.start();

		assertTrue(started.await(2, TimeUnit.SECONDS), "Consumer thread did not start in time");
		Thread.sleep(200);
		trigger.run();
		consumer.join(5000);
		assertFalse(consumer.isAlive(), "Consumer thread still blocked — next() never returned");
		if (err.get() != null) {
			throw new RuntimeException(err.get());
		}
		return got.get();
	}

	private static String recordIdOf(LiveNotification notification) {
		if (notification == null || notification.getValue() == null || !notification.getValue().isObject()) {
			return "";
		}
		Value id = notification.getValue().getObject().get("id");
		if (id == null || !id.isRecordId()) {
			return "";
		}
		return id.getRecordId().toString();
	}

	@Test
	void selectLive_recordIdTarget_routesToTableAndFiltersById() throws Exception {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			surreal.query("DEFINE TABLE person SCHEMALESS");
			surreal.query("CREATE person:1 CONTENT { name: 'one' }");
			surreal.query("CREATE person:2 CONTENT { name: 'two' }");

			try (LiveStream stream = surreal.selectLive("person:1")) {
				Optional<LiveNotification> n = awaitOne(stream, () -> {
					surreal.query("UPDATE person:2 SET name = 'skip'");
					surreal.query("UPDATE person:1 SET name = 'match'");
				});
				assertTrue(n.isPresent(), "Expected a matching notification");
				assertEquals("person:1", recordIdOf(n.get()));
			}
		}
	}

	@Test
	void selectLive_whereInsideTarget_parsesAndFilters() throws Exception {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			surreal.query("DEFINE TABLE person SCHEMALESS");
			surreal.query("CREATE person:1 CONTENT { name: 'one' }");
			surreal.query("CREATE person:2 CONTENT { name: 'two' }");

			try (LiveStream stream = surreal.selectLive("person WHERE id INSIDE [person:`1` ]")) {
				Optional<LiveNotification> n = awaitOne(stream, () -> {
					surreal.query("UPDATE person:2 SET name = 'skip'");
					surreal.query("UPDATE person:1 SET name = 'match'");
				});
				assertTrue(n.isPresent(), "Expected a matching notification");
				assertEquals("person:1", recordIdOf(n.get()));
			}
		}
	}

	@Test
	void filteredLiveStream_closeUnblocksWhenWaitingForMatch() throws Exception {
		AtomicReference<Optional<LiveNotification>> got = new AtomicReference<>();
		AtomicReference<Throwable> err = new AtomicReference<>();
		CountDownLatch started = new CountDownLatch(1);

		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			surreal.query("DEFINE TABLE person SCHEMALESS");
			surreal.query("CREATE person:1 CONTENT { name: 'one' }");
			surreal.query("CREATE person:2 CONTENT { name: 'two' }");

			LiveStream stream = surreal.selectLive("person:1");
			Thread consumer = new Thread(() -> {
				try {
					started.countDown();
					got.set(stream.next());
				} catch (Throwable t) {
					err.set(t);
				}
			});
			consumer.setDaemon(true);
			consumer.start();

			assertTrue(started.await(2, TimeUnit.SECONDS), "Consumer thread did not start in time");
			surreal.query("UPDATE person:2 SET name = 'skip'");
			Thread.sleep(150);
			stream.close();

			consumer.join(5000);
			assertFalse(consumer.isAlive(), "Consumer thread still blocked after close()");
			assertNotNull(got.get(), "next() should return Optional.empty() after close()");
			assertFalse(got.get().isPresent(), "Expected no notification after close()");
			if (err.get() != null) {
				throw new RuntimeException(err.get());
			}
		}
	}
}
