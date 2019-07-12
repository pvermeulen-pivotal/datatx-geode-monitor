package util.geode.monitor;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Set;

import javax.management.AttributeList;
import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.ObjectName;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import util.geode.monitor.log.LogHeader;
import util.geode.monitor.log.LogMessage;
import util.geode.monitor.xml.ExcludedMessage;

public class Util {

	/**
	 * unmarshall xml to object
	 * 
	 * @param jaxbContext
	 * @param fileName
	 * @return
	 */
	public Object processJAXB(JAXBContext jaxbContext, String fileName) {
		try {
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			JAXBElement<?> unmarshalledObject = (JAXBElement<?>) unmarshaller
					.unmarshal(ClassLoader.getSystemResourceAsStream(fileName));
			return unmarshalledObject.getValue();
		} catch (JAXBException e) {
			throw new RuntimeException("Error unmarshalling " + fileName + ": "
					+ e.getMessage());
		}
	}

	/**
	 * parse the message and create message header
	 * 
	 * @param notification
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public LogHeader parseHeader(Notification notification) {
		LogHeader header = new LogHeader();
		SimpleDateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT);
		SimpleDateFormat tf = new SimpleDateFormat(Constants.TIME_FORMAT);
		SimpleDateFormat zf = new SimpleDateFormat(Constants.ZONE_FORMAT);
		header.setDate(df.format(notification.getTimeStamp()));
		header.setTime(tf.format(notification.getTimeStamp()));
		header.setZone(zf.format(notification.getTimeStamp()));

		if (notification.getUserData() != null) {
			Map<String, String> userData = (Map<String, String>) notification
					.getUserData();
			Set<String> entries = userData.keySet();
			for (String entry : entries) {
				if (Constants.ALERT_LEVEL.toUpperCase().equals(
						entry.toUpperCase())) {
					if (userData.get(entry).toUpperCase()
							.equals(Constants.SEVERE)) {
						header.setSeverity(Constants.CRITICAL);
					} else if (userData.get(entry).toUpperCase()
							.equals(Constants.ERROR)) {
						header.setSeverity(Constants.MAJOR);
					} else if (userData.get(entry).toUpperCase()
							.equals(Constants.WARNING)) {
						header.setSeverity(Constants.WARNING);
					}
				} else if (Constants.THREAD.toUpperCase().equals(
						entry.toUpperCase())) {
					header.setEvent(userData.get(entry));
				} else if (Constants.TID.toUpperCase().equals(
						entry.toUpperCase())) {
					header.setTid(userData.get(entry));
				} else if (Constants.MEMBER.toUpperCase().equals(
						entry.toUpperCase())) {
					header.setMember(userData.get(entry));
				}
			}
			if (header.getTid() == null) {
				if (header.getEvent() != null) {
					if (header.getEvent().toUpperCase().contains("TID=")) {
						int offset = header.getEvent().toUpperCase()
								.indexOf("TID=");
						header.setTid(header.getEvent().substring(offset)
								.trim());
						header.setEvent(header.getEvent().substring(0,
								offset - 1));
					}
				}
			}
		}
		return header;
	}

	/**
	 * get fields from notification user data
	 * 
	 * @param key
	 * @param userData
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String getUserDataInfo(String key, Object userData) {
		if (userData != null) {
			Map<String, String> user = (Map<String, String>) userData;
			String value = user.get(key);
			if (Constants.ALERT_LEVEL.toUpperCase().equals(key.toUpperCase())) {
				if (value != null && value.length() > 0) {
					return value.toUpperCase();
				}
			} else if (Constants.MEMBER.toUpperCase().equals(key.toUpperCase())) {
				if (value != null && value.length() > 0) {
					return value;
				}
			}
		}
		return null;
	}

	/**
	 * search the excluded messages to see if message should be dropped
	 * 
	 * @param logMessage
	 * @param message
	 * @return
	 */
	public boolean validateCriteria(LogMessage logMessage,
			ExcludedMessage message) {
		boolean result = true;
		int count = 0;
		for (String criteria : message.getCriteria()) {
			if (logMessage.getBody().contains(criteria))
				count++;
		}
		if (count == message.getCriteria().size())
			result = false;
		return result;
	}

