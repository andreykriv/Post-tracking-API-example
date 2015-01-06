
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ru.aplix.posttrackingapi.russianpost.client.request.OperationXML;
import ru.aplix.posttrackingapi.russianpost.dao.AttributesDao;
import ru.aplix.posttrackingapi.russianpost.entity.Attribute;
import ru.aplix.posttrackingapi.russianpost.operationhistory.dao.OperationDao;
import ru.aplix.posttrackingapi.russianpost.operationhistory.dao.OperationTypeDao;
import ru.aplix.posttrackingapi.russianpost.operationhistory.entity.Operation;
import ru.aplix.posttrackingapi.russianpost.operationhistory.entity.OperationType;
import ru.aplix.posttrackingapi.services.dao.TracksDao;
import ru.aplix.posttrackingapi.services.entity.Track;
import ru.aplix.posttrackingapi.services.util.DateUtil;

@Service("russianpostOperationService")
public class OperationDBService {
	
	@Autowired
	private TracksDao tracksDao;
	@Autowired
	protected OperationDao operationDao;
	@Autowired
	protected OperationTypeDao operationTypesDao;
	@Autowired
	protected AttributesDao attributesDao;

	@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
	public void addOperations(String barcode, List<OperationXML> opers) {
		List<Operation> dbOpers = operationDao.getTrackOperations(track.getBarcode());
		Track dbTrack = tracksDao.getTrackByBarcode(barcode);
		for (OperationXML oper : opers) {
			Date operDate = oper.getDateOper() == null ? null : DateUtil.parse(oper.getDateOper());
			boolean exists = false;
			for (Operation dbOper : dbOpers) {
				OperationType type = dbOper.getOperType();
				if (type.getTypeId().equals(oper.getOperTypeId()) && 
					oper.getIndexOper().trim().equals(oper.getIndexOper()) &&
					operDate != null && operDate.equals(dbOper.getDateOper())) {
					exists = true;
					break;
				}
			}
			if (!exists && operDate != null) {
				Operation trackOper = new Operation();
				trackOper.setTrack(dbTrack);
				OperationType type = operationTypesDao.getOperType(oper.getOperTypeId(), oper.getOperName());
				trackOper.setOperType(type);
				trackOper.setAttribute(attributesDao.getAttribute(oper.getOperTypeId(), oper.getOperCtgId()));
				trackOper.setDateOper(operDate);
				trackOper.setIndexOper(oper.getIndexOper());
				trackOper.setApiBroadcast(true);
				trackOper.setEmailBroadcast(true);
				trackOper.setSmppBroadcast(true);
				operationDao.add(trackOper);
			}
		}		
	}
	/* TODO:service methods... */
}
