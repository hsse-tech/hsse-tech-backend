package com.mipt.hsse.hssetechbackend.auxiliary.serializablebytesarray;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.util.Base64;
public class BytesSerializer extends StdSerializer<BytesArray> {
  private static final long serialVersionUID = -5510353102817291511L;

  public BytesSerializer() {
    super(BytesArray.class);
  }

  @Override
  public void serialize(BytesArray value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeString(Base64.getEncoder().encodeToString(value.bytes()));
  }
}
