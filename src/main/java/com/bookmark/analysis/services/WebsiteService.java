package com.bookmark.analysis.services;

import cn.hutool.core.date.DatePattern;
import com.bookmark.analysis.common.util.SSLHelper;
import com.bookmark.analysis.dao.BaseDao;
import com.bookmark.analysis.dao.WebsiteDao;
import com.bookmark.analysis.entity.Website;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.net.ssl.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static com.bookmark.analysis.common.util.SSLHelper.USER_AGENT;

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
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
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


    public List<Website> findByParam(Map<String, String> param) {
        List<Website> websites = findAll((root, criteriaQuery, criteriaBuilder) -> {
            String keyword = param.get("keyword");
            keyword = "%"+keyword.toLowerCase()+"%";
            String remark = param.get("remark");
            String title = param.get("title");
            String url = param.get("url");
            String description = param.get("description");
            String domain = param.get("domain");
            String keywords = param.get("keywords");

            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.isNotBlank(remark)) {
                predicates.add(criteriaBuilder.like(root.get("remark"), "%" + remark + "%"));
            }
            if (StringUtils.isNotBlank(title)) {
                predicates.add(criteriaBuilder.like(root.get("title"), "%" + title + "%"));
            }
            if (StringUtils.isNotBlank(url)) {
                predicates.add(criteriaBuilder.like(root.get("url"), "%" + url + "%"));
            }
            if (StringUtils.isNotBlank(description)) {
                predicates.add(criteriaBuilder.like(root.get("description"), "%" + description + "%"));
            }
            if ("is null".equals(domain)) {
                predicates.add(criteriaBuilder.isNull(root.get("domain")));
            } else if (StringUtils.isNotBlank(domain)) {
                predicates.add(criteriaBuilder.like(root.get("domain"), "%" + domain + "%"));
            }
            if (StringUtils.isNotBlank(remark)) {
                predicates.add(criteriaBuilder.like(root.get("keywords"), "%" + keywords + "%"));
            }

            Predicate condition = criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            if (StringUtils.isNotBlank(keyword)) {
                List<Predicate> orpres = genLikePredicates(root,criteriaBuilder,keyword,"title","remark","description","keywords","domain","domainTitle","url");
                Predicate key = criteriaBuilder.or(orpres.toArray(new Predicate[orpres.size()]));
                condition = criteriaBuilder.and(condition, key);
            }
            return condition;
        });
        return websites;
    }

    private List<Predicate> genLikePredicates(Root root, CriteriaBuilder criteriaBuilder,String keyword, String... props){
        List<Predicate> orpres = new ArrayList<>();
        for (String prop : props) {
            orpres.add(criteriaBuilder.like(criteriaBuilder.lower(root.get(prop)), keyword));
        }
        return orpres;
    }

    @Async
    public void analysisWebsites(List<Website> websites) {
        SSLHelper.init();
        websites.stream().parallel().forEach(website -> {
            String url = website.getUrl();
            try {
                Connection connect = Jsoup.connect(url).userAgent(USER_AGENT);
                connect.timeout(3000);
                connect.ignoreHttpErrors(true);
//				if(url.startsWith("https")){
//                connect.followRedirects(true);
                connect.timeout(60000);
//				}
                Document doc = connect.get();

                Connection.Response response = connect.response();
                Elements body = doc.getElementsByTag("body");
                String title = doc.title();
                String keywords = doc.head().select("meta[name=keywords]").attr("content");
                String description = doc.head().select("meta[name=description]").attr("content");
                String icon = doc.head().select("link[type=image/x-icon]").attr("href");
                String icon2 = doc.head().select("link[rel='shortcut icon']").attr("href");
                String icon3 = doc.head().select("link[rel='fluid-icon']").attr("href");
                icon = StringUtils.defaultIfBlank(StringUtils.defaultIfBlank(icon, icon2), icon3);
                if (!icon.startsWith("http")) {
                    java.net.URL url1 = new URL(url);
                    icon = "http://" + url1.getHost() + "/" + icon;
                }

                String title2 = body.select("title").text();
                title = StringUtils.defaultIfBlank(title, title2);
                website.setKeywords(keywords);
                website.setIcon(icon);
                website.setDescription(description);
                website.setTitle(StringUtils.defaultIfBlank(title, "空白"));

                URL baseUrl = new URL(website.getUrl());
                Document baseDoc = Jsoup.connect(baseUrl.getProtocol() + "://" + baseUrl.getHost()).get();
                String baseTitle = baseDoc.head().select("title").text();
                website.setDomain(baseUrl.getHost());
                website.setDomainTitle(baseTitle);

                String pageDateStr = response.headers().get("Date");
                if (StringUtils.isNotBlank(pageDateStr)) {
                    Date pageDate = DatePattern.HTTP_DATETIME_FORMAT.parse(pageDateStr);
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

}
