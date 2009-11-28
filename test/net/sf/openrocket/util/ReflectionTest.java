package net.sf.openrocket.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

public class ReflectionTest {

	@Test
	public void textHandleInvocationTargetException() {
		Throwable cause = null;
		
		try {
			cause = new InvocationTargetException(null);
			Reflection.handleInvocationTargetException((InvocationTargetException)cause);
			fail();
		} catch (BugException e) {
			assertTrue(cause == e.getCause());
		}
		
		try {
			cause = new IllegalStateException("Test");
			Reflection.handleInvocationTargetException(new InvocationTargetException(cause));
			fail();
		} catch (IllegalStateException e) {
			assertTrue(cause == e);
		}
		
		try {
			cause = new AbstractMethodError();
			Reflection.handleInvocationTargetException(new InvocationTargetException(cause));
			fail();
		} catch (AbstractMethodError e) { 
			assertTrue(cause == e);
		}
		
		try {
			cause = new IOException();
			Reflection.handleInvocationTargetException(new InvocationTargetException(cause));
			fail();
		} catch (BugException e) { 
			assertTrue(cause == e.getCause());
		}
		
	}
	
}
