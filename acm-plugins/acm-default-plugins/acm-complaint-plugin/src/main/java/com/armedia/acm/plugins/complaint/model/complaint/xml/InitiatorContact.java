/**
 * 
 */
package com.armedia.acm.plugins.complaint.model.complaint.xml;

/*-
 * #%L
 * ACM Default Plugin: Complaints
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

import com.armedia.acm.plugins.addressable.model.ContactMethod;
import com.armedia.acm.plugins.addressable.model.PostalAddress;
import com.armedia.acm.plugins.addressable.model.xml.InitiatorContactMethod;
import com.armedia.acm.plugins.addressable.model.xml.InitiatorPostalAddress;
import com.armedia.acm.plugins.complaint.model.complaint.Contact;
import com.armedia.acm.plugins.complaint.model.complaint.MainInformation;
import com.armedia.acm.plugins.complaint.model.complaint.SearchResult;
import com.armedia.acm.plugins.person.model.Organization;
import com.armedia.acm.plugins.person.model.PersonAlias;
import com.armedia.acm.plugins.person.model.xml.InitiatorOrganization;
import com.armedia.acm.plugins.person.model.xml.InitiatorPersonAlias;

import javax.xml.bind.annotation.XmlElement;

import java.util.List;

/**
 * @author riste.tutureski
 *
 */
public class InitiatorContact extends Contact
{

    @XmlElement(name = "initiatorId")
    @Override
    public Long getId()
    {
        return super.getId();
    }

    @Override
    public void setId(Long id)
    {
        super.setId(id);
    }

    @XmlElement(name = "initiatorContactType")
    @Override
    public String getContactType()
    {
        return super.getContactType();
    }

    @Override
    public void setContactType(String contactType)
    {
        super.setContactType(contactType);
    }

    @XmlElement(name = "searchExistingInitiator", type = InitiatorSearchResult.class)
    @Override
    public SearchResult getSearchResult()
    {
        return super.getSearchResult();
    }

    @Override
    public void setSearchResult(SearchResult searchResult)
    {
        super.setSearchResult(searchResult);
    }

    @XmlElement(name = "initiatorMainInformation", type = InitiatorMainInformation.class)
    @Override
    public MainInformation getMainInformation()
    {
        return super.getMainInformation();
    }

    @Override
    public void setMainInformation(MainInformation mainInformation)
    {
        super.setMainInformation(mainInformation);
    }

    @XmlElement(name = "initiatorCommunicationDevice", type = InitiatorContactMethod.class)
    @Override
    public List<ContactMethod> getCommunicationDevice()
    {
        return super.getCommunicationDevice();
    }

    @Override
    public void setCommunicationDevice(List<ContactMethod> communicationDevice)
    {
        super.setCommunicationDevice(communicationDevice);
    }

    @XmlElement(name = "initiatorOrganizationInformation", type = InitiatorOrganization.class)
    @Override
    public List<Organization> getOrganization()
    {
        return super.getOrganization();
    }

    @Override
    public void setOrganization(List<Organization> organization)
    {
        super.setOrganization(organization);
    }

    @XmlElement(name = "initiatorLocationInformation", type = InitiatorPostalAddress.class)
    @Override
    public List<PostalAddress> getLocation()
    {
        return super.getLocation();
    }

    @Override
    public void setLocation(List<PostalAddress> location)
    {
        super.setLocation(location);
    }

    @XmlElement(name = "initiatorAliasInformation", type = InitiatorPersonAlias.class)
    @Override
    public PersonAlias getAlias()
    {
        return super.getAlias();
    }

    @Override
    public void setAlias(PersonAlias alias)
    {
        super.setAlias(alias);
    }

    @XmlElement(name = "initiatorNotes")
    @Override
    public String getNotes()
    {
        return super.getNotes();
    }

    @Override
    public void setNotes(String notes)
    {
        super.setNotes(notes);
    }

    @Override
    public Contact returnBase()
    {
        Contact base = new Contact();

        base.setId(getId());
        base.setContactType(getContactType());
        base.setSearchResult(getSearchResult());
        base.setMainInformation(getMainInformation());
        base.setCommunicationDevice(getCommunicationDevice());
        base.setOrganization(getOrganization());
        base.setLocation(getLocation());
        base.setAlias(getAlias());
        base.setNotes(getNotes());

        return base;
    }

}
