package com.armedia.acm.services.notification.dao;

/*-
 * #%L
 * ACM Service: Notification
 * %%
 * Copyright (C) 2014 - 2018 ArkCase LLC
 * %%
 * This file is part of the ArkCase software. 
 * 
 * If the software was purchased under a paid ArkCase license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * ArkCase is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * ArkCase is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with ArkCase. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.armedia.acm.data.AcmAbstractDao;
import com.armedia.acm.services.notification.model.Notification;
import com.armedia.acm.services.notification.model.NotificationConstants;
import com.armedia.acm.services.notification.model.NotificationRule;
import com.armedia.acm.services.notification.service.CustomTitleFormatter;
import com.armedia.acm.services.notification.service.NotificationUtils;
import com.armedia.acm.services.notification.service.UsersNotified;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Parameter;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NotificationDao extends AcmAbstractDao<Notification>
{
    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @PersistenceContext
    private EntityManager entityManager;

    private NotificationUtils notificationUtils;

    @Override
    protected Class<Notification> getPersistenceClass()
    {
        return Notification.class;
    }

    @Override
    public String getSupportedObjectType()
    {
        return NotificationConstants.OBJECT_TYPE;
    }

    @Transactional
    public int purgeNotifications(Date threshold)
    {
        int retval = 0;

        Query update = getEm().createQuery("DELETE FROM " +
                "Notification notification " +
                "WHERE " +
                "notification.modified <= :threshold");

        update.setParameter("threshold", threshold);

        try
        {
            retval = update.executeUpdate();
        }
        catch (Exception e)
        {
            LOG.error("Cannot purge notifications.", e);
        }

        return retval;
    }

    public List<Notification> listNotifications(String user)
    {
        String everyone = "EVERYONE";
        Query notification = getEntityManager().createQuery(
                "SELECT notification " +
                        "FROM Notification notification " +
                        "WHERE notification.user = :user " +
                        "OR notification.user = :everyone " +
                        "AND notification.status != 'DELETE'");
        notification.setParameter("user", user);
        notification.setParameter("everyone", everyone);

        List<Notification> notifications = (List<Notification>) notification.getResultList();
        if (null == notifications)
        {
            notifications = new ArrayList();
        }
        return notifications;
    }

    @Transactional
    public void deleteNotificationById(Long id)
    {
        Query queryToDelete = getEntityManager().createQuery(
                "SELECT notification " + "FROM Notification notification " +
                        "WHERE notification.id = :notificationId");
        queryToDelete.setParameter("notificationId", id);

        Notification notificationToBeDeleted = (Notification) queryToDelete.getSingleResult();
        entityManager.remove(notificationToBeDeleted);
    }

    /**
     * This method is used only for notification rules. We have two possibilities - when we want to create notifications
     * ('create' should be true) and get already existing notifications that rich the rules that we want to update
     *
     * @param parameters
     * @param firstResult
     * @param maxResult
     * @param rule
     * @return
     */
    public List<Notification> executeQuery(Map<String, Object> parameters, int firstResult, int maxResult, NotificationRule rule)
    {
        List<Notification> notifications = new ArrayList<>();

        switch (rule.getQueryType())
        {
        case CREATE:
            notifications = createNotifications(parameters, firstResult, maxResult, rule);
            break;

        case SELECT:
            notifications = getNotifications(parameters, firstResult, maxResult, rule.getJpaQuery());
            break;
        }

        return notifications;
    }

    /**
     * This method is called when we have mixed query and depends on that query, new notification should be created.
     *
     * @param parameters
     * @param firstResult
     * @param maxResult
     * @param rule
     * @return
     */
    private List<Notification> createNotifications(Map<String, Object> parameters, int firstResult, int maxResult,
            NotificationRule rule)
    {
        TypedQuery<Object[]> select = getEm().createQuery(rule.getJpaQuery(), Object[].class);

        select = (TypedQuery<Object[]>) populateQueryParameters(select, parameters);
        select.setFirstResult(firstResult);
        select.setMaxResults(maxResult);

        List<Notification> notifications = new ArrayList<>();

        for (Object[] obj : select.getResultList())
        {
            Long parentId = (Long) obj[3];
            String parentType = (String) obj[4];
            Long relatedObjectId = (Long) obj[7];
            String relatedObjectType = (String) obj[8];
            String objectType = relatedObjectType != null ? relatedObjectType : parentType;
            Long objectId = relatedObjectType != null ? relatedObjectId : parentId;
            UsersNotified usersNotified = rule.getUsersNotified();
            List<Notification> notificationsForAssociatedUsers = usersNotified.getNotifications(obj, objectId, objectType);

            setNotificationsTitle(notificationsForAssociatedUsers, rule);
            setNotificationsRelatedObjectNumber(notificationsForAssociatedUsers);

            notifications.addAll(notificationsForAssociatedUsers);
        }

        return notifications;
    }

    private void setNotificationsTitle(List<Notification> notifications, NotificationRule rule)
    {
        CustomTitleFormatter customTitleFormatter = rule.getCustomTitleFormatter();
        if (customTitleFormatter != null)
        {
            notifications.forEach(notification -> {
                String title = customTitleFormatter.format(notification);
                notification.setTitle(title);
                notification.setNote(title);
            });
        }
    }

    private void setNotificationsRelatedObjectNumber(List<Notification> notifications)
    {
        notifications.forEach(notification -> {
            String relatedObjectNumber = getNotificationUtils()
                    .getNotificationParentOrRelatedObjectNumber(notification.getRelatedObjectType(),
                            notification.getRelatedObjectId());
            notification.setRelatedObjectNumber(relatedObjectNumber);
        });
    }

    /**
     * This method is called when we have to take already existing notifications with rules defined in the query
     *
     * @param parameters
     * @param firstResult
     * @param maxResult
     * @param query
     * @return
     */
    private List<Notification> getNotifications(Map<String, Object> parameters, int firstResult, int maxResult, String query)
    {
        Query select = getEm().createQuery(query);

        select = populateQueryParameters(select, parameters);
        select.setFirstResult(firstResult);
        select.setMaxResults(maxResult);

        @SuppressWarnings("unchecked")
        List<Notification> retval = (List<Notification>) select.getResultList();

        if (null == retval)
        {
            retval = new ArrayList<>();
        }

        return retval;
    }

    /**
     * This method will populate query parameters. If JPQL don't have specific property, that will be excluded from
     * adding them in the query
     *
     * @param query
     * @param parameters
     * @return
     */
    private Query populateQueryParameters(Query query, Map<String, Object> parameters)
    {
        if (parameters != null)
        {
            // Get parameters that are in the query
            Set<Parameter<?>> queryParameters = query.getParameters();

            if (queryParameters != null)
            {
                for (Parameter<?> queryParameter : queryParameters)
                {
                    // Take query parameter name
                    String key = queryParameter.getName();

                    // If query parameter name exist in the provided parameters, take the value and add it
                    if (parameters.containsKey(key))
                    {
                        query.setParameter(key, parameters.get(key));
                    }
                }
            }
        }

        return query;
    }

    public EntityManager getEntityManager()
    {
        return entityManager;
    }

    public NotificationUtils getNotificationUtils()
    {
        return notificationUtils;
    }

    public void setNotificationUtils(NotificationUtils notificationUtils)
    {
        this.notificationUtils = notificationUtils;
    }
}
