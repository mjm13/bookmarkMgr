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
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.net.ssl.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.Predicate;
import java.io.IOException;
import java.net.*;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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


	public List<Website> findByKeyword(String keyword){
		String finalKeyword = keyword;
		List<Website> websites = findAll((root, criteriaQuery, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();
			predicates.add(criteriaBuilder.like(criteriaBuilder.coalesce(root.get("title"), ""), "%" + finalKeyword + "%"));
			predicates.add(criteriaBuilder.like(criteriaBuilder.coalesce(root.get("remark"), ""), "%" + finalKeyword + "%"));
			predicates.add(criteriaBuilder.like(criteriaBuilder.coalesce(root.get("description"), ""), "%" + finalKeyword + "%"));
			predicates.add(criteriaBuilder.like(criteriaBuilder.coalesce(root.get("keywords"), ""), "%" + finalKeyword + "%"));
			predicates.add(criteriaBuilder.like(criteriaBuilder.coalesce(root.get("domain"), ""), "%" + finalKeyword + "%"));
			predicates.add(criteriaBuilder.like(criteriaBuilder.coalesce(root.get("domainTitle"), ""), "%" + finalKeyword + "%"));
			predicates.add(criteriaBuilder.like(criteriaBuilder.coalesce(root.get("url"), ""), "%" + finalKeyword + "%"));
			return criteriaBuilder.or(predicates.toArray(new Predicate[predicates.size()]));
		});
		return websites;
	}

	@Async
	public void analysisWebsites(List<Website> websites) {
		websites.stream().parallel().forEach(website -> {
			String url = website.getUrl();
			try {
				Connection connect = Jsoup.connect(url);
				connect.timeout(3000);
				connect.header("Accept-Encoding", "gzip,deflate,sdch");
				connect.header("Connection", "close");
				connect.ignoreHttpErrors(true);
				if(url.startsWith("https")){
					connect.sslSocketFactory(socketFactory());
				}
				if(url.startsWith("https://github.com/")){
					connect.timeout(600000);
				}
				Document doc = connect.get();

				Connection.Response response = connect.response();
				Elements body = doc.getElementsByTag("body");
				String title = doc.title();
				String keywords = doc.head().select("meta[name=keywords]").attr("content");
				String description = doc.head().select("meta[name=description]").attr("content");
				String icon = doc.head().select("link[type=image/x-icon]").attr("href");
				String icon2 = doc.head().select("link[rel='shortcut icon']").attr("href");
				String icon3 = doc.head().select("link[rel='fluid-icon']").attr("href");
				icon = StringUtils.defaultIfBlank(StringUtils.defaultIfBlank(icon, icon2),icon3);
				if (!icon.startsWith("http")) {
					java.net.URL url1 = new URL(url);
					icon = "http://" + url1.getHost() +"/"+ icon;
				}

				String title2 = body.select("title").text();
				title = StringUtils.defaultIfBlank(title, title2);
				website.setKeywords(keywords);
				website.setIcon(icon);
				website.setDescription(description);
				website.setTitle(StringUtils.defaultIfBlank(title, "空白"));

				URL baseUrl = new URL(website.getUrl());
				Document baseDoc = Jsoup.connect(baseUrl.getProtocol()+"://"+baseUrl.getHost()).get();
				String baseTitle = baseDoc.head().select("title").text();
				website.setDomain(baseUrl.getHost());
				website.setDomainTitle(baseTitle);

				String pageDateStr = response.headers().get("Date");
				if(StringUtils.isNotBlank(pageDateStr)){
					Date pageDate= DatePattern.HTTP_DATETIME_FORMAT.parse(pageDateStr);
					website.setPageDate(pageDate);
				}
				log.info("over url:{}", url);
			} catch (Exception e) {
				website.setTitle(e.getMessage());
				log.error(e.getMessage() + "url:" + url);
			}
			websiteDao.save(website);
		});

	}

	private SSLSocketFactory socketFactory() {
		TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		}};

		try {
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
			return sslContext.getSocketFactory();
		} catch (Exception e) {
			throw new RuntimeException("Failed to create a SSL socket factory", e);
		}
	}

	public static void main(String[] args) throws ParseException {
		String temp = "Fri, 22 May 2020 19:36:40 GMT";
		System.out.println(DatePattern.HTTP_DATETIME_FORMAT.parse(temp));
	}
}
