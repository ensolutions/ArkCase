
/**
 * 
 */
package com.armedia.acm.frevvo.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.armedia.acm.core.exceptions.AcmCreateObjectFailedException;
import com.armedia.acm.file.AcmMultipartFile;
import com.armedia.acm.plugins.ecm.service.EcmFileService;
import com.armedia.acm.services.authenticationtoken.service.AuthenticationTokenService;
import com.armedia.acm.services.users.dao.ldap.UserDao;

/**
 * @author riste.tutureski
 *
 */
public abstract class FrevvoFormAbstractService implements FrevvoFormService{

	private Logger LOG = LoggerFactory.getLogger(FrevvoFormAbstractService.class);
	
	private Map<String, Object> properties;
	private HttpServletRequest request;
	private Authentication authentication;
	private AuthenticationTokenService authenticationTokenService;
	private UserDao userDao;
	private EcmFileService ecmFileService;
    private String servletContextPath;


	@Override
	public Map<String, Object> getProperties() {
		return properties;
	}

	@Override
	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	@Override
	public HttpServletRequest getRequest() {
		return request;
	}

	@Override
	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	@Override
	public Authentication getAuthentication() {
		return authentication;
	}

	@Override
	public void setAuthentication(Authentication authentication) {
		this.authentication = authentication;
	}

	@Override
	public AuthenticationTokenService getAuthenticationTokenService() {
		return authenticationTokenService;
	}

	@Override
	public void setAuthenticationTokenService(
			AuthenticationTokenService authenticationTokenService) {
		this.authenticationTokenService = authenticationTokenService;		
	}

	@Override
	public UserDao getUserDao() {
		return userDao;
	}

	@Override
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}
	
	@Override
	public EcmFileService getEcmFileService()
    {
        return ecmFileService;
    }
	
	@Override
	public void setEcmFileService(EcmFileService ecmFileService)
    {
        this.ecmFileService = ecmFileService;
    }
	
	@Override
	public String getServletContextPath()
    {
        return servletContextPath;
    }

	@Override
    public void setServletContextPath(String servletContextPath)
    {
        this.servletContextPath = servletContextPath;
    }
	
	public Object convertFromXMLToObject(String xml, Class<?> c) {
		Object obj = null;
		try{
			InputStream inputStream = new ByteArrayInputStream(xml.getBytes());
	        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	        Document document = documentBuilder.parse(inputStream);
	        Element element = document.getDocumentElement();
	        JAXBContext context = JAXBContext.newInstance(c);
	        Unmarshaller unmarshaller = context.createUnmarshaller();
	        JAXBElement<?> jaxbElement = unmarshaller.unmarshal(element, c);
	        obj = jaxbElement.getValue();
		}
        catch(Exception e) {
        	LOG.error("Error while creating Object from XML. " + e);
        }
		
		return obj;
	}
	
	public void saveAttachments(MultiValueMap<String, MultipartFile> attachments, String targetCmisFolderId, String parentObjectType, Long parentObjectId, String parentObjectName)
	{
		if ( attachments != null )
		{
			for ( Map.Entry<String, List<MultipartFile>> entry : attachments.entrySet() )
			{
				final List<MultipartFile> attachmentsList = entry.getValue();
		            	
				if (attachmentsList != null && attachmentsList.size() > 0) {
					for (final MultipartFile attachment : attachmentsList) {
						try
						{
							AcmMultipartFile file = new AcmMultipartFile(attachment.getName(), attachment.getOriginalFilename(), attachment.getContentType(), attachment.isEmpty(), attachment.getSize(), attachment.getBytes(), attachment.getInputStream(), true);
		                   
							getEcmFileService().upload(
		                    		file,
		                            "application/json",
		                            getServletContextPath(),
		                            getAuthentication(),
		                            targetCmisFolderId,
		                            parentObjectType,
		                            parentObjectId,
		                            parentObjectName
                    		);
						}
			            catch (AcmCreateObjectFailedException e)
			            {
			                LOG.error("Could not upload file: " + e.getMessage(), e);
				        }
						catch(IOException e1)
						{
							LOG.error("Could not create AcmMultipartFile object: " + e1.getMessage(), e1);
						}
					}
				}
			}
		}
	}

}