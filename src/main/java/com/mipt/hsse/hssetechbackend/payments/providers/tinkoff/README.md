# Написание запросов к Tinkoff REST API

Чтобы отправлять запросы Tinkoff REST API необходимо использовать обертку `TinkoffApiClientBase`

## Базовые запросы

Чтобы создать сущность, которая будет являться телом запроса, необходимо ее класс унаследовать от [`TinkoffRequestBase`](entities/requests/TinkoffRequestBase.java).
Все свойства, которые должны использоваться в подписи, должны быть помечены `@TinkoffProperty` с обязательным параметром `name`.
Пример:

```java
import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.entities.requests.TinkoffRequestBase;
import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.signing.TinkoffProperty;

public class TinkoffRequestExample extends TinkoffRequestBase {
  public String exampleProperty() {
    return "Hello World";
  }
}
```

## Подпись запросов

Если Вам необходимо подписать [запрос](https://www.tinkoff.ru/kassa/dev/payments/#section/Podpis-zaprosa), тогда необходимо добавить аннотацию `@TinkoffSign`.
Все свойства, которые должны участвовать в подписи, должны быть помечены `@TinkoffProperty` с обязательным параметром `name`.

```java
import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.entities.requests.TinkoffRequestBase;
import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.signing.TinkoffProperty;
import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.signing.TinkoffSign;

@TinkoffSign
public class TinkoffRequestExample extends TinkoffRequestBase {
  @TinkoffProperty(name = "ExampleProperty")
  public String exampleProperty() {
    return "Hello World";
  }

  public String ignoreProperty() {
    return "Это свойство будет проигнорировано, т.к. нет аннотации.";
  }

  @TinkoffProperty(name = "PropertyWithParameters")
  public String ignorePropertyWithParameters(int a, int b) {
    return "Это свойство будет проигнорировано, потому что параметров не должно быть.";
  }

  @TinkoffProperty(name = "NotScalarObject")
  public SomeNestedObject ignorePropertyWithNestedObject() {
    return "Это свойство будет проигнорировано, потому что возвращаем тип - это скалярный объект.";
  }
}

public record SomeNestedObject(String a, Integer b, Byte c) {

}
```
