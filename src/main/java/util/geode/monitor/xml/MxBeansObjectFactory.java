package util.geode.monitor.xml;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

@XmlRegistry
public class MxBeansObjectFactory {
	private final QName _MxBeans_QNAME = new QName("", "mxBeans");
	private final QName _MxBean_QNAME = new QName("", "mxBean");
	private final QName _Fields_QNAME = new QName("", "fields");
	private final QName _Field_QNAME = new QName("", "field");

	public MxBeansObjectFactory() {
	}

	@XmlElementDecl(namespace = "", name = "mxBeans")
	public JAXBElement<MxBeans> createMxBeans(MxBeans value) {
		return new JAXBElement<MxBeans>(_MxBeans_QNAME, MxBeans.class, null,
				value);
	}

	@XmlElementDecl(namespace = "", name = "mxBean")
	public JAXBElement<MxBeans.MxBean> createMxBeansMxBean(MxBeans.MxBean value) {
		return new JAXBElement<MxBeans.MxBean>(_MxBean_QNAME,
				MxBeans.MxBean.class, null, value);
	}

	@XmlElementDecl(namespace = "", name = "fields")
	public JAXBElement<MxBeans.MxBean.Fields> createMxBeansMxBeanFields(
			MxBeans.MxBean.Fields value) {
		return new JAXBElement<MxBeans.MxBean.Fields>(_Fields_QNAME,
				MxBeans.MxBean.Fields.class, null, value);
	}

	@XmlElementDecl(namespace = "", name = "field")
	public JAXBElement<MxBeans.MxBean.Fields.Field> createMxBeansMxBeanFieldsField(
			MxBeans.MxBean.Fields.Field value) {
		return new JAXBElement<MxBeans.MxBean.Fields.Field>(_Field_QNAME,
				MxBeans.MxBean.Fields.Field.class, null, value);
	}

}
