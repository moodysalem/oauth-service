package com.oauth2cloud.server.applications.admin.resources;

import com.moodysalem.jaxrs.lib.exceptions.RequestProcessingException;
import com.oauth2cloud.server.applications.oauth.resources.BaseResource;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("support")
public class SupportResource extends BaseResource {

    public static class SupportRequest {
        private String email;
        private String name;
        private String issue;

        public String getIssue() {
            return issue;
        }

        public void setIssue(String issue) {
            this.issue = issue;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response sendMessage(SupportRequest ms) {
        if (ms.getEmail() == null || ms.getEmail().trim().isEmpty() ||
            ms.getName() == null || ms.getName().trim().isEmpty() ||
            ms.getIssue() == null || ms.getIssue().trim().isEmpty()) {
            throw new RequestProcessingException(Response.Status.BAD_REQUEST, "E-mail, name, and issue are required.");
        }

        sendEmail(ms.getEmail(), "moody.salem+oauth2cloud@gmail.com", "OAuth2Cloud Support Request", "Support.ftl", ms);

        return Response.noContent().build();
    }
}
