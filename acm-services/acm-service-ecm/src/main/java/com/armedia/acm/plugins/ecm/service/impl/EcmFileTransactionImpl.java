package com.armedia.acm.plugins.ecm.service.impl;

import com.armedia.acm.data.AuditPropertyEntityAdapter;
import com.armedia.acm.plugins.ecm.model.EcmFile;
import com.armedia.acm.plugins.ecm.service.EcmFileTransaction;
import com.armedia.acm.plugins.objectassociation.model.ObjectAssociation;

import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.commons.io.IOUtils;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.client.MuleClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by armdev on 4/22/14.
 */
public class EcmFileTransactionImpl implements EcmFileTransaction
{
    private MuleClient muleClient;

    private AuditPropertyEntityAdapter auditPropertyEntityAdapter;

    private Logger log = LoggerFactory.getLogger(getClass());


    @Override
    public EcmFile addFileTransaction(
            Authentication authentication,
            String fileType,
            InputStream fileInputStream,
            String mimeType,
            String fileName,
            String cmisFolderId,
            String parentObjectType,
            Long parentObjectId,
            String parentObjectName)
            throws MuleException
    {
        // by default, files are documents
        String category = "DOCUMENT";
        EcmFile retval = addFileTransaction(authentication, fileType, category, fileInputStream, mimeType, fileName,
                cmisFolderId, parentObjectType, parentObjectId, parentObjectName);

        return retval;
    }

    @Override
    public EcmFile addFileTransaction(
            Authentication authentication,
            String fileType,
            String fileCategory,
            InputStream fileInputStream,
            String mimeType,
            String fileName,
            String cmisFolderId,
            String parentObjectType,
            Long parentObjectId,
            String parentObjectName)
            throws MuleException
    {
        EcmFile toAdd = new EcmFile();
        toAdd.setFileMimeType(mimeType);
        toAdd.setFileName(fileName);
        toAdd.setFileType(fileType);

        ObjectAssociation parent = new ObjectAssociation();
        parent.setParentId(parentObjectId);
        parent.setParentType(parentObjectType);
        parent.setParentName(parentObjectName);
        parent.setCategory(fileCategory);
        parent.setTargetSubtype(fileType);
        toAdd.addParentObject(parent);

        Map<String, Object> messageProps = new HashMap<>();
        messageProps.put("ecmFolderId", cmisFolderId);
        messageProps.put("inputStream", fileInputStream);
        messageProps.put("acmUser", authentication);
        messageProps.put("auditAdapter", getAuditPropertyEntityAdapter());
        MuleMessage received = getMuleClient().send("vm://addFile.in", toAdd, messageProps);
        EcmFile saved = received.getPayload(EcmFile.class);

        MuleException e = received.getInboundProperty("saveException");
        if ( e != null )
        {
            throw e;
        }

        Map<String, Object> headers = new HashMap<>();

        MuleMessage response = getMuleClient().send("jms://solrContentFile.in", saved, headers);

        MuleException exc = response.getInboundProperty("saveException");
        if ( exc != null )
        {
            throw exc;
        }

        return saved;
    }
    
    @Override
    public EcmFile updateFileTransaction(
            Authentication authentication,
            EcmFile ecmFile,
            InputStream fileInputStream)
            throws MuleException
    {
 
        Map<String, Object> messageProps = new HashMap<>();
        messageProps.put("ecmFileId", ecmFile.getEcmFileId());
        messageProps.put("fileName", ecmFile.getFileName());
        messageProps.put("mimeType", ecmFile.getFileMimeType());
        messageProps.put("inputStream", fileInputStream);
        messageProps.put("acmUser", authentication);
        messageProps.put("auditAdapter", getAuditPropertyEntityAdapter());




        MuleMessage received = getMuleClient().send("vm://updateFile.in", ecmFile, messageProps);
        ObjectId objectId = received.getPayload(ObjectId.class);

        MuleException e = received.getInboundProperty("updateException");
        if ( e != null )
        {
            throw e;
        }
        
        if (null == objectId || !objectId.getId().replaceAll(";.*", "").equals(ecmFile.getEcmFileId()))
        {
        	throw new RuntimeException("Updating of the file " + ecmFile.getFileName() + " failed.");
        }

        Map<String, Object> headers = new HashMap<>();

        MuleMessage response = getMuleClient().send("jms://solrContentFile.in", ecmFile, headers);

        MuleException exc = response.getInboundProperty("saveException");
        if ( exc != null )
        {
            throw exc;
        }

        return ecmFile;
    }
    
    @Override
    public String downloadFileTransaction(EcmFile ecmFile) throws MuleException {
    	try 
		{			
			MuleMessage message = getMuleClient().send("vm://downloadFileFlow.in", ecmFile.getEcmFileId(), null);
			
			String result = getContent((ContentStream) message.getPayload());
			
			return result;
		} 
		catch (MuleException e) 
		{
			log.error("Cannot download file: " + e.getMessage(), e);
			throw e;
		}
    }
    
    private String getContent(ContentStream contentStream)
	{
		String content = "";
		InputStream inputStream = null;
		
		try
        {
			inputStream = contentStream.getStream();
			StringWriter writer = new StringWriter();
			IOUtils.copy(inputStream, writer);
			content = writer.toString();
        } 
		catch (IOException e) 
		{
        	log.error("Could not copy input stream to the writer: " + e.getMessage(), e);
		}
		finally
        {
            if ( inputStream != null )
            {
                try
                {
                	inputStream.close();
                }
                catch (IOException e)
                {
                    log.error("Could not close CMIS content stream: " + e.getMessage(), e);
                }
            }
        }
		
		return content;
	}

    public MuleClient getMuleClient()
    {
        return muleClient;
    }

    public void setMuleClient(MuleClient muleClient)
    {
        this.muleClient = muleClient;
    }

    public AuditPropertyEntityAdapter getAuditPropertyEntityAdapter()
    {
        return auditPropertyEntityAdapter;
    }

    public void setAuditPropertyEntityAdapter(AuditPropertyEntityAdapter auditPropertyEntityAdapter)
    {
        this.auditPropertyEntityAdapter = auditPropertyEntityAdapter;
    }

}
