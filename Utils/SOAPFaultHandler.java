import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

public class SOAPFaultHandler<T> {
	
	public T faultResponse;
	public Class<T> faultClass;
	
	public SOAPFaultHandler(Class<T> faultClass) {
		this.faultClass = faultClass;
	}
	
	public void handleFault(SOAPMessage response, Unmarshaller unmarshaller) throws JAXBException, SOAPException {
		JAXBElement<T> fault = unmarshaller.unmarshal(response.getSOAPBody().extractContentAsDocument(), this.faultClass);
		if (fault != null)
			this.faultResponse = fault.getValue();
	}
	
	public T getFault() {
		return this.faultResponse;
	}

}
