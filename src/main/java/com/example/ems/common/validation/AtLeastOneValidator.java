package com.example.ems.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

public class AtLeastOneValidator implements ConstraintValidator<AtLeastOne, Object> {

    private String[] fields;

    @Override
    public void initialize(AtLeastOne constraintAnnotation) {
        this.fields = constraintAnnotation.fields();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        if (fields == null || fields.length == 0) {
            return true;
        }

        BeanWrapperImpl wrapper = new BeanWrapperImpl(value);
        for (String field : fields) {
            try {
                Object propertyValue = wrapper.getPropertyValue(field);
                if (propertyValue != null) {
                    if (propertyValue instanceof String str) {
                        if (!str.trim().isEmpty()) {
                            return true;
                        }
                    } else {
                        return true;
                    }
                }
            } catch (Exception ignored) {
                // Ignore property read errors and check next field
            }
        }
        return false;
    }
}
