package ru.practicum.serialization;


import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

public class SimilarityAvroDeserializer extends BaseAvroDeserializer<EventSimilarityAvro> {

    public SimilarityAvroDeserializer() {
        super(EventSimilarityAvro.getClassSchema());
    }
}
