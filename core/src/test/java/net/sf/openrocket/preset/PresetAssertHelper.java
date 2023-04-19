package net.sf.openrocket.preset;

import static org.junit.Assert.*;

public abstract class PresetAssertHelper {

	public static void assertInvalidPresetException( InvalidComponentPresetException exceptions, TypedKey<?>[] keys, String[] messages ) {
		if ( keys != null ) {
			assertEquals( keys.length, exceptions.getInvalidParameters().size() );
			for( TypedKey<?> expectedKey : keys ) {
				boolean keyFound = false;
				for( TypedKey<?> k : exceptions.getInvalidParameters() ) {
					if ( expectedKey == k ) {
						keyFound = true;
						break;
					}
				}
				if ( ! keyFound ) {
					fail( "Expected key " + expectedKey + " not in exception");
				}
			}
		} else {
			assertEquals(0, exceptions.getInvalidParameters().size() );
		}
		if ( messages != null ) {
			assertEquals( messages.length, exceptions.getErrors().size() );
			for( String expectedMessage : messages ) {
				boolean stringMatched = false;
				for ( String s : exceptions.getErrors() ) {
					if ( s.contains( expectedMessage ) ) {
						stringMatched = true;
						break;
					}
				}
				if( !stringMatched ) {
					fail( "Expected string \"" + expectedMessage + "\" not reported in errors");
				}
			}
		} else {
			assertEquals(0, exceptions.getErrors().size() );
		}
	}
}
