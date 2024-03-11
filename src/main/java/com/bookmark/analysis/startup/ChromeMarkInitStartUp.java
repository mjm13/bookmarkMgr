package com.bookmark.analysis.startup;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bookmark.analysis.dao.WebsiteDao;
import com.bookmark.analysis.entity.Website;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Order(1)
@ConditionalOnProperty(name = "bookmark.browser", havingValue = "chrome")
@Component
public class ChromeMarkInitStartUp implements ApplicationRunner {

    @Autowired
    private WebsiteDao websiteDao;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        long count = websiteDao.count();
        if (count > 0) {
            return;
        }
        String userHome = System.getenv("USERPROFILE");
        File bookmarksFile = new File(userHome+"\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\Bookmarks");
        JSONObject bms = JSONUtil.readJSONObject(bookmarksFile, StandardCharsets.UTF_8);
        JSONObject roots = bms.getJSONObject("roots");
        List<Website> websites = new ArrayList<>();
        for (String s : roots.keySet()) {
            getBookMark(websites,roots.getJSONObject(s),"");
        }
        websiteDao.saveAll(websites);
    }
    
    private void getBookMark(List<Website> websites,JSONObject node,String path){
        String type = node.getStr("type");
        if ("folder".equals(type)) {
            JSONArray children =  node.getJSONArray("children");
            for (JSONObject ch : children.jsonIter()) {
                getBookMark(websites,ch,path+"/"+node.getStr("name",""));
            }
        }else if("url".equals(type)){
            Website website = new Website();
            website.setUrl(node.getStr("url"));
            website.setBookMarkPath(path);
            website.setDescription(node.getStr("name"));
            website.setTitle(node.getStr("name"));
            website.setCreatedDate(new Date(node.getLong("date_added") * 1000));
            websites.add(website);
        }else {
            log.warn("无法解析节点:{}",node.toJSONString(1));       
        }
    }
}
