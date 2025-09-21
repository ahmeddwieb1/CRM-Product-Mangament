package org.elmorshedy.security;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Parameter(
    description = "MongoDB ObjectId (24 hex string)",
    schema = @Schema(type = "string", example = "689ccdf16d6fb66fb8b77d2e")
)
public @interface ObjectIdParam {
}
