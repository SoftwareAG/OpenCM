package org.opencm.util;

import java.io.File;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

    public static ByteArrayOutputStream compress(File inputDirectory) throws IOException {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ZipOutputStream zos = new ZipOutputStream(baos); 
    	try { 
    	    compressDirectory(inputDirectory, inputDirectory.getName(), zos); 
    	    zos.close(); 
    	} catch (Exception ex) { 
			System.out.println("OpenCM ZipUtils :: compress :: Exception: " + ex.toString());
    	}
    	baos.close();
    	return baos;
    }

    public static void decompress(InputStream inStream, File outdir) throws IOException {
        byte[] buffer = new byte[1024];
    	
    	ZipInputStream zis = new ZipInputStream(inStream);
    	ZipEntry ze = zis.getNextEntry();
   		
    	while (ze != null) {
    		String fileName = ze.getName();
    		File newFile = new File(outdir + File.separator + fileName);
    		new File(newFile.getParent()).mkdirs();
    		FileOutputStream fos = new FileOutputStream(newFile);             

    		int len;
           
    		while ((len = zis.read(buffer)) > 0) {
    			fos.write(buffer, 0, len);
    		}
    		fos.close();   
    		ze = zis.getNextEntry();
    	}
   	
    	zis.closeEntry();
    	zis.close();
    }
    

    private static void compressDirectory(File indir, String path, ZipOutputStream zos) { 
    	try {
    		String[] dirList = indir.list(); 
    		byte[] readBuffer = new byte[2156]; 
    		int bytesIn = 0; 
    		
    		for (int i = 0; i < dirList.length; i++) { 
    			File f = new File(indir, dirList[i]); 
    			if(f.isDirectory()) {
    				compressDirectory(f, path + File.separator + f.getName(), zos); 
    				continue; 
    			} 
    			
    			FileInputStream fis = new FileInputStream(f); 
    			ZipEntry anEntry = new ZipEntry(path + File.separator + f.getName()); 
    			zos.putNextEntry(anEntry); 
    			
    			while((bytesIn = fis.read(readBuffer)) != -1) { 
    				zos.write(readBuffer, 0, bytesIn); 
    			}
    			
    			fis.close(); 
    		} 
    	} catch (Exception ex) { 
			System.out.println("OpenCM ZipUtils :: compressDirectory :: Exception: " + ex.toString());
    	} 
    }

}
