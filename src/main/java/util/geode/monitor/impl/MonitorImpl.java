package util.geode.monitor.impl;

import java.io.*;
import java.math.BigDecimal;
import java.net.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.*;
import javax.management.remote.*;
import javax.xml.bind.JAXBContext;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

//import org.apache.commons.lang3.StringUtils;

import util.geode.monitor.Constants;
import util.geode.monitor.Constants.ListType;
import util.geode.monitor.Constants.LogType;
import util.geode.monitor.Constants.ObjectNameType;
import util.geode.monitor.Monitor;
import util.geode.monitor.ThresholdDetail;
import util.geode.monitor.ThresholdDetail.DetailType;
import util.geode.monitor.Util;
import util.geode.monitor.log.LogHeader;
import util.geode.monitor.log.LogMessage;
import util.geode.monitor.xml.ExcludedMessage;
import util.geode.monitor.xml.ExcludedMessageObjectFactory;
import util.geode.monitor.xml.ExcludedMessages;
import util.geode.monitor.xml.FieldSizeType;
import util.geode.monitor.xml.GemFireThread;
import util.geode.monitor.xml.GemFireThreadObjectFactory;
import util.geode.monitor.xml.GemFireThreads;
import util.geode.monitor.xml.MxBeanType;
import util.geode.monitor.xml.MxBeans;
import util.geode.monitor.xml.MxBeansObjectFactory;
import util.geode.monitor.xml.MxBeans.MxBean;

/**
 * @author PaulVermeulen
 *
 */

public abstract class MonitorImpl implements Monitor {
	private Util util = new Util();
	private MxBeans mxBeans;
	private JMXConnector jmxConnection;
	private MBeanServerConnection mbs;
	private ObjectName systemName;
	private NotificationListener connectionListener;
	private NotificationListener mbsListener;
	private AtomicBoolean jmxConnectionActive = new AtomicBoolean(false);
	private List<LogMessage> messages = new ArrayList<LogMessage>();
	private ExcludedMessages excludedMessages = new ExcludedMessages();
	private GemFireThreads gemfireThreads = new GemFireThreads();
	private Logger applicationLog;
	private Logger exceptionLog;
	private ScheduledThreadPoolExecutor scheduleExecutor = new ScheduledThreadPoolExecutor(1);
	private ScheduledFuture<AgentMonitorTask> agentStartTimer;
	private long messageLifeDuration = 60000 * 15;
	private long reconnectWaitTime = 60;
	private String jmxHost;
	private String site = Constants.NOT_DEFINED;
	private String environment = Constants.NOT_DEFINED;
	private String cluster = Constants.NOT_DEFINED;
	private int messageDuplicateLimit = 3;
	private int jmxPort = 1099;
	private int commandPort = 6780;
	private int reconnectRetryAttempts = 5;
	private int reconnectRetryCount = 0;
	private boolean shutdown = false;
	private String[] servers;
	private ObjectName[] members;
	private ObjectName[] cacheServers;
	private ObjectName[] distributedRegions;
	private ObjectName[] distributedLocks;
	private List<String> jmxHosts = new ArrayList<String>();
	private int nextHostIndex;
	private String lastJmxHost = "";
	private JMXServiceURL url = null;
	private String[] blockers;

	public MonitorImpl() {
	}

	/**
	 * Checks to see if the jmx connection is still valid
	 * 
	 * process the xml files connect to agent
	 * 
	 * @throws Exception
	 */
	public boolean isAttachedToManager() {
		try {
			Thread.sleep(1000);
			jmxConnection.getConnectionId();
		} catch (IOException e) {
			setJmxConnectionActive(false);
			return false;
		} catch (InterruptedException e) {
			// do nothing
		}
		return getJmxConnectionActive();
	}

	/**
	 * Initialize the health and threshold monitor
	 * 
	 * process the xml files connect to agent
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void initialize() throws Exception {
		loadMonitorProps();
		createLogAppender();
		setGemfireThreads((GemFireThreads) getUtil()
				.processJAXB(JAXBContext.newInstance(GemFireThreadObjectFactory.class), Constants.GEMFIRE_THREAD_FILE));
		setExcludedMessages((ExcludedMessages) getUtil().processJAXB(
				JAXBContext.newInstance(ExcludedMessageObjectFactory.class), Constants.EXCLUDED_MESSAGE_FILE));
		setMxBeans((MxBeans) getUtil().processJAXB(JAXBContext.newInstance(MxBeansObjectFactory.class),
				Constants.MXBEAN_FILE));

		if (!connect()) {
			if (getAgentStartTimer() == null) {
				setAgentStartTimer((ScheduledFuture<AgentMonitorTask>) getScheduleExecutor()
						.schedule(new AgentMonitorTask(), getReconnectWaitTime(), TimeUnit.SECONDS));
			} else {
				if (getAgentStartTimer().isDone()) {
					setAgentStartTimer((ScheduledFuture<AgentMonitorTask>) getScheduleExecutor()
							.schedule(new AgentMonitorTask(), getReconnectWaitTime(), TimeUnit.SECONDS));
				}
			}
		} else {
			setLastJmxHost(getJmxHost());
		}
	}

	/**
	 * Creates the log appender for application and exception log
	 */
	private void createLogAppender() {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL url = loader.getResource("log4j.properties");
		PropertyConfigurator.configure(url);
		setApplicationLog(Logger.getLogger("applicationLog"));
		setExceptionLog(Logger.getLogger("exceptionLog"));
	}

