package com.squallwc.twsimple;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.servlet.http.*;

import java.util.logging.Logger;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.Cache;
import javax.cache.CacheManager;

import com.google.appengine.api.memcache.stdimpl.GCacheFactory;

@SuppressWarnings("serial")
public class TwsimpleServlet extends HttpServlet implements Constants{
	
	private static final Logger log = Logger.getLogger(TwsimpleServlet.class.getName());
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("application/xml");
		
		resp.getWriter().println("<?xml version=\"1.0\"?>");
		resp.getWriter().println("<rss version=\"2.0\">");
		resp.getWriter().println("<channel>");
		resp.getWriter().println("<title>Tw feed</title>");
		resp.getWriter().println("<description>Tw feed</description>");

		Twitter twitter = new Twitter(twId,twPwd);
		
		resp.getWriter().println("<link>"+ twitter.getBaseURL() +"</link>");
		
		try {
			Map props = new HashMap();
			props.put(GCacheFactory.EXPIRATION_DELTA, CACHE_VALIDITY_IN_SECONDS);
			
			Cache cache;

	        CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
	        cache = cacheFactory.createCache(props);
			
	        ArrayList<Status> statuses;
	        
	        log.info("cache.size(): "+cache.size());
	        
	        if(cache.containsKey(twId))
	        {
	        	log.info("retrieved data from cache: "+twId);
	        	statuses = (ArrayList<Status>) cache.get(twId);
	        	if(statuses==null)
	        	{
//	        		cache.clear();
	        		
	        		log.info("in cache but null: "+twId);
	        		statuses = (ArrayList<Status>) twitter.getUserTimeline(twId);
	        		cache.put(twId, statuses);
	        	}
	        }
	        else
	        {
	        	log.info("retrieved data from twitter: "+twId);
	        	statuses = (ArrayList<Status>) twitter.getUserTimeline(twId);
	        	cache.put(twId, statuses);
	        }
	        
	        log.info("statuses.size() "+statuses.size());
	        
			int count=0;
		    for (Status status : statuses) {
		    	//ignore all twitter @replies
		    	if(status.getText().charAt(0)=='@')
		    	{
		    		continue;
		    	}
		    	
		    	if(count>MAX_FEED_ITEM_AMT)
		    		break;
		    	else if(count==0) //operation on latest(first item) twitter status post
		    	{
		    		PersistenceManager pm = PMF.get().getPersistenceManager();
		    		
		    		try{
			    		LatestPost latestPost = pm.getObjectById(LatestPost.class, twId);
		    		
			    		if(latestPost.getStatusId().compareTo(status.getId())!=0) //post id not same
			    		{
			    			log.info("ping feed burner");
			    			URL url = new URL(urlAddress);
			    			url.openStream();
			    		
			    			log.info("save post id: "+status.getId());
				    		latestPost.setLastUpdateDate(new Date());
				    		latestPost.setStatusId(status.getId());
			    		}
		    		}catch(JDOObjectNotFoundException e){
		    			log.info("JDOObjectNotFoundException: "+status.getUser().getScreenName());
		    			LatestPost newPost = new LatestPost(status.getUser().getScreenName(),status.getId(),new Date());
			            pm.makePersistent(newPost);
		    		}finally{
		                pm.close();
	    			}
		    	}
		    	
		    	resp.getWriter().println("<item>");
		    	
		    	resp.getWriter().print("<title>");
		    	resp.getWriter().print(status.getText());
		    	resp.getWriter().print("</title>");
		    	
		    	resp.getWriter().println();
		    	
		    	String statusLink = twitter.getBaseURL()+status.getUser().getScreenName()+"/status/"+status.getId();
		    	resp.getWriter().print("<link>");
		    	resp.getWriter().print(statusLink);
		    	resp.getWriter().print("</link>");
		    	
		    	resp.getWriter().println();
		    	
		    	resp.getWriter().print("<description>");
		    	resp.getWriter().print(status.getText());
		    	resp.getWriter().print("</description>");
		    	
		    	resp.getWriter().println();
		    	
		    	resp.getWriter().print("<pubDate>");
		    	SimpleDateFormat formatter=
		            new SimpleDateFormat("dd MMM yyyy HH:mm:ss Z");
		    	resp.getWriter().print(formatter.format(status.getCreatedAt()));
		    	resp.getWriter().print("</pubDate>");
		    	
		    	resp.getWriter().println();
		    	resp.getWriter().println("</item>");

		    	count++;
		    }
		} catch (CacheException e) {
	        log.warning("CacheException: "+e.getMessage());
	        e.printStackTrace();
		} catch (TwitterException e) {
			log.warning("TwitterException: "+e.getMessage());
			e.printStackTrace();
		}
		resp.getWriter().println("</channel>");
	    resp.getWriter().println("</rss>");
	}
}
