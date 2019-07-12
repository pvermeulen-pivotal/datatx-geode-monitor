package util.geode.monitor.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "gemfireThread"
})
@XmlRootElement(name = "gemfireThread")
public class GemFireThreads {

    protected List<GemFireThread> gemfireThread;

    public List<GemFireThread> getGemfireThreadList() {
        if (gemfireThread == null) {
            gemfireThread = new ArrayList<GemFireThread>();
        }
        return this.gemfireThread;
    }
}
