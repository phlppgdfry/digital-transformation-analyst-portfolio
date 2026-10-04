package be.lab;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class OrderMessageTest {
    String valid="<Order><MessageId>test-1</MessageId><Partner>ACME</Partner><Sku>BOX</Sku><Quantity>3</Quantity></Order>";
    @Test void mapsOrder() { assertEquals(new OrderMessage("test-1","ACME","BOX",3),OrderMessage.parse(valid)); }
    @Test void rejectsInvalidQuantity() { for(String n:new String[]{"0","-1","abc","100000000"}) assertThrows(IllegalArgumentException.class,()->OrderMessage.parse(valid.replace(">3<",">"+n+"<"))); }
    @Test void rejectsDuplicateAndUnknownFields() { for(String field:new String[]{"<Sku>X</Sku>","<Other>X</Other>"}) assertThrows(IllegalArgumentException.class,()->OrderMessage.parse(valid.replace("</Order>",field+"</Order>"))); }
    @Test void blocksExternalEntities() { assertThrows(IllegalArgumentException.class,()->OrderMessage.parse("<!DOCTYPE Order [<!ENTITY x SYSTEM 'file:///etc/passwd'>]>"+valid.replace("BOX","&x;"))); }
    @Test void rejectsMalformedAndOversizedXml() { assertThrows(IllegalArgumentException.class,()->OrderMessage.parse("<Order>")); assertThrows(IllegalArgumentException.class,()->OrderMessage.parse("x".repeat(16385))); }
    @Test void rejectsMissingFieldsAndInvalidIds() { assertThrows(IllegalArgumentException.class,()->OrderMessage.parse(valid.replace("<Partner>ACME</Partner>",""))); assertThrows(IllegalArgumentException.class,()->OrderMessage.parse(valid.replace("test-1","bad/id"))); }
}
