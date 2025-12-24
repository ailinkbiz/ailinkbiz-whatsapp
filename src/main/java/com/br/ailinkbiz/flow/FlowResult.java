package com.br.ailinkbiz.flow;

import com.br.ailinkbiz.model.ConversationState;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FlowResult {

    private final String output;
    private final ConversationState nextState;

}