package be.lab;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.sql.SQLException;
import java.util.*;

@Path("/") @RequestScoped @Produces(MediaType.APPLICATION_JSON)
public class Api {
    @Inject MessageService service;
    @GET @Path("health") public Object health() throws SQLException {
        service.query("SELECT COUNT(*) AS n FROM messages"); return Map.of("status","UP","database","Oracle");
    }
    @POST @Path("messages") @Consumes(MediaType.APPLICATION_XML)
    public Response receive(String xml) throws SQLException {
        Map<String,Object> message;
        try { message=service.receive(xml); }
        catch(IllegalArgumentException e) { throw MessageService.error(400,"INVALID_XML"); }
        return Response.status("PROCESSED".equals(message.get("status"))?201:422).entity(message).build();
    }
    @GET @Path("messages") public Object messages() throws SQLException {
        return service.query("SELECT message_id,status,attempts,error_code,received_at FROM messages ORDER BY received_at DESC FETCH FIRST 100 ROWS ONLY");
    }
    @GET @Path("messages/{id}") public Object message(@PathParam("id") String id) throws SQLException { return service.get(id); }
    @POST @Path("messages/{id}/reprocess") public Response retry(@PathParam("id") String id) throws SQLException {
        var result=service.process(id);
        return Response.status("PROCESSED".equals(result.get("status"))?200:422).entity(result).build();
    }
    @GET @Path("reports/partners") public Object report() throws SQLException {
        return service.query("SELECT * FROM partner_report ORDER BY code");
    }
}
