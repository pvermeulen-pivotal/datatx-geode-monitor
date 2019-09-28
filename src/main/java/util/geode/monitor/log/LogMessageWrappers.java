package util.geode.monitor.log;

import java.util.ArrayList;
import java.util.List;

public class LogMessageWrappers {

	private List<LogMessageWrapper> logMessageWrappers;

	public LogMessageWrappers() {
		this.logMessageWrappers = new ArrayList<LogMessageWrapper>();
	}

	public void removeWrapper(LogMessageWrapper wrapper) {
		synchronized (logMessageWrappers) {
			List<LogMessageWrapper> wrappers = new ArrayList<LogMessageWrapper>();
			for (LogMessageWrapper lmw : this.logMessageWrappers) {
				if (!lmw.getLogMessage().getBody().equals(wrapper.getLogMessage().getBody())) {
					wrappers.add(lmw);
				}
			}
			this.logMessageWrappers = wrappers;
			wrappers = null;
		}
	}

	public void addWrapper(LogMessageWrapper wrapper) {
		synchronized (logMessageWrappers) {
			this.logMessageWrappers.add(wrapper);
		}
	}

	public void updateWrapper(LogMessageWrapper wrapper) {
		synchronized (logMessageWrappers) {
			for (int i = 0; i < logMessageWrappers.size(); i++) {
				if (logMessageWrappers.get(i).getLogMessage().getBody().equals(wrapper.getLogMessage().getBody())) {
					logMessageWrappers.set(i, wrapper);
					return;
				}
			}
		}
	}

	public LogMessageWrapper getWrapper(LogMessage logMessage) {
		synchronized (logMessageWrappers) {
			for (LogMessageWrapper wrapper : this.logMessageWrappers) {
				if (wrapper.getLogMessage().getBody().equals(logMessage.getBody())) {
					return wrapper;
				}
			}
			return null;
		}
	}

	public List<LogMessageWrapper> checkWrappers(long time) {
		List<LogMessageWrapper> wrappers = new ArrayList<LogMessageWrapper>();
		synchronized (logMessageWrappers) {
			for (LogMessageWrapper lmw : this.logMessageWrappers) {
				if (lmw.checkTime(time)) {
					wrappers.add(lmw);
				}
			}
			return wrappers;
		}
	}
}
