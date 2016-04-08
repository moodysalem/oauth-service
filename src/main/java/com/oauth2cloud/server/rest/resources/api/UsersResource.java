package com.oauth2cloud.server.rest.resources.api;

import com.moodysalem.jaxrs.lib.exceptions.RequestProcessingException;
import com.oauth2cloud.server.hibernate.model.Application;
import com.oauth2cloud.server.hibernate.model.Application_;
import com.oauth2cloud.server.hibernate.model.User;
import com.oauth2cloud.server.hibernate.model.User_;
import com.oauth2cloud.server.rest.OAuth2Application;
import com.oauth2cloud.server.rest.filter.AuthorizationHeaderTokenFeature;
import org.mindrot.jbcrypt.BCrypt;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

@AuthorizationHeaderTokenFeature.ReadToken
@Path(OAuth2Application.API + "/users")
public class UsersResource extends BaseEntityResource<User> {
    public static final String MANAGE_USERS = "manage_users";

    @Override
    public Class<User> getEntityClass() {
        return User.class;
    }

    @Override
    public boolean canCreate(User user) {
        mustBeLoggedIn();
        checkScope(MANAGE_USERS);

        if (user.getApplication() == null) {
            return false;
        }

        com.oauth2cloud.server.hibernate.model.Application ap = em.find(com.oauth2cloud.server.hibernate.model.Application.class, user.getApplication().getId());
        return ap != null && ap.isActive() && ap.getOwner().idMatch(getUser());
    }

    @Override
    public boolean canEdit(User user) {
        mustBeLoggedIn();
        checkScope(MANAGE_USERS);
        return user.getApplication().getOwner().idMatch(getUser());
    }

    @Override
    public boolean canDelete(User user) {
        return false;
    }

    @Override
    protected void validateEntity(List<String> list, User user) {
    }

    @Override
    public void beforeCreate(User user) {
        if (getUser(user.getEmail(), user.getApplication()) != null) {
            throw new RequestProcessingException(Response.Status.CONFLICT, "E-mail address is already in use.");
        }
        if (user.getNewPassword() == null || user.getNewPassword().trim().length() == 0) {
            throw new RequestProcessingException(Response.Status.BAD_REQUEST, "User password is required.");
        }
        user.setPassword(BCrypt.hashpw(user.getNewPassword(), BCrypt.gensalt()));
    }

    /**
     * Get the user corresponding to an e-mail and application
     *
     * @param email       email of user
     * @param application application of user
     * @return User if exists
     */
    private User getUser(String email, Application application) {
        CriteriaQuery<User> users = cb.createQuery(User.class);
        Root<User> u = users.from(User.class);
        List<User> list = em.createQuery(users.select(u).where(
                cb.equal(u.get(User_.email), email),
                cb.equal(u.get(User_.application), application)
        )).getResultList();
        return list.size() == 1 ? list.get(0) : null;
    }

    @Override
    public void beforeEdit(User user, User t1) {
        if (t1.getNewPassword() != null && t1.getNewPassword().trim().length() > 0) {
            t1.setPassword(BCrypt.hashpw(t1.getNewPassword(), BCrypt.gensalt()));
        }
    }

    @QueryParam("search")
    String search;

    @QueryParam("applicationId")
    UUID applicationId;

    @QueryParam("active")
    Boolean active;

    @Override
    protected void getPredicatesFromRequest(List<Predicate> list, Root<User> root) {
        mustBeLoggedIn();
        checkScope(MANAGE_USERS);

        list.add(cb.equal(root.join(User_.application).get(Application_.owner), getUser()));

        if (applicationId != null) {
            list.add(cb.equal(root.join(User_.application).get(Application_.id), applicationId));
        }

        if (search != null) {
            String toSearch = "%" + search.trim() + "%";
            list.add(cb.or(
                    cb.like(root.get(User_.firstName), toSearch),
                    cb.like(root.get(User_.lastName), toSearch),
                    cb.like(root.get(User_.email), toSearch)
            ));
        }

        if (active != null) {
            list.add(cb.equal(root.get(User_.active), true));
        }
    }

    @Override
    public void afterCreate(User user) {

    }

    @Override
    public void beforeSend(User user) {

    }
}
