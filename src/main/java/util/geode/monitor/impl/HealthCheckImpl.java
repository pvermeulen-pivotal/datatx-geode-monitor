package util.geode.monitor.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import util.geode.health.domain.GatewaySender;
import util.geode.health.domain.GatewaySender.GatewayType;
import util.geode.health.domain.Health;
import util.geode.health.domain.Member;
import util.geode.monitor.Constants;
import util.geode.monitor.HealthCheck;
import util.geode.monitor.Util;
import util.geode.monitor.Constants.LogType;
import util.geode.monitor.log.LogMessage;

public class HealthCheckImpl implements HealthCheck {
	private static final String CONNECTED = "Connected";
	private static final String RUNNING = "Running";
	private static final String MEMBER = "member";
	private static final String MAJOR = "MAJOR";
	private static final String NAME = "name";
	private static final String HOST = "host";
	private static final String PORT = "port";
	private static final String GC_TIME_MILLIS = "gcTimeMillis";
	private static final String MAX_GC_TIME_MILLIS = "maximumGCTimeMillis";
	private static final String MAX_HEAP_USAGE_PERCENT = "maximumHeapUsagePercent";
	private static final String GATEWAY_SENDER = "gatewaySender";
	private static final String EVENT_QUEUE_SIZE = "EventQueueSize";
	private static final String NUMBER_GATEWAYS = "NumGateways";
	private static final String GATEWAY_MAX_QUEUE_SIZE = "gatewayMaximumQueueSize";
	private static final String SHOW_JVM_METRICS = "showJVMMetrics";
	private static final String JMX_MEMBER_COUNT = "MemberCount";
	private static final String JMX_LOCATOR_COUNT = "LocatorCount";
	private static final String TOT_HEAP_SPACE = "TotalHeapSize";
	private static final String USED_HEAP_SPACE = "UsedHeapSize";
	private static final String JSON_LOCATOR_COUNT = "locatorCount";
	private static final String JSON_SERVER_COUNT = "serverCount";
	private static final String LOCATORS = "locators";
	private static final String SERVERS = "servers";
	private static final String PARALLEL = "Parallel";
	private static final String PRIMARY = "Primary";
	private static final String LOG_LEVEL = "log-level";
	private static final String LOG_FILE = "log-file";
	private static final String CONFIG = "CONFIG";

	private Util util;
	private MBeanServerConnection mbs;
	private ObjectName systemName;
	private Logger applicationLog;
	private List<LogMessage> logMessages;
	private String cluster;
	private String site;
	private String environment;

	public enum MemberType {
		LOCATOR, SERVER
	};

	public HealthCheckImpl(MBeanServerConnection mbs, ObjectName systemName, Logger applicationLog, Util util,
			String cluster, String site, String environment) {
		this.applicationLog = applicationLog;
		this.mbs = mbs;
		this.systemName = systemName;
		this.util = util;
		this.cluster = cluster;
		this.site = site;
		this.environment = environment;
	}

	private void log(String logType, String member, String message, Object user) {
		util.log(applicationLog, logType, member, message, user, cluster, site, environment);
	}

