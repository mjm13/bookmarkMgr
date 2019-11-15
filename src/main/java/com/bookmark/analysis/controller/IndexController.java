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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.persistence.criteria.Predicate;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author mjm
 * @createtime 2019/11/8-10:31
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
	public ResponseEntity<Map<String, Object>> bookMarkList(String keyword) {
//		List<Website> websites = websiteService.search(keyword);
		keyword = keyword.replaceAll(",","");
		Pageable pageable = PageRequest.of(0, 100);
		String finalKeyword = keyword;
		List<Website> websites = websiteService.findAll((root, criteriaQuery, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();
			predicates.add(criteriaBuilder.like(criteriaBuilder.coalesce(root.get("title"), ""), "%" + finalKeyword + "%"));
			predicates.add(criteriaBuilder.like(criteriaBuilder.coalesce(root.get("remark"), ""), "%" + finalKeyword + "%"));
			predicates.add(criteriaBuilder.like(criteriaBuilder.coalesce(root.get("description"), ""), "%" + finalKeyword + "%"));
			predicates.add(criteriaBuilder.like(criteriaBuilder.coalesce(root.get("keywords"), ""), "%" + finalKeyword + "%"));
			return criteriaBuilder.or(predicates.toArray(new Predicate[predicates.size()]));
		},pageable).getContent();

		Map<String, Object> result = new HashMap<>();
		result.put("data", websites);
		result.put("count", websites.size());
		result.put("code", "0");
		return ResponseEntity.ok(result);
	}


	@RequestMapping("/upload")
	public ResponseEntity<Map<String, Object>> batchAddExteriorOperations(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws IOException {
		Map<String, Object> data = new HashMap<>(15);
		data.put("status", HttpStatus.OK.value());
		data.put("msg","操作完成");
		long id = 1L;
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

			log.info("begin analysis");
			websites = websites.stream().parallel().peek(website -> {
				String url = website.getUrl();
				try {
					Document doc = Jsoup.connect(url).get();
					Elements body = doc.getElementsByTag("body");
					String title = doc.head().select("title").text();
					String keywords = doc.head().select("meta[name=keywords]").attr("content");
					String description = doc.head().select("meta[name=description]").attr("content");
					String icon = doc.head().select("link[type=image/x-icon]").attr("href");
					String icon2 = doc.head().select("link[rel='shortcut icon']").attr("href");
					icon = StringUtils.defaultIfBlank(icon, icon2);
					if (!icon.startsWith("http")) {
						java.net.URL url1 = new URL(url);
						icon = "http://" + url1.getHost() + icon;
					}

					String title2 = body.select("title").text();
					title = StringUtils.defaultIfBlank(title, title2);
					website.setKeywords(keywords);
					website.setIcon(icon);
					website.setDescription(description);
					website.setTitle(StringUtils.defaultIfBlank(title, "空白"));
					log.info("over url:{}", url);
				} catch (Exception e) {
					log.error(e.getMessage() + "url:" + url);
				}
			}).collect(Collectors.toList());
			log.info("end analysis");
			websiteService.saveAll(websites);
//			websiteService.createIndexer();
		}
		return ResponseEntity.ok(data);
	}
}
