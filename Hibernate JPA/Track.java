package ru.aplix.posttrackingapi.services.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="POSTAPI_TRACKS")
public class Track implements Serializable {

	private static final long serialVersionUID = -6622182677902375382L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Basic(optional=false)
	@Column(name="ID")
	private Long id;
	
    @Column(name="BARCODE")
    protected String barcode;
    
    @Column(name="TYPE")
    private String type;
    
    @Column(name="CREATION_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;

    @Column(name="REQUEST_HISTORY", columnDefinition="bit default 1", length=1, nullable=false)
    private Boolean requestHistory;
    
    @Column(name="ACTIVE", columnDefinition="bit default 1", length=1, nullable=false)
    private Boolean active;
    
    @Column(name="HISTORY_UPDATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date historyUpdateTime;   
    
    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getBarcode() {
        return barcode;
    }

	public void setBarcode(String value) {
        this.barcode = value;
    }
	    
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}		

	public Date getHistoryUpdateTime() {
		return historyUpdateTime;
	}

	public void setHistoryUpdateTime(Date historyUpdateTime) {
		this.historyUpdateTime = historyUpdateTime;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Boolean getRequestHistory() {
		return requestHistory;
	}

	public void setRequestHistory(Boolean requestHistory) {
		this.requestHistory = requestHistory;
	}	
}