	/**
	 * Perform the health check using GemFire cluster and CMDB
	 * 
	 * @throws Exception
	 */
	public List<LogMessage> doHealthCheck(String jsonStr) {
		if (jsonStr == null || jsonStr.length() == 0) {
			logMessages.add(util.buildSpecialLogMessage("(doHealthCheck) json CMBD string is null or empty", MAJOR,
					new Date().getTime(), cluster));
			log(LogType.ERROR.toString(), "internal", " (doHealthCheck) json CMBD string is null or empty", null);
			;
			return logMessages;
		}

		Health health = null;
		JSONObject jsonObj = null;
		logMessages = new ArrayList<LogMessage>();
		List<Member> locators = null;
		List<Member> unresponsiveMembers = null;
		List<Member> servers = null;
		ObjectName[] objects = null;

		try {
			// get GemFire health
			health = getHealthDetails();
		} catch (Exception e) {
			logMessages.add(util.buildSpecialLogMessage("(doHealthCheck) getHealthDetails exception: " + e.getMessage(),
					MAJOR, new Date().getTime(), cluster));
			log(LogType.ERROR.toString(), "internal", "(doHealthCheck) getHealthDetails exception: " + e.getMessage(),
					null);
			return logMessages;
		}

		try {
			// get CMDB health
			jsonObj = new JSONObject(jsonStr);
		} catch (Exception e) {
			logMessages.add(util.buildSpecialLogMessage("(doHealthCheck) json object exception: " + e.getMessage(),
					MAJOR, new Date().getTime(), cluster));
			log(LogType.ERROR.toString(), "internal", "(doHealthCheck) json object exception: " + e.getMessage(), null);
			return logMessages;
		}

		try {
			// get all locators
			locators = getMembers(Arrays.asList(health.getLocators()), jsonObj, LOCATORS, MemberType.LOCATOR);
		} catch (Exception e) {
			logMessages.add(
					util.buildSpecialLogMessage("(doHealthCheck) getMembers (locators) exception: " + e.getMessage(),
							MAJOR, new Date().getTime(), cluster));
			log(LogType.ERROR.toString(), "internal",
					"(doHealthCheck) getMembers (locators) exception: " + e.getMessage(), null);
		}

		try {
			// check locator count to CMDB
			if (!checkMemberCount(health.getLocatorCnt(), jsonObj, JSON_LOCATOR_COUNT)) {
				// If missing locator(s) send alert
				sendMissingMemberAlert(locators, jsonObj);
			}
		} catch (Exception e) {
			logMessages.add(util.buildSpecialLogMessage(
					"(doHealthCheck) checkMemberCount (locators) exception: " + e.getMessage(), MAJOR,
					new Date().getTime(), cluster));
			log(LogType.ERROR.toString(), "internal",
					"(doHealthCheck) checkMemberCount (locators) exception: " + e.getMessage(), null);
		}

		try {
			// Connect to all locators
			unresponsiveMembers = connectToMembers(locators, health.getRegions(), jsonObj, true);
		} catch (Exception e) {
			logMessages.add(util.buildSpecialLogMessage(
					"(doHealthCheck) connectToMembers (locators) exception: " + e.getMessage(), MAJOR,
					new Date().getTime(), cluster));
			log(LogType.ERROR.toString(), "internal",
					"(doHealthCheck) connectToMembers (locators) exception: " + e.getMessage(), null);
		}

		// send alert for non-responsive locator(s)
		sendUnresponsiveMemberAlert(unresponsiveMembers, jsonObj);

		try {
			// get all cache servers
			servers = getMembers(Arrays.asList(health.getServers()), jsonObj, SERVERS, MemberType.SERVER);
		} catch (Exception e) {
			logMessages.add(util.buildSpecialLogMessage(
					"(doHealthCheck) getMembers (cache-servers) exception: " + e.getMessage(), MAJOR,
					new Date().getTime(), cluster));
			log(LogType.ERROR.toString(), "internal",
					"(doHealthCheck) getMembers (cache-servers) exception: " + e.getMessage(), null);
		}

		try {
			// check cache server count to CMDB
			if (!checkMemberCount(health.getServerCnt(), jsonObj, JSON_SERVER_COUNT)) {
				// If missing cache server(s) send alert
				sendMissingMemberAlert(servers, jsonObj);
			}
		} catch (Exception e) {
			logMessages.add(util.buildSpecialLogMessage(
					"(doHealthCheck) checkMemberCount (cache-servers) exception: " + e.getMessage(), MAJOR,
					new Date().getTime(), cluster));
			log(LogType.ERROR.toString(), "internal",
					"(doHealthCheck) checkMemberCount (cache-servers) exception: " + e.getMessage(), null);
		}

		try {
			// connect to all servers
			unresponsiveMembers = connectToMembers(servers, null, jsonObj, false);
		} catch (Exception e) {
			logMessages.add(util.buildSpecialLogMessage(
					"(doHealthCheck) connectToMembers (cache-servers) exception: " + e.getMessage(), MAJOR,
					new Date().getTime(), cluster));
			log(LogType.ERROR.toString(), "internal",
					"(doHealthCheck) connectToMembers (cache-servers) exception: " + e.getMessage(), null);
		}

		// send alert for non-responsive cache server(s)
		sendUnresponsiveMemberAlert(unresponsiveMembers, jsonObj);

		// for each cache server get GC Time
		for (Member member : servers) {
			try {
				// get GC time and verify against threshold
				getJVMGCTime(member, GC_TIME_MILLIS, jsonObj, MAX_GC_TIME_MILLIS);
			} catch (Exception e) {
				logMessages.add(util.buildSpecialLogMessage("(doHealthCheck) getJVMGCTime exception: " + e.getMessage(),
						MAJOR, new Date().getTime(), cluster));
				log(LogType.ERROR.toString(), "internal", "(doHealthCheck) getJVMGCTime exception: " + e.getMessage(),
						null);
			}
		}

		// check to see if cluster used heap greater than threshold
		double usagePercent = jsonObj.getDouble(MAX_HEAP_USAGE_PERCENT);
		if (health.getUsedHeap() > (health.getTotalHeap() * usagePercent)) {
			// used heap over threshold send alert
			logMessages.add(util.buildSpecialLogMessage(
					"(doHealthCheck) Cluster's maximum heap for all cache servers exceeded. Total used heap="
							+ health.getUsedHeap(),
					MAJOR, new Date().getTime(), cluster));
			log(LogType.ERROR.toString(), "internal",
					"(doHealthCheck) maximum heap for all cache servers exceeded. Total used heap="
							+ health.getUsedHeap(),
					null);
		}

		try {
			// get gateway senders if any
			objects = util.getObjectNames(mbs, systemName, Constants.ObjectNameType.GATEWAY_SENDERS);
			if (objects == null || objects.length == 0)
				return null;
		} catch (Exception e) {
			log(LogType.ERROR.toString(), "internal",
					"(doHealthCheck) get gateway senders exception: " + e.getMessage(), null);
		}

		health.setGateway(true);
		List<String> gatewaySenders = new ArrayList<String>();
		for (ObjectName name : objects) {
			String prop = name.getKeyProperty(GATEWAY_SENDER);
			// add to list all gateway senders with different sender ids
			if (prop != null && !gatewaySenders.contains(prop))
				gatewaySenders.add(prop);
		}

		// for each gateway sender see if connected and queue size is not over threshold
		String oName = null;
		for (String sender : gatewaySenders) {
			try {
				List<ObjectName> objectsByName = getGatewayObjectsByName(objects, sender);
				for (ObjectName object : objectsByName) {
					oName = object.getKeyProperty("member");
					AttributeList attrs = util.getAttributes(mbs, object, new String[] { EVENT_QUEUE_SIZE, CONNECTED });
					if (attrs == null || attrs.size() == 0)
						break;
					Attribute attr = (Attribute) attrs.get(0);
					int eventQueueSize = (int) attr.getValue();
					attr = (Attribute) attrs.get(1);
					boolean connected = (boolean) attr.getValue();
					// check to see if gateway is a parallel gateway
					if (checkForParallelGateway(object)) {
						// if parallel create sender and add to list
						health.addGatewaySender(
								new GatewaySender(object.getKeyProperty(GATEWAY_SENDER), object.getKeyProperty(MEMBER),
										object, GatewayType.PARALLEL, false, eventQueueSize, connected));
					} else {
						// if gateway sender is a serial gateway
						if (getPrimarySerialGateway(object)) {
							// get the primary serial gateway create sender and add to list
							health.addGatewaySender(new GatewaySender(object.getKeyProperty(GATEWAY_SENDER),
									object.getKeyProperty(MEMBER), object, GatewayType.SERIAL, true, eventQueueSize,
									connected));
						} else {
							// add secondary gateway to list
							health.addGatewaySender(new GatewaySender(object.getKeyProperty(GATEWAY_SENDER),
									object.getKeyProperty(MEMBER), object, GatewayType.SERIAL, false, eventQueueSize,
									connected));
						}
					}
				}
			} catch (Exception e) {
				logMessages.add(util.buildSpecialLogMessage(
						"(doHealthCheck) process gateway sender objects attributes " + sender + ":" + oName, MAJOR,
						new Date().getTime(), oName));
				log(LogType.ERROR.toString(), "internal",
						"(doHealthCheck) process gateway sender objects attributes " + sender + ":" + oName, null);
			}
		}

		// for each unique gateway sender
		for (GatewaySender gatewaySender : health.getGatewaySenders()) {
			// if parallel gateway
			if (gatewaySender.getType().equals(GatewayType.PARALLEL)) {
				if (!gatewaySender.isConnected()) {
					// if gateway not connected to remote side send event
					logMessages.add(util.buildSpecialLogMessage("Sender not connected to remote system", MAJOR,
							new Date().getTime(), gatewaySender.getMember()));
					log(LogType.WARNING.toString(), "internal",
							"Sender not connected to remote system. Member=" + gatewaySender.getMember(), null);
				} else if (gatewaySender.getEventQueueSize() > jsonObj.getInt(GATEWAY_MAX_QUEUE_SIZE)) {
					// if gateway is connected and maximum queue size exceeds threashold send alert
					logMessages.add(util.buildSpecialLogMessage(
							"Queue size greater than limit of " + jsonObj.getInt(GATEWAY_MAX_QUEUE_SIZE), MAJOR,
							new Date().getTime(), gatewaySender.getMember()));
					log(LogType.WARNING.toString(), "internal", "Queue size greater than limit of "
							+ jsonObj.getInt(GATEWAY_MAX_QUEUE_SIZE) + " Member: " + gatewaySender.getMember(), null);
				}
			} else {
				// if a serial gateway
				if (gatewaySender.isPrimary()) {
					// if gateway is the primary serial gateway
					if (!gatewaySender.isConnected()) {
						// if gateway is not connected to remote side send alert
						logMessages.add(util.buildSpecialLogMessage("Sender not connected to remote system", MAJOR,
								new Date().getTime(), gatewaySender.getMember()));
						log(LogType.ERROR.toString(), "internal",
								"Sender not connected to remote system. Member: " + gatewaySender.getMember(), null);
					} else if (gatewaySender.getEventQueueSize() > jsonObj.getInt(GATEWAY_MAX_QUEUE_SIZE)) {
						// if serial gateway is connected and maximum queue size exceeds threshold send
						// alert
						logMessages.add(util.buildSpecialLogMessage(
								"Queue size greater than limit of " + jsonObj.getInt(GATEWAY_MAX_QUEUE_SIZE), MAJOR,
								new Date().getTime(), gatewaySender.getMember()));
						log(LogType.ERROR.toString(), "internal", "Queue size greater than limit of "
								+ jsonObj.getInt(GATEWAY_MAX_QUEUE_SIZE) + " Member: " + gatewaySender.getMember(),
								null);
					}
				}
			}
		}

		try {
			// get gateway receivers if any
			objects = util.getObjectNames(mbs, systemName, Constants.ObjectNameType.GATEWAY_RECEIVERS);
			if (objects == null || objects.length == 0)
				return null;
		} catch (Exception e) {
			log(LogType.ERROR.toString(), "internal",
					"(doHealthCheck) get gateway receivers exception: " + e.getMessage(), null);
		}

		for (ObjectName object : objects) {
			oName = object.getKeyProperty("member");
			try {
				AttributeList attrs = util.getAttributes(mbs, object, new String[] { RUNNING, NUMBER_GATEWAYS });
				if (attrs == null || attrs.size() == 0)
					break;
				Attribute attr = (Attribute) attrs.get(0);
				boolean running = (boolean) attr.getValue();
				attr = (Attribute) attrs.get(1);
				int numberGateways = (int) attr.getValue();
				if (!running) {
					logMessages.add(util.buildSpecialLogMessage("The Gateway Receiver is not active and running", MAJOR,
							new Date().getTime(), "Gateway Receiver"));
					log(LogType.ERROR.toString(), "internal",
							"The Gateway Receiver is not active and running" + " Member: Gateway Receiver", null);
				} else {
					if (numberGateways == 0) {
						logMessages.add(util.buildSpecialLogMessage(
								"There are no remote gateway senders connected to local Gateway Receiver", MAJOR,
								new Date().getTime(), "Gateway Receiver"));
						log(LogType.ERROR.toString(), "internal",
								"There are no remote gateway senders connected to local Gateway Receiver"
										+ " Member: Gateway Receiver",
								null);
					}
				}
			} catch (Exception e) {
				logMessages
						.add(util.buildSpecialLogMessage("(doHealthCheck) process gateway receiver objects attributes "
								+ oName + " exception: " + e.getMessage(), MAJOR, new Date().getTime(), oName));
				log(LogType.ERROR.toString(), "internal", "(doHealthCheck) process gateway sender objects attributes "
						+ oName + " exception: " + e.getMessage(), null);
			}
		}
		return logMessages;
	}

