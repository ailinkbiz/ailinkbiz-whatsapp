package com.br.ailinkbiz.flow;

import com.br.ailinkbiz.model.ConversationState;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FlowContext {

    private final String clientId;
    private final String userId;
    private final String input;
    private final ConversationState state;

}
