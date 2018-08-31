package OpenCM.pub.dsp;

// -----( IS Java Code Template v1.2

import com.wm.data.*;
import com.wm.util.Values;
import com.wm.app.b2b.server.Service;
import com.wm.app.b2b.server.ServiceException;
// --- <<IS-START-IMPORTS>> ---
import java.io.File;
import org.opencm.util.PackageUtils;
// --- <<IS-END-IMPORTS>> ---

public final class util

{
	// ---( internal utility methods )---

	final static util _instance = new util();

	static util _newInstance() { return new util(); }

	static util _cast(Object o) { return (util)o; }

	// ---( server methods )---




	public static final void hasCmdataLink (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(hasCmdataLink)>> ---
		// @sigtype java 3.5
		// [o] field:0:required exists
		String cmDataDir = new File(PackageUtils.getPackageConfigPath()).getParent() + File.separator + "pub" + File.separator + "cmdata";
		File fDir = new File(cmDataDir);
		
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		IDataUtil.put( pipelineCursor, "exists", fDir.exists() );
		pipelineCursor.destroy();
			
		// --- <<IS-END>> ---

                
	}



	public static final void hasOutputLink (IData pipeline)
        throws ServiceException
	{
		// --- <<IS-START(hasOutputLink)>> ---
		// @sigtype java 3.5
		// [o] field:0:required exists
		String cmDataDir = new File(PackageUtils.getPackageConfigPath()).getParent() + File.separator + "pub" + File.separator + "output";
		File fDir = new File(cmDataDir); 
		
		// pipeline
		IDataCursor pipelineCursor = pipeline.getCursor();
		IDataUtil.put( pipelineCursor, "exists", fDir.exists() );
		pipelineCursor.destroy();
			
		// --- <<IS-END>> ---

                
	}
}

