package com.oauth2cloud.server.applications.admin.resources;

import com.oauth2cloud.server.hibernate.model.Scope;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

public class ScopeResource extends BaseEntityResource<Scope> {
    @Override
    public Class<Scope> getEntityClass() {
        return Scope.class;
    }

    @Override
    public boolean canCreate(Scope scope) {
        return false;
    }

    @Override
    public boolean canEdit(Scope scope) {
        return false;
    }

    @Override
    public boolean canDelete(Scope scope) {
        return false;
    }

    @Override
    protected void validateEntity(List<String> list, Scope scope) {

    }

    @Override
    public void beforeCreate(Scope scope) {

    }

    @Override
    public void beforeEdit(Scope scope, Scope t1) {

    }

    @Override
    protected void getPredicatesFromRequest(List<Predicate> list, Root<Scope> root) {
        list.add(cb.equal(root.join("application").get("owner"), getUser()));
    }

    @Override
    public void afterCreate(Scope scope) {

    }

    @Override
    public void beforeSend(Scope scope) {

    }
}
