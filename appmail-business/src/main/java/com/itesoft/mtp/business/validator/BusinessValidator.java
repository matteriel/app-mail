package com.itesoft.mtp.business.validator;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

public class BusinessValidator {

	private static BusinessValidator businessValidator = new BusinessValidator();
	
	protected Validator validator = null;
	
	public BusinessValidator() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	    validator = factory.getValidator();
	}
	
	public static BusinessValidator getInstance() {
		if(businessValidator == null)
			businessValidator = new BusinessValidator();
		
		return businessValidator;
	}
	
	public <T> void validate(T object) throws Exception {
		Set<ConstraintViolation<T>> violations = validator.validate(object);

        for (ConstraintViolation<T> violation : violations) {
            throw new Exception(violation.getMessage());
        }
	}
}
