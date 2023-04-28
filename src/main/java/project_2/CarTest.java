package project_2;

import org.checkerframework.checker.units.qual.A;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class CarTest {
    @Test
    public void testCarMethods() {
        Car car = new Car();
        car.setName("Rover");
        assertEquals("Rover", car.getName());
    }
}
