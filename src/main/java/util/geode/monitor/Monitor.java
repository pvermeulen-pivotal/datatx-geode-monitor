package util.geode.monitor;

import org.apache.log4j.Logger;

import util.geode.monitor.log.LogMessage;
import util.geode.monitor.xml.ExcludedMessages;

/**
 * @author PaulVermeulen
 *
 */
public interface Monitor {
	public void sendAlert(LogMessage logMessage, Logger log);

	public String getCmdbHealth(Logger log);

	public int getCommandPort();

	public Logger getExceptionLog();

	public Logger getApplicationLog();

	public void disconnect();

	public Util getUtil();

	public void initialize() throws Exception;

	public boolean isAttachedToManager();

	public boolean isShutdown();

	public void setExcludedMessages(ExcludedMessages messages);

	public void setShutdown(boolean shutdown);

	public String[] getBlockers();

	public void addBlocker(String blockerId);

	public void removeBlocker(String blockerId);

	public void start();

	public String getSite();

	public void setSite(String site);

	public String getEnvironment();

	public void setEnvironment(String environment);

	public String getCluster();

	public void setCluster(String cluster);
}
