package com.oauth2cloud.server.util;

import com.moodysalem.hibernate.model.BaseEntity;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Crud<T extends BaseEntity> {
    public static class Param {
        private final String name;
        private final Object[] values;

        public Param(String name, Object... values) {
            this.name = name;
            this.values = values;
        }

        public String getName() {
            return name;
        }

        public Object[] getValues() {
            return values;
        }
    }

    private final WebTarget base;
    private final String token;
    private final Class<T> clazz;

    public Crud(final Class<T> clazz, final WebTarget base, String token) {
        this.clazz = clazz;
        this.base = base;
        this.token = token;
    }

    public Response listResponse() {
        return listResponse(new Param[0]);
    }

    public Response listResponse(Param... params) {
        return auth(params(base, params).request(), token).get();
    }

    public List<T> list() {
        return list(new Param[0]);
    }

    public List<T> list(Param... params) {
        return listResponse(params).readEntity(listType());
    }


    public Response getResponse(final UUID id) {
        return auth(base.path(Objects.toString(id)).request(), token).get();
    }

    public T get(final UUID id) {
        return getResponse(id).readEntity(singleType());
    }

    public Response saveResponse(final T ent) {
        return saveResponse(Collections.singletonList(ent));
    }

    public T save(final T ent) {
        return save(Collections.singletonList(ent)).get(0);
    }

    public Response saveResponse(final List<T> list) {
        return auth(base.request(), token).post(Entity.json(list));
    }

    public List<T> save(final List<T> list) {
        return saveResponse(list).readEntity(listType());
    }

    public Response delete(final T ent) {
        return auth(base.path(Objects.toString(ent.getId())).request(), token).delete();
    }

    public Response delete(final Param... params) {
        return auth(params(base, params).request(), token).delete();
    }

    private Invocation.Builder auth(final Invocation.Builder builder, final String token) {
        return builder.header("Authorization", "Bearer " + token);
    }

    private WebTarget params(final WebTarget start, final Param... params) {
        if (params == null) {
            return start;
        }

        WebTarget result = start;
        for (final Param p : params) {
            result = result.queryParam(p.name, p.values);
        }

        return result;
    }

    private <T> GenericType<T> singleType() {
        return new GenericType<>(clazz);
    }

    private <T> GenericType<List<T>> listType() {
        return new GenericType<>(new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[]{clazz};
            }

            @Override
            public Type getRawType() {
                return List.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        });
    }

}
