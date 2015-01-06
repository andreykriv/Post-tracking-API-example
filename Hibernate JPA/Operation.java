import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;

import ru.aplix.posttrackingapi.russianpost.entity.Attribute;
import ru.aplix.posttrackingapi.services.entity.Track;
import ru.aplix.posttrackingapi.services.util.DateUtil;

@Entity
@Table(name="POSTAPI_OPERATIONS")
public class Operation implements Serializable {

	private static final long serialVersionUID = 6088777485711464813L;

	@Id
	@GeneratedValue(generator="system-uuid")
	@GenericGenerator(name="system-uuid", strategy="uuid")
	@Column(name="ID", unique=true)
	private String id;
	
	@ManyToOne(targetEntity=OperationType.class, cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	@JoinColumn(name="OPER_TYPE_ID", referencedColumnName="ID")
	private OperationType operType;
	
	@ManyToOne(targetEntity=Attribute.class, cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	@JoinColumn(name="ATTR_ID", referencedColumnName="ID")
	private Attribute attribute;
	
	@Column(name="DATE_OPER")
	@Temporal(TemporalType.TIMESTAMP)
    private Date dateOper;
	
	@Column(name="INDEX_OPER")
    private String indexOper;
	
	@Column(name="API_BROADCAST", columnDefinition="bit default 1")
	private Boolean apiBroadcast;
	
	@Column(name="EMAIL_BROADCAST", columnDefinition="bit default 1")
	private Boolean emailBroadcast;
	
	@Column(name="SMS_BROADCAST", columnDefinition="bit default 1")
	private Boolean smppBroadcast;
			
	@ManyToOne(targetEntity=Track.class, cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinColumn(name="TRACK_ID", referencedColumnName="ID")
	private Track track;
	
	@Column(name="TRACK_ID", insertable=false, updatable=false)
	private Long trackId;
	
    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public OperationType getOperType() {
		return operType;
	}

	public void setOperType(OperationType operType) {
		this.operType = operType;
	}	

	public void setDateOper(Date dateOper) {
		this.dateOper = dateOper;
	}  

	public Attribute getAttribute() {
		return attribute;
	}

	public void setAttribute(Attribute attribute) {
		this.attribute = attribute;
	}

	public Date getDateOper() {
        return dateOper;
    }

    public void setDateOper(String value) {
		this.dateOper = DateUtil.parse(value);
    }

    public String getIndexOper() {
        return indexOper;
    }   

    public void setIndexOper(String value) {
        this.indexOper = value;
    }
    	
	public Long getTrackId() {
		return trackId;
	}

	public void setTrackId(Long trackId) {
		this.trackId = trackId;
	}

	public Boolean getApiBroadcast() {
		return apiBroadcast;
	}

	public void setApiBroadcast(Boolean apiBroadcast) {
		this.apiBroadcast = apiBroadcast;
	}	

	public Boolean getEmailBroadcast() {
		return emailBroadcast;
	}

	public void setEmailBroadcast(Boolean emailBroadcast) {
		this.emailBroadcast = emailBroadcast;
	}
	
	public Boolean getSmppBroadcast() {
		return smppBroadcast;
	}

	public void setSmppBroadcast(Boolean smppBroadcast) {
		this.smppBroadcast = smppBroadcast;
	}

	public Track getTrack() {
		return track;
	}

	public void setTrack(Track track) {
		this.track = track;
	}	
}
