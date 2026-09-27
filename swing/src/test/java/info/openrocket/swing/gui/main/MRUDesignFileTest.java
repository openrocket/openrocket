package info.openrocket.swing.gui.main;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;

import info.openrocket.core.startup.Application;
import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

public class MRUDesignFileTest extends BaseTestCase {

	@AfterEach
	public void clearStoredList() {
		new MRUDesignFile().setMRUFileList(List.of());
	}

	@Test
	public void storedListFollowsAddsAndRemoves() {
		MRUDesignFile mru = new MRUDesignFile();
		mru.setMRUFileList(List.of());
		mru.addFile("/a.ork");
		mru.addFile("/b.ork");
		mru.addFile("/c.ork");
		assertEquals(List.of("/c.ork", "/b.ork", "/a.ork"), new MRUDesignFile().getMRUFileList());

		// Reopening a file moves it to the top
		mru.addFile("/a.ork");
		assertEquals(List.of("/a.ork", "/c.ork", "/b.ork"), new MRUDesignFile().getMRUFileList());

		// Removing a file shifts the others up and clears the last stored entry
		mru.removeFile("/c.ork");
		assertEquals(List.of("/a.ork", "/b.ork"), new MRUDesignFile().getMRUFileList());
		assertNull(Application.getPreferences().getString(MRUDesignFile.MRU_FILE_LIST_PROPERTY + 2, null));
	}

	@Test
	public void storedListIsLimitedToMaxSize() {
		MRUDesignFile mru = new MRUDesignFile();
		List<String> expected = new ArrayList<>();
		for (int i = 0; i < MRUDesignFile.MAX_SIZE + 2; i++) {
			mru.addFile("/" + i + ".ork");
			expected.add(0, "/" + i + ".ork");
		}

		assertEquals(expected.subList(0, MRUDesignFile.MAX_SIZE), new MRUDesignFile().getMRUFileList());
	}
}
