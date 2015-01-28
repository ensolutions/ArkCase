package com.armedia.acm.services.search.web.api;

import java.util.*;

import javax.servlet.http.HttpSession;

import com.armedia.acm.pluginmanager.model.AcmPlugin;
import com.armedia.acm.pluginmanager.service.AcmPluginManager;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.client.MuleClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.armedia.acm.services.search.model.ApplicationSearchEvent;
import com.armedia.acm.services.search.model.solr.SolrDocument;
import com.armedia.acm.services.search.model.solr.SolrResponse;
import com.armedia.acm.services.search.service.SearchEventPublisher;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Controller
@RequestMapping( { "/api/v1/plugin/search", "/api/latest/plugin/search"} )
public class SearchObjectByTypeAPIController {

    private Logger log = LoggerFactory.getLogger(getClass());

    private MuleClient muleClient;
    private SearchEventPublisher searchEventPublisher;
    private AcmPluginManager acmPluginManager;

    @RequestMapping(value = "/{objectType}", method  = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String searchObjectByType(
    		@PathVariable("objectType") String objectType,
            @RequestParam(value = "s", required = false, defaultValue = "") String sort,
            @RequestParam(value = "start", required = false, defaultValue = "0") int startRow,
            @RequestParam(value = "n", required = false, defaultValue = "10") int maxRows,
            @RequestParam(value = "assignee", required = false, defaultValue = "") String assignee,
            @RequestParam(value = "activeOnly", required = false, defaultValue = "true") boolean activeOnly,
            @RequestParam(value = "filters", required = false, defaultValue = "") String filters,
            Authentication authentication,
            HttpSession httpSession
    ) throws Exception {
        String[] f = null;
        String sortParams = null;
        String params = "";
        String query = "object_type_s:" + objectType;
        if (StringUtils.isBlank(filters)) {
            if (!StringUtils.isBlank(assignee)) {
                query += " AND assignee_s:" + assignee;
            }

            if (activeOnly) {
                query += " AND -status_s:COMPLETE AND -status_s:DELETE AND -status_s:CLOSED";
            }
            if (log.isDebugEnabled()) {
                log.debug("User '" + authentication.getName() + "' is searching for '" + query + "'");
            }
        } else {
                f = filters.split(",");
                List<String> testFilters = null;
                if (f != null) {
                    testFilters = findFilters(objectType, f);
                    StringBuilder stringBuilder = new StringBuilder();
                    int i =0;
                    for( String filter:testFilters ) {
                        if( i>0 ) {
                            stringBuilder.append("&");
                            stringBuilder.append(filter);
                        } else {
                            stringBuilder.append(filter);
                        }
                        i++;
                    }
                    params=stringBuilder.toString();
                }
            }

        if (!StringUtils.isBlank(sort)){
          sortParams = findSortValuesAndCreateSotrString(objectType, sort);
        }

        // try what the user sent, if no sort properties were found
        sortParams = StringUtils.isBlank(sortParams) ? sort : sortParams;

        Map<String, Object> headers = new HashMap<>();
        headers.put("query", query);
        headers.put("firstRow", startRow);
        headers.put("maxRows", maxRows);
        headers.put("sort", sortParams);
        headers.put("acmUser", authentication);
        headers.put("rowQueryParametars",params);



        MuleMessage response = getMuleClient().send("vm://quickSearchQuery.in", "", headers);

        log.debug("Response type: " + response.getPayload().getClass());

        if ( response.getPayload() instanceof String )
        {
            String responsePayload = (String) response.getPayload();
           
            publishSearchEvent(authentication, httpSession, true, responsePayload);
          
            return responsePayload;
        }

        throw new IllegalStateException("Unexpected payload type: " + response.getPayload().getClass().getName());
    }
    
    @RequestMapping(value = "/advanced/{objectType}", method  = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String searchAdvancedObjectByType(
    		@PathVariable("objectType") String objectType,
            @RequestParam(value = "s", required = false, defaultValue = "") String sort,
            @RequestParam(value = "start", required = false, defaultValue = "0") int startRow,
            @RequestParam(value = "n", required = false, defaultValue = "10") int maxRows,
            @RequestParam(value = "assignee", required = false, defaultValue = "") String assignee,
            @RequestParam(value = "activeOnly", required = false, defaultValue = "true") boolean activeOnly,
            Authentication authentication,
            HttpSession httpSession
    ) throws MuleException, Exception
    {
        String query = "object_type_s:" + objectType;  
        
        if (!StringUtils.isBlank(assignee)) {
            query += " AND assignee_s:" + assignee;
        }

        if ( activeOnly )
        {
            query += " AND -status_s:COMPLETE AND -status_s:DELETE AND -status_s:CLOSED";
        }
        
        if ( log.isDebugEnabled() )
        {
            log.debug("Advanced Search: User '" + authentication.getName() + "' is searching for '" + query + "'");
        }
     
        Map<String, Object> headers = new HashMap<>();
        headers.put("query", query);
        headers.put("firstRow", startRow);
        headers.put("maxRows", maxRows);
        headers.put("sort", sort);
        headers.put("acmUser", authentication);

        MuleMessage response = getMuleClient().send("vm://advancedSearchQuery.in", "", headers);

        log.debug("Response type: " + response.getPayload().getClass());

        if ( response.getPayload() instanceof String )
        {
            String responsePayload = (String) response.getPayload();
           
            publishSearchEvent(authentication, httpSession, true, responsePayload);
          
            return responsePayload;
        }

        throw new IllegalStateException("Unexpected payload type: " + response.getPayload().getClass().getName());
    }
    
    protected void publishSearchEvent(Authentication authentication,
            HttpSession httpSession,
            boolean succeeded, String jsonPayload)
    {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        SolrResponse solrResponse = gson.fromJson(jsonPayload, SolrResponse.class);
        
        if ( solrResponse.getResponse() != null ) {
            List<SolrDocument> solrDocs = solrResponse.getResponse().getDocs();
            String ipAddress = (String) httpSession.getAttribute("acm_ip_address");
            Long objectId = null;
            for ( SolrDocument doc : solrDocs ) {
                // in case when objectID is not Long like in USER case
                try {
                    objectId = Long.parseLong(doc.getObject_id_s());
                } catch (NumberFormatException e) {
                    objectId = new Long(-1);
                }
                ApplicationSearchEvent event = new ApplicationSearchEvent(objectId, doc.getObject_type_s(),
                        authentication.getName(), succeeded, ipAddress);
                getSearchEventPublisher().publishSearchEvent(event);
            }
        }
    }

    private String findSortValuesAndCreateSotrString(String objectType,String sort){
        String[] srt = sort.split(",");
        Collection<AcmPlugin> plugins = getAcmPluginManager().getAcmPlugins();
        List<String> suportedObjectTypes = null;
        StringBuilder stringBuilder = new StringBuilder();
        boolean isFirstSortArgument = true;
        for( AcmPlugin plugin: plugins ) {
            if( plugin.getSuportedObjectTypesNames()!=null ) {
                suportedObjectTypes = plugin.getSuportedObjectTypesNames();
            } else {
                continue;
            }
            for ( String objectTypeName:suportedObjectTypes) {
                if ( objectType.equals(objectTypeName) ) {
                    for( String s:srt ){
                        String jsonString = (String) plugin.getPluginProperties().get("search.tree.sort");
                        JSONArray jsonArray = new JSONArray(jsonString);
                        for( int i=0;i< jsonArray.length(); i++) {
                            JSONObject jObj = jsonArray.getJSONObject(i);
                            if(jObj.getString("name").equals(s)){
                                if(isFirstSortArgument) {
                                    stringBuilder.append(jObj.getString("value").trim());
                                    isFirstSortArgument = false;
                                } else {
                                    stringBuilder.append(", ");
                                    stringBuilder.append(jObj.getString("value").trim());
                                }
                            }
                        }
                    }
                }
            }
        }
        return stringBuilder.toString();
    }

    private List<String> findFilters(String objectType, String[] filterNames) {
        Collection<AcmPlugin> plugins = getAcmPluginManager().getAcmPlugins();
        List<String> suportedObjectTypes = null;
        JSONObject objectFromProperty = null;
        List<String> filters = new ArrayList<>();
        for( AcmPlugin plugin: plugins ) {
            if( plugin.getSuportedObjectTypesNames()!=null ) {
                suportedObjectTypes = plugin.getSuportedObjectTypesNames();
            } else {
                continue;
            }
            for ( String objectTypeName:suportedObjectTypes ) {
                if ( objectType.equals(objectTypeName) ) {
                    for ( String filterName: filterNames) {
                          String jsonString = (String) plugin.getPluginProperties().get("search.tree.filter");
                          JSONArray jsonArray = new JSONArray(jsonString);
                         for( int i=0;i< jsonArray.length(); i++ ) {
                              JSONObject jObj = jsonArray.getJSONObject(i);
                              if(jObj.getString("name").equals(filterName)){
                                filters.add(jObj.getString("value").trim());
                              }
                         }
                    }
                }
            }
        }
        return filters;
    }

    public AcmPluginManager getAcmPluginManager() {
        return acmPluginManager;
    }

    public void setAcmPluginManager(AcmPluginManager acmPluginManager) {
        this.acmPluginManager = acmPluginManager;
    }

    public MuleClient getMuleClient()
    {
        return muleClient;
    }

    public void setMuleClient(MuleClient muleClient)
    {
        this.muleClient = muleClient;
    }

    public SearchEventPublisher getSearchEventPublisher() {
        return searchEventPublisher;
    }

    public void setSearchEventPublisher(SearchEventPublisher searchEventPublisher) {
        this.searchEventPublisher = searchEventPublisher;
    }
   
}