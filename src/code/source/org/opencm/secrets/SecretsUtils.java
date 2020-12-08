package org.opencm.secrets;

import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.StringTokenizer;

import org.linguafranca.pwdb.kdbx.KdbxCreds;
import org.linguafranca.pwdb.kdbx.simple.SimpleEntry;
import org.linguafranca.pwdb.kdbx.simple.SimpleGroup;
import org.linguafranca.pwdb.kdbx.simple.SimpleDatabase;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;

import org.opencm.util.LogUtils;

import java.util.ArrayList;

public class SecretsUtils {
	
    public static final File 	KEEPASS_DATABASE_FILE		= new File(SecretsConfiguration.SECRETS_CONFIG_DIRECTORY.getPath() + File.separator + SecretsConfiguration.SECRETS_LOCAL_KEEPASS_FILE);
	
    public static final int 	KEEPASS_ICON_GROUP			= 48;
    public static final int 	KEEPASS_ICON_INSTALLATION	= 28;
	
	public static SimpleDatabase getDatabase(SecretsConfiguration secConfig) {
		LogUtils.logDebug("Getting Keepass database: " + KEEPASS_DATABASE_FILE.getPath());
		try {
			KdbxCreds creds = new KdbxCreds(secConfig.getKeepassPassword().getBytes());
			InputStream kpStream = new FileInputStream(KEEPASS_DATABASE_FILE);
			SimpleDatabase keepassDb = SimpleDatabase.load(creds, kpStream);
			kpStream.close();
			return keepassDb;
		} catch (Exception ex) {
    		LogUtils.logError("SecretsUtils - getDatabase Exception: " + ex.getMessage());
		} 
		return null;
		
	}
	
	public static SimpleDatabase createEntry(SecretsConfiguration secConfig, SimpleDatabase db, ArrayList<String> path, String title, String username, String password) {
		
		LogUtils.logDebug("SecretsUtils - createEntry: " + path.toString());
		try {
			SimpleGroup group = db.getRootGroup();
		
			for (String p : path) {
				LogUtils.logDebug("SecretsUtils - processing path " + path);
				boolean groupExists = false;
				for (SimpleGroup g : group.getGroups()) {
					if (p.equals(g.getName())) {
						// Group exists
						groupExists = true;
						group = g;
						break;
					}
				}
				LogUtils.logDebug("SecretsUtils - group exists ? " + groupExists);
				LogUtils.logDebug("Path equals the installation? " + path.get(path.size() - 1 ).equals(p));
				if (!groupExists) {
					SimpleGroup sg = db.newGroup();
					sg.setName(p);
					if (path.get(path.size() - 1 ).equals(p)) {
						// Installation....
						sg.setIcon(db.newIcon(KEEPASS_ICON_INSTALLATION));
					} else {
						sg.setIcon(db.newIcon(KEEPASS_ICON_GROUP));
					}
					group.addGroup(sg);
					group = sg;
				}
				if (path.get(path.size() - 1 ).equals(p)) {
					LogUtils.logDebug("SecretsUtils - Writing the entry " + title);
					// last path - write entry to this group
					// Check if it exists already: if so, remove it (but keep the pwd).
					String oldPwd = "";
					for (SimpleEntry entry : group.getEntries()) {
						if (entry.getTitle().equals(title)) {
							oldPwd = entry.getPassword();
							group.removeEntry(entry);
							break;
						}
					}
					if ((password == null) || (password.equals(""))) {
						password = oldPwd;
					}
					SimpleEntry se = db.newEntry();
					se.setTitle(title);
					se.setUsername(username);
					se.setPassword(password);
					group.addEntry(se);
				}
			}
			
		} catch (Exception ex) {
    		LogUtils.logError("SecretsUtils - createEntry Exception: " + ex.getMessage());
		} 
		
		return db;
	}
	
	public static String getPassword(SecretsConfiguration secConfig, String username, String passwordHandle) {
		if (secConfig.getType().equals(SecretsConfiguration.TYPE_LOCAL)) {
			return getKeepassPassword(secConfig,username,passwordHandle);
		} else {
			return getVaultPassword(secConfig,username,passwordHandle);
		}
	}
	
	private static String getKeepassPassword(SecretsConfiguration secConfig, String username, String passwordHandle) {
		SimpleDatabase db = getDatabase(secConfig);
		StringTokenizer stHandle = new StringTokenizer(passwordHandle, "/");
		
		SimpleGroup group = db.getRootGroup();
		ArrayList<String> path = new ArrayList<String>();
		while (stHandle.hasMoreTokens()) {
			String token = stHandle.nextToken();
			path.add(token);
		}
		String entryName = path.get(path.size() - 1);
		path.remove(path.size() - 1);
		
		for (String pathLevel : path) {
			boolean groupExists = false;
			for (SimpleGroup g : group.getGroups()) {
				if (pathLevel.equals(g.getName())) {
					// Group exists
					groupExists = true;
					group = g;
					break;
				}
			}
			if (!groupExists) {
	    		LogUtils.logError("SecretsUtils - getPassword: Unable to get password for group/path: " + path);
	    		return null;
			}
			// Last item in path?
			if (pathLevel.equals(path.get(path.size() - 1))) {
				for (SimpleEntry e : group.getEntries()) {
					if (e.getTitle().equals(entryName) && (e.getUsername().equals(username))) {
						return e.getPassword();
					}
				}
	    		LogUtils.logError("SecretsUtils - Group found (" + group.getName() + ") but no entry found for entry: " + entryName + " and username : " + username);
	    		break;
			}
			
		}
		
		return null;
	}
	
	private static String getVaultPassword(SecretsConfiguration secConfig, String username, String passwordHandle) {
		
		try {
			VaultConfig vaultConfig = new VaultConfig()
	                .address(secConfig.getVaultURL())
	                .token(secConfig.getVaultToken())
	                .build();
			
			Vault vault = new Vault(vaultConfig, new Integer(secConfig.getVaultVersion()));
			
			return vault.logical()
                    .read(passwordHandle)
                    .getData().get(username);
			
		} catch (Exception ex) {
			LogUtils.logError("SecretsUtils - getVaultPassword Exception: " + ex.getMessage());
		}
		return null;
	}
	
	public static void writeDatabase(SecretsConfiguration secConfig, SimpleDatabase db) {
		
        try {
    		FileOutputStream kpStream = new FileOutputStream(KEEPASS_DATABASE_FILE);
    		KdbxCreds creds = new KdbxCreds(secConfig.getKeepassPassword().getBytes());
    		
            db.save(creds, kpStream);
		} catch (Exception ex) {
    		LogUtils.logError("SecretsUtils - writeDatabase Exception: " + ex.getMessage());
		} 
        
	}

}
