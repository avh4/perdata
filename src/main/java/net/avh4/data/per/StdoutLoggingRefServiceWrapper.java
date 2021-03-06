package net.avh4.data.per;

import net.avh4.data.per.service.RefService;
import net.avh4.data.per.service.TransactionException;

@SuppressWarnings("UnusedDeclaration")
public class StdoutLoggingRefServiceWrapper<T> implements RefService<T> {
    private final RefService<T> inner;

    public StdoutLoggingRefServiceWrapper(RefService<T> inner) {
        this.inner = inner;
    }

    @Override public String getContentKey(String refName) {
        return inner.getContentKey(refName);
    }

    @Override public T getContent(String contentKey) {
        return inner.getContent(contentKey);
    }

    @Override public String put(T object) {
        final String key = inner.put(object);
        log("Put for " + key + ": " + object.toString());
        return key;
    }

    @Override
    public void updateRef(String refName, String currentContentKey, String newContentKey) throws TransactionException {
        try {
            inner.updateRef(refName, currentContentKey, newContentKey);
            log("Set ref \"" + refName + "\": " + currentContentKey + " -> " + newContentKey);
        } catch (TransactionException e) {
            log("Set ref \"" + refName + "\": !! " + e.getLocalizedMessage());
            throw e;
        } catch (RuntimeException e) {
            log("Set ref \"" + refName + "\": !! " + e.getLocalizedMessage());
            throw e;
        }
    }

    private void log(String message) {
        System.out.println(message);
    }
}
