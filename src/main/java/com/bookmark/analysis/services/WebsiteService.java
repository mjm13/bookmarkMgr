package com.bookmark.analysis.services;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.bookmark.analysis.dao.BaseDao;
import com.bookmark.analysis.dao.WebsiteDao;
import com.bookmark.analysis.entity.Website;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.net.SocketException;
import java.text.ParseException;
import java.util.List;

/**
 * @author mjm
 * @createtime 2019/11/8-10:28
 **/
@Service
@Slf4j
public class WebsiteService extends BaseService<Website, Long> {

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private WebsiteDao websiteDao;

	@Override
	public BaseDao<Website, Long> getDao() {
		return websiteDao;
	}
	public void createIndexer() {
		try {
			FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
			fullTextEntityManager.createIndexer().startAndWait();
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
		}
	}

	public List<Website> search(String text) {
		// get the full text entity manager
		FullTextEntityManager fullTextEntityManager =Search.getFullTextEntityManager(entityManager);
		// create the query using Hibernate Search query DSL
		QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(Website.class).get();
		// a very basic query by keywords
		org.apache.lucene.search.Query query =
				queryBuilder
						.keyword()
						.onFields("description", "keywords", "remark", "title", "url")
						.matching(text)
						.createQuery();
		// wrap Lucene query in an Hibernate Query object
		org.hibernate.search.jpa.FullTextQuery jpaQuery = fullTextEntityManager.createFullTextQuery(query, Website.class);
		jpaQuery.setMaxResults(100);

		List<Website> results = jpaQuery.getResultList();
		return results;
	}

	@Async
	public Website analysisWebsites(Website website) {
		String url = website.getUrl();
//		StringBuffer content = new StringBuffer();
		try {
			Document doc = Jsoup.connect(url).get();
			Elements body = doc.getElementsByTag("body");
			String title = doc.head().select("title").text();
			String keywords = doc.head().select("meta[name=keywords]").attr("content");
			String description = doc.head().select("meta[name=description]").attr("content");
			String title2 = body.select("title").text();
			title = StringUtils.defaultIfBlank(title, title2);
//			for (Element Text : body) {
//				String text = Text.text();
//				content.append(text);
//			}
//			List<String> labels = HanLP.extractPhrase(content.toString(), 10);
			website.setKeywords(keywords);
			website.setDescription(description);
			website.setTitle(StringUtils.defaultIfBlank(title, "空白"));
//			website.setLabels(labels);
			log.info("over url:{}", url);
		} catch (HttpStatusException e) {
			log.info("HttpStatusException url:{}", url);
		} catch (SocketException e) {
			log.info("SocketException url:{}", url);
//			websiteDao.delete(website);
		} catch (Exception e) {
			log.error(e.getMessage() + "url:" + url);
		}
		return website;
	}

	public static void main(String[] args) throws ParseException {
		String temp = "Fri, 22 May 2020 19:36:40 GMT";
		System.out.println(DatePattern.HTTP_DATETIME_FORMAT.parse(temp));
	}
}
