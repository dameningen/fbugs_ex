package sample;

import java.math.BigDecimal;

public class Target {

    /**
     * FindBugsチェック対象クラス
     */
    public void test() {
        BigDecimal a = new BigDecimal(0.1);
        BigDecimal b = a.add(new BigDecimal("0.1"));
        System.out.println(b);

        String str = "01234567890";
        int len = str.length();
        if (len > 11) {
            System.out.println("test");
        }

    }

}
