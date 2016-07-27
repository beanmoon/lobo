import com.google.gson.Gson;
import com.jd.lobo.bean.CommentRequstBody;
import com.jd.lobo.bean.RequestType;
import com.jd.lobo.cass.CassSessionFactory;
import com.jd.lobo.config.LoboConstants;
import com.jd.lobo.util.CommentFetcher;
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
	public static CommentFetcher commentFetcher = null;

	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		PropertyConfigurator.configure("log4j.properties");

		logger.info("Prepare to start the Rest Server on {}", LoboConstants.PORT);
		Server server = new Server(LoboConstants.PORT);

		ServletHandler handler = new ServletHandler();
		server.setHandler(handler);

		FilterHolder holder = new FilterHolder(GzipFilter.class);
		// holder.setInitParameter("mimetypes", MimeTypeUtils.APPLICATION_JSON_VALUE);
		holder.setInitParameter("mimetypes", "application/json");
		holder.setInitParameter("deflateCompressionLevel", "9");
		holder.setInitParameter("minGzipSize", "0");
		holder.setInitParameter("methods", "GET,POST");

		handler.addFilterWithMapping(holder, "/*", EnumSet.of(DispatcherType.REQUEST));

		handler.addServletWithMapping(JsonRequestServlet.class, "/lobo/comment");
		handler.addServletWithMapping(HtmlRequstServlet.class, "/lobo/page");

		CassSessionFactory.init(new String[]{"127.0.0.1"});
		commentFetcher = new CommentFetcher();
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
			Object r = null;
			switch (requestType) {
				case JSON:
					// logger.info("reader: " + request.getReader().readLine());
					String paramStr = request.getReader().readLine();
					CommentRequstBody commentRequstBody = new CommentRequstBody();
					if(paramStr.startsWith("{")){
						commentRequstBody = gson.fromJson(paramStr, CommentRequstBody.class);
					} else {
						String[] params = paramStr.split("&");
						if(params.length == 3){
							for(String param : params){
								String[] keyValue = param.split("=");
								switch (keyValue[0]){
									case "spu_id":
										commentRequstBody.setSpuId(Long.valueOf(keyValue[1]));
										break;
									case "page":
										commentRequstBody.setPage(Integer.valueOf(keyValue[1]));
										break;
									case "page_size":
										commentRequstBody.setPageSize(Integer.valueOf(keyValue[1]));
										break;
								}
							}
						}
					}


					if (commentRequstBody == null) {
						logger.error("Invalid request input " + request.getReader());
						throw new Exception("invalid request input");
					}

					logger.info("params {}", gson.toJson(commentRequstBody));

					logger.info("comment request received!");

					r = commentFetcher.fetchComment(commentRequstBody);
					break;
				case HTML:
					logger.info("page request received!");

					break;
				default:
					r = null;
					break;
			}

			Map<String, Object> result = new HashMap<>();
			if(r == null)
				r = "empty result returned!";

			result.put("data", r);
			result.put("code", 0);
			result.put("message", "");

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
