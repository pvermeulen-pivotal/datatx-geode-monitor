package util.geode.monitor;

import org.apache.log4j.Logger;

import util.geode.monitor.log.LogMessage;
import util.geode.monitor.xml.ExcludedMessages;

/**
 * @author PaulVermeulen
 *
 */
public interface Monitor {
	void sendAlert(LogMessage logMessage);
	int getCommandPort();
	Logger getExceptionLog();
	Logger getApplicationLog();
	void disconnect();
	Util getUtil();
	void initialize() throws Exception;
	boolean isAttachedToManager();
	boolean isShutdown();
	void setExcludedMessages(ExcludedMessages messages);
	void setShutdown(boolean shutdown);
	String[] getBlockers();
	void addBlocker(String blockerId);
	void removeBlocker(String blockerId);
}
