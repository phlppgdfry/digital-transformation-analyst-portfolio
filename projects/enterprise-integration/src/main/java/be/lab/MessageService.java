package be.lab;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.annotation.Resource;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

@ApplicationScoped
public class MessageService {
    @Resource(lookup="java:/jdbc/OracleDS") DataSource ds;
    private static final Logger LOG=Logger.getLogger(MessageService.class.getName());
    public static WebApplicationException error(int status,String code) {
        return new WebApplicationException(Response.status(status).entity(Map.of("error",code)).build());
    }
    public Map<String,Object> receive(String xml) throws SQLException {
        var order=OrderMessage.parse(xml);
        try(var c=ds.getConnection(); var s=c.prepareStatement("INSERT INTO messages(message_id,payload) VALUES (?,?)")) {
            s.setString(1,order.messageId()); s.setString(2,xml);
            try { s.executeUpdate(); }
            catch(SQLException e) { if(e.getErrorCode()==1) throw error(409,"DUPLICATE_MESSAGE_ID"); throw e; }
        }
        LOG.info("messageId="+order.messageId()+" stage=RECEIVED");
        return process(order.messageId());
    }
    public Map<String,Object> process(String id) throws SQLException {
        try(var c=ds.getConnection()) {
            c.setAutoCommit(false);
            try {
                String xml,status;
                try(var s=c.prepareStatement("SELECT payload,status FROM messages WHERE message_id=? FOR UPDATE")) {
                    s.setString(1,id);
                    try(var r=s.executeQuery()) {
                        if(!r.next()) throw error(404,"MESSAGE_NOT_FOUND");
                        xml=r.getString(1); status=r.getString(2);
                    }
                }
                if(status.equals("PROCESSED")) { c.commit(); return get(id); }
                var order=OrderMessage.parse(xml);
                boolean known;
                try(var s=c.prepareStatement("SELECT code FROM partners WHERE code=?")) {
                    s.setString(1,order.partner()); try(var r=s.executeQuery()) { known=r.next(); }
                }
                if(known) {
                    try(var s=c.prepareStatement("INSERT INTO orders(message_id,partner_code,sku,quantity) VALUES (?,?,?,?)")) {
                        s.setString(1,id); s.setString(2,order.partner()); s.setString(3,order.sku()); s.setInt(4,order.quantity()); s.executeUpdate();
                    }
                }
                status=known?"PROCESSED":"FAILED";
                try(var s=c.prepareStatement("UPDATE messages SET status=?,attempts=attempts+1,error_code=?,updated_at=CURRENT_TIMESTAMP WHERE message_id=?")) {
                    s.setString(1,status); s.setString(2,known?null:"UNKNOWN_PARTNER"); s.setString(3,id); s.executeUpdate();
                }
                c.commit();
                LOG.info("messageId="+id+" stage="+status);
            } catch(Exception e) { c.rollback(); throw e; }
        }
        return get(id);
    }
    public Map<String,Object> get(String id) throws SQLException {
        var rows=query("SELECT message_id,status,attempts,error_code,received_at,updated_at FROM messages WHERE message_id=?",id);
        if(rows.isEmpty()) throw error(404,"MESSAGE_NOT_FOUND");
        return rows.getFirst();
    }
    public List<Map<String,Object>> query(String sql,String... args) throws SQLException {
        try(var c=ds.getConnection();var s=c.prepareStatement(sql)) {
            for(int i=0;i<args.length;i++) s.setString(i+1,args[i]);
            try(var r=s.executeQuery()) {
                var rows=new ArrayList<Map<String,Object>>();
                while(r.next()) {
                    var row=new LinkedHashMap<String,Object>();
                    for(int i=1;i<=r.getMetaData().getColumnCount();i++) {
                        Object value=r.getObject(i);
                        row.put(r.getMetaData().getColumnLabel(i).toLowerCase(Locale.ROOT),value instanceof Timestamp?value.toString():value);
                    }
                    rows.add(row);
                }
                return rows;
            }
        }
    }
}