	/**
	 * Get gateway mbean objects by name
	 * 
	 * @param objects
	 * @param name
	 * @return
	 */
	private List<ObjectName> getGatewayObjectsByName(ObjectName[] objects, String name) {
		List<ObjectName> objectsByName = new ArrayList<ObjectName>();
		if (objects != null && objects.length > 0) {
			for (ObjectName object : objects) {
				if (object.getKeyProperty(GATEWAY_SENDER).equals(name)) {
					objectsByName.add(object);
				}
			}
		}
		return objectsByName;
	}

	/**
	 * Check gateway mbeans for a parallel gateway implementation
	 * 
	 * @param name
	 * @return
	 * @throws Exception
	 */
	private boolean checkForParallelGateway(ObjectName name) throws Exception {
		if (name == null) {
			return false;
		}
		AttributeList attrs = util.getAttributes(mbs, name, new String[] { PARALLEL });
		if (attrs != null && attrs.size() == 1) {
			Attribute attr = (Attribute) attrs.get(0);
			if (PARALLEL.equalsIgnoreCase(attr.getName())) {
				return (boolean) attr.getValue();
			}
		}
		return false;
	}

	/**
	 * Check gateway mbeans for a serial gateway implementation
	 * 
	 * @param name
	 * @return
	 * @throws Exception
	 */
	private boolean getPrimarySerialGateway(ObjectName name) throws Exception {
		if (name == null) {
			return false;
		}

		boolean primary = false;
		AttributeList attrs = util.getAttributes(mbs, name, new String[] { PRIMARY });
		if (attrs != null && attrs.size() == 1) {
			Attribute attr = (Attribute) attrs.get(0);
			if (PRIMARY.equalsIgnoreCase(attr.getName())) {
				primary = (boolean) attr.getValue();
				if (primary)
					return primary;
			}
		}
		return false;
	}

