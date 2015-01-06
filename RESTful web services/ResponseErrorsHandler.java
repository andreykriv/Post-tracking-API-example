import org.jboss.resteasy.spi.BadRequestException;
import org.jboss.resteasy.spi.MethodNotAllowedException;
import org.jboss.resteasy.spi.NotAcceptableException;
import org.jboss.resteasy.spi.NotFoundException;
import org.jboss.resteasy.spi.UnsupportedMediaTypeException;
import org.springframework.beans.factory.annotation.Autowired;

import ru.aplix.posttrackingapi.services.util.Logger;
import ru.aplix.posttrackingapi.services.util.MessagesUtil;

public class ResponseErrorsHandler {
	
	public class ErrorMessage {
		private int status;
		private String message;
		
		ErrorMessage(Integer status, String message) {
			this.status = status;
			this.message = message;
		}
		
		public int getStatus() {
			return status;
		}		
		public String getMessage() {
			return message;
		}
	}
	
	@Autowired
	private MessagesUtil messages;
	@Autowired
	private Logger logger;
	
	public void setMessages(MessagesUtil messages) {
		this.messages = messages;
	}	

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public <T extends Exception> ErrorMessage getResponse(T ex) {
		String errorMessage = this.messages.get("request.validation.internal");
		int status = 500;
		if (ex instanceof BadRequestException) {
			errorMessage = this.messages.get("request.validation.badrequest");
			status = 400;
		}
		else if (ex instanceof MethodNotAllowedException) {
			errorMessage = this.messages.get("request.validation.notallowed");
			status = 405;
		}
		else if (ex instanceof NotFoundException) {
			errorMessage = this.messages.get("request.validation.notfound");
			status = 404;
		}
		else if (ex instanceof NotAcceptableException) {
			errorMessage = this.messages.get("request.validation.unsupported");
			status = 406;
		}
		else if (ex instanceof UnsupportedMediaTypeException) {
			errorMessage = this.messages.get("request.validation.method");
			status = 404;
		}
		if (status == 500)
			logger.error(ResponseErrorsHandler.class, "Internal server error", ex);
		return new ErrorMessage(status, errorMessage);
	}
}

