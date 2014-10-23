package com.armedia.acm.services.search.model.solr;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by armdev on 10/21/14.
 */
public class SolrAdvancedSearchDocument implements SolrBaseDocument
{
    private String id;
    private String object_id_s;
    private String object_type_s;
    private String title_parseable;
    private String name;
    private Date incident_date_tdt;
    private String priority_lcs;
    private String assignee_id_lcs;
    private String assignee_first_name_lcs;
    private String assignee_last_name_lcs;
    private String incident_type_lcs;
    private String status_lcs;
    private String person_title_lcs;
    private String first_name_lcs;
    private String last_name_lcs;
    private String type_lcs;
    private String value_parseable;
    private String location_street_address_lcs;
    private String location_city_lcs;
    private String location_state_lcs;
    private String location_postal_code_sdo;
    private List<SolrBaseDocument> _childDocuments_ = new ArrayList<>();
    private String child_id_s;
    private String child_type_s;
    private String parent_id_s;
    private String parent_type_s;
    private String description_parseable;

    @Override
    public String getId()
    {
        return id;
    }

    @Override
    public void setId(String id)
    {
        this.id = id;
    }

    public String getObject_id_s()
    {
        return object_id_s;
    }

    public void setObject_id_s(String object_id_s)
    {
        this.object_id_s = object_id_s;
    }

    public String getObject_type_s()
    {
        return object_type_s;
    }

    public void setObject_type_s(String object_type_s)
    {
        this.object_type_s = object_type_s;
    }

    public String getTitle_parseable()
    {
        return title_parseable;
    }

    public void setTitle_parseable(String title_parseable)
    {
        this.title_parseable = title_parseable;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Date getIncident_date_tdt()
    {
        return incident_date_tdt;
    }

    public void setIncident_date_tdt(Date incident_date_tdt)
    {
        this.incident_date_tdt = incident_date_tdt;
    }

    public String getPriority_lcs()
    {
        return priority_lcs;
    }

    public void setPriority_lcs(String priority_lcs)
    {
        this.priority_lcs = priority_lcs;
    }

    public String getAssignee_id_lcs()
    {
        return assignee_id_lcs;
    }

    public void setAssignee_id_lcs(String assignee_id_lcs)
    {
        this.assignee_id_lcs = assignee_id_lcs;
    }

    public String getAssignee_first_name_lcs()
    {
        return assignee_first_name_lcs;
    }

    public void setAssignee_first_name_lcs(String assignee_first_name_lcs)
    {
        this.assignee_first_name_lcs = assignee_first_name_lcs;
    }

    public String getAssignee_last_name_lcs()
    {
        return assignee_last_name_lcs;
    }

    public void setAssignee_last_name_lcs(String assignee_last_name_lcs)
    {
        this.assignee_last_name_lcs = assignee_last_name_lcs;
    }

    public String getIncident_type_lcs()
    {
        return incident_type_lcs;
    }

    public void setIncident_type_lcs(String incident_type_lcs)
    {
        this.incident_type_lcs = incident_type_lcs;
    }

    public String getStatus_lcs()
    {
        return status_lcs;
    }

    public void setStatus_lcs(String status_lcs)
    {
        this.status_lcs = status_lcs;
    }

    public String getPerson_title_lcs()
    {
        return person_title_lcs;
    }

    public void setPerson_title_lcs(String person_title_lcs)
    {
        this.person_title_lcs = person_title_lcs;
    }

    public String getFirst_name_lcs()
    {
        return first_name_lcs;
    }

    public void setFirst_name_lcs(String first_name_lcs)
    {
        this.first_name_lcs = first_name_lcs;
    }

    public String getLast_name_lcs()
    {
        return last_name_lcs;
    }

    public void setLast_name_lcs(String last_name_lcs)
    {
        this.last_name_lcs = last_name_lcs;
    }

    public String getLocation_street_address_lcs()
    {
        return location_street_address_lcs;
    }

    public void setLocation_street_address_lcs(String location_street_address_lcs)
    {
        this.location_street_address_lcs = location_street_address_lcs;
    }

    public String getLocation_city_lcs()
    {
        return location_city_lcs;
    }

    public void setLocation_city_lcs(String location_city_lcs)
    {
        this.location_city_lcs = location_city_lcs;
    }

    public String getLocation_state_lcs()
    {
        return location_state_lcs;
    }

    public void setLocation_state_lcs(String location_state_lcs)
    {
        this.location_state_lcs = location_state_lcs;
    }

    public String getLocation_postal_code_sdo()
    {
        return location_postal_code_sdo;
    }

    public void setLocation_postal_code_sdo(String location_postal_code_sdo)
    {
        this.location_postal_code_sdo = location_postal_code_sdo;
    }

    public String getType_lcs()
    {
        return type_lcs;
    }

    public void setType_lcs(String type_lcs)
    {
        this.type_lcs = type_lcs;
    }

    public String getValue_parseable()
    {
        return value_parseable;
    }

    public void setValue_parseable(String value_parseable)
    {
        this.value_parseable = value_parseable;
    }

    public List<SolrBaseDocument> get_childDocuments_()
    {
        return _childDocuments_;
    }

    public void set_childDocuments_(List<SolrBaseDocument> _childDocuments_)
    {
        this._childDocuments_ = _childDocuments_;
    }

    public void setChild_id_s(String child_id_s)
    {
        this.child_id_s = child_id_s;
    }

    public String getChild_id_s()
    {
        return child_id_s;
    }

    public void setChild_type_s(String child_type_s)
    {
        this.child_type_s = child_type_s;
    }

    public String getChild_type_s()
    {
        return child_type_s;
    }


    public void setParent_id_s(String parent_id_s)
    {
        this.parent_id_s = parent_id_s;
    }

    public String getParent_id_s()
    {
        return parent_id_s;
    }

    public void setParent_type_s(String parent_type_s)
    {
        this.parent_type_s = parent_type_s;
    }

    public String getParent_type_s()
    {
        return parent_type_s;
    }

    public void setDescription_parseable(String description_parseable)
    {
        this.description_parseable = description_parseable;
    }

    public String getDescription_parseable()
    {
        return description_parseable;
    }
}
