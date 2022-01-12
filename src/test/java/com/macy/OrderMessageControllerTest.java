package com.macy;

import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.macy.config.ConsumerMessageConfig;
import com.macy.controller.OrderMessageController;
import com.macy.dto.FulFillmentOrder;
import com.macy.dto.OrderJsonDto;
import com.macy.entity.OrderJsonEntity;
import com.macy.entity.OrderXMLEntity;
import com.macy.repository.OrderMessageJsonRepository;
import com.macy.repository.OrderMessageXMLRepository;
import com.macy.util.Constants;

@WebMvcTest(controllers=OrderMessageController.class, excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ConsumerMessageConfig.class))
//@SpringBootTest
public class OrderMessageControllerTest {
	@Autowired
	MockMvc mockMvc;
	
	@MockBean
	RabbitAdmin rabbitAdmin;
	
	@MockBean
	ModelMapper modelMapper;
	
	@MockBean
	@Qualifier("jsontemplate")
	RabbitTemplate rabbitJsonTemplate;
	
	@MockBean
	@Qualifier("xmltemplate")
	RabbitTemplate rabbitXmlTemplate;
	
	@MockBean
	OrderMessageJsonRepository orderMessageJsonRepository;

	@MockBean
	OrderMessageXMLRepository orderMessageXMLRepository;
	
	FulFillmentOrder fulFillmentOrder1 = new FulFillmentOrder(1, "Digital", "SAM123", "Created", null, null, 0, null, 0, 0, null, null, null);
	OrderXMLEntity orderXMLEntity1 = new OrderXMLEntity(1, "Digital", "SAM123", "Created", null, null, 0, null, 0, 0, null, null, null);
	
	OrderJsonDto orderJsonDto1 = new OrderJsonDto("", "", "", "", 0, 0, 0, 0, null, null, 0, null, null);
	OrderJsonEntity orderJsonEntity1 = new OrderJsonEntity(1, null, null, null, null, 0, 0, 0, 0, null, null, 0, null, null);
	
	@Test
	public void retrieveXmlMessages() throws Exception {
		QueueInformation queinformation = mock(QueueInformation.class);
		Mockito.when(rabbitAdmin.getQueueInfo(Constants.QUEUE_XML)).thenReturn(queinformation);
		Mockito.when(queinformation.getMessageCount()).thenReturn(5);
		Mockito.when(rabbitXmlTemplate.receiveAndConvert(Constants.QUEUE_XML)).thenReturn(fulFillmentOrder1);
		Mockito.when(modelMapper.map(fulFillmentOrder1, OrderXMLEntity.class)).thenReturn(orderXMLEntity1);
		Mockito.when(orderMessageXMLRepository.save(orderXMLEntity1)).thenReturn(orderXMLEntity1);
		mockMvc.perform(MockMvcRequestBuilders.get("/ordermessageconsumer/consumexmlmessage"))
		.andExpect(MockMvcResultMatchers.status().isOk());
	}
	
	@Test
	public void retrieveJsonMessages() throws Exception {
		QueueInformation queinformation = mock(QueueInformation.class);
		Mockito.when(rabbitAdmin.getQueueInfo(Constants.QUEUE_JSON)).thenReturn(queinformation);
		Mockito.when(queinformation.getMessageCount()).thenReturn(5);
		Mockito.when(rabbitJsonTemplate.receiveAndConvert(Constants.QUEUE_JSON)).thenReturn(orderJsonDto1);
		Mockito.when(modelMapper.map(orderJsonDto1, OrderJsonEntity.class)).thenReturn(orderJsonEntity1);
		Mockito.when(orderMessageJsonRepository.save(orderJsonEntity1)).thenReturn(orderJsonEntity1);
		mockMvc.perform(MockMvcRequestBuilders.get("/ordermessageconsumer/consumejsonmessage"))
		.andExpect(MockMvcResultMatchers.status().isOk());
	}
}
