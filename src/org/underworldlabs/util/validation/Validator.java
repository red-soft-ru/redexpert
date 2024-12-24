package org.underworldlabs.util.validation;

/// @author Aleksey Kozlov
public interface Validator {

    boolean isValid(Object value);

    /**
     * Creates a validator instance based on the provided property key.
     *
     * @param key the property key used to determine the type of validator to create.
     * @return <code>Validator</code> instance corresponding to the given key.
     */
    static Validator of(String key) {
        return AbstractValidator.of(key);
    }

}
