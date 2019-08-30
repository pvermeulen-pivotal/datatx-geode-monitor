package util.geode.monitor;

import java.util.List;

import util.geode.monitor.log.LogMessage;

public interface HealthCheck {
	public List<LogMessage> doHealthCheck(String jsonStr) throws Exception;
}