	/**
	 * Connect to the JMX manager
	 * 
	 * add connection notification listener add JMX notification listener set up
	 * health monitoring
	 */
	private boolean connect() {
		boolean connection = true;
		for (int i = 0; i < getJmxHosts().size(); i++) {
			try {
				setJmxHost(getJmxHosts().get(getNextHostIndex() - 1));
				if ((getNextHostIndex() + 1) > getJmxHosts().size()) {
					setNextHostIndex(1);
				} else {
					setNextHostIndex(getNextHostIndex() + 1);
				}
				String urlString = "service:jmx:rmi://" + jmxHost + "/jndi/rmi://" + getJmxHost() + ":" + getJmxPort()
						+ "/jmxrmi";

				setUrl(new JMXServiceURL(urlString));
				setJmxConnection(JMXConnectorFactory.connect(url));
				setSystemName(new ObjectName(Constants.DISTRIBUTED_SYSTEM_OBJECT_NAME));
				setMbs(jmxConnection.getMBeanServerConnection());
				setConnectionListener(addConnectionNotificationListener());
				getJmxConnection().addConnectionNotificationListener(getConnectionListener(), null, null);
				setMbsListener(addGemFireNotificationListener());
				getMbs().addNotificationListener(getSystemName(), getMbsListener(), null, null);
				setReconnectRetryCount(0);
				setupMonitoring();
				break;
			} catch (IOException e) {
				log(LogType.ERROR.toString(), getJmxHost(),
						"JMX Manager not running for URL: " + getUrl() + " " + e.getMessage(), null);
				connection = false;
			} catch (Exception e) {
				log(LogType.ERROR.toString(), getJmxHost(), e.getMessage(), null);
				connection = false;
			}
		}

		setJmxConnectionActive(connection);

		return connection;
	}

	/**
	 * Change the log level from error to warning to get all warning or higher
	 * alerts
	 * 
	 * create the JMX Object names defined in the cluster
	 * 
	 * get JMX details for logging start the threshold monitor thread
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("incomplete-switch")
	private void setupMonitoring() throws Exception {
		int i;
		int j;

		changeLogLevel();

		setServers((String[]) getUtil().getNames(mbs, systemName, Constants.ListType.SERVERS));
		setMembers(new ObjectName[getServers().length]);
		for (i = 0; i < getServers().length; i++) {
			setMember(getUtil().getObjectName(mbs, systemName, Constants.ObjectNameType.MEMBER, getServer(i), null), i);
		}

		setCacheServers(getUtil().getObjectNames(mbs, systemName, Constants.ObjectNameType.CACHE_SERVERS));

		for (MxBean mxBean : getMxBeans().getMxBean()) {
			j = 0;
			switch (mxBean.getMxBeanName()) {
			case DISTRIBUTED_REGION_MX_BEAN:
				setDistributedRegions(new ObjectName[mxBean.getFields().getField().size()]);
				for (MxBeans.MxBean.Fields.Field field : mxBean.getFields().getField()) {
					setDistributedRegion(getUtil().getObjectName(mbs, systemName, Constants.ObjectNameType.REGION_DIST,
							field.getBeanProperty(), null), j);
					j++;
				}
				break;
			case DISTRIBUTED_LOCK_SERVICE_MX_BEAN:
				setDistributedLocks(new ObjectName[mxBean.getFields().getField().size()]);
				for (MxBeans.MxBean.Fields.Field field : mxBean.getFields().getField()) {
					setDistributedLock(getUtil().getObjectName(mbs, systemName, Constants.ObjectNameType.LOCK_DIST,
							field.getBeanProperty(), null), j);
					j++;
				}
				break;
			}
		}
		getJMXDetails();
		Thread thread = new Thread(new ThresholdMonitorTask());
		thread.start();
	}

	/**
	 * this method disconnects from the JMX manager
	 */
	public void disconnect() {
		try {
			getMbs().removeNotificationListener(getSystemName(), getMbsListener());
		} catch (Exception e) {
			log(LogType.ERROR.toString(), "Internal", e.getMessage(), null);
		}

		try {
			getJmxConnection().removeConnectionNotificationListener(getConnectionListener());
			getJmxConnection().close();
		} catch (Exception e) {
			log(LogType.ERROR.toString(), "Internal", e.getMessage(), null);
		}
	}

	/**
	 * JMX connection notification listener
	 * 
	 * Handles connection notifications
	 */
	private NotificationListener addConnectionNotificationListener() {

		return new NotificationListener() {

			@SuppressWarnings("unchecked")
			public void handleNotification(Notification notification, Object handback) {
				long connectTime;

				if (notification instanceof JMXConnectionNotification) {
					if (notification.getType().equals(Constants.M_JMX_CLOSED)
							|| notification.getType().equals(Constants.M_JMX_FAILED)
							|| notification.getType().equals(Constants.M_JMX_LOST)) {
						log(LogType.ERROR.toString(), notification.getSource().toString(), notification.getMessage(),
								notification.getUserData());

						if (!shutdown) {
							if ((notification.getMessage().contains(Constants.M_JMX_CONNECT_EXCEPTION))
									|| (notification.getMessage().contains(Constants.M_JMX_SERVER_FAILED))) {

								closeConnections();

								buildSpecialLogMessage(jmxHost + " : " + Constants.M_JMX_MGR_DEPARTED, Constants.MAJOR,
										notification.getTimeStamp(), jmxHost);

								if (jmxHosts.size() > 1) {
									connectTime = 5L;
								} else {
									connectTime = getReconnectWaitTime();
								}

								if (agentStartTimer == null) {
									agentStartTimer = (ScheduledFuture<AgentMonitorTask>) getScheduleExecutor()
											.schedule(new AgentMonitorTask(), connectTime, TimeUnit.SECONDS);
								} else {
									if (agentStartTimer.isDone()) {
										agentStartTimer = (ScheduledFuture<AgentMonitorTask>) getScheduleExecutor()
												.schedule(new AgentMonitorTask(), connectTime, TimeUnit.SECONDS);
									}
								}
							}
						} else {
							buildSpecialLogMessage(Constants.M_MON_MGR_LEFT, Constants.WARNING, new Date().getTime(),
									"Monitor");
						}
					} else if (notification.getType().equals(Constants.M_JMX_OPENED)) {
						createNotification(LogType.INFO.toString(), notification,
								notification.getSource() + " : " + Constants.M_JMX_MGR_OPENED, Constants.CLEAR);
					}
					return;
				}

			}
		};

	}

