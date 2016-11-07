package com.oauth2cloud.server.rest.endpoints.api;


import com.oauth2cloud.server.OAuth2Test;
import com.oauth2cloud.server.model.api.TokenResponse;
import com.oauth2cloud.server.model.api.UserInfo;
import com.oauth2cloud.server.model.db.*;
import com.oauth2cloud.server.util.Crud;
import com.oauth2cloud.server.util.TokenUtil;
import org.testng.annotations.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserLinkingTest extends OAuth2Test {

    private Response createGroup(final String token, final UUID applicationId) {
        return target("users").path("create-group")
                .request()
                .header("Authorization", "Bearer " + token)
                .post(Entity.json(applicationId));
    }

    @Test
    public void testUserLinking() {
        final String token = getToken().getAccessToken();
        final Crud<Application> ac = applicationCrud(token);
        final Crud<User> uc = userCrud(token);
        final Crud<Client> cc = clientCrud(token);

        Application a = new Application();
        a.setName(UUID.randomUUID().toString());
        a.setSupportEmail("moody.salem@gmail.com");
        a = ac.save(a);

        assert createGroup("123", a.getId()).getStatus() == 401;
        assert createGroup(token, UUID.randomUUID()).getStatus() == 400;
        assert createGroup(token, null).getStatus() == 400;
        final UserGroup userGroup = createGroup(token, a.getId()).readEntity(UserGroup.class);

        User one = new User();
        one.setEmail("moody.salem@gmail.com");
        one.setApplication(a);
        one = uc.save(one);
        assert one.getId() != null;

        User two = new User();
        two.setEmail("moody.salem+yahoo@gmail.com");
        two.setApplication(a);
        two = uc.save(two);
        assert two.getId() != null;

        two.setGroup(userGroup);
        two = uc.save(two);
        assert two.getGroup().getId() != null;

        one.setGroup(userGroup);
        one = uc.save(one);
        assert one.getGroup().idMatch(two.getGroup());
        assert one.getGroup().getUsers().size() == 2;

        {
            Application b = new Application();
            b.setName(UUID.randomUUID().toString());
            b.setSupportEmail("other@gmail.com");
            b = ac.save(b);

            User wrongApplication = new User();
            wrongApplication.setApplication(b);
            wrongApplication.setEmail("moody.salem@gmail.com");
            wrongApplication = uc.save(wrongApplication);
            assert wrongApplication.getId() != null;

            wrongApplication.setGroup(one.getGroup());
            assert uc.saveResponse(wrongApplication).getStatus() == 403;
        }

        User three = new User();
        three.setEmail("moody.salem@gmail.com");
        three.setApplication(a);
        assert uc.saveResponse(three).getStatus() == 409;
        three.setEmail("moody+test@gmail.com");
        three = uc.save(three);
        assert three.getGroup() == null;
        three.setGroup(new UserGroup());
        three.getGroup().setId(one.getGroup().getId());
        three = uc.save(three);
        assert three.getGroup() != null && three.getGroup().getUsers().size() == 3;


        Client atc = new Client();
        atc.setConfidential(false);
        atc.setUris(Collections.singleton("http://localhost:8080"));
        atc.setLoginCodeTtl(3600);
        atc.setName(UUID.randomUUID().toString());
        atc.setTokenTtl(3600);
        atc.setFlows(Collections.singleton(GrantFlow.IMPLICIT));
        atc.setApplication(a);
        atc = cc.save(atc);
        assert atc.getId() != null;


        verifyLinked(atc, one, two, three);

        // break the link
        two.setGroup(null);
        two = uc.save(two);
        assert two.getGroup() == null;

        verifyLinked(atc, one, three);
        verifyLinked(atc, two);
    }

    private void verifyLinked(final Client client, final User... users) {
        for (final User u : users) {
            final TokenResponse tr = getToken(u.getEmail(), client.getCredentials().getId(), client.getUris().iterator().next());
            assert tr != null;
            final TokenResponse ti = TokenUtil.tokenInfo(target(), tr.getAccessToken(), client.getApplication().getId());
            assert ti != null;

            assert ti.getUser().getEmail().equals(u.getEmail());
            assert ti.getUser().getUserId().equals(u.getId());
            {
                final Set<UserInfo> others = Stream.of(users)
                        .filter(other -> !u.idMatch(other)).map(UserInfo::from).collect(Collectors.toSet());
                assert ti.getUser().getLinkedUsers().size() == others.size();
                assert ti.getUser().getLinkedUsers().containsAll(others);
            }
        }
    }

}
