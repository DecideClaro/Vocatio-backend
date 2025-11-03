package com.acme.vocatio.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/** Verifica que el grado exista en el catálogo peruano. */
@Documented
@Constraint(validatedBy = ValidAcademicGradeValidator.class)
@Target({FIELD, PARAMETER, ANNOTATION_TYPE})
@Retention(RUNTIME)
public @interface ValidAcademicGrade {

    String message() default "El grado academico no es valido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
