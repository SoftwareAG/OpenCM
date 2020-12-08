package org.opencm.freemarker;

import org.opencm.configuration.Configuration;
import org.opencm.util.Cache;
import org.opencm.util.LogUtils;

import freemarker.template.*;

public class FMConfiguration {

    public static final String  		FREEMARKER_CACHE_KEY				= "OPENCM_FREEMARKER";

	private freemarker.template.Configuration fmConfiguration;
    
    public FMConfiguration() {
    }
    
    public static FMConfiguration instantiate() {
		LogUtils.logDebug("Freemarker Instantiation starting .... ");
		FMConfiguration fmc = (FMConfiguration) Cache.getInstance().get(FREEMARKER_CACHE_KEY);
    	if (fmc != null) {
    		LogUtils.logDebug("FMConfiguration already in cache: Returning object .... ");
    		return fmc;
    	}

		LogUtils.logDebug("FMConfiguration not in cache - generating .... ");
		
		fmc = new FMConfiguration();
		try {
			freemarker.template.Configuration fmcfg = new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_28);
			fmcfg.setDirectoryForTemplateLoading(Configuration.getPackageConfigDirectory());
			fmcfg.setDefaultEncoding("UTF-8");
			fmcfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
			fmcfg.setLogTemplateExceptions(false);
			fmcfg.setWrapUncheckedExceptions(true);
			fmc.setConfiguration(fmcfg);
    	} catch (Exception e) {
    		LogUtils.logError(" FMConfiguration - Exception: " + e.toString());
    		return null;
    	}
    	
    	Cache.getInstance().set(FREEMARKER_CACHE_KEY, fmc);
		LogUtils.logDebug(" FMConfiguration Instantiation finishing .... ");
    	return fmc;
    }
    
    public freemarker.template.Configuration getConfiguration() {
        return this.fmConfiguration;
    }
    public void setConfiguration(freemarker.template.Configuration cnf) {
        this.fmConfiguration = cnf;
    }
    
}
