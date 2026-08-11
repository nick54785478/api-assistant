package com.apiassistant.agent.domain.playbook.aggregate.vo;

import java.util.Objects;

public class StepParameter {
    private ParameterType type;
    private String key;
    private String value;

    public StepParameter() {
    }

    public StepParameter(ParameterType type, String key, String value) {
        this.type = type;
        this.key = key;
        this.value = value;
    }

    public ParameterType getType() {
        return type;
    }

    public void setType(ParameterType type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StepParameter that = (StepParameter) o;
        return type == that.type &&
                Objects.equals(key, that.key) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, key, value);
    }

    @Override
    public String toString() {
        return "StepParameter{" +
                "type=" + type +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

    public static StepParameterBuilder builder() {
        return new StepParameterBuilder();
    }

    public static class StepParameterBuilder {
        private ParameterType type;
        private String key;
        private String value;

        StepParameterBuilder() {
        }

        public StepParameterBuilder type(ParameterType type) {
            this.type = type;
            return this;
        }

        public StepParameterBuilder key(String key) {
            this.key = key;
            return this;
        }

        public StepParameterBuilder value(String value) {
            this.value = value;
            return this;
        }

        public StepParameter build() {
            return new StepParameter(type, key, value);
        }
    }
}
