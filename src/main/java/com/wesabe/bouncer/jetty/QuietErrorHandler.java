package com.wesabe.bouncer.jetty;

import static javax.servlet.http.HttpServletResponse.*;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpGenerator;
import org.mortbay.jetty.MimeTypes;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Response;
import org.mortbay.jetty.handler.ErrorHandler;
import org.mortbay.jetty.servlet.Context;

/**
 * An {@link ErrorHandler} for Jetty {@link Context}s which outputs simple
 * plain-text error messages without Jetty branding or stack traces.
 * 
 * @author coda
 */
public class QuietErrorHandler extends ErrorHandler {
	@Override
	public void handle(String target, HttpServletRequest req, HttpServletResponse resp,
			int dispatch) throws IOException {
		final Request request = (Request) req;
		final Response response = (Response) resp;
		response.resetBuffer();
		writeErrorMessage(request, response);
	}

	private void writeErrorMessage(Request request, Response response) throws IOException {
		response.setContentType(MimeTypes.TEXT_PLAIN_UTF_8);
		final PrintWriter writer = response.getWriter();
		writer.println(getErrorMessage(request, response));
		writer.close();
	}
	
	private String getErrorMessage(Request request, Response response) {
		final int status = response.getStatus();
		final StringBuilder builder;
		// As much as I hate case statements, this was the least ugly way I
		// could find of writing this. Other options included a crap-ton of
		// writeXXXError methods and a map of functions.
		switch (status) {
		case SC_BAD_REQUEST:
			return "Your HTTP client sent a request that this server could not " +
					"understand.";
		case SC_CONFLICT:
			return "The request could not be completed due to a conflict with " +
					"the current state of the resource.";
		case SC_EXPECTATION_FAILED:
			return "The server could not meet the expectation given in the Expect request header.";
		case SC_FORBIDDEN:
			return "You don't have permission to access the requested resource.";
		case SC_GONE:
			return "The requested resource used to exist but no longer does.";
		case SC_INTERNAL_SERVER_ERROR:
			return "The server encountered an internal error and was unable to " +
					"complete your request.";
		case SC_LENGTH_REQUIRED:
			builder = new StringBuilder();
			builder.append("A request with the ");
			builder.append(request.getMethod());
			builder.append(" method requires a valid Content-Length header.");
			return builder.toString();
		case SC_METHOD_NOT_ALLOWED:
			builder = new StringBuilder();
			builder.append("The ");
			builder.append(request.getMethod());
			builder.append(" method is not allowed for the requested resource.");
			return builder.toString();
		case SC_NOT_ACCEPTABLE:
			return "The resource identified by the request is only capable of " +
					"generating response entities which have content " +
					"characteristics not acceptable according to the accept " +
					"headers sent in the request.";
		case SC_NOT_FOUND:
			return "The requested resource could not be found on this server.";
		case SC_NOT_IMPLEMENTED:
			return "The server does not support the action requested.";
		case SC_OK:
			return "";
		case SC_PRECONDITION_FAILED:
			return "The precondition on the request for the resource failed " +
					"positive evaluation.";
		case SC_REQUEST_ENTITY_TOO_LARGE:
			builder = new StringBuilder();
			builder.append("The ");
			builder.append(request.getMethod());
			builder.append(" method does not allow the data transmitted, or the " +
							"data volume exceeds the capacity limit.");
			return builder.toString();
		case SC_REQUEST_TIMEOUT:
			return "The server closed the network connection because your HTTP " +
					"client didn't finish the request within the specified time.";
		case SC_REQUEST_URI_TOO_LONG:
			return "The length of the requested URL exceeds the capacity limit for " +
					"this server. The request cannot be processed.";
		case SC_REQUESTED_RANGE_NOT_SATISFIABLE:
			return "The server cannot serve the requested byte range.";
		case SC_SERVICE_UNAVAILABLE:
			return "The server is temporarily unable to service your " +
					"request due to maintenance downtime or capacity " +
					"problems. Please try again later.";
		case SC_UNAUTHORIZED:
			return 	"This server could not verify that you are authorized to access " +
					"this resource.\n" +
					"You either supplied the wrong credentials (e.g., bad " +
					"password), or your HTTP client doesn't understand how to " +
					"supply the credentials required.";
		case SC_UNSUPPORTED_MEDIA_TYPE:
			return "The server does not support the media type transmitted in " +
					"the request.";
		default:
			builder = new StringBuilder();
			builder.append("Your request could not be processed: ");
			builder.append(HttpGenerator.getReason(status));
			return builder.toString();
		}
	}
}
