package util.geode.monitor.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "excludedMessage"
})
@XmlRootElement(name = "excludedMessages")
public class ExcludedMessages {

    @XmlElement(required = true)
    protected List<ExcludedMessage> excludedMessage;

    public List<ExcludedMessage> getExcludedMessageList() {
        if (excludedMessage == null) {
            excludedMessage = new ArrayList<ExcludedMessage>();
        }
        return this.excludedMessage;
    }

}