	/**
	 * JMX notifications for GemFire logs with warning level or higher
	 * 
	 * @throws Exception
	 */
	private NotificationListener addGemFireNotificationListener() throws Exception {

		return new NotificationListener() {

			public void handleNotification(Notification notification, Object handback) {

				if (isAttachedToManager()) {
					if (Constants.JOINED.equals(notification.getType())) {
						if (!isBlocked((String) notification.getSource())) {
							createNotification(LogType.INFO.toString(), notification,
									notification.getSource() + " : " + Constants.M_MEMBER_JOINED, Constants.CLEAR);
						}
					} else if (Constants.LEFT.equals(notification.getType())) {
						if (!isBlocked((String) notification.getSource())) {
							createNotification(LogType.ERROR.toString(), notification,
									notification.getSource() + " : " + Constants.M_MEMBER_LEFT, Constants.MAJOR);
						}
					} else if (Constants.CRASHED.equals(notification.getType())) {
						if (!isBlocked((String) notification.getSource())) {
							createNotification(LogType.ERROR.toString(), notification,
									notification.getSource() + " : " + Constants.M_MEMBER_CRASH, Constants.CRITICAL);
						}
					} else if (Constants.SUSPECT.equals(notification.getType())) {
						if (!notification.getMessage().contains("By")) {
							if (!isBlocked((String) notification.getSource())) {
								createNotification(LogType.ERROR.toString(), notification,
										notification.getSource() + " : " + Constants.M_MEMBER_SUSPECT,
										Constants.CRITICAL);
							}
						}
					} else if (Constants.ALERT.equals(notification.getType())) {
						if (!isBlocked((String) notification.getSource())) {
							log(getUtil().getUserDataInfo(Constants.ALERT_LEVEL, notification.getUserData()),
									(String) getUtil().getUserDataInfo(Constants.MEMBER, notification.getUserData()),
									notification.getMessage(), notification.getUserData());
							processMessage(notification);
						}
					}
				}
			}
		};
	}

	/**
	 * Checks to see if a member is blocked
	 * 
	 * @param blockId
	 * 
	 * @return boolean
	 */
	private boolean isBlocked(String blockId) {
		if (blockers != null && blockers.length > 0) {
			for (String blocker : blockers) {
				if (blocker.equals(blockId))
					return true;
			}
		}
		return false;
	}

	/**
	 * Changes the default log level to warning
	 * 
	 * @throws Exception
	 */
	private void changeLogLevel() throws Exception {
		mbs.invoke(getSystemName(), Constants.ALERT_LEVEL_CHANGE, new Object[] { Constants.WARNING.toLowerCase() },
				new String[] { String.class.getName() });
	}

	/**
	 * Creates a notification alert message
	 * 
	 * @param logType
	 * @param notification
	 * @param specialMsg
	 * @param error
	 */
	private void createNotification(String logType, Notification notification, String specialMsg, String error) {
		log(logType, notification.getSource().toString(), notification.getMessage(), notification.getUserData());
		if (specialMsg != null) {
			buildSpecialLogMessage(specialMsg, error, notification.getTimeStamp(), notification.getSource().toString());
		}
	}

	/**
	 * Get the details from the locator member for the cluster name, environment and
	 * site
	 */
	private void getJMXDetails() {
		try {
			String[] members = util.getNames(mbs, systemName, ListType.LOCATORS);
			ObjectName memberName = new ObjectName(Constants.MEMBER_OBJECT_NAME + members[0]);
			String status = (String) mbs.invoke(memberName, Constants.STATUS.toLowerCase(), new Object[] {},
					new String[] {});
			status = status.substring(1, status.length() - 2);
			String[] statuses = status.split(",");
			for (String stat : statuses) {
				stat = stat.replaceAll("\"", "");
				String[] keyValue = stat.split(":");
				if (keyValue[0].contains(Constants.GF_CLUSTER)) {
					setCluster(keyValue[0].substring(Constants.GF_CLUSTER.length()).toUpperCase());
				} else if (keyValue[0].contains(Constants.GF_ENV)) {
					setEnvironment(keyValue[0].substring(Constants.GF_ENV.length()).toUpperCase());
				} else if (keyValue[0].contains(Constants.GF_SITE)) {
					setSite(keyValue[0].substring(Constants.GF_SITE.length()).toUpperCase());
				}
			}
		} catch (Exception e) {
			log(LogType.ERROR.toString(), "Internal", "getJMXDetails: " + e.getMessage(), null);
			throw new RuntimeException(e.toString());
		}
	}

	/**
	 * 
	 * close connections with the MXBean server and JMX
	 * 
	 */
	private void closeConnections() {
		disconnect();
		setJmxConnectionActive(false);
	}

