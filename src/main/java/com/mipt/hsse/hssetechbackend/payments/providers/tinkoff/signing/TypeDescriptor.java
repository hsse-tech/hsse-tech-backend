package com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.signing;

import lombok.Getter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;
import java.util.function.Function;

public class TypeDescriptor {
  @Getter
  private final boolean needSign;
  private final ArrayList<PropertyInfo> properties;

  private static final String PASSWORD_FIELD_NAME = "Password";

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

  private record PropertyInfo(String propName, Function<Object, String> extractString) {
    public String getValue(Object object) {
      return extractString.apply(object);
    }

    public static PropertyInfo executable(Method method) {
      return new PropertyInfo(
              method.getAnnotation(TinkoffProperty.class).name(),
              (o) -> {
                try {
                  var invocationResult = method.invoke(o);

                  return invocationResult == null ? "" : invocationResult.toString();
                } catch (Exception e) {
                  throw new RuntimeException(e);
                }
              });
    }

    public static PropertyInfo constant(String name, String returnString) {
      return new PropertyInfo(name, (o) -> returnString);
    }
  }

  private TypeDescriptor(boolean needSign, ArrayList<PropertyInfo> properties) {
    this.needSign = needSign;
    this.properties = properties;
  }

  public String serialize(Object object) {
    StringBuilder builder = new StringBuilder();

    for (PropertyInfo property : properties) {
      builder.append(property.getValue(object));
    }

    return builder.toString();
  }

  public static <T> TypeDescriptor create(Class<T> type, String password) {
    var props = new ArrayList<PropertyInfo>();
    var needSign = type.isAnnotationPresent(TinkoffSign.class);

    for (Class<?> contextClass = type; contextClass != null; contextClass = contextClass.getSuperclass()) {
      for (Method method : contextClass.getDeclaredMethods()) {
        if (isGetter(method) && trustedClasses.contains(method.getReturnType())) {
          props.add(PropertyInfo.executable(method));
        }
      }
    }

    props.add(PropertyInfo.constant(PASSWORD_FIELD_NAME, password));
    props.sort(Comparator.comparing(PropertyInfo::propName));

    return new TypeDescriptor(needSign, props);
  }

  private static boolean isGetter(Method method) {
    return method.isAnnotationPresent(TinkoffProperty.class) &&
            !method.getReturnType().equals(Void.TYPE) &&
            method.getParameterCount() == 0;
  }
}
