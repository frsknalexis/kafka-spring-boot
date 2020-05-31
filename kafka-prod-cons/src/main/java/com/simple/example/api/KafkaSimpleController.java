package com.simple.example.api;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/kafka")
public class KafkaSimpleController {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private Gson jsonConverter;

    @PostMapping
    public void post(@RequestBody SimpleModel simpleModel) {
        kafkaTemplate.send("my-topic", jsonConverter.toJson(simpleModel));
    }

    @PostMapping("/post")
    public MoreSimpleModel postMoreSimpleModel(@RequestBody MoreSimpleModel moreSimpleModel) {
        kafkaTemplate.send("my-topic-2", jsonConverter.toJson(moreSimpleModel));
        return moreSimpleModel;
    }

    @KafkaListener(topics = { "my-topic" })
    public void getFromKafka(String simpleModel) {
        System.out.println(simpleModel);
        SimpleModel simpleModelObject = jsonConverter.fromJson(simpleModel, SimpleModel.class);
        System.out.println(simpleModelObject.toString());
    }

    @KafkaListener(topics = { "my-topic-2" })
    public void getFromKafkaMoreSimpleModel(String moreSimpleModel) {
        System.out.println(moreSimpleModel);
        MoreSimpleModel moreSimpleModelObject = jsonConverter.fromJson(moreSimpleModel, MoreSimpleModel.class);
        System.out.println(moreSimpleModelObject.toString());
    }
}
