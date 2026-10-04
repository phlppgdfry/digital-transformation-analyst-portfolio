package be.lab;
import jakarta.ws.rs.ext.*;
import jakarta.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.*;
@Provider
public class DatabaseErrors implements ExceptionMapper<SQLException> {
    private static final Logger LOG=Logger.getLogger(DatabaseErrors.class.getName());
    public Response toResponse(SQLException error) {
        LOG.log(Level.SEVERE,"stage=DATABASE_FAILURE oracleCode="+error.getErrorCode(),error);
        return Response.status(503).header("Retry-After","5").entity(Map.of("error","DATABASE_UNAVAILABLE","action","Check message status before retrying the same MessageId")).build();
    }
}
