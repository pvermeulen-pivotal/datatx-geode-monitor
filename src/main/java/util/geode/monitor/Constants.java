package util.geode.monitor;

/**
 * @author PaulVermeulen
 *
 */
public class Constants {

	public static enum LogType {
		FATAL, ERROR, WARNING, INFO
	};

	public static enum ListType {
		MEMBERS, SERVERS, LOCATORS, REGIONS, REGION_PATHS, GROUPS, SENDERS, RECEIVERS
	}

	public static enum ObjectNameType { MEMBER, CACHE_SERVERS, GATEWAY_RECEIVERS, GATEWAY_SENDERS,
		DISK_STORE, LOCK, LOCK_DIST, REGION, REGION_DIST, ASYNC_QUEUES };
	
	public static final String CRITICAL = "CRITICAL";
	public static final String WARNING = "WARNING";
	public static final String MAJOR = "MAJOR";
	public static final String CLEAR = "CLEAR";
	public static final String SEVERE = "SEVERE";
	public static final String ERROR = "ERROR";
	public static final String EXCLUDED_MESSAGE_FILE = "excludedMessages.xml";
	public static final String GEMFIRE_THREAD_FILE = "gemfireThreads.xml";
	public static final String MXBEAN_FILE = "mxbeans.xml";
	public static final String MONITOR_PROPS = "monitor.properties";
	public static final String HEALTH_PROPS = "health.properties";
	public static final String DATE_TIME_FORMAT = "yyyy/MM/dd HH:mm:ss:SSS";
	public static final String DATE_FORMAT = "yyyy/MM/dd";
	public static final String TIME_FORMAT = "HH:mm:ss:SSS";
	public static final String ZONE_FORMAT = "zzz";
	public static final String JOINED = "gemfire.distributedsystem.cache.member.joined";
	public static final String LEFT = "gemfire.distributedsystem.cache.member.departed";
	public static final String CRASHED = "gemfire.distributedsystem.cache.member.crashed";
	public static final String SUSPECT = "gemfire.distributedsystem.cache.member.suspect";
	public static final String ALERT = "system.alert";
	public static final String OK = "OK";
	public static final String INVALID_CMD = "INVALID COMMAND";
	public static final String RUNNING = "RUNNING - NOT CONNECTED TO JMX MANAGER";
	public static final String RUNNING_CONNECT = "RUNNING - CONNECTED TO JMX MANAGER";
	public static final String DISTRIBUTED_SYSTEM_OBJECT_NAME = "GemFire:service=System,type=Distributed";
	public static final String MEMBER_OBJECT_NAME = "GemFire:type=Member,member=";
	public static final String ALERT_LEVEL = "AlertLevel";
	public static final String ALERT_LEVEL_CHANGE = "changeAlertLevel";
	public static final String LIST_LOCATOR_MEMBERS = "listLocatorMembers";
	public static final String MEMBER = "Member";
	public static final String CLUSTER="Cluster";
	public static final String TID = "tid";
	public static final String THREAD = "Thread";
	public static final String GF_CLUSTER = "-DclusterName=";
	public static final String GF_ENV = "-DenvironmentName=";
	public static final String GF_SITE = "-DsiteName=";
	public static final String M_JMX_CLOSED = "jmx.remote.connection.closed";
	public static final String M_JMX_FAILED = "jmx.remote.connection.failed";
	public static final String M_JMX_LOST = "jmx.remote.connection.notifs_lost";
	public static final String M_JMX_CONNECT_EXCEPTION = "java.rmi.ConnectException";
	public static final String M_JMX_SERVER_FAILED = "Failed to communicate with the server";
	public static final String M_JMX_OPENED = "jmx.remote.connection.opened";
	public static final String M_JMX_MGR_DEPARTED = "JMX Manager has left the distributed system";
	public static final String M_JMX_MGR_JOINED = "JMX Manager has rejoined the distributed system";
	public static final String M_JMX_MGR_OPENED = "JMX Manager Connection opened";
	public static final String M_MON_MGR_LEFT = "Monitor has left the distributed system";
	public static final String M_MON_MGR_NEW = "Monitor connected to another JMX manager ";
	public static final String M_MEMBER_JOINED = "New member has joined distributed system";
	public static final String M_MEMBER_LEFT = "Member has left the distributed system";
	public static final String M_MEMBER_CRASH = "Member has crashed in the distributed system";
	public static final String M_MEMBER_SUSPECT = "Member is suspected of crashing in the distributed system";
	public static final String P_MANAGERS = "managers";
	public static final String P_PORT = "port";
	public static final String P_COMMAND_PORT = "command-port";
	public static final String P_MSG_DUR = "message-duration";
	public static final String P_MAX_DUPS = "maximum-duplicates";
	public static final String P_RECONNECT_W_TIME = "reconnect-wait-time";
	public static final String P_RECONNECT_R_ATTEMPTS = "reconnect-retry-attempts";
	public static final String P_HEALTH_CHK = "health-check-enabled";
	public static final String P_HEALTH_CHK_INT = "health-check-interval";
	public static final String E_HOST = "host name must be set.";
	public static final String E_PORT = "port must be an integer between zero and 65535.";
	public static final String E_COMMAND_PORT = "command port must be an integer between zero and 65535.";
	public static final String E_PROC_PROPS = "Error processing monitor properties: ";
	public static final String RELOAD = "RELOAD";
	public static final String SHUTDOWN = "SHUTDOWN";
	public static final String STATUS = "STATUS";
	public static final String BLOCK = "BLOCK";
	public static final String UNBLOCK = "UNBLOCK";
	public static final String LIST_CACHE_SERVERS_OBJECTS = "listCacheServerObjectNames";
	public static final String LIST_GATEWAY_SENDERS_OBJECTS = "listGatewaySenderObjectNames";
	public static final String LIST_GATEWAY_RECEIVERS_OBJECTS = "listGatewayReceiverObjectNames";
	public static final String LIST_MEMBER_NAMES="listMembers";
	public static final String LIST_SERVER_NAMES="listServers";
	public static final String LIST_LOCATOR_NAMES="listLocatorMembers";
	public static final String LIST_REGION_NAMES="listRegions";
	public static final String LIST_REGION_PATH_NAMES="listAllRegionPaths";
	public static final String LIST_GROUP_NAMES="listGroups";
	public static final String LIST_SENDER_NAMES="listGatewaySenders";
	public static final String LIST_RECEIVER_NAMES="listGatewayReceivers";
	public static final String FETCH_MEMBER_NAME_OBJECT="fetchMemberObjectName";
	public static final String FETCH_DISK_STORE_OBJECT="fetchDiskStoreObjectName";
	public static final String FETCH_DISTRIBUTED_LOCK_OBJECT="fetchDistributedLockServiceObjectName";
	public static final String FETCH_LOCK_OBJECT="fetchLockServiceObjectName";
	public static final String FETCH_DISTRIBUTED_REGION_OBJECT="fetchDistributedRegionObjectName";
	public static final String FETCH_REGION_OBJECT="fetchRegionObjectName";
	public static final String FETCH_GATEWAY_RECEIVER_OBJECT="fetchGatewayReceiverObjectName";
	public static final String FETCH_GATEWAY_SENDER_OBJECT="fetchGatewaySenderObjectName";
	public static final String ASYNC_OBJECT_NAME="GemFire:service=AsyncEventQueue,queue=qname,type=Member,member=mname";
	public static final String THRESHOLD_MESSAGE="Threshold Exceeded";
	public static final String SOURCE="MONITOR";
}
