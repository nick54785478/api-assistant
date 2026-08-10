package com.apiassistant.agent.domain.playbook.aggregate.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StepParameter {
    private ParameterType type;
    private String key;
    private String value;
}
