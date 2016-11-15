/**
 * 
 */
package com.armedia.acm.form.ebrief.service;

import com.armedia.acm.form.ebrief.model.EbriefForm;
import com.armedia.acm.frevvo.config.FrevvoFormName;
import com.armedia.acm.frevvo.config.FrevvoFormService;
import com.armedia.acm.plugins.casefile.model.CaseEvent;
import com.armedia.acm.plugins.casefile.model.CaseFileConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

import java.util.Properties;

/**
 * @author riste.tutureski
 *
 */
public class EbriefUpdatedListener implements ApplicationListener<CaseEvent>
{

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private Properties properties;
    private FrevvoFormService ebriefService;

    @Override
    public void onApplicationEvent(CaseEvent event)
    {
        if ("com.armedia.acm.casefile.updated".equals(event.getEventType().toLowerCase()))
        {
            LOG.debug("Updating Frevvo XML file ...");

            if (getProperties() != null)
            {
                if (getProperties().containsKey(CaseFileConstants.ACTIVE_CASE_FORM_KEY))
                {
                    String activeFormName = (String) getProperties().get(CaseFileConstants.ACTIVE_CASE_FORM_KEY);

                    if (FrevvoFormName.EBRIEF.equals(activeFormName))
                    {
                        getEbriefService().updateXML(event.getCaseFile(), event.getEventUser(), EbriefForm.class);
                    }
                }
            }
        }
    }

    public Properties getProperties()
    {
        return properties;
    }

    public void setProperties(Properties properties)
    {
        this.properties = properties;
    }

    public FrevvoFormService getEbriefService()
    {
        return ebriefService;
    }

    public void setEbriefService(EbriefService ebriefService)
    {
        this.ebriefService = ebriefService;
    }
}