	/**
	 * 
	 * Build a special log message for EMM
	 * 
	 * @param message
	 * @param level
	 * @param timeStamp
	 * @param member
	 */
	private void buildSpecialLogMessage(String message, String level, long timeStamp, String member) {
		SimpleDateFormat df = new SimpleDateFormat(Constants.DATE_FORMAT);
		SimpleDateFormat tf = new SimpleDateFormat(Constants.TIME_FORMAT);
		SimpleDateFormat zf = new SimpleDateFormat(Constants.ZONE_FORMAT);

		LogHeader header = new LogHeader(level, df.format(timeStamp), tf.format(timeStamp), zf.format(timeStamp),
				member, null, null);

		LogMessage logMessage = new LogMessage(header, message);
		logMessage.setEvent(null);
		sendAlert(logMessage);
		writeLog(logMessage);
	}

	private void processMessage(Notification notification) {
		process(notification);
	}

	private void process(Notification notification) {
		if ((notification.getMessage() == null) || (notification.getMessage().length() == 0))
			return;
		removeOldMessages();
		LogMessage logMessage = new LogMessage();
		logMessage.setHeader(getUtil().parseHeader(notification));
		logMessage.setEvent(notification);
		logMessage.setBody(notification.getMessage());
		if (validMessage(logMessage)) {
			if (!checkForDuplicateMessage(logMessage)) {
				messages.add(logMessage);
				sendAlert(logMessage);
				writeLog(logMessage);
			}
		}
	}