	/**
	 * Send alert for missing GemFire cluster member(s)
	 * 
	 * @param members
	 */
	private void sendMissingMemberAlert(List<Member> members, JSONObject jsonObj) {
		for (Member member : members) {
			if (member.isMissing()) {
				logMessages.add(
						util.buildSpecialLogMessage("Member is down", MAJOR, new Date().getTime(), member.getName()));
				log(LogType.ERROR.toString(), "internal",
						"(sendMissingMemberAlert) Member is down Member: " + member.getName(), null);
			}
		}
	}

	/**
	 * Send alert for unresponsive GemFire member(s)
	 * 
	 * @param members
	 */
	private void sendUnresponsiveMemberAlert(List<Member> members, JSONObject jsonObj) {
		for (Member member : members) {
			logMessages.add(util.buildSpecialLogMessage("Member is unresponsive", MAJOR, new Date().getTime(),
					member.getName()));
			log(LogType.ERROR.toString(), "internal",
					"(sendUnresponsiveMemberAlert) Member is unresponsive Member: " + member.getName(), null);
		}
	}

	/**
	 * Connect to member GemFire locator and/or cache servers
	 * 
	 * @param members
	 * @return
	 */
	private List<Member> connectToMembers(List<Member> members, String[] regions, JSONObject jObj,
			boolean processRegions) {
		boolean regionsProcessed = false;
		List<Member> unresponsiveMembers = new ArrayList<Member>();
		for (Member member : members) {
			if (!member.isMissing()) {
				ClientCache cache = createConnection(member);
				if (cache == null) {
					unresponsiveMembers.add(member);
				} else {
					if (processRegions) {
						if (!regionsProcessed) {
							regionsProcessed = true;
							checkRegion(cache, regions, member, jObj);
						}
					}
					cache.close();
					cache = null;
				}
			}
		}
		return unresponsiveMembers;
	}

