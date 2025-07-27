package com.AutoGenClass.generator;

import java.lang.annotation.*;
import java.util.Map;

/**
 * this is used to auto generate simple classes like DTOs and trivial Classes
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE , ElementType.ANNOTATION_TYPE})
public @interface AutoGen {
    /**
     * fields names for fields do not require special serialization
     * @return
     */
    String[] simpleFields();

    /**
     * fields names for fields require serialization
     * @return
     */
    String[] serializedFields();

    /**
     * serializers for fields require serialization , must be in the same order
     * each serializer must be in same position of its corresponding field
     * @return
     */
    String[] serializers();

    /**
     * name of the output class we need to deal with
     * @return
     */
    String name();

    /**
     * module name where the DTO should be created
     * if not specified, will use the same module as the source class
     * @return
     */
    String module() default "";
}
