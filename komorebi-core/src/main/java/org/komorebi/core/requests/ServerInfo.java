package org.komorebi.core.requests;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
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
@Path("serverInfo")
public class ServerInfo {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String get() {
		JSONObject json = new JSONObject();

		// version
		json.put("version", Version.versionString());

		// start time
		RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();
		json.put("startTime", rb.getStartTime());

		// readable uptime
		long uptime = rb.getUptime();
		json.put("uptimeReadable", String.format(
				"%d days, %d min, %d sec",
				TimeUnit.DAYS.toDays(uptime),
				TimeUnit.MILLISECONDS.toMinutes(uptime) - TimeUnit.DAYS.toSeconds(TimeUnit.MINUTES.toDays(uptime)),
				TimeUnit.MILLISECONDS.toSeconds(uptime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(uptime))
				)
		);

		return json.toString();
	}
}
