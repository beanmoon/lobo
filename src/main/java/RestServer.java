import com.google.gson.Gson;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlets.GzipFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class RestServer {
	static Logger logger = LoggerFactory.getLogger(RestServer.class);

	static Gson gson = new Gson();

	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		logger.info("Start to load Spring config file");
		int port = 8080;

		logger.info("Prepare to start the Rest Server on {}", port);
		Server server = new Server(port);

		ServletHandler handler = new ServletHandler();
		server.setHandler(handler);

		FilterHolder holder = new FilterHolder(GzipFilter.class);
		// holder.setInitParameter("mimetypes", MimeTypeUtils.APPLICATION_JSON_VALUE);
		holder.setInitParameter("mimetypes", "application/json");
		holder.setInitParameter("deflateCompressionLevel", "9");
		holder.setInitParameter("minGzipSize", "0");
		holder.setInitParameter("methods", "GET,POST");

		handler.addFilterWithMapping(holder, "/*", EnumSet.of(DispatcherType.REQUEST));

		handler.addServletWithMapping(Mv5GetRecResultServlet.class, "/recommend/mv5/list");
		handler.addServletWithMapping(Mv5GetCoudanResultServlet.class, "/recommend/mv5/free");
		handler.addServletWithMapping(JdxhItemTagsServlet.class, "/recommend/mv5/tags");

		server.start();
		logger.info("Server started successfully.");

		server.join();
	}

	@SuppressWarnings("serial")
	public static class Mv5GetRecResultServlet extends HttpServlet {

		@Override
		protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			doPostAction(request, response, ActionType.GET_REC);
		}
	}

	@SuppressWarnings("serial")
	public static class Mv5GetCoudanResultServlet extends HttpServlet {

		@Override
		protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			doPostAction(request, response, ActionType.GET_COUDAN);
		}
	}

	@SuppressWarnings("serial")
	public static class JdxhItemTagsServlet extends HttpServlet {

		@Override
		protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			doPostAction(request, response, ActionType.GET_TAGS);
		}
	}

	static void doPostAction(HttpServletRequest request, HttpServletResponse response, ActionType actionType) throws ServletException, IOException {

		try {
			Mv5RequestBody requestJsonObject = gson.fromJson(request.getReader(), Mv5RequestBody.class);

			if (requestJsonObject == null) {
				logger.error("Invalid request input " + request.getReader());
				throw new DetailedRuntimeException(ParamValidationErrorCode.INVALID_INPUT);
			}

			logger.info("params {}", gson.toJson(requestJsonObject));

			Object r;
			switch (actionType) {
			case GET_REC:
				overallFuncMonHandler = metricsSender.createFuncHandler("jinggang.pdserver." + configService.getConfig().get(CommonConstants.CONFIG_METRICS_PREFIX, String.class)
						+ ".rec.overall.timecost");
				overallFuncMonHandler.startFuncMonitor();
				r = scoringService.getRecResult(requestJsonObject);
				break;
			case GET_COUDAN:
				overallFuncMonHandler = metricsSender.createFuncHandler("jinggang.pdserver." + configService.getConfig().get(CommonConstants.CONFIG_METRICS_PREFIX, String.class)
						+ ".coudan.overall.timecost");
				overallFuncMonHandler.startFuncMonitor();
				r = scoringService.getCoudanResult(requestJsonObject);
				break;
			case GET_TAGS:
				overallFuncMonHandler = metricsSender.createFuncHandler("jinggang.pdserver." + configService.getConfig().get(CommonConstants.CONFIG_METRICS_PREFIX, String.class)
						+ ".tags.overall.timecost");
				overallFuncMonHandler.startFuncMonitor();
				r = scoringService.getSkuTags(requestJsonObject);
				break;
			default:
				r = null;
				break;
			}

			Map<String, Object> result = new HashMap<>();
			result.put("data", r);
			Error err = new Error();
			err.code = 0;
			err.message = "";
			result.put("error", err);

			response.setContentType(AppCommonConstants.SCORINGSERVER_CONTENTTYPE_JSON_UTF8);
			response.setStatus(HttpServletResponse.SC_OK);

			response.getWriter().println(gson.toJson(result));
		} catch (DetailedRuntimeException e) {
			logger.error("Error happend when processing: " + e.getErrorMessage(), e);

			Map<String, Object> result = new HashMap<>();
			Error err = new Error();
			err.code = e.getCodeNumber();
			err.message = e.getErrorMessage();
			result.put("error", err);

			response.setContentType(AppCommonConstants.SCORINGSERVER_CONTENTTYPE_JSON_UTF8);
			response.setStatus(HttpServletResponse.SC_OK);

			response.getWriter().println(gson.toJson(result));
		} catch (Exception ex) {
			logger.error("Failed to process", ex);

			Map<String, Object> result = new HashMap<>();
			Error err = new Error();
			err.code = Mv5ScoringConstants.DEFAULT_ERROR_CDOE;
			err.message = "Internal server error: " + ex.getMessage();
			result.put("error", err);

			response.setContentType(AppCommonConstants.SCORINGSERVER_CONTENTTYPE_JSON_UTF8);
			response.setStatus(HttpServletResponse.SC_OK);

			response.getWriter().println(gson.toJson(result));
		} finally {
			overallFuncMonHandler.endFuncMonitor();
		}
	}

	public static class Error {
		public int code;
		public String message;
	}

}
