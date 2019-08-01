package util.geode.monitor.log;

/**
 * @author PaulVermeulen
 *
 */
public class LogHeader {
	private String severity;
	private String date;
	private String time;
	private String zone;
	private String member;
	private String event;
	private String tid;

	public LogHeader() {
	}

	public LogHeader(String severity, String date, String time, String zone,
			String member, String event, String tid) {
		this.severity = severity;
		this.date = date;
		this.zone = zone;
		this.member = member;
		this.event = event;
		this.time = time.replace(".", ":");
		this.tid = tid;
	}

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getZone() {
		return zone;
	}

	public void setZone(String zone) {
		this.zone = zone;
	}

	public String getMember() {
		return member;
	}

	public void setMember(String member) {
		this.member = member;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public String getTid() {
		return tid;
	}

	public void setTid(String tid) {
		this.tid = tid;
	}

	@Override
	public String toString() {
		return "[severity=" + severity + ", date=" + date
				+ ", time=" + time + ", zone=" + zone + ", member=" + member
				+ ", event=" + event + ", tid=" + tid + "]";
	}
}
