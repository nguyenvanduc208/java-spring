package com.github.jacekszymanski.realcamel;

import com.github.jacekszymanski.realcamel.model.CreateUserRequest;
import com.github.jacekszymanski.realcamel.testutil.UriUtil;
import com.github.jacekszymanski.realcamel.testutil.UserUtil;
import org.apache.camel.Exchange;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.SQLException;

@CamelSpringBootTest
@SpringBootTest(classes = CamelApplication.class)
public class SaveUserTest extends TestsBase {

  public static final String ENTRY_ENDPOINT = "direct:saveUser";
  public static final String JPA_ENDPOINT = "jpa:com.github.jacekszymanski.realcamel.entity.User";

  @Test
  public void testNewUsernameTaken() throws Exception {
    final CreateUserRequest req = UserUtil.defaultUserRequest();

    final Exception nonUniqueException =
        new ConstraintViolationException("unique index violated", new SQLException(), "unique");

    AdviceWith.adviceWith(camelContext, UriUtil.fromEndpointToRouteId(ENTRY_ENDPOINT),
        a -> {
          a.weaveByToUri(JPA_ENDPOINT).replace().throwException(nonUniqueException);
        });

    final Exchange resultExchange = producerTemplate.send(ENTRY_ENDPOINT,
        exchange -> exchange.getIn().setBody(req));

    Assertions.assertEquals(422, resultExchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE));

  }

}
