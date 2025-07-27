package info.openrocket.core.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Test;

public class ReflectionTest {

	@Test
	public void textHandleInvocationTargetException() {
		Throwable cause = null;

		try {
			cause = new InvocationTargetException(null);
			Reflection.handleWrappedException((InvocationTargetException) cause);
			fail();
		} catch (BugException e) {
			assertTrue(cause == e.getCause());
		}

		try {
			cause = new IllegalStateException("Test");
			Reflection.handleWrappedException(new InvocationTargetException(cause));
			fail();
		} catch (IllegalStateException e) {
			assertTrue(cause == e);
		}

		try {
			cause = new AbstractMethodError();
			Reflection.handleWrappedException(new InvocationTargetException(cause));
			fail();
		} catch (AbstractMethodError e) {
			assertTrue(cause == e);
		}

		try {
			cause = new IOException();
			Reflection.handleWrappedException(new InvocationTargetException(cause));
			fail();
		} catch (BugException e) {
			assertTrue(cause == e.getCause());
		}

	}

}
