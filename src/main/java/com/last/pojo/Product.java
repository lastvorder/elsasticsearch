package com.last.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author chenzihao
 * @date 2021/03/26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class Product {
    private String img;
    private String price;
    private String title;
}
