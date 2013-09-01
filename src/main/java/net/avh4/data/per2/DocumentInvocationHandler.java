package net.avh4.data.per2;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

class DocumentInvocationHandler implements InvocationHandler {
    private final String entityId;
    private final DatumStore store;

    public static <T> T getDocument(DatumStore store, Class<T> documentClass, final String entityId) {
        //noinspection unchecked
        return (T) Proxy.newProxyInstance(documentClass.getClassLoader(), new Class[]{documentClass},
                new DocumentInvocationHandler(store, entityId));
    }

    public DocumentInvocationHandler(DatumStore store, String entityId) {
        this.entityId = entityId;
        this.store = store;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final Class<?> returnType = method.getReturnType();
        final String attribute_name = method.getName();
        final String storedValue = store.get(entityId, attribute_name);

        if (returnType.equals(String.class)) {
            return storedValue;
        } else if (returnType.isArray()) {
            return jsonToArray(returnType.getComponentType(), storedValue);
        } else  {
            return getDocument(store, returnType, storedValue);
        }
    }

    private Object jsonToArray(Class<?> itemClass, String json) throws JSONException {
        final JSONArray jsonArray = new JSONArray(json);
        final Object a = Array.newInstance(itemClass, jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            Array.set(a, i, jsonArray.get(i));
        }
        return a;
    }
}
