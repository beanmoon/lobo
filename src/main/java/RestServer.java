import com.google.gson.Gson;
import com.jd.lobo.bean.RequestBody;
import com.jd.lobo.bean.RequestType;
import com.jd.lobo.config.LoboConstants;
import org.apache.log4j.PropertyConfigurator;
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
		int port = 16010;
		PropertyConfigurator.configure("log4j.properties");

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

		handler.addServletWithMapping(JsonRequestServlet.class, "/recommend/mv5/list");
		handler.addServletWithMapping(HtmlRequstServlet.class, "/recommend/mv5/free");

		server.start();
		logger.info("Server started successfully.");

		server.join();
	}

	@SuppressWarnings("serial")
	public static class JsonRequestServlet extends HttpServlet {

		@Override
		protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			doPostAction(request, response, RequestType.JSON);
		}
	}

	@SuppressWarnings("serial")
	public static class HtmlRequstServlet extends HttpServlet {

		@Override
		protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			doPostAction(request, response, RequestType.HTML);
		}
	}


	static void doPostAction(HttpServletRequest request, HttpServletResponse response, RequestType requestType) throws ServletException, IOException {

		try {
			RequestBody requestJsonObject = gson.fromJson(request.getReader(), RequestBody.class);

			if (requestJsonObject == null) {
				logger.error("Invalid request input " + request.getReader());
				throw new Exception("invalid request input");
			}

			logger.info("params {}", gson.toJson(requestJsonObject));

			Object r = null;
			switch (requestType) {
				case JSON:

				break;
				case HTML:

				break;

			default:
				r = null;
				break;
			}

			Map<String, Object> result = new HashMap<>();
			r = "hello,world";
			result.put("data", r);
			Error err = new Error();
			err.code = 0;
			err.message = "";
			result.put("error", err);

			response.setContentType(LoboConstants.CONTENTTYPE_JSON_UTF8);
			response.setStatus(HttpServletResponse.SC_OK);

			response.getWriter().println(gson.toJson(result));
		} catch (Exception ex) {
			logger.error("Failed to process", ex);

			Map<String, Object> result = new HashMap<>();
			Error err = new Error();
			err.code = LoboConstants.DEFAULT_ERROR_CDOE;
			err.message = "Internal server error: " + ex.getMessage();
			result.put("error", err);

			response.setContentType(LoboConstants.CONTENTTYPE_JSON_UTF8);
			response.setStatus(HttpServletResponse.SC_OK);

			response.getWriter().println(gson.toJson(result));
		} 
	}

	public static class Error {
		public int code;
		public String message;
	}

}
