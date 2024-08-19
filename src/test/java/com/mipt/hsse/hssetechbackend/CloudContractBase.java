package com.mipt.hsse.hssetechbackend;

import com.mipt.hsse.hssetechbackend.controllers.payments.TinkoffEventsListenerController;
import com.mipt.hsse.hssetechbackend.payments.providers.events.AcquiringEventsListener;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = TinkoffEventsListenerController.class)
@DirtiesContext
public abstract class CloudContractBase {
    @Autowired
    private TinkoffEventsListenerController tinkoffEventsListenerController;

    @MockBean
    private AcquiringEventsListener acquiringEventsListener;

    @BeforeEach
    public void setup() {
        StandaloneMockMvcBuilder telcBuilder = MockMvcBuilders.standaloneSetup(tinkoffEventsListenerController);
        RestAssuredMockMvc.standaloneSetup(telcBuilder);
    }
}
