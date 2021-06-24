import ms.kenchen.Calculator.CalculatorResponse;
import ms.kenchen.Calculator.CalculatorService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


class CalculatorServiceTest {

    @Test
    public void addTest() {
        CalculatorService s = new CalculatorService();
        int x = 5;
        int y = 6;
        int r = 5+6;
        CalculatorResponse res = s.Add(x, y);
        assertEquals(r, res.getResult());
    }

    @Test
    public void subTest() {
        CalculatorService s = new CalculatorService();
        int x = 9;
        int y = 5;
        int r = 9-5;
        CalculatorResponse res = s.Sub(x, y);
        assertEquals(r, res.getResult());
    }

    @Test
    public void mulTest() {
        CalculatorService s = new CalculatorService();
        int x = 8;
        int y = 3;
        int r = 8*3;
        CalculatorResponse res = s.Mul(x, y);
        assertEquals(r, res.getResult());
    }

    @Test
    public void divTest() {
        CalculatorService s = new CalculatorService();
        int x = 4;
        int y = 2;
        int r = 4/2;
        CalculatorResponse res = s.Div(x, y);
        assertEquals(r, res.getResult());
    }

}