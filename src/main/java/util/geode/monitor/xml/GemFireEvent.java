package util.geode.monitor.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
public class GemFireEvent {

    @XmlAttribute(name = "event")
    protected String event;

    public String getEvent() {
        return event;
    }

    public void setEvent(String value) {
        this.event = value;
    }

}
