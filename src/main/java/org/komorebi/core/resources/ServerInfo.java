package org.komorebi.core.resources;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONObject;
import org.komorebi.core.Version;

/**
 * This request provides current information about the server.
 * 
 * @author lycis
 * 
 */
@Path("server/info")
public class ServerInfo {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String get() {
		JSONObject json = new JSONObject();

		// version
		json.put("version", Version.versionString());

		// start time
		RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();
		json.put("starttime", rb.getStartTime());
		
		// human readable start time
		SimpleDateFormat df = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(rb.getStartTime());
		json.put("starttimeReadable", 
				df.format(cal.getTime())
		);

		// readable uptime
		long uptime = rb.getUptime();
		json.put("uptimeReadable", String.format(
				"%d days %d min %d sec",
				TimeUnit.MILLISECONDS.toDays(uptime),
				TimeUnit.MILLISECONDS.toMinutes(uptime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(uptime)),
				TimeUnit.MILLISECONDS.toSeconds(uptime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(uptime))
				)
		);
		
		// numeric uptime
		json.put("uptime", uptime);

		return json.toString();
	}
}
