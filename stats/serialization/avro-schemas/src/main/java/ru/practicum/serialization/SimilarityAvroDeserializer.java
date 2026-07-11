package ru.practicum.serialization;


import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

public class SimilarityAvroDeserializer extends BaseAvroDeserializer<EventSimilarityAvro> {

    public SimilarityAvroDeserializer() {
        super(EventSimilarityAvro.getClassSchema());
    }
}
