package util.geode.monitor.xml;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "mxBean"
})
@XmlRootElement(name = "mxBeans")
public class MxBeans {

    @XmlElement(required = true)
    protected List<MxBeans.MxBean> mxBean;
    @XmlAttribute(name = "sampleTime")
    protected Integer sampleTime;

    public List<MxBeans.MxBean> getMxBean() {
        if (mxBean == null) {
            mxBean = new ArrayList<MxBeans.MxBean>();
        }
        return this.mxBean;
    }

    /**
     * Gets the value of the sampleTime property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getSampleTime() {
        return sampleTime;
    }

    /**
     * Sets the value of the sampleTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSampleTime(Integer value) {
        this.sampleTime = value;
    }


    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "fields"
    })
    public static class MxBean {

        @XmlElement(required = true)
        protected MxBeans.MxBean.Fields fields;
        @XmlAttribute(name = "mxBeanName")
        protected MxBeanType mxBeanName;

        /**
         * Gets the value of the fields property.
         * 
         * @return
         *     possible object is
         *     {@link MxBeans.MxBean.Fields }
         *     
         */
        public MxBeans.MxBean.Fields getFields() {
            return fields;
        }

        /**
         * Sets the value of the fields property.
         * 
         * @param value
         *     allowed object is
         *     {@link MxBeans.MxBean.Fields }
         *     
         */
        public void setFields(MxBeans.MxBean.Fields value) {
            this.fields = value;
        }

        /**
         * Gets the value of the mxBeanName property.
         * 
         * @return
         *     possible object is
         *     {@link MxBeanType }
         *     
         */
        public MxBeanType getMxBeanName() {
            return mxBeanName;
        }

        /**
         * Sets the value of the mxBeanName property.
         * 
         * @param value
         *     allowed object is
         *     {@link MxBeanType }
         *     
         */
        public void setMxBeanName(MxBeanType value) {
            this.mxBeanName = value;
        }


        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "field"
        })
        public static class Fields {

            @XmlElement(required = true)
            protected List<MxBeans.MxBean.Fields.Field> field;

            public List<MxBeans.MxBean.Fields.Field> getField() {
                if (field == null) {
                    field = new ArrayList<MxBeans.MxBean.Fields.Field>();
                }
                return this.field;
            }

            @XmlAccessorType(XmlAccessType.FIELD)
            @XmlType(name = "")
            public static class Field {

                @XmlAttribute(name = "beanProperty")
                protected String beanProperty;
                @XmlAttribute(name = "fieldName")
                protected String fieldName;
                @XmlAttribute(name = "fieldSize")
                protected FieldSizeType fieldSize;
                @XmlAttribute(name = "count")
                protected Integer count;
                @XmlAttribute(name = "percentage")
                protected BigDecimal percentage;
                @XmlAttribute(name = "percentageField")
                protected String percentageField;
                @XmlAttribute(name = "percentageFieldSize")
                protected FieldSizeType percentageFieldSize;

                /**
                 * Gets the value of the beanProperty property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getBeanProperty() {
                    return beanProperty;
                }

                /**
                 * Sets the value of the beanProperty property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setBeanProperty(String value) {
                    this.beanProperty = value;
                }

                /**
                 * Gets the value of the fieldName property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getFieldName() {
                    return fieldName;
                }

                /**
                 * Sets the value of the fieldName property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setFieldName(String value) {
                    this.fieldName = value;
                }

                /**
                 * Gets the value of the fieldSize property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link FieldSizeType }
                 *     
                 */
                public FieldSizeType getFieldSize() {
                    return fieldSize;
                }

                /**
                 * Sets the value of the fieldSize property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link FieldSizeType }
                 *     
                 */
                public void setFieldSize(FieldSizeType value) {
                    this.fieldSize = value;
                }

                /**
                 * Gets the value of the count property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link Integer }
                 *     
                 */
                public Integer getCount() {
                    return count;
                }

                /**
                 * Sets the value of the count property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link Integer }
                 *     
                 */
                public void setCount(Integer value) {
                    this.count = value;
                }

                /**
                 * Gets the value of the percentage property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link BigDecimal }
                 *     
                 */
                public BigDecimal getPercentage() {
                    return percentage;
                }

                /**
                 * Sets the value of the percentage property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link BigDecimal }
                 *     
                 */
                public void setPercentage(BigDecimal value) {
                    this.percentage = value;
                }

                /**
                 * Gets the value of the percentageField property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link String }
                 *     
                 */
                public String getPercentageField() {
                    return percentageField;
                }

                /**
                 * Sets the value of the percentageField property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link String }
                 *     
                 */
                public void setPercentageField(String value) {
                    this.percentageField = value;
                }

                /**
                 * Gets the value of the percentageFieldSize property.
                 * 
                 * @return
                 *     possible object is
                 *     {@link FieldSizeType }
                 *     
                 */
                public FieldSizeType getPercentageFieldSize() {
                    return percentageFieldSize;
                }

                /**
                 * Sets the value of the percentageFieldSize property.
                 * 
                 * @param value
                 *     allowed object is
                 *     {@link FieldSizeType }
                 *     
                 */
                public void setPercentageFieldSize(FieldSizeType value) {
                    this.percentageFieldSize = value;
                }
            }
        }
    }
}
