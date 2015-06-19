import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * Created by hadoop on 2015/5/12 0012.
 */
public class MethodHandleTest {
    static class ClassA{
        public void println(String s){
            System.out.println("ClassA"+s);
        }
    }

    public static void main(String[] args) throws Throwable {
        Object obj = System.currentTimeMillis()%2==0?System.out:new ClassA();
        getPrintlnMH(obj).invokeExact("尼玛死了");

    }
    private static MethodHandle getPrintlnMH(Object reveiver) throws NoSuchMethodException, IllegalAccessException {
        MethodType mt = MethodType.methodType(void.class,String.class);
        return MethodHandles.lookup().findVirtual(reveiver.getClass(),"println",mt).bindTo(reveiver);
    }

}
