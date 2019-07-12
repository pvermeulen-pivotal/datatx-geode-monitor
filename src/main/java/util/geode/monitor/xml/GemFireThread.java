package util.geode.monitor.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
public class GemFireThread {

    @XmlAttribute(name = "thread")
    protected String thread;

    public String getThread() {
        return thread;
    }

    public void setThread(String value) {
        this.thread = value;
    }

}
