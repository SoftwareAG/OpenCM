package org.opencm.freemarker;

import java.io.File;

import org.opencm.configuration.Configuration;
import org.opencm.util.Cache;
import org.opencm.util.LogUtils;

import freemarker.template.*;

public class FMConfiguration {

    public static final String  		FREEMARKER_CACHE_KEY				= "OPENCM_FREEMARKER";

	private freemarker.template.Configuration fmConfiguration;
    
    public FMConfiguration() {
    }
    
    public static FMConfiguration instantiate(Configuration opencmConfig) {
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," Freemarker Instantiation starting .... ");
		FMConfiguration fmc = (FMConfiguration) Cache.getInstance().get(FREEMARKER_CACHE_KEY);
    	if (fmc != null) {
    		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," FMConfiguration already in cache: Returning object .... ");
    		return fmc;
    	}

		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," FMConfiguration not in cache - generating .... ");
		
		fmc = new FMConfiguration();
		try {
			freemarker.template.Configuration fmcfg = new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_28);
			fmcfg.setDirectoryForTemplateLoading(new File(opencmConfig.getConfigDirectory() + File.separator + Configuration.OPENCM_FREEMARKER_DIR_TMPL));
			fmcfg.setDefaultEncoding("UTF-8");
			fmcfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
			fmcfg.setLogTemplateExceptions(false);
			fmcfg.setWrapUncheckedExceptions(true);
			fmc.setConfiguration(fmcfg);
    	} catch (Exception e) {
    		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_CRITICAL," Inventory - Exception: " + e.toString());
    		return null;
    	}
    	
    	Cache.getInstance().set(FREEMARKER_CACHE_KEY, fmc);
		LogUtils.log(opencmConfig.getDebug_level(),Configuration.OPENCM_LOG_DEBUG," FMConfiguration Instantiation finishing .... ");
    	return fmc;
    }
    
    public freemarker.template.Configuration getConfiguration() {
        return this.fmConfiguration;
    }
    public void setConfiguration(freemarker.template.Configuration cnf) {
        this.fmConfiguration = cnf;
    }
    
}
