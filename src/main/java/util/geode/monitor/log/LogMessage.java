package util.geode.monitor.log;

import javax.management.Notification;

public class LogMessage {
	private LogHeader header;
	private Notification event;
	private String body;
	private int count = 1;

	public LogMessage() {
	}

	public LogMessage(LogHeader header, String body) {
		this.header = header;
		this.body = body;
	}

	public LogHeader getHeader() {
		return header;
	}

	public void setHeader(LogHeader header) {
		this.header = header;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public Notification getEvent() {
		return event;
	}

	public void setEvent(Notification event) {
		this.event = event;
	}

	@Override
	public String toString() {
		return "LogMessage [header=" + header + ", event=" + event
				+ ", body=" + body + ", count=" + count + "]";
	}

}
