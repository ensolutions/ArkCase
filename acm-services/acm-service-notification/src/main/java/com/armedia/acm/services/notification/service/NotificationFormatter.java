package com.armedia.acm.services.notification.service;

import com.armedia.acm.core.AcmApplication;
import com.armedia.acm.core.AcmObjectType;
import com.armedia.acm.services.notification.model.Notification;
import com.armedia.acm.services.notification.model.NotificationConstants;

import java.util.Properties;

/**
 * Created by armdev on 6/26/15.
 */
public class NotificationFormatter
{
    private Properties notificationProperties;

    private AcmApplication acmAppConfiguration;

    public Notification replaceFormatPlaceholders(Notification notification)
    {

        String objectTypeLabelPlaceholder = NotificationConstants.OBJECT_TYPE_LABEL_PLACEHOLDER;
        String anchorPlaceholder = NotificationConstants.ANCHOR_PLACEHOLDER;

        if (notification.getTitle() != null && notification.getTitle().contains(objectTypeLabelPlaceholder))
        {
            String updatedTitle = replaceObjectTypeLabel(notification.getTitle(), objectTypeLabelPlaceholder,
                    notification.getParentType());
            notification.setTitle(updatedTitle);
        }

        if (notification.getNote() != null && notification.getNote().contains(objectTypeLabelPlaceholder))
        {
            String updatedNote = replaceObjectTypeLabel(notification.getNote(), objectTypeLabelPlaceholder,
                    notification.getParentType());
            notification.setNote(updatedNote);
        }

        if (notification.getNote() != null && notification.getNote().contains(anchorPlaceholder))
        {
            String updatedNote = replaceAnchor(notification.getNote(), anchorPlaceholder, notification.getParentType(),
                    notification.getParentId(), notification.getRelatedObjectType(), notification.getRelatedObjectId());
            notification.setNote(updatedNote);
        }

        return notification;


    }

    private String replaceAnchor(String withPlaceholder, String anchorPlaceholder, String parentType, Long parentId, String relatedObjectType, Long relatedObjectId)
    {
        String keyBaseUrl = "arkcase.url.base";

        String baseUrl = getNotificationProperties().getProperty(keyBaseUrl);

        String url = "";
        // find the object type from the ACM application configuration, and get the URL from the object type
        for (AcmObjectType objectType : getAcmAppConfiguration().getObjectTypes())
        {
            String objectUrl = objectType.getUrl();

            if (objectType.getName().equals(parentType))
            {
                if (!objectUrl.isEmpty())
                {
                    // The parent object is top level (Case File or Complaint)
                    url = String.format("%s%s/%d", baseUrl, objectUrl, parentId);
                } else if (objectUrl.isEmpty() && relatedObjectType != null)
                // The parent object (eg. Person association) is nested in top level object = relatedObjectType
                {
                    // Find the target container url
                    if (relatedObjectType.equals(NotificationConstants.NOTIFICATION_CONTAINER_CASE))
                    {
                        objectUrl = objectType.getUrlContainerCase();
                    } else if (relatedObjectType.equals(NotificationConstants.NOTIFICATION_CONTAINER_COMPLAINT))
                    {
                        objectUrl = objectType.getUrlContainerComplaint();
                    }
                    url = String.format("%s%s/%d%s", baseUrl, objectUrl, relatedObjectId, objectType.getUrlEnd());
                }
            }

            // The parent object is top level, but has nested object = relatedObjectType (eg. NOTE)
            if (relatedObjectType != null && objectUrl.isEmpty() && objectType.getName().equals(relatedObjectType))
            {
                // Find the target container url
                if (parentType.equals(NotificationConstants.NOTIFICATION_CONTAINER_CASE))
                {
                    objectUrl = objectType.getUrlContainerCase();
                } else if (parentType.equals(NotificationConstants.NOTIFICATION_CONTAINER_COMPLAINT))
                {
                    objectUrl = objectType.getUrlContainerComplaint();
                }
                url = String.format("%s%s/%d%s", baseUrl, objectUrl, parentId, objectType.getUrlEnd());
            }
        }

        if (!url.isEmpty())
        {
            String withAnchor = withPlaceholder.replace(anchorPlaceholder, url);
            return withAnchor;
        }

        return withPlaceholder;

    }

    private String replaceObjectTypeLabel(String withPlaceholder, String placeholder, String parentType)
    {
        String keyLabel = parentType + ".label";
        String objectTypeLabel = getNotificationProperties().getProperty(keyLabel);
        String withObjectType = withPlaceholder.replace(placeholder, objectTypeLabel);

        return withObjectType;
    }

    public Properties getNotificationProperties()
    {
        return notificationProperties;
    }

    public void setNotificationProperties(Properties notificationProperties)
    {
        this.notificationProperties = notificationProperties;
    }

    public AcmApplication getAcmAppConfiguration()
    {
        return acmAppConfiguration;
    }

    public void setAcmAppConfiguration(AcmApplication acmAppConfiguration)
    {
        this.acmAppConfiguration = acmAppConfiguration;
    }

}
