package com.br.ailinkbiz.flow.impl;

import com.br.ailinkbiz.flow.FlowHandler;
import com.br.ailinkbiz.flow.FlowResolver;
import org.springframework.stereotype.Component;

@Component
public class DefaultFlowResolver implements FlowResolver {

    private final DefaultFlowHandler defaultFlowHandler;
    private final SimpleInfoFlowHandler simpleInfoFlowHandler;

    public DefaultFlowResolver(
            DefaultFlowHandler defaultFlowHandler,
            SimpleInfoFlowHandler simpleInfoFlowHandler
    ) {
        this.defaultFlowHandler = defaultFlowHandler;
        this.simpleInfoFlowHandler = simpleInfoFlowHandler;
    }

    @Override
    public FlowHandler resolve(String clientId) {

        // exemplo didático
        if (clientId.endsWith("1234")) {
            return simpleInfoFlowHandler;
        }

        return defaultFlowHandler;
    }

}