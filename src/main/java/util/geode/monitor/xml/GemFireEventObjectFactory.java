package util.geode.monitor.xml;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

@XmlRegistry
public class GemFireEventObjectFactory {
    private final QName _GemFireThreads_QNAME = new QName("", "gemfireThreads");
    private final QName _GemFireThread_QNAME = new QName("", "gemfireThread");

    public GemFireEventObjectFactory() {
    }

    @XmlElementDecl(namespace = "", name = "gemfireThreads")
    public JAXBElement<GemFireThreads> createGemfireMessages(GemFireThreads value) {
        return new JAXBElement<GemFireThreads>(_GemFireThreads_QNAME, GemFireThreads.class, null, value);
    }

    @XmlElementDecl(namespace = "", name = "gemfireThread")
    public JAXBElement<GemFireThread> createGemfireMessage(GemFireThread value) {
        return new JAXBElement<GemFireThread>(_GemFireThread_QNAME, GemFireThread.class, null, value);
    }

}
