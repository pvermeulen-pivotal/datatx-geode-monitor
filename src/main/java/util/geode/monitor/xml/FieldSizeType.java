//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.03.10 at 10:15:23 AM EDT 
//


package util.geode.monitor.xml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for fieldSizeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="fieldSizeType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="ACTUAL"/>
 *     &lt;enumeration value="KILOBYTES"/>
 *     &lt;enumeration value="MEGABYTES"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "fieldSizeType")
@XmlEnum
public enum FieldSizeType {

    ACTUAL,
    KILOBYTES,
    MEGABYTES;

    public String value() {
        return name();
    }

    public static FieldSizeType fromValue(String v) {
        return valueOf(v);
    }

}
