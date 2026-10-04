package be.lab;
import java.io.StringReader;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;
import org.w3c.dom.Element;

public record OrderMessage(String messageId, String partner, String sku, int quantity) {
    public static OrderMessage parse(String xml) {
        if (xml == null || xml.length() > 16384) throw new IllegalArgumentException("INVALID_XML");
        try {
            var factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            var builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new DefaultHandler());
            var root = builder.parse(new InputSource(new StringReader(xml))).getDocumentElement();
            if (!root.getTagName().equals("Order") || root.getAttributes().getLength()!=0) throw new Exception();
            var fields = new java.util.HashMap<String,String>();
            for (var node=root.getFirstChild();node!=null;node=node.getNextSibling()) {
                if (node instanceof Element e) {
                    if (e.getAttributes().getLength()!=0 || e.getElementsByTagName("*").getLength()!=0 || fields.put(e.getTagName(),e.getTextContent().trim())!=null) throw new Exception();
                } else if (!node.getTextContent().isBlank()) throw new Exception();
            }
            if (!fields.keySet().equals(java.util.Set.of("MessageId","Partner","Sku","Quantity"))) throw new Exception();
            String id=fields.get("MessageId"), partner=fields.get("Partner"), sku=fields.get("Sku");
            if (!id.matches("[A-Za-z0-9_-]{1,64}") || !partner.matches("[A-Z0-9_-]{1,40}") || !sku.matches("[A-Za-z0-9_-]{1,40}")) throw new Exception();
            int quantity=Integer.parseInt(fields.get("Quantity"));
            if (quantity<1 || quantity>99999999) throw new Exception();
            return new OrderMessage(id,partner,sku,quantity);
        } catch (Exception e) { throw new IllegalArgumentException("INVALID_XML"); }
    }
}
