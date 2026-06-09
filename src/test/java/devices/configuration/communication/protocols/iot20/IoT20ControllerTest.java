package devices.configuration.communication.protocols.iot20;

import devices.configuration.communication.CommunicationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = IoT20Controller.class)
class IoT20ControllerTest {

    @Autowired
    private MockMvc rest;
    @MockitoBean
    private CommunicationService communicationService;

    @Test
    void updateAll() throws Exception {
        Mockito.when(communicationService.handleBoot(
                        eq("device-id"), any()
                ))
                .thenReturn(new CommunicationService.HeartbeatResponse(
                        Instant.parse("2023-06-28T06:15:30.00Z"), 1800
                ));


        rest.perform(post("/protocols/iot20/bootnotification/{deviceId}", "device-id")
                        .with(jwt())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                   "device": {
                                     "serialNumber": "820394A93203",
                                     "model": "CPF25 Family",
                                     "modem": {
                                       "iccid": "1122 3344 5566 7788 99 C 1",
                                       "imsi": "082931213347973812"
                                     },
                                     "vendorName": "Garo",
                                     "firmwareVersion": "1.1"
                                   },
                                   "reason": "PowerUp"
                                 }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                                {
                                  "currentTime": "2023-06-28T06:15:30Z",
                                  "interval": 1800,
                                  "status": "Accepted"
                                }
                        """, true));

        Mockito.verify(communicationService).handleBoot(eq("device-id"), any());
    }
}