	/**
	 * Select only three (3) regions from list of all regions and get three (3) keys
	 * from each of the three (3) regions. After selecting region and region keys
	 * read from the server to ensure server is working correctly.
	 * 
	 * @param cache
	 * @param regions
	 * @param member
	 */
	private void checkRegion(ClientCache cache, String[] regions, Member member, JSONObject json) {
		Region<Object, Object> region = null;
		String lastRegionName = null;
		int[] regionsToCheck = new int[] { 0, 0, 0 };
		Object[] keysToCheck = new Object[] { null, null, null };
		regionsToCheck[0] = 1;
		regionsToCheck[1] = (regions.length / 2) + 1;
		regionsToCheck[2] = regions.length;
		for (int i = 0; i < regionsToCheck.length; i++) {
			if (i < regions.length) {
				String regionName = regions[i];
				if (lastRegionName == null || !lastRegionName.equals(regionName)) {
					region = createRegion(cache, regionName);
					lastRegionName = regionName;
				}
				Set<Object> keys = region.keySetOnServer();
				Object[] objKeys = keys.toArray(new Object[keys.size()]);
				if (objKeys != null && objKeys.length > 0) {
					keysToCheck[0] = objKeys[0];
					int index = objKeys.length / 2;
					keysToCheck[1] = objKeys[index];
					keysToCheck[2] = objKeys[objKeys.length - 1];
					for (int j = 0; j < keysToCheck.length; j++) {
						Object value = region.get(keysToCheck[j]);
						if (value == null) {
							logMessages.add(util.buildSpecialLogMessage(
									"Member " + member.getName() + " region " + region.getName()
											+ " region object missing for key = " + keysToCheck[j],
									MAJOR, new Date().getTime(), member.getName()));
							log(LogType.ERROR.toString(), "internal",
									"(checkRegion) Member region " + region.getName()
											+ " region object missing for key = " + keysToCheck[j] + " Member: "
											+ member.getName(),
									null);
						}
					}
				}
				region.close();
				region = null;
			}
		}
	}

