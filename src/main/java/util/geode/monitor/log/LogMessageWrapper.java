package util.geode.monitor.log;

public class LogMessageWrapper {
	private long timeStamp;
	private LogMessage logMessage;

	public LogMessageWrapper(LogMessage logMessage, long timeStamp) {
		this.logMessage = logMessage;
		this.timeStamp = timeStamp;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public LogMessage getLogMessage() {
		return logMessage;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public boolean checkTime(long time) {
		if (System.currentTimeMillis() > (this.timeStamp + time)) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return "LogMessageWrapper [timeStamp=" + timeStamp + ", logMessage=" + logMessage + "]";
	}
}
