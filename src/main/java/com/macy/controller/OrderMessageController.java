package com.macy.controller;

import org.modelmapper.ModelMapper;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.macy.dto.FulFillmentOrder;
import com.macy.dto.OrderJsonDto;
import com.macy.entity.OrderJsonEntity;
import com.macy.entity.OrderXMLEntity;
import com.macy.repository.OrderMessageJsonRepository;
import com.macy.repository.OrderMessageXMLRepository;
import com.macy.util.Constants;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/ordermessageconsumer")
public class OrderMessageController {

	@Autowired
	@Qualifier("jsontemplate")
	RabbitTemplate rabbitJsonTemplate;

	@Autowired
	@Qualifier("xmltemplate")
	RabbitTemplate rabbitXmlTemplate;

	@Autowired
	RabbitAdmin rabbitAdmin;

	@Autowired
	OrderMessageJsonRepository orderMessageJsonRepository;

	@Autowired
	OrderMessageXMLRepository orderMessageXMLRepository;

	@Autowired
	ModelMapper modelMapper;

	@GetMapping(value = "/consumexmlmessage", produces = MediaType.APPLICATION_XML_VALUE)
	public List<FulFillmentOrder> retrieveXMLMessages() {
		List<FulFillmentOrder> list = new ArrayList<>();
		QueueInformation queueInformation = rabbitAdmin.getQueueInfo(Constants.QUEUE_XML);
		if (queueInformation != null) {
			int count = queueInformation.getMessageCount();
			for (int i = 0; i < count-1; i++) {
				FulFillmentOrder order = (FulFillmentOrder) rabbitXmlTemplate.receiveAndConvert(Constants.QUEUE_XML);
				list.add(order);
				orderMessageXMLRepository.save(modelMapper.map(order, OrderXMLEntity.class));
			}
		}
		return list;
	}

	@GetMapping(value = "/consumejsonmessage", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<OrderJsonDto> retrieveJSONMessages() {
		QueueInformation queueInformation = rabbitAdmin.getQueueInfo(Constants.QUEUE_JSON);
		List<OrderJsonDto> list = new ArrayList<>();
		if (queueInformation != null) {
			int count = queueInformation.getMessageCount();
			for (int i = 0; i < count-1; i++) {
				OrderJsonDto dto = (OrderJsonDto) rabbitJsonTemplate.receiveAndConvert(Constants.QUEUE_JSON);
				list.add(dto);
				OrderJsonEntity entity = modelMapper.map(dto, OrderJsonEntity.class);
				orderMessageJsonRepository.save(entity);
			}
		}
		return list;
	}
}
