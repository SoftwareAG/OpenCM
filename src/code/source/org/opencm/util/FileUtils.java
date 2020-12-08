package org.opencm.util;

import java.io.File;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.StringTokenizer;

public class FileUtils {

	public static boolean createDirectory(String dir) {
		
		File fDir = new File(dir);
		if (!fDir.exists()) {
			if (!fDir.mkdir()) {
				LogUtils.logError("FileUtils :: createDirectory :: Not Created: " + dir);
				return false;
			}
		}
		return true;
	}

	public static void removeDirectory(String dir) {
		File fDir = new File(dir);
		if (fDir.exists()) {
			try {
				org.apache.commons.io.FileUtils.deleteDirectory(fDir);
			} catch (IOException ex) {
				LogUtils.logError("FileUtils :: removeDirectory :: Not Deleted: " + ex.toString());
			}
		}
	}
	
	public static void writeToFile(String filename, String content) {
		File fFile = new File(filename);
		try {
            Writer fWriter = new OutputStreamWriter(new FileOutputStream(fFile), StandardCharsets.UTF_8);
            fWriter.write(content);
            fWriter.close();
		} catch (IOException ex) {
			LogUtils.logError("FileUtils :: writeToFile :: " + ex.toString());
		}
	}
	
	public static void renameFile(File origFile, File targetFile) {
		if (!origFile.exists()) {
			LogUtils.logError("FileUtils :: renameFile :: Original file does not exist: " + origFile.getPath());
		}
		if (!origFile.renameTo(targetFile)) {
			LogUtils.logError("FileUtils :: renameFile :: Unable to rename to : " + targetFile.getPath());
		}
	}
	
	public static String readFromFile(String filename) {
		File fFile = new File(filename);
		if (!fFile.exists()) {
			return null;
		}
		try {
			return org.apache.commons.io.FileUtils.readFileToString(fFile, java.nio.charset.StandardCharsets.UTF_8);
		} catch (IOException ex) {
			LogUtils.logError("FileUtils :: readFromFile :: " + ex.toString());
		}
		return null;
	}
	 
	 public static ArrayList<String> getSnapshotDirs(String runtimeDir) {
		 // Get a list sorted by latest first
	     ArrayList<String> alSnapshots = new ArrayList<String>();
	     File dir = new File(runtimeDir);
	     if (!dir.exists()) {
	    	 return alSnapshots;
	     }
	     File [] fSnapshotsDirs = dir.listFiles();
	     for(File path:fSnapshotsDirs) {
	          if (path.isDirectory()) {
	              alSnapshots.add(path.getName());
	          }
	     }
	     java.util.Collections.sort(alSnapshots);
	     java.util.Collections.reverse(alSnapshots);
	     return alSnapshots;
	 }


	 /*
	  * Filter parameter supports the following:
	  *  - Comma delimited list of subdirectories
	  *  - Each element can contain a regexp string (e.g. "integrationServer-.*")
	  *  
	  */
	 public static LinkedList<String> getSubDirectories(String rootDir, String filter) {
			File fRootDir = new File(rootDir);
			LinkedList<String> subDirs = new LinkedList<String>();
			LinkedList<String> filterElements = new LinkedList<String>();
			if (filter != null) {
				int multipleElementsIdx = filter.indexOf(",");
				if (multipleElementsIdx > 0) {
					StringTokenizer st = new StringTokenizer(filter,",");
					while (st.hasMoreTokens()) {
						filterElements.add(st.nextToken().trim());
					}
				} else {
					filterElements.add(filter);
				}
			}

			for (int i = 0; i < filterElements.size(); i++) {
				String stElement = StringUtils.getRegexString(filterElements.get(i));
				String eFilter = stElement;
				FilenameFilter prefixFilter = new FilenameFilter() {
					public boolean accept(File dir, String name) {
						if (name.matches(eFilter)) {
							return true;
						} else {
							return false;
						}
					}
				};

				File[] files = fRootDir.listFiles(prefixFilter);
				for (File file : files) {
					if (file.isDirectory()) {
						if (!subDirs.contains(file.getName())) {
							subDirs.add(file.getName());
						}
					}
				}
			}
			
			return subDirs;
		}


}