	/**
	 * Log a message to the application log file
	 * 
	 * @param logType
	 * @param member
	 * @param message
	 * @param user
	 */
	private void log(String logType, String member, String message, Object user) {
		Object userData = "";
		String memberData = "";
		if (user != null)
			userData = user;
		if (member != null)
			memberData = member;
		StringBuilder str = new StringBuilder();
		SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT);
		String dt = sdf.format(new Date());
		str.append(getLoggingHeader());
		str.append(" | " + dt);
		str.append(" | " + logType);
		str.append(" | " + memberData);
		getApplicationLog().info(str.toString() + " | " + message + " " + userData);
	}

	/**
	 * Creates the environment, cluster and site names if defined otherwise use
	 * default names
	 * 
	 * @return environment, cluster and site names
	 */
	private String getLoggingHeader() {
		StringBuilder sb = new StringBuilder();
		if (getEnvironment() != null && getEnvironment().length() > 0) {
			sb.append(getEnvironment());
		} else {
			sb.append("Environment");
		}
		if (getCluster() != null && getCluster().length() > 0) {
			sb.append(" | " + getCluster());
		} else {
			sb.append(" | Cluster");
		}
		if (getSite() != null && getSite().length() > 0) {
			sb.append(" | " + getSite());
		} else {
			sb.append(" | Site");
		}
		return sb.toString();
	}

	/**
	 * Writes a message to the monitor exception log
	 * 
	 * @param logMessage
	 */
	private void writeLog(LogMessage logMessage) {
		StringBuilder str = new StringBuilder();
		str.append(getLoggingHeader());
		str.append(" | " + logMessage.getHeader().getDate() + " " + logMessage.getHeader().getTime());
		str.append(" | " + logMessage.getHeader().getSeverity());
		str.append(" | " + logMessage.getHeader().getMember());
		str.append(" | " + logMessage.getBody());
		getExceptionLog().warn(str.toString());
	}

	/**
	 * send alert with message
	 * 
	 * @param logMessage
	 */
	public abstract void sendAlert(LogMessage logMessage);

	/**
	 * Check for duplicate event messages. Event message timeout after a define
	 * period.
	 * 
	 * This code prevents multiple duplicate message during a time frame
	 * 
	 * @param message
	 * @return
	 */
	private boolean checkForDuplicateMessage(LogMessage message) {
		for (LogMessage logMessage : messages) {
			if (logMessage.getHeader().getSeverity().equals(message.getHeader().getSeverity())
					&& logMessage.getHeader().getMember().equals(message.getHeader().getMember())) {
				if (!logMessage.getHeader().getEvent().equals(message.getHeader().getEvent())) {
					if (checkGemfireThread(logMessage.getHeader().getEvent(), message.getHeader().getEvent()))
						return true;
				} else {
					if (checkGemfireThread(logMessage.getHeader().getEvent(), message.getHeader().getEvent()))
						return true;
				}
				if (logMessage.getBody().equals(message.getBody())) {
					logMessage.setCount(logMessage.getCount() + 1);
					if (logMessage.getCount() > messageDuplicateLimit) {
						return true;
					}
				} else {
					if (message.getHeader().getTid().equals(Constants.THRESHOLD_MESSAGE)) {
						if ((logMessage.getHeader().getEvent().equals(message.getHeader().getEvent()))
								&& (logMessage.getHeader().getTid().equals(message.getHeader().getTid()))) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Checks for a special duplicate thread message
	 * 
	 * GF example. When the gateway is running you can get the same message for
	 * various threads that are identical. This method prevents sending duplicates.
	 * 
	 * @param existingThread
	 * @param newThread
	 * @return
	 */
	private boolean checkGemfireThread(String existingThread, String newThread) {
		for (GemFireThread gfMessage : getGemfireThreads().getGemfireThreadList()) {
			if ((existingThread.contains(gfMessage.getThread())) && (newThread.contains(gfMessage.getThread()))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks to see if the message received is valid
	 * 
	 * @param logMessage
	 * @return
	 */
	private boolean validMessage(LogMessage logMessage) {
		boolean result = true;
		for (ExcludedMessage message : excludedMessages.getExcludedMessageList()) {
			result = getUtil().validateCriteria(logMessage, message);
			if (!result)
				break;
		}
		return result;
	}

	/**
	 * This method removes old messages that have reached the expiration time.
	 * 
	 */
	private void removeOldMessages() {
		List<LogMessage> newMessages = new ArrayList<LogMessage>();
		long dateTimeNow = new Date().getTime();
		for (LogMessage logMessage : messages) {
			SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_TIME_FORMAT);
			try {
				Date messageDate = sdf.parse(logMessage.getHeader().getDate() + " " + logMessage.getHeader().getTime());
				long msgDate = messageDate.getTime();
				msgDate = (dateTimeNow - msgDate);
				if (msgDate < messageLifeDuration) {
					newMessages.add(logMessage);
				}
			} catch (ParseException e) {
				newMessages.add(logMessage);
			}
		}
		messages = newMessages;
		newMessages = null;
		return;
	}

	/**
	 * This method processes the monitor property file and configures the service
	 * 
	 */
	private void loadMonitorProps() {
		int value = 0;
		long lValue = 0;
		String[] split;
		Properties monitorProps = new Properties();

		try {
			monitorProps.load(MonitorImpl.class.getClassLoader().getResourceAsStream(Constants.HM_PROPS));

			setJmxHost(monitorProps.getProperty(Constants.P_MANAGERS));
			if ((getJmxHost() == null) || (getJmxHost().length() == 0)) {
				throw new RuntimeException(Constants.E_HOST);
			}

			split = getJmxHost().split(",");
			if (split.length > 0) {
				for (String str : split) {
					addJmxHost(str);
				}
			} else {
				split = jmxHost.split(" ");
				if (split.length > 0) {
					for (String str : split) {
						addJmxHost(str);
					}
				}
			}

			setNextHostIndex(1);
			if (getJmxHosts().size() == 0) {
				addJmxHost(jmxHost);
			}

			value = Integer.parseInt(monitorProps.getProperty(Constants.P_PORT));
			if (value == 0) {
				throw new RuntimeException(Constants.E_PORT);
			} else {
				setJmxPort(value);
			}

			value = Integer.parseInt(monitorProps.getProperty(Constants.P_COMMAND_PORT));
			if (value == 0) {
				throw new RuntimeException(Constants.E_COMMAND_PORT);
			} else {
				setCommandPort(value);
			}

			lValue = Long.parseLong(monitorProps.getProperty(Constants.P_MSG_DUR));
			if (lValue > 0) {
				setMessageLifeDuration(60000 * lValue);
			}

			value = Integer.parseInt(monitorProps.getProperty(Constants.P_MAX_DUPS));
			if (value > 0) {
				setMessageDuplicateLimit(value);
			}

			lValue = Long.parseLong(monitorProps.getProperty(Constants.P_RECONNECT_W_TIME));
			if (lValue > 0) {
				setReconnectWaitTime(lValue);
			}

			value = Integer.parseInt(monitorProps.getProperty(Constants.P_RECONNECT_R_ATTEMPTS));
			if (value > 0) {
				setReconnectRetryAttempts(value);
			}

		} catch (IOException e) {
			throw new RuntimeException(Constants.E_PROC_PROPS + e.getMessage());
		}
	}

	/**
	 * This class is used to schedule reconnection in the event the JMX monitor went
	 * down
	 * 
	 */
	private class AgentMonitorTask extends TimerTask {
		@SuppressWarnings("unchecked")
		public void run() {
			if (!connect()) {
				agentStartTimer = (ScheduledFuture<AgentMonitorTask>) getScheduleExecutor()
						.schedule(new AgentMonitorTask(), reconnectWaitTime, TimeUnit.SECONDS);
				setReconnectRetryCount(getReconnectRetryCount() + 1);
				if (getReconnectRetryCount() > getReconnectRetryAttempts()) {
					setReconnectRetryCount(0);
					buildSpecialLogMessage("JMX Agent:" + Constants.M_JMX_FAILED, Constants.MAJOR, new Date().getTime(),
							"JMX Agent");
				}
			} else {
				log(LogType.INFO.toString(), "jmxHost", getJmxHost() + " : " + Constants.M_JMX_MGR_JOINED, null);
				if (!(getJmxHost().equals(getLastJmxHost())) && (getLastJmxHost() != "")) {
					buildSpecialLogMessage(Constants.M_MON_MGR_NEW + " new manager: " + getJmxHost()
							+ " previous manager: " + getLastJmxHost(), Constants.CLEAR, new Date().getTime(),
							getLastJmxHost());
				}
				setLastJmxHost(getJmxHost());
			}
		}
	}

	/**
	 * This method process the mxbeans defined in statistics xml file
	 * 
	 * @throws Exception
	 */
	private void processMbeans() throws Exception {

		for (MxBeans.MxBean bean : getMxBeans().getMxBean()) {
			switch (bean.getMxBeanName()) {
			case DISTRIBUTED_SYSTEM_MX_BEAN:
				getAttributes(bean, getSystemName());
				break;
			case MEMBER_MX_BEAN:
				for (ObjectName name : getMembers()) {
					if (!isBlocked(name.getKeyProperty("member")))
						getAttributes(bean, name);
				}
				break;
			case CACHE_SERVER_MX_BEAN:
				for (ObjectName name : getCacheServers()) {
					if (!isBlocked(name.getKeyProperty("member")))
						getAttributes(bean, name);
				}
				break;
			case DISK_STORE_MX_BEAN:
				for (String name : getServers()) {
					if (!isBlocked(name))
						getUniqueAttributes(bean, name, Constants.ObjectNameType.DISK_STORE);
				}
				break;
			case REGION_MX_BEAN:
				for (String name : getServers()) {
					if (!isBlocked(name))
						getUniqueAttributes(bean, name, Constants.ObjectNameType.REGION);
				}
				break;
			case LOCK_SERVICE_MX_BEAN:
				for (String name : getServers()) {
					if (!isBlocked(name))
						getUniqueAttributes(bean, name, Constants.ObjectNameType.LOCK);
				}
				break;
			case ASYNC_EVENT_QUEUE_MX_BEAN:
				for (String name : getServers()) {
					if (!isBlocked(name))
						getUniqueAttributes(bean, name, Constants.ObjectNameType.ASYNC_QUEUES);
				}
				break;
			case GATEWAY_SENDER_MX_BEAN:
				for (String name : getServers()) {
					if (!isBlocked(name))
						getUniqueAttributes(bean, name, Constants.ObjectNameType.GATEWAY_SENDERS);
				}
				break;
			case GATEWAY_RECEIVER_MX_BEAN:
				for (String name : getServers()) {
					if (!isBlocked(name))
						getUniqueAttributes(bean, name, Constants.ObjectNameType.GATEWAY_RECEIVERS);
				}
				break;
			case DISTRIBUTED_REGION_MX_BEAN:
				for (ObjectName name : getDistributedRegions()) {
					if (!isBlocked(name.getKeyProperty("member")))
						getAttributes(bean, name);
				}
				break;
			case DISTRIBUTED_LOCK_SERVICE_MX_BEAN:
				for (ObjectName name : getDistributedLocks()) {
					if (!isBlocked(name.getKeyProperty("member")))
						getAttributes(bean, name);
				}
				break;
			}
		}
	}

	/**
	 * This method processes each field for an mbean and queries the attribute to
	 * get the value and then call the method to validate the threshold
	 * 
	 * this method handles query for object name
	 * 
	 * @param bean
	 * @param oName
	 * @return
	 * @throws Exception
	 */
	private void getUniqueAttributes(MxBeans.MxBean bean, String server, ObjectNameType type) throws Exception {
		boolean percentFieldValue = false;
		String[] attributes = null;
		ObjectName oName;

		for (MxBeans.MxBean.Fields.Field field : bean.getFields().getField()) {
			percentFieldValue = false;
			attributes = new String[2];
			if (field.getPercentageField().equals("")) {
				attributes[0] = field.getFieldName();
			} else if (field.getPercentageField() != null && StringUtils.isNumeric(field.getPercentageField())) {
				percentFieldValue = true;
			} else {
				attributes[0] = field.getFieldName();
				attributes[1] = field.getPercentageField();
			}

			if (ObjectNameType.ASYNC_QUEUES.equals(type)) {
				String name = Constants.ASYNC_OBJECT_NAME.replace("qname", field.getBeanProperty()).replace("mname",
						server);
				oName = new ObjectName(name);

			} else {
				oName = getUtil().getObjectName(mbs, systemName, type, server, field.getBeanProperty());
			}
			AttributeList attrList = getUtil().getAttributes(mbs, oName, attributes);
			if (percentFieldValue) {
				attrList.add(new Attribute(field.getFieldName() + "-constant", field.getPercentageField()));
			}
			doThreshold(attrList, bean, field, oName);
		}
	}

	/**
	 * This method processes each field for an mbean and queries the attribute to
	 * get the value and then call the method to validate the threshold
	 * 
	 * this method handles a known object name
	 * 
	 * @param bean
	 * @param oName
	 * @return
	 * @throws Exception
	 */
	private void getAttributes(MxBeans.MxBean bean, ObjectName oName) throws Exception {
		boolean percentFieldValue = false;
		String[] attributes = null;
		for (MxBeans.MxBean.Fields.Field field : bean.getFields().getField()) {
			percentFieldValue = false;
			attributes = new String[2];
			if (field.getPercentageField().equals("") || field.getPercentageField() == null) {
				attributes[0] = field.getFieldName();
			} else if (field.getPercentageField() != null && StringUtils.isNumeric(field.getPercentageField())) {
				percentFieldValue = true;
			} else {
				attributes[0] = field.getFieldName();
				attributes[1] = field.getPercentageField();
			}
			AttributeList attrList = getUtil().getAttributes(mbs, oName, attributes);
			if (percentFieldValue) {
				attrList.add(new Attribute(field.getFieldName() + "-constant", field.getPercentageField()));
			}
			doThreshold(attrList, bean, field, oName);
		}
	}

	/**
	 * This method calls the check threshold method and if threshold is exceeded
	 * creates a threshold detail object
	 * 
	 * 1param attrList
	 * 
	 * @param bean
	 * @param fields
	 * @param oName
	 */
	private void doThreshold(AttributeList attrList, MxBeans.MxBean bean, MxBeans.MxBean.Fields.Field field,
			ObjectName oName) {
		StringBuilder str;
		ThresholdDetail tDetail = checkThreshold(attrList, field.getCount(), field.getPercentage(),
				field.getFieldSize(), field.getPercentageFieldSize());
		if (tDetail != null) {
			// send alert
			str = new StringBuilder();
			str.append(Constants.THRESHOLD_MESSAGE);
			str.append(" " + bean.getMxBeanName());
			str.append(" Property: " + field.getBeanProperty());
			str.append(" Field: " + tDetail.getField() + " value=" + tDetail.getValue() + " exceeds" + " threshold="
					+ tDetail.getThresholdValue());
			if (tDetail.getType().equals(DetailType.PERCENT)) {
				str.append(" [" + tDetail.getPercentage() + "% of field " + tDetail.getPercentageField() + ":"
						+ tDetail.getPercentageValue() + "]");
			} else {
				str.append(" [count]");
			}
			Map<String, String> nObject = new HashMap<String, String>();
			nObject.put(Constants.ALERT_LEVEL, Constants.WARNING);
			if ((field.getBeanProperty() != null) && (field.getBeanProperty() != "")) {
				nObject.put(Constants.THREAD, field.getBeanProperty() + ":" + tDetail.getField());
			} else {
				nObject.put(Constants.THREAD, bean.getMxBeanName().toString() + ":" + tDetail.getField());
			}
			nObject.put(Constants.TID, Constants.THRESHOLD_MESSAGE);
			Notification notification = new Notification(Constants.ALERT, Constants.SOURCE, 0L, new Date().getTime(),
					str.toString());
			if ((bean.getMxBeanName().equals(MxBeanType.DISTRIBUTED_SYSTEM_MX_BEAN))
					|| (bean.getMxBeanName().equals(MxBeanType.DISTRIBUTED_REGION_MX_BEAN))
					|| (bean.getMxBeanName().equals(MxBeanType.DISTRIBUTED_LOCK_SERVICE_MX_BEAN))) {
				nObject.put(Constants.MEMBER, Constants.CLUSTER);
				notification.setUserData(nObject);
				processMessage(notification);
			} else {
				nObject.put(Constants.MEMBER, oName.getKeyProperty("member"));
				notification.setUserData(nObject);
				processMessage(notification);
			}
		}
	}

	/**
	 * This method check the attributes for a mxbean and validates if a threshold
	 * has been exceeded.
	 * 
	 * @param attributes
	 * @param count
	 * @param percent
	 * @return
	 */
	private ThresholdDetail checkThreshold(AttributeList attributes, int count, BigDecimal percent,
			FieldSizeType fieldType, FieldSizeType percentFieldType) {
		long l1 = 0;
		long l2 = 0;
		long cnt = Long.parseLong(String.valueOf(count));
		Attribute attribute = (Attribute) attributes.get(0);
		if (attribute.getValue() instanceof Integer) {
			l1 = Long.parseLong(String.valueOf(attribute.getValue()));
			if (attributes.size() == 2) {
				attribute = (Attribute) attributes.get(1);
				l2 = Long.parseLong(String.valueOf(attribute.getValue()));
			}
		} else {
			l1 = (Long) attribute.getValue();
			if (attributes.size() == 2) {
				attribute = (Attribute) attributes.get(1);
				l2 = (Long) attribute.getValue();
			}
		}
		if (attributes.size() == 2) {
			// use percent
			if (l2 == -1)
				l2 = 0;

			if (l1 == -1)
				l1 = 0;

			if (fieldType != null) {
				if (FieldSizeType.MEGABYTES.equals(fieldType)) {
					l1 = (l1 * 1000000);
				} else if (FieldSizeType.KILOBYTES.equals(percentFieldType)) {
					l1 = (l1 * 1000);
				}
			}

			if (percentFieldType != null) {
				if (FieldSizeType.MEGABYTES.equals(percentFieldType)) {
					l2 = (l2 * 1000000);
				} else if (FieldSizeType.KILOBYTES.equals(percentFieldType)) {
					l2 = (l2 * 1000);
				}
			}

			long result = percent.multiply(new BigDecimal(l2)).longValue();
			if (l1 > result) {
				Attribute attr1 = (Attribute) attributes.get(0);
				Attribute attr2 = (Attribute) attributes.get(1);
				return new ThresholdDetail(l1, result, attr1.getName(), percent, attr2.getName(), l2);
			}
			return null;
		} else {
			// use count
			if (l1 == -1)
				l1 = 0;

			if (fieldType != null) {
				if (FieldSizeType.MEGABYTES.equals(fieldType)) {
					l1 = (l1 * 1000000);
				} else if (FieldSizeType.KILOBYTES.equals(percentFieldType)) {
					l1 = (l1 * 1000);
				}
			}

			if (l1 > cnt) {
				Attribute attr1 = (Attribute) attributes.get(0);
				return new ThresholdDetail(l1, cnt, attr1.getName());
			}
			return null;
		}
	}

	/**
	 * This class is used to spawn a thread for threshold monitoring
	 * 
	 */
	private class ThresholdMonitorTask extends Thread {

		public void run() {
			while (!shutdown) {
				try {
					Thread.sleep(mxBeans.getSampleTime());
					if (getJmxConnectionActive())
						processMbeans();
				} catch (InterruptedException e) {
					// do nothing
				} catch (Exception e) {
					if (e instanceof java.rmi.ConnectException) {
						if (getJmxConnectionActive()) {
							setJmxConnectionActive(false);
						}
					}
					log(LogType.INFO.toString(), "ThresholdMonitorTask: ", e.getMessage(), null);
				}
			}
		}
	}

	private ScheduledThreadPoolExecutor getScheduleExecutor() {
		return scheduleExecutor;
	}

	private String getSite() {
		return site;
	}

	private void setSite(String site) {
		this.site = site;
	}

	private String getEnvironment() {
		return environment;
	}

	private void setEnvironment(String environment) {
		this.environment = environment;
	}

	private String getCluster() {
		return cluster;
	}

	private void setCluster(String cluster) {
		this.cluster = cluster;
	}

	private String[] getServers() {
		return servers;
	}

	private String getServer(int index) {
		return servers[index];
	}

	private void setServers(String[] servers) {
		this.servers = servers;
	}

	private ObjectName[] getMembers() {
		return members;
	}

	private void setMembers(ObjectName[] members) {
		this.members = members;
	}

	private void setMember(ObjectName members, int index) {
		this.members[index] = members;
	}

	private ObjectName[] getCacheServers() {
		return cacheServers;
	}

	private void setCacheServers(ObjectName[] cacheServers) {
		this.cacheServers = cacheServers;
	}

	private ObjectName[] getDistributedRegions() {
		return distributedRegions;
	}

	private void setDistributedRegions(ObjectName[] distributedRegions) {
		this.distributedRegions = distributedRegions;
	}

	private void setDistributedRegion(ObjectName distributedRegion, int index) {
		this.distributedRegions[index] = distributedRegion;
	}

	private ObjectName[] getDistributedLocks() {
		return distributedLocks;
	}

	private void setDistributedLocks(ObjectName[] distributedLocks) {
		this.distributedLocks = distributedLocks;
	}

	private void setDistributedLock(ObjectName distributedLock, int index) {
		this.distributedLocks[index] = distributedLock;
	}

	private Logger getApplicationLog() {
		return applicationLog;
	}

	private void setApplicationLog(Logger applicationLog) {
		this.applicationLog = applicationLog;
	}

	private Logger getExceptionLog() {
		return exceptionLog;
	}

	private void setExceptionLog(Logger exceptionLog) {
		this.exceptionLog = exceptionLog;
	}

	private ScheduledFuture<AgentMonitorTask> getAgentStartTimer() {
		return agentStartTimer;
	}

	private void setAgentStartTimer(ScheduledFuture<AgentMonitorTask> agentStartTimer) {
		this.agentStartTimer = agentStartTimer;
	}

	private MxBeans getMxBeans() {
		return mxBeans;
	}

	private void setMxBeans(MxBeans mxBeans) {
		this.mxBeans = mxBeans;
	}

	private GemFireThreads getGemfireThreads() {
		return gemfireThreads;
	}

	private void setGemfireThreads(GemFireThreads gemfireThreads) {
		this.gemfireThreads = gemfireThreads;
	}

	private void setReconnectRetryAttempts(int reconnectRetryAttempts) {
		this.reconnectRetryAttempts = reconnectRetryAttempts;
	}

	private void setMessageLifeDuration(long messageLifeDuration) {
		this.messageLifeDuration = messageLifeDuration;
	}

	private void setMessageDuplicateLimit(int messageDuplicateLimit) {
		this.messageDuplicateLimit = messageDuplicateLimit;
	}

	private JMXServiceURL getUrl() {
		return url;
	}

	private void setUrl(JMXServiceURL url) {
		this.url = url;
	}

	private int getJmxPort() {
		return jmxPort;
	}

	private void setJmxPort(int jmxPort) {
		this.jmxPort = jmxPort;
	}

	private JMXConnector getJmxConnection() {
		return jmxConnection;
	}

	private void setJmxConnection(JMXConnector jmxConnection) {
		this.jmxConnection = jmxConnection;
	}

	private MBeanServerConnection getMbs() {
		return mbs;
	}

	private void setMbs(MBeanServerConnection mbs) {
		this.mbs = mbs;
	}

	private ObjectName getSystemName() {
		return systemName;
	}

	private void setSystemName(ObjectName systemName) {
		this.systemName = systemName;
	}

	private NotificationListener getConnectionListener() {
		return connectionListener;
	}

	private void setConnectionListener(NotificationListener connectionListener) {
		this.connectionListener = connectionListener;
	}

	private NotificationListener getMbsListener() {
		return mbsListener;
	}

	private void setMbsListener(NotificationListener mbsListener) {
		this.mbsListener = mbsListener;
	}

	private void setCommandPort(int commandPort) {
		this.commandPort = commandPort;
	}

	private int getNextHostIndex() {
		return nextHostIndex;
	}

	private void setNextHostIndex(int nextHostIndex) {
		this.nextHostIndex = nextHostIndex;
	}

	private List<String> getJmxHosts() {
		return jmxHosts;
	}

	private void addJmxHost(String jmxHost) {
		this.jmxHosts.add(jmxHost);
	}

	private boolean getJmxConnectionActive() {
		return jmxConnectionActive.get();
	}

	private void setJmxConnectionActive(boolean jmxConnectionActive) {
		this.jmxConnectionActive.set(jmxConnectionActive);
	}

	private String getJmxHost() {
		return jmxHost;
	}

	private void setJmxHost(String jmxHost) {
		this.jmxHost = jmxHost;
	}

	private int getReconnectRetryCount() {
		return reconnectRetryCount;
	}

	private void setReconnectRetryCount(int reconnectRetryCount) {
		this.reconnectRetryCount = reconnectRetryCount;
	}

	private String getLastJmxHost() {
		return lastJmxHost;
	}

	private void setLastJmxHost(String lastJmxHost) {
		this.lastJmxHost = lastJmxHost;
	}

	private long getReconnectWaitTime() {
		return reconnectWaitTime;
	}

	private void setReconnectWaitTime(long reconnectWaitTime) {
		this.reconnectWaitTime = reconnectWaitTime;
	}

	private int getReconnectRetryAttempts() {
		return reconnectRetryAttempts;
	}

	public int getCommandPort() {
		return commandPort;
	}

	public boolean isShutdown() {
		return shutdown;
	}

	public void setShutdown(boolean stop) {
		shutdown = stop;
	}

	public void setExcludedMessages(ExcludedMessages messages) {
		excludedMessages = messages;
	}

	public Util getUtil() {
		return util;
	}

	public String[] getBlockers() {
		return blockers;
	}

	public void addBlocker(String blockerId) {
		List<String> lBlockers = null;
		if (blockers == null || blockers.length == 0) {
			lBlockers = new ArrayList<String>();
		} else {
			lBlockers = Arrays.asList(blockers);
		}
		lBlockers.add(blockerId);
		String[] aBlockers = new String[lBlockers.size()];
		lBlockers.toArray(aBlockers);
		blockers = aBlockers;
	}

	public void removeBlocker(String blockerId) {
		List<String> lBlockers = new ArrayList<String>();
		for (String blocker : blockers) {
			if (!blocker.equals(blockerId)) {
				lBlockers.add(blocker);
			}
		}
		String[] aBlockers = new String[lBlockers.size()];
		lBlockers.toArray(aBlockers);
		blockers = aBlockers;
	}

}
