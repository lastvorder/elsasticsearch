package com.last.util;

import com.last.pojo.Product;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chenzihao
 * @date 2021/03/26
 */
public class HtmlParseUtil {
    public static void main(String[] args) throws IOException {
        new HtmlParseUtil().parseJd("爱").forEach(System.out::println);
    }

    public List<Product> parseJd(String keyword) throws IOException {
        // 获取请求
        // 前提需要联网 ajax不能获取到
        String url = "https://search.jd.com/Search?keyword=" + keyword;
        // 解析网页（Jsoup返回的Document就是页面对象）
        Document document = Jsoup.parse(new URL(url), 3000);
        // 在这里可以的使用所有的JS方法
        Element element = document.getElementById("J_goodsList");
        // 获取所有的li元素
        Elements lis = element.getElementsByTag("li");

        List<Product> productList = new ArrayList<>();
        for (Element li : lis) {
            if ("gl-item".equalsIgnoreCase(li.attr("class"))) {
                String img = li.getElementsByTag("img").eq(0).attr("data-lazy-img");
                String price = li.getElementsByClass("p-price").eq(0).text();
                String title = li.getElementsByClass("p-name").eq(0).text();
                Product product = new Product(img, price, title);
                productList.add(product);
            }
        }
        return productList;
    }
}
