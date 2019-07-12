package util.geode.monitor.xml;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

@XmlRegistry
public class ExcludedMessageObjectFactory {
    private final QName _Criteria_QNAME = new QName("", "criteria");
    private final QName _ExcludedMessages_QNAME = new QName("", "excludedMessages");
    private final QName _ExcludedMessage_QNAME = new QName("", "excludedMessage");

    public ExcludedMessageObjectFactory() {
    }

    @XmlElementDecl(namespace = "", name = "excludedMessage")
    public JAXBElement<ExcludedMessage> createExcludedMessage(ExcludedMessage value) {
        return new JAXBElement<ExcludedMessage>(_ExcludedMessage_QNAME, ExcludedMessage.class, null, value);
    }

    /**
     * Create an instance of {@link ExcludedMessages }
     * 
     */
    @XmlElementDecl(namespace = "", name = "excludedMessages")
    public JAXBElement<ExcludedMessages> createExcludedMessages(ExcludedMessages value) {
        return new JAXBElement<ExcludedMessages>(_ExcludedMessages_QNAME, ExcludedMessages.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "criteria")
    public JAXBElement<String> createCriteria(String value) {
        return new JAXBElement<String>(_Criteria_QNAME, String.class, null, value);
    }
}
