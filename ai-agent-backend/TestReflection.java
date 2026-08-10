import java.lang.reflect.Method;
import java.util.Arrays;

public class TestReflection {
    public static void main(String[] args) throws Exception {
        Class<?> clazz = Class.forName("org.springframework.ai.chat.memory.ChatMemory");
        for (Method m : clazz.getMethods()) {
            System.out.println(m.getName() + " " + Arrays.toString(m.getParameterTypes()));
        }
    }
}
