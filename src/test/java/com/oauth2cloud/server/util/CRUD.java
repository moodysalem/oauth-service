package com.oauth2cloud.server.util;

import com.moodysalem.hibernate.model.BaseEntity;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CRUD<T extends BaseEntity> {
    private final WebTarget base;

    public CRUD(final WebTarget base) {
        this.base = base;
    }

    public Response list(final String token) {
        return list(token, null);
    }

    public Response list(final String token, final MultivaluedMap<String, String> params) {
        return auth(params(base, params).request(), token).get();
    }

    public List<T> listObjects(final String token) {
        return listObjects(token, null);
    }

    public List<T> listObjects(final String token, final MultivaluedMap<String, String> params) {
        return list(token, params).readEntity(new GenericType<List<T>>() {
        });
    }

    public Response get(final String token, final UUID id) {
        return auth(base.path(Objects.toString(id)).request(), token).get();
    }

    public T getObject(final String token, final UUID id) {
        return get(token, id).readEntity(new GenericType<T>() {
        });
    }

    public Response save(final String token, final T ent) {
        return save(token, Collections.singletonList(ent));
    }

    public T saveObject(final String token, final T ent) {
        return save(token, ent).readEntity(new GenericType<T>() {
        });
    }

    public Response save(final String token, final List<T> list) {
        return auth(base.request(), token).post(Entity.json(list));
    }

    public List<T> saveObject(final String token, final List<T> list) {
        return save(token, list).readEntity(new GenericType<List<T>>() {
        });
    }

    public Response delete(final String token, final UUID id) {
        return auth(base.path(Objects.toString(id)).request(), token).delete();
    }

    public Response delete(final String token, final MultivaluedMap<String, String> params) {
        return auth(params(base, params).request(), token).delete();
    }

    private Invocation.Builder auth(final Invocation.Builder builder, final String token) {
        return builder.header("Authorization", "Bearer " + token);
    }

    private WebTarget params(final WebTarget start, final MultivaluedMap<String, String> params) {
        if (params == null) {
            return start;
        }

        WebTarget result = start;
        for (final String key : params.keySet()) {
            result = result.queryParam(key, params.get(key));
        }

        return result;
    }

}
