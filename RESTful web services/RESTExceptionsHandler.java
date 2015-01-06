import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import ru.aplix.posttrackingapi.services.model.postapi.BaseResponse;
import ru.aplix.posttrackingapi.services.util.ContextProvider;
import ru.aplix.posttrackingapi.webserver.xml.provider.ResponseErrorsHandler.ErrorMessage;

@Provider
public class RESTExceptionsHandler implements ExceptionMapper<Exception> {
	
	private ResponseErrorsHandler responseErrorsHandler;

	public ResponseErrorsHandler getResponseErrorsHandler() {
		if (responseErrorsHandler == null)
			responseErrorsHandler = (ResponseErrorsHandler) 
			ContextProvider.getContext().getBean("responseErrorsHandler");
		return responseErrorsHandler;
	}
	
	@Override
	public Response toResponse(Exception ex) {
		ErrorMessage msg = getResponseErrorsHandler().getResponse(ex);
		BaseResponse resp = new BaseResponse();
		resp.getError().add(msg.getMessage());
		ResponseBuilder respBuilder = Response.status(msg.getStatus());
		respBuilder.entity(resp);
		return respBuilder.build();
	}
}
