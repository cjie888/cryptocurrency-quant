package com.cjie.cryptocurrency.quant.api.huobi.domain;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class HuobiOrderBookEntryDeserializer extends JsonDeserializer<HuobiOrderBookEntry> {

    @Override
    public HuobiOrderBookEntry deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {
        ObjectCodec oc = jp.getCodec();
        JsonNode node = oc.readTree(jp);
        final double price = node.get(0).asDouble();
        final double qty = node.get(1).asDouble();
        return new HuobiOrderBookEntry(price,qty);
    }
}