	/**
	 * Create a client region to access server region
	 * 
	 * @param cache
	 * @param name
	 * @return
	 */
	private Region<Object, Object> createRegion(ClientCache cache, String name) {
		return cache.createClientRegionFactory(ClientRegionShortcut.PROXY).create(name);
	}

	/**
	 * Create GemFire client connection
	 * 
	 * @param member
	 * @return
	 */
	private ClientCache createConnection(Member member) {
		if (member.getType().equals(MemberType.LOCATOR)) {
			return new ClientCacheFactory().addPoolLocator(member.getHost(), member.getPort())
					.set(NAME, member.getName()).setPdxReadSerialized(true).set(LOG_LEVEL, CONFIG)
					.set(LOG_FILE, "logs/gemfire-health-client.log").create();
		} else {
			return new ClientCacheFactory().addPoolServer(member.getHost(), member.getPort())
					.set(NAME, member.getName()).setPdxReadSerialized(true).set(LOG_LEVEL, CONFIG)
					.set(LOG_FILE, "logs/gemfire-health-client.log").create();
		}
	}

	/**
	 * Get GemFire cluster members and add to member list
	 * 
	 * @param members
	 * @param jsonObj
	 * @param key
	 * @param type
	 * @return
	 */
	private List<Member> getMembers(List<String> members, JSONObject jsonObj, String key, MemberType type) {
		List<Member> memberList = new ArrayList<Member>();
		JSONArray jarray = jsonObj.getJSONArray(key);
		for (int i = 0; i < jarray.length(); i++) {
			JSONObject jObj = (JSONObject) jarray.get(i);
			String name = jObj.getString(NAME);
			String host = jObj.getString(HOST);
			int port = jObj.getInt(PORT);
			if (!members.contains(name)) {
				memberList.add(new Member(name, host, port, true, type));
			} else {
				memberList.add(new Member(name, host, port, false, type));
			}
		}
		return memberList;
	}

