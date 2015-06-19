package com.springframework.beans.factory.config;

import com.springframework.beans.factory.ObjectFactory;

/**
 * Created by hadoop on 2015/5/6 0006.
 */
public interface Scope {
    /**
     * Return the object with the given name from the underlying scope,
     * {@link com.springframework.beans.factory.ObjectFactory#getObject() creating it}
     * if not found in the underlying storage mechanism.
     * <p>This is the central operation of a Scope, and the only operation
     * that is absolutely required.
     */
    Object get(String name, ObjectFactory<?> objectFactory);

    /**
     * Remove the object with the given {@code name} from the underlying scope.
     * <p>Returns {@code null} if no object was found; otherwise
     * returns the removed {@code Object}.
     * <p>Note that an implementation should also remove a registered destruction
     * callback for the specified object, if any. It does, however, <i>not</i>
     * need to <i>execute</i> a registered destruction callback in this case,
     * since the object will be destroyed by the caller (if appropriate).
     * <p><b>Note: This is an optional operation.</b> Implementations may throw
     */
    Object remove(String name);

    /**
     * Register a callback to be executed on destruction of the specified
     * object in the scope (or at destruction of the entire scope, if the
     * scope does not destroy individual objects but rather only terminates
     * in its entirety).
     * <p><b>Note: This is an optional operation.</b> This method will only
     * be called for scoped beans with actual destruction configuration
     * (DisposableBean, destroy-method, DestructionAwareBeanPostProcessor).
     * Implementations should do their best to execute a given callback
     * at the appropriate time. If such a callback is not supported by the
     * underlying runtime environment at all, the callback <i>must be
     * ignored and a corresponding warning should be logged</i>.
     * <p>Note that 'destruction' refers to to automatic destruction of
     * the object as part of the scope's own lifecycle, not to the individual
     * scoped object having been explicitly removed by the application.
     * If a scoped object gets removed via this facade's {@link #remove(String)}
     * method, any registered destruction callback should be removed as well,
     * assuming that the removed object will be reused or manually destroyed.
     * @param name the name of the object to execute the destruction callback for
     * @param callback the destruction callback to be executed.
     * Note that the passed-in Runnable will never throw an exception,
     * so it can safely be executed without an enclosing try-catch block.
     * Furthermore, the Runnable will usually be serializable, provided
     * that its target object is serializable as well.
     */
    void registerDestructionCallback(String name, Runnable callback);

    /**
     * Resolve the contextual object for the given key, if any.
     * E.g. the HttpServletRequest object for key "request".
     * @param key the contextual key
     * @return the corresponding object, or {@code null} if none found
     */
    Object resolveContextualObject(String key);

    /**
     * Return the <em>conversation ID</em> for the current underlying scope, if any.
     * <p>The exact meaning of the conversation ID depends on the underlying
     * storage mechanism. In the case of session-scoped objects, the
     * conversation ID would typically be equal to (or derived from) the
     * {@link javax.servlet.http.HttpSession#getId() session ID}; in the
     * case of a custom conversation that sits within the overall session,
     * the specific ID for the current conversation would be appropriate.
     * <p><b>Note: This is an optional operation.</b> It is perfectly valid to
     * return {@code null} in an implementation of this method if the
     * underlying storage mechanism has no obvious candidate for such an ID.
     * @return the conversation ID, or {@code null} if there is no
     * conversation ID for the current scope
     */
    String getConversationId();
}
