package com.mipt.hsse.hssetechbackend.auxiliary.serializablebytesarray;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.util.Base64;
import java.io.IOException;

public class BytesDeserializer extends StdDeserializer<BytesArray> {

  private static final long serialVersionUID = 1514703510863497028L;

  public BytesDeserializer() {
    super(BytesArray.class);
  }

  @Override
  public BytesArray deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    JsonNode node = p.getCodec().readTree(p);
    String base64 = node.asText();
    return new BytesArray(Base64.getDecoder().decode(base64));
  }
}
