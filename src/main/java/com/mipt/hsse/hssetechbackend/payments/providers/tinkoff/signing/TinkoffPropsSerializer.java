package com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.signing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;

@Component
public class TinkoffPropsSerializer {
  private static final Set<Class<?>> trustedClasses = Set.of(
    String.class,
    Integer.class, int.class,
    Float.class, float.class,
    Double.class, double.class,
    Boolean.class, boolean.class,
    Byte.class, byte.class,
    Character.class, char.class,
    Short.class, short.class,
    Long.class, long.class);

  private static final String PASSWORD_FIELD_NAME = "Password";

  private final String password;

  private record PropData(String propName, String value) {
    public static PropData fromMethod(Method method, Object target) {
      try {
        var invocationResult = method.invoke(target);
        return new PropData(method.getName().substring(3), invocationResult == null ? "" : invocationResult.toString());
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  public TinkoffPropsSerializer(
          @Value("#{environment.getProperty('TINKOFF_PASSWORD')}") String password) {
    this.password = password;
  }

  public String serialize(Object object) {
    var getters = new ArrayList<PropData>();

    for (var contextClass = object.getClass(); contextClass != null; contextClass = contextClass.getSuperclass()) {
      for (Method method : contextClass.getDeclaredMethods()) {
        if (isGetter(method) && trustedClasses.contains(method.getReturnType())) {
          getters.add(PropData.fromMethod(method, object));
        }
      }
    }

    getters.add(new PropData(PASSWORD_FIELD_NAME, password));
    getters.sort(Comparator.comparing(PropData::propName));

    var builder = new StringBuilder();

    for (var getter : getters) {
      builder.append(getter.value);
    }

    return builder.toString();
  }

  private static boolean isGetter(Method method) {
    return method.getName().startsWith("get");
  }
}
