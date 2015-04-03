/**
 * 
 */
package com.armedia.acm.form.time.model;

import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.armedia.acm.form.config.xml.ApproverItem;
import com.armedia.acm.frevvo.config.FrevvoFormName;
import com.armedia.acm.frevvo.config.FrevvoFormNamespace;
import com.armedia.acm.frevvo.model.FrevvoForm;
import com.armedia.acm.objectonverter.adapter.DateFrevvoAdapter;

/**
 * @author riste.tutureski
 *
 */
@XmlRootElement(name="form_" + FrevvoFormName.TIMESHEET, namespace=FrevvoFormNamespace.TIMESHEET_NAMESPACE)
public class TimeForm extends FrevvoForm {

	private Long id;
	private String user;
	private List<String> userOptions;	
	private Date period;
	private List<TimeItem> items;
	private String status;
	private List<String> statusOptions;
	private String details;
	private List<ApproverItem> approvers;
	private List<String> approverOptions;
	private List<String> totals;
	
	@XmlElement(name="id")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@XmlElement(name="user")
	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	@XmlTransient
	public List<String> getUserOptions() {
		return userOptions;
	}

	public void setUserOptions(List<String> userOptions) {
		this.userOptions = userOptions;
	}	
	
	@XmlElement(name="period")
	@XmlJavaTypeAdapter(value=DateFrevvoAdapter.class)
	public Date getPeriod() {
		return period;
	}
	
	public void setPeriod(Date period) {
		this.period = period;
	}
	
	@XmlElement(name="timeTableItem")
	public List<TimeItem> getItems() {
		return items;
	}
	
	public void setItems(List<TimeItem> items) {
		this.items = items;
	}

	@XmlElement(name="status")
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@XmlTransient
	public List<String> getStatusOptions() {
		return statusOptions;
	}

	public void setStatusOptions(List<String> statusOptions) {
		this.statusOptions = statusOptions;
	}

	@XmlElement(name="details")
	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}
	
	@XmlElement(name="approverItem")
	public List<ApproverItem> getApprovers() {
		return approvers;
	}

	public void setApprovers(List<ApproverItem> approvers) {
		this.approvers = approvers;
	}

	@XmlTransient
	public List<String> getApproverOptions() {
		return approverOptions;
	}

	public void setApproverOptions(List<String> approverOptions) {
		this.approverOptions = approverOptions;
	}
	
	@XmlElement(name="totalTableItem")
	public List<String> getTotals() {
		return totals;
	}

	public void setTotals(List<String> totals) {
		this.totals = totals;
	}
}