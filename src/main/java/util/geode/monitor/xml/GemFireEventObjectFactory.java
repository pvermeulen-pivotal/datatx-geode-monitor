package util.geode.monitor.xml;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

@XmlRegistry
public class GemFireEventObjectFactory {
    private final QName _GemFireEvents_QNAME = new QName("", "gemfireEvents");
    private final QName _GemFireEvent_QNAME = new QName("", "gemfireEvent");

    public GemFireEventObjectFactory() {
    }

    @XmlElementDecl(namespace = "", name = "gemfireEvents")
    public JAXBElement<GemFireEvents> createGemfireMessages(GemFireEvents value) {
        return new JAXBElement<GemFireEvents>(_GemFireEvents_QNAME, GemFireEvents.class, null, value);
    }

    @XmlElementDecl(namespace = "", name = "gemfireEvent")
    public JAXBElement<GemFireEvent> createGemfireMessage(GemFireEvent value) {
        return new JAXBElement<GemFireEvent>(_GemFireEvent_QNAME, GemFireEvent.class, null, value);
    }

}
