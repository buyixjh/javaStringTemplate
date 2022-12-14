package com.wurenzhi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PACKAGE})
@Retention(RetentionPolicy.SOURCE)
public @interface StrTemplate {

}