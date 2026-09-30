package no.nav.dokprod_infotrygdbrev.common;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test utils
 *
 */
@UtilityClass
public class TestUtils {

	/**
	 * Get a log mock for a given class, use {@link MockAppender#verify(String)} to assert logs
	 */
	public static MockAppender getMockedAppender(String... names) {
		List<Logger> loggers = Lists.newArrayList();
		for (String name : names) {
			Logger testLogger = (Logger) LoggerFactory.getLogger(name);
			loggers.add(testLogger);
		}
		return getMockAppender(loggers);
	}

	/**
	 * Get a log mock for a given class, use {@link MockAppender#verify(String)} to assert logs
	 */
	public static MockAppender getMockedAppender(Class... clazzes) {
		List<Logger> loggers = Lists.newArrayList();
		for (Class clazz : clazzes) {
			Logger testLogger = (Logger) LoggerFactory.getLogger(clazz);
			loggers.add(testLogger);
		}
		return getMockAppender(loggers);
	}

	/**
	 * NB! Use in @Before, NOT on class level
	 *
	 * @param testLogger
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static MockAppender getMockAppender(List<Logger> testLogger) {
		Appender<ILoggingEvent> mockAppender = Mockito.mock(Appender.class);
		when(mockAppender.getName()).thenReturn("MOCK");
		for (Logger logger : testLogger) {
			logger.addAppender(mockAppender);
		}
		return new MockAppender(mockAppender);
	}

	public static class MockAppender {

		private final Appender<ILoggingEvent> mockAppender;

		MockAppender(Appender<ILoggingEvent> mockAppender) {
			this.mockAppender = mockAppender;
		}

		public void verify(String token) {
			Mockito.verify(mockAppender).doAppend(argThat(hasMessageContaining(token)));
		}

		public void verify(String token, VerificationMode verificationMode) {
			Mockito.verify(mockAppender, verificationMode).doAppend(argThat(hasMessageContaining(token)));
		}

		public void verifyZeroInteractions() {
			verifyNoInteractions(mockAppender);
		}

		private static ArgumentMatcher<ILoggingEvent> hasMessageContaining(final String token) {
			return new ArgumentMatcher<ILoggingEvent>() {
				@Override
				public boolean matches(ILoggingEvent iLoggingEvent) {
					return iLoggingEvent.getFormattedMessage().contains(token);
				}
			};
		}

	}

	public static Map<String, String> headers(String headers) {
		if (StringUtils.isEmpty(headers)) {
			return Maps.newHashMap();
		}
		return new HashMap<>(Splitter.on(",").withKeyValueSeparator(":").split(headers));
	}

	public static Map<String, Object> headers(String... headerValuePairs) {
		HashMap<String, Object> map = new HashMap<>();
		for (int i = 0; i < headerValuePairs.length / 2; i++) {
			map.put(headerValuePairs[i * 2], headerValuePairs[i * 2 + 1]);
		}
		return map;
	}

	/**
	 * Matcher that asserts a time to be less than 1000ms away
	 */
	public static Matcher<? super LocalDateTime> aboutNow() {
		return new BaseMatcher<LocalDateTime>() {
			@Override
			public boolean matches(Object o) {
				if (o instanceof LocalDateTime) {
					long ms = new Duration(LocalDateTime.now().toDateTime(), ((LocalDateTime) o).toDateTime()).getMillis();
					return ms < 1000;
				}
				return false;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("timestamp is not about now");
			}
		};
	}
}
