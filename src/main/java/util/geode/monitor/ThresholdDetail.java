package util.geode.monitor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author PaulVermeulen
 *
 */
public class ThresholdDetail {

	public enum DetailType {
		COUNT, PERCENT
	};

	protected String beanName;
	protected String beanProperty;
	protected String field;
	protected String percentageField;
	protected int thresholdCount = 1;
	protected long thresholdTm = 0;
	protected double thresholdValue;
	protected double value;
	protected double percentageValue;
	protected boolean sendAlert = false;
	protected BigDecimal percentage;
	protected DetailType type;

	public ThresholdDetail(double value, double thresholdValue, String field) {
		this.value = value;
		this.thresholdValue = thresholdValue;
		this.field = field;
		this.type = DetailType.COUNT;
		this.thresholdTm = new Date().getTime();
	}

	public ThresholdDetail(String beanName, String beanProperty, double value, double thresholdValue, String field) {
		this.beanName = beanName;
		this.beanProperty = beanProperty;
		this.value = value;
		this.thresholdValue = thresholdValue;
		this.field = field;
		this.type = DetailType.COUNT;
		this.thresholdTm = new Date().getTime();
	}

	public ThresholdDetail(double value, double thresholdValue, String field, BigDecimal percentage,
			String percentageField, double percentageValue) {
		this.value = value;
		this.thresholdValue = thresholdValue;
		this.field = field;
		this.percentage = percentage;
		this.percentageField = percentageField;
		this.percentageValue = percentageValue;
		this.type = DetailType.PERCENT;
		this.thresholdTm = new Date().getTime();
	}

	public ThresholdDetail(String beanName, String beanProperty, double value, double thresholdValue, String field,
			BigDecimal percentage, String percentageField, double percentageValue) {
		this.beanName = beanName;
		this.beanProperty = beanProperty;
		this.value = value;
		this.thresholdValue = thresholdValue;
		this.field = field;
		this.percentage = percentage;
		this.percentageField = percentageField;
		this.percentageValue = percentageValue;
		this.type = DetailType.PERCENT;
		this.thresholdTm = new Date().getTime();
	}

	public double getThresholdValue() {
		return thresholdValue;
	}

	public double getValue() {
		return value;
	}

	public String getField() {
		return field;
	}

	public BigDecimal getPercentage() {
		return percentage;
	}

	public String getPercentageField() {
		return percentageField;
	}

	public double getPercentageValue() {
		return percentageValue;
	}

	public DetailType getType() {
		return type;
	}

	public String getBeanName() {
		return beanName;
	}

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	public String getBeanProperty() {
		return beanProperty;
	}

	public void setBeanProperty(String beanProperty) {
		this.beanProperty = beanProperty;
	}

	public int getThresholdCount() {
		return thresholdCount;
	}

	public void setThresholdCount(int thresholdCount) {
		this.thresholdCount = thresholdCount;
	}

	public long getThresholdTm() {
		return thresholdTm;
	}

	public boolean isSendAlert() {
		return sendAlert;
	}

	public void setSendAlert(boolean alertSent) {
		this.sendAlert = alertSent;
	}
}