	/**
	 * Check the count GemFire cluster locator and server members
	 * 
	 * @param count
	 * @param jObj
	 * @param key
	 * @return
	 */
	private boolean checkMemberCount(int count, JSONObject jObj, String key) {
		if (count != jObj.getInt(key)) {
			return false;
		}
		return true;
	}

	/**
	 * Get the JCM GC times for each cache server in the GemFire cluster
	 * 
	 * @param member
	 * @param name
	 * @param jObj
	 * @param jsonName
	 * @throws Exception
	 */
	private void getJVMGCTime(Member member, String name, JSONObject jObj, String jsonName) throws Exception {
		CompositeDataSupport cds = null;
		long currentGCTimeMillis = 0;
		long maximumGCTimeMillis = 0;
		if (!member.isMissing()) {
			cds = (CompositeDataSupport) util.invokeGetComposite(mbs, systemName, SHOW_JVM_METRICS,
					new Object[] { member.getName() }, new String[] { String.class.getName() });
			currentGCTimeMillis = (long) cds.get(name);
			maximumGCTimeMillis = jObj.getLong(jsonName);
			if (currentGCTimeMillis > maximumGCTimeMillis) {
				logMessages.add(util.buildSpecialLogMessage(
						"Member " + member.getName() + " current GC time exceeds limit of " + maximumGCTimeMillis,
						MAJOR, new Date().getTime(), member.getName()));
				log(LogType.ERROR.toString(), "internal", "(getJVMGCTime) Member current GC time exceeds limit of "
						+ maximumGCTimeMillis + " Member: " + member.getName(), null);
			}
		}
	}

	/**
	 * Retrieve GemFire cluster health details
	 * 
	 * @return
	 * @throws Exception
	 */
	private Health getHealthDetails() throws Exception {
		applicationLog.info("Retrieving GemFire health details");
		Health health = new Health();
		AttributeList al = getHealthAttributes(new String[] { JMX_MEMBER_COUNT, JMX_LOCATOR_COUNT }, systemName);
		health.setLocatorCnt(Integer.parseInt(String.valueOf(((Attribute) al.get(1)).getValue())));
		health.setServerCnt(
				Integer.parseInt(String.valueOf(((Attribute) al.get(0)).getValue())) - health.getLocatorCnt());
		al = getHealthAttributes(new String[] { TOT_HEAP_SPACE, USED_HEAP_SPACE }, systemName);
		health.setTotalHeap(Double.valueOf(String.valueOf(((Attribute) al.get(0)).getValue())));
		health.setUsedHeap(Double.valueOf(String.valueOf(((Attribute) al.get(1)).getValue())));
		String[] senders = getNames(Constants.ListType.SENDERS);
		if (senders.length > 0)
			health.setGateway(true);
		health.setLocators(getNames(Constants.ListType.LOCATORS));
		health.setServers(getNames(Constants.ListType.SERVERS));
		health.setRegions(getNames(Constants.ListType.REGIONS));
		return health;
	}

	/**
	 * Get JMX mbean names
	 * 
	 * @param type
	 * @return
	 * @throws Exception
	 */
	private String[] getNames(Constants.ListType type) throws Exception {
		return util.getNames(mbs, systemName, type);
	}

	/**
	 * Get the health attributes for JMX mbean
	 * 
	 * @param attrs
	 * @param oName
	 * @return
	 * @throws Exception
	 */
	private AttributeList getHealthAttributes(String[] attrs, ObjectName oName) throws Exception {
		return util.getAttributes(mbs, oName, attrs);
	}
}
