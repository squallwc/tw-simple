package com.squallwc.twsimple;

import java.io.IOException;
import javax.servlet.http.*;

import twitter4j.Status;
import twitter4j.Twitter;

public class Post extends HttpServlet implements Constants{
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String msg = req.getParameter("q");
		resp.setContentType("text/html");
		
		try {
			Twitter twitter = new Twitter(twId,twPwd);
			Status status = twitter.updateStatus(msg);
			
			resp.getWriter().println("Successfully updated the status");
			resp.getWriter().println("<!--" + status.getText() + "-->");
		} catch (Exception e) {
			e.printStackTrace();
			
			resp.getWriter().println("error");
			resp.getWriter().println("<!--" + msg + "-->");
		}
	}

}
