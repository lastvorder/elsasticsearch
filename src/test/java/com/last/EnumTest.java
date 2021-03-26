package com.last;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author chenzihao
 * @date 2021/03/26
 */
enum Color {
    RED {
        public String getColor() {//枚举对象实现抽象方法
            return "红色";
        }
    },
    GREEN {
        public String getColor() {//枚举对象实现抽象方法
            return "绿色";
        }
    },
    BLUE {
        public String getColor() {//枚举对象实现抽象方法
            return "蓝色";
        }
    };

    // 构造函数
    Color() {
        System.out.println("Constructor called for : " + this.toString());
    }

    public abstract String getColor();//定义抽象方法

    public void colorInfo() {
        System.out.println("Universal Color");
    }
}

@SpringBootTest
class EnumTest {
    @Test
    void testEnum() {
        Color c1 = Color.RED;
        System.out.println(c1);
        for (Color myVar : Color.values()) {
            System.out.println(myVar);
        }
    }

    @Test
    void testMethod() {
        // 调用 values()
        Color[] arr = Color.values();
        // 迭代枚举
        for (Color col : arr) {
            // 查看索引
            System.out.println(col + " at index " + col.ordinal());
        }
        // 使用 valueOf() 返回枚举常量，不存在的会报错 IllegalArgumentException
        System.out.println(Color.valueOf("RED"));
        // System.out.println(Color.valueOf("WHITE"));
    }

    @Test
    void testConstructor() {
        // 输出
        Color c1 = Color.RED;
        System.out.println(c1);
        c1.colorInfo();
    }

    @Test
    void testObject() {
        for (Color c : Color.values()) {
            System.out.print(c.getColor() + "、");
        }
    }
}
