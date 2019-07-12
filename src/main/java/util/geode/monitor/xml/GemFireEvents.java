package util.geode.monitor.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "gemfireEvent"
})
@XmlRootElement(name = "gemfireEvent")
public class GemFireEvents {

    protected List<GemFireEvent> gemfireEvent;

    public List<GemFireEvent> getGemfireEventList() {
        if (gemfireEvent == null) {
            gemfireEvent = new ArrayList<GemFireEvent>();
        }
        return this.gemfireEvent;
    }
}
