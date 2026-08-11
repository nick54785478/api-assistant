package com.apiassistant.agent.presentation.resource.out;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.context.ApplicationContext;
import java.util.Arrays;

@RestController
public class InspectController {
    
    private final ApplicationContext context;
    
    public InspectController(ApplicationContext context) {
        this.context = context;
    }

    @GetMapping("/inspect-beans")
    public String inspectBeans() {
        StringBuilder sb = new StringBuilder();
        String[] beanNames = context.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        for (String name : beanNames) {
            if (name.toLowerCase().contains("mcp")) {
                Object bean = context.getBean(name);
                sb.append("Bean: ").append(name)
                  .append(" Type: ").append(bean.getClass().getName())
                  .append("\n");
            }
        }
        return sb.toString();
    }
}