	@SuppressWarnings("incomplete-switch")
	public ObjectName[] getObjectNames(MBeanServerConnection mbs,
			ObjectName objectName, Constants.ObjectNameType type)
			throws Exception {
		switch (type) {
		case CACHE_SERVERS:
			return (ObjectName[]) mbs.invoke(objectName,
					Constants.LIST_CACHE_SERVERS_OBJECTS, null, null);
		case GATEWAY_RECEIVERS:
			return (ObjectName[]) mbs.invoke(objectName,
					Constants.LIST_GATEWAY_RECEIVERS_OBJECTS, null, null);
		case GATEWAY_SENDERS:
			return (ObjectName[]) mbs.invoke(objectName,
					Constants.LIST_GATEWAY_SENDERS_OBJECTS, null, null);
		}
		return null;
	}

	public String[] getNames(MBeanServerConnection mbs, ObjectName objectName,
			Constants.ListType type) throws Exception  {
		switch (type) {
		case MEMBERS:
			return (String[]) mbs.invoke(objectName,
					Constants.LIST_MEMBER_NAMES, null, null);
		case SERVERS:
			return (String[]) mbs.invoke(objectName,
					Constants.LIST_SERVER_NAMES, null, null);
		case LOCATORS:
			return (String[]) mbs.invoke(objectName,
					Constants.LIST_LOCATOR_NAMES, new Object[] { true },
					new String[] { "boolean" });
		case REGIONS:
			return (String[]) mbs.invoke(objectName,
					Constants.LIST_REGION_NAMES, null, null);
		case REGION_PATHS:
			return (String[]) mbs.invoke(objectName,
					Constants.LIST_REGION_PATH_NAMES, null, null);
		case GROUPS:
			return (String[]) mbs.invoke(objectName,
					Constants.LIST_GROUP_NAMES, null, null);
		}
		return null;
	}

	@SuppressWarnings("incomplete-switch")
	public ObjectName getObjectName(MBeanServerConnection mbs,
			ObjectName objectName, Constants.ObjectNameType type, String name,
			String name1) throws Exception {
		switch (type) {
		case MEMBER:
			return (ObjectName) mbs.invoke(objectName,
					Constants.FETCH_MEMBER_NAME_OBJECT, new Object[] { name },
					new String[] { String.class.getName() });
		case DISK_STORE:
			return (ObjectName) mbs.invoke(objectName,
					Constants.FETCH_DISK_STORE_OBJECT, new Object[] { name,
							name1 }, new String[] { String.class.getName(),
							String.class.getName() });
		case LOCK_DIST:
			return (ObjectName) mbs.invoke(objectName,
					Constants.FETCH_DISTRIBUTED_LOCK_OBJECT,
					new Object[] { name },
					new String[] { String.class.getName() });
		case LOCK:
			return (ObjectName) mbs.invoke(
					objectName,
					Constants.FETCH_LOCK_OBJECT,
					new Object[] { name, name1 },
					new String[] { String.class.getName(),
							String.class.getName() });
		case REGION_DIST:
			return (ObjectName) mbs.invoke(objectName,
					Constants.FETCH_DISTRIBUTED_REGION_OBJECT,
					new Object[] { name },
					new String[] { String.class.getName() });
		case REGION:
			return (ObjectName) mbs.invoke(
					objectName,
					Constants.FETCH_REGION_OBJECT,
					new Object[] { name, name1 },
					new String[] { String.class.getName(),
							String.class.getName() });
		case GATEWAY_RECEIVERS:
			return (ObjectName) mbs.invoke(objectName,
					Constants.FETCH_GATEWAY_RECEIVER_OBJECT,
					new Object[] { name },
					new String[] { String.class.getName() });

		case GATEWAY_SENDERS:
			return (ObjectName) mbs.invoke(objectName,
					Constants.FETCH_GATEWAY_SENDER_OBJECT, new Object[] { name,
							name1 }, new String[] { String.class.getName(),
							String.class.getName() });
		}
		return null;
	}

	public AttributeList getAttributes(MBeanServerConnection mbs,
			ObjectName objectName, String[] attributes) throws Exception {
		return mbs.getAttributes(objectName, attributes);
	}
}
