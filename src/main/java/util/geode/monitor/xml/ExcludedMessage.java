package util.geode.monitor.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "excludedMessage", propOrder = { "criteria" })
public class ExcludedMessage {

	@XmlElement(required = true)
	protected List<String> criteria;

	public ExcludedMessage() {
	}

	public ExcludedMessage(String[] excludes) {
		this.criteria = Arrays.asList(excludes);
	}

	public List<String> getCriteria() {
		if (criteria == null) {
			criteria = new ArrayList<String>();
		}
		return this.criteria;
	}

}
