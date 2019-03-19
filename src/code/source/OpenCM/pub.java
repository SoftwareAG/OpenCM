package OpenCM;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import org.opencm.util.JsonUtils;
// --- <<IS-END-IMPORTS>> ---

public final class pub

{
	// ---( internal utility methods )---

	final static pub _instance = new pub();

	static pub _newInstance() { return new pub(); }

	static pub _cast(Object o) { return (pub)o; }

	// ---( server methods )---




	public static final void test (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(test)>> ---
		// @sigtype java 3.5
		// [o] field:0:required out
		IDataCursor pipelineCursor = pipeline.getCursor();
		String json = JsonUtils.createJsonField("response","Whatever");
		json = JsonUtils.addField(json,"rc","200");
		IDataUtil.put( pipelineCursor, "out", json ); 
		pipelineCursor.destroy();
			
		// --- <<IS-END>> ---

                
	}
}

