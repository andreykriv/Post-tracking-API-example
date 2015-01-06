import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import ru.aplix.posttrackingapi.russianpost.operationhistory.server.HistoryResponse;
import ru.aplix.posttrackingapi.services.model.history.HistoryRequest;
import ru.aplix.posttrackingapi.services.model.postapi.BaseResponse;
import ru.aplix.posttrackingapi.services.model.postapi.PostRequest;
import ru.aplix.posttrackingapi.services.model.postapi.ReportRequest;
import ru.aplix.posttrackingapi.services.model.postapi.SubscriberInfoRequest;
import ru.aplix.posttrackingapi.services.model.postapi.SubscriberInfoResponse;
import ru.aplix.posttrackingapi.services.model.postapi.SubscriberRequest;
import ru.aplix.posttrackingapi.services.model.postapi.TracksRequest;
import ru.aplix.posttrackingapi.services.model.postapi.TracksResponse;
import ru.aplix.posttrackingapi.services.util.ContextProvider;
import ru.aplix.posttrackingapi.webserver.application.RESTService;
import ru.aplix.posttrackingapi.webserver.processor.PostApiProcessor;

@Path("/rest/tracks")
@Consumes({MediaType.APPLICATION_XML})
@Produces({MediaType.APPLICATION_XML})
public class PostApiRESTService extends RESTService {
		
	private PostApiProcessor postApiProcessor;
	
	public PostApiRESTService() {
		super("apiRestParser");
	}

	public PostApiProcessor getPostAPIProcessor() {
		if (postApiProcessor == null)
			postApiProcessor = (PostApiProcessor) 
			ContextProvider.getContext().getBean("webserverPostApiProcessor");
		return postApiProcessor;
	}

	@POST
	@Path("/set")
	public BaseResponse setTracks(PostRequest request, 
			@Context HttpServletRequest httpReq,
			@Context HttpServletResponse httpResp) {
		BaseResponse resp = getPostAPIProcessor().setTracks(request);
		this.setRespTraceSuccess(PostApiRESTService.class, httpReq, httpResp, resp, resp.getSuccess() != null);
		return resp;
	}
	
	@POST
	@Path("/get")
	public TracksResponse getTracks(TracksRequest data,
			@Context HttpServletRequest httpReq,
			@Context HttpServletResponse httpResp) {
		TracksResponse resp = getPostAPIProcessor().getTracks(data);
		this.setRespTraceSuccess(PostApiRESTService.class, httpReq, httpResp, resp, resp.getError().isEmpty());
		return resp;
	}
	
	@POST
	@Path("/subscribe/api")
	public BaseResponse setApiSubscribeStatus(SubscriberRequest request,
			@Context HttpServletRequest httpReq,
			@Context HttpServletResponse httpResp) {
		BaseResponse resp = getPostAPIProcessor().setApiSubscribeStatus(request);
		this.setRespTraceSuccess(PostApiRESTService.class, httpReq, httpResp, resp, resp.getSuccess()!=null);
		return resp;
	}
	
	@POST
	@Path("/subscribe/email")
	public BaseResponse setMailSubscribeStatus(SubscriberRequest request,
			@Context HttpServletRequest httpReq,
			@Context HttpServletResponse httpResp) {
		BaseResponse resp = getPostAPIProcessor().setMailSubscribeStatus(request);
		this.setRespTraceSuccess(PostApiRESTService.class, httpReq, httpResp, resp, resp.getSuccess()!=null);
		return resp;
	}
	
	@POST
	@Path("/subscribe/smpp")
	public BaseResponse setSMPPSubscribeStatus(SubscriberRequest request,
			@Context HttpServletRequest httpReq,
			@Context HttpServletResponse httpResp) {
		BaseResponse resp = getPostAPIProcessor().setSMPPSubscribeStatus(request);
		this.setRespTraceSuccess(PostApiRESTService.class, httpReq, httpResp, resp, resp.getSuccess()!=null);
		return resp;
	}
	
	@POST
	@Path("/subscriber/info")
	public SubscriberInfoResponse receiveApiBroadcastInfo(SubscriberInfoRequest request,
			@Context HttpServletRequest httpReq,
			@Context HttpServletResponse httpResp) {
		SubscriberInfoResponse resp = getPostAPIProcessor().receiveApiBroadcastInfo(request);
		this.setRespTraceSuccess(PostApiRESTService.class, httpReq, httpResp, resp, resp.getError().isEmpty());
		return resp;
	}
	
	@POST
	@Path("/history")
	public HistoryResponse getTrackHistory(HistoryRequest request,
			@Context HttpServletRequest httpReq,
			@Context HttpServletResponse httpResp) {
		HistoryResponse resp = getPostAPIProcessor().getTrackHistory(request);
		this.setRespTraceSuccess(PostApiRESTService.class, httpReq, httpResp, resp, resp.getError().isEmpty());
		return resp;
	}

	@POST
	@Path("/report")
	public BaseResponse receiveErrorReport(ReportRequest request,
			@Context HttpServletRequest httpReq,
			@Context HttpServletResponse httpResp) {
		BaseResponse resp = getPostAPIProcessor().receiveErrorReport(request);
		this.setRespTraceSuccess(PostApiRESTService.class, httpReq, httpResp, resp, resp.getSuccess() != null);
		return resp; 
	}
	
}
