package OpenCM.pub;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import de.slackspace.openkeepass.KeePassDatabase;
import de.slackspace.openkeepass.domain.KeePassFile;
import java.util.List;
import de.slackspace.openkeepass.domain.Entry;
import de.slackspace.openkeepass.domain.Group;
// --- <<IS-END-IMPORTS>> ---

public final class tesdfgsfd

{
	// ---( internal utility methods )---

	final static tesdfgsfd _instance = new tesdfgsfd();

	static tesdfgsfd _newInstance() { return new tesdfgsfd(); }

	static tesdfgsfd _cast(Object o) { return (tesdfgsfd)o; }

	// ---( server methods )---




	public static final void test (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(test)>> ---
		// @sigtype java 3.5
		System.out.println(" ----------------------------------------------- ");
		 // Open Database
		KeePassFile database = KeePassDatabase.getInstance("C:\\SoftwareAG\\keepass\\OpenCM.kdbx").openDatabase("manage");
			
		// Retrieve all entries
		List<Entry> entries = database.getEntries();
		for (Entry entry : entries) {
			System.out.println("Title: " + entry.getTitle() + " Password: " + entry.getPassword());
		}
		
		// Retrieve all top groups
		List<Group> groups = database.getTopGroups();
		for (Group group : groups) {
			System.out.println(group.getName());
		}
			
		// --- <<IS-END>> ---

                
	}

	// --- <<IS-START-SHARED>> ---

		
	// --- <<IS-END-SHARED>> ---
}

