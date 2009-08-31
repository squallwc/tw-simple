package com.squallwc.twsimple;

import java.util.Date;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class LatestPost {
    @PrimaryKey
    private String screenName;
	
    @Persistent
    private Long statusId;
    
	@Persistent
    private Date lastUpdateDate;

	public LatestPost(String screenName, Long statusId, Date lastUpdateDate)
	{
		this.screenName = screenName;
		this.statusId = statusId;
		this.lastUpdateDate=lastUpdateDate;
	}

	public String getScreenName() {
		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	public Date getLastUpdateDate() {
		return lastUpdateDate;
	}

	public void setLastUpdateDate(Date lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}

	public Long getStatusId() {
		return statusId;
	}

	public void setStatusId(Long statusId) {
		this.statusId = statusId;
	}
}
