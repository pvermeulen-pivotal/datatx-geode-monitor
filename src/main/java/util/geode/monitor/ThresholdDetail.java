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

	protected long thresholdValue;
	protected long value;
	protected String field;
	protected BigDecimal percentage;
	protected String percentageField;
	protected long percentageValue;
	protected DetailType type;

	public ThresholdDetail(long value, long thresholdValue, String field) {
		this.value = value;
		this.thresholdValue = thresholdValue;
		this.field = field;
		this.type = DetailType.COUNT;
	}

	public ThresholdDetail(long value, long thresholdValue, String field,
			BigDecimal percentage, String percentageField, long percentageValue) {
		this.value = value;
		this.thresholdValue = thresholdValue;
		this.field = field;
		this.percentage = percentage;
		this.percentageField = percentageField;
		this.percentageValue = percentageValue;
		this.type = DetailType.PERCENT;
	}

	public long getThresholdValue() {
		return thresholdValue;
	}

	public long getValue() {
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

	public long getPercentageValue() {
		return percentageValue;
	}

	public DetailType getType() {
		return type;
	}
}
