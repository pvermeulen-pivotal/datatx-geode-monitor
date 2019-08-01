package util.geode.monitor;

import java.math.BigDecimal;

/**
 * @author PaulVermeulen
 *
 */
public class ThresholdDetail {

	public enum DetailType {
		COUNT, PERCENT
	};

	protected double thresholdValue;
	protected double value;
	protected String field;
	protected BigDecimal percentage;
	protected String percentageField;
	protected double percentageValue;
	protected DetailType type;

	public ThresholdDetail(double value, double thresholdValue, String field) {
		this.value = value;
		this.thresholdValue = thresholdValue;
		this.field = field;
		this.type = DetailType.COUNT;
	}

	public ThresholdDetail(double value, double thresholdValue, String field,
			BigDecimal percentage, String percentageField, double percentageValue) {
		this.value = value;
		this.thresholdValue = thresholdValue;
		this.field = field;
		this.percentage = percentage;
		this.percentageField = percentageField;
		this.percentageValue = percentageValue;
		this.type = DetailType.PERCENT;
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
}
