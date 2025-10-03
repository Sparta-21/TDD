package com.sparta.tdd.common.helper;

import com.sparta.tdd.domain.user.enums.UserAuthority;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = CustomWithMockUserSecurityContextFactory.class)
public @interface CustomWithMockUser {

    long userId() default 1L;

    String username() default "testUser";

    UserAuthority authority() default UserAuthority.CUSTOMER;
}