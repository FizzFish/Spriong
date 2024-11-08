package org.example.resources;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


@Path("/api")
public class RestService {

    @POST
    @Path("/authenticate")
    @Produces(MediaType.APPLICATION_JSON)
    public Response authenticate(@FormParam("username") String username, @FormParam("password") String password) {
        if ("admin".equals(username) && "admin".equals(password)) {
            return Response.ok("{\"token\":\"valid-token\"}").build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).entity("{\"message\":\"Invalid credentials\"}").build();
    }

    @POST
    @Path("/updatePassword")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updatePassword(@FormParam("newPassword") String newPassword) {
        // 逻辑：更新密码（此处简化）
        return Response.ok("{\"message\":\"Password updated successfully\"}").build();
    }

    @GET
    @Path("/view")
    @Produces(MediaType.APPLICATION_JSON)
    public Response view() {
        // 逻辑：查看用户信息（此处简化）
        return Response.ok("{\"message\":\"This is a protected view\"}").build();
    }

    @POST
    @Path("upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(@FormDataParam("file") InputStream fis,
                               @FormDataParam("file") FormDataContentDisposition header) {
        // 逻辑：上传文件（此处简化）
        String filePath = "/path/to/upload/" + header.getFileName();
        try {
            FileOutputStream fos = new FileOutputStream(filePath);
            byte[] bytes = new byte[1024];
            while (fis.read(bytes) != -1) {
                fos.write(bytes);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Response.ok("{\"message\":\"File uploaded successfully\"}").build();
    }

}
