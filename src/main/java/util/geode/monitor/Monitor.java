package util.geode.monitor;

import util.geode.monitor.log.LogMessage;
import util.geode.monitor.xml.ExcludedMessages;

/**
 * @author PaulVermeulen
 *
 */
public interface Monitor {
	public void sendAlert(LogMessage logMessage);	
	public int getCommandPort();
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
}
