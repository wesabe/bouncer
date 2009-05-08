package com.wesabe.bouncer.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ErrorFilter implements Filter {
	private final String from, to, service;
	
	public ErrorFilter(String from, String to, String service) {
		this.from = from;
		this.to = to;
		this.service = service;
	}
	
	@Override
	public void destroy() {
		
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		try {
			chain.doFilter(req, resp);
		} catch (Throwable e) {
			final HttpServletRequest request = (HttpServletRequest) req;
			final HttpServletResponse response = (HttpServletResponse) resp;
			
			response.reset();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			final PrintWriter writer = response.getWriter();
			writer.println("The server encountered an unexpected condition which prevented it from fulfilling the request.");
			writer.println();
			writer.println("Wesabe engineers have been alerted to this error. If you have further questions, please contact <support@wesabe.com>.");
			
			final String localhost = InetAddress.getLocalHost().getHostName();
			
			final PrintWriter email;
			if (localhost.endsWith("wesabe.com")) { // i.e., we're on a Wesabe server, production or otherwise
				final Process sendmail = new ProcessBuilder().command("sendmail", "-t").start();
				email = new PrintWriter(sendmail.getOutputStream());
			} else {
				writer.println("\nAnd here's the email you would have received.");
				writer.println("========================");
				email = writer;
			}
			 
			email.print("To: ");
			email.println(to);
			email.print("From: ");
			email.println(from);
			email.print("Subject: [ERROR] ");
			email.print(service);
			email.print(" threw a ");
			email.print(e.getClass().getSimpleName());
			final String errorMessage = e.getMessage();
			if (errorMessage != null) {
				email.print(": ");
				email.print(errorMessage);
			}
			email.println();
			email.println();
			
			email.print("A ");
			email.print(e.getClass().getCanonicalName());
			email.print(" was thrown while responding to ");
			email.print(request.getRequestURL().toString());
			email.print(" on ");

			email.print(localhost);
			email.println(".\n");
			
			email.print("This request from ");
			email.print(request.getRemoteAddr());
			email.println(":\n");
			email.print("\t");
			email.println(request.toString().replaceAll("Authorization: .*", "Authorization: [REDACTED]").replace("\n", "\n\t"));
			email.println("Produced this error:\n");
			
			e.printStackTrace(email);

			email.println();
			email.println();
			email.println("Hugs and kisses,");
			email.println("\tErrorFilter");
			email.flush();
			
			writer.close();
		}
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		
	}

}
