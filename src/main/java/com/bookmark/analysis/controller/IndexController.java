package com.bookmark.analysis.controller;

import com.bookmark.analysis.entity.Website;
import com.bookmark.analysis.services.WebsiteService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

/**
 * @author mjm
 * @createtime 2019/11/8-10:31
 * 按title查找重复
 * select  t.title,count(1) from website t where t.domain is null GROUP BY t.title having count(1)>1 order by count(1) desc;
 * 按title查询
 * select  t.remark,t.title,t.url from website t where t.domain is null and t.title = '';
 **/
@Controller
@Slf4j
public class IndexController {
    @Autowired
    private WebsiteService websiteService;

    @RequestMapping("/")
    public ModelAndView index(Model model, HttpSession session) {
        return new ModelAndView("/index");
    }

    @RequestMapping(value = "/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> bookMarkList(@RequestParam Map<String, String> param) {
        List<Website> websites = websiteService.findByParam(param);

        Map<String, Object> result = new HashMap<>();
        result.put("data", websites);
        result.put("count", websites.size());
        result.put("code", "0");
        return ResponseEntity.ok(result);
    }

    @RequestMapping(value = "/analysis")
    public ResponseEntity<Map<String, Object>> analysis(@RequestParam Map<String, String> param) {
        List<Website> websites = websiteService.findByParam(param);
        websiteService.analysisWebsites(websites);
        Map<String, Object> result = new HashMap<>();
        result.put("msg", "操作中");
        return ResponseEntity.ok(result);
    }


    @RequestMapping("/upload")
    public ResponseEntity<Map<String, Object>> batchAddExteriorOperations(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws IOException {
        Map<String, Object> data = new HashMap<>(15);
        data.put("status", HttpStatus.OK.value());
        data.put("msg", "操作完成");
        if (!file.isEmpty()) {
            Document html = Jsoup.parse(file.getInputStream(), "UTF-8", "");
            Elements elements = html.body().getElementsByTag("dt").select("a");
            List<Website> websites = new ArrayList<>();
            for (Element a : elements) {
                String href = a.attr("href");
                String add_date = a.attr("add_date");
                String last_modified = a.attr("last_modified");
                String title = a.ownText();
                Website website = new Website();
                website.setId(UUID.randomUUID().toString());
                website.setUrl(href);
                website.setRemark(title);
                website.setCreatedDate(new Date(Long.valueOf(add_date) * 1000));
                if (StringUtils.isNotBlank(last_modified)) {
                    website.setLastModifiedDate(new Date(Long.valueOf(last_modified) * 1000));
                }
                if (href.startsWith("http")) {
                    websites.add(website);
                }
            }
            websiteService.saveAll(websites);
        }
        return ResponseEntity.ok(data);
    }
}
